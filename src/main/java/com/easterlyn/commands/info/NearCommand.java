package com.easterlyn.commands.info;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * EasterlynCommand for locating nearby players.
 *
 * @author Jikoo
 */
public class NearCommand extends EasterlynCommand {

	private final int maxRadius;

	public NearCommand(Easterlyn plugin) {
		super(plugin, "near");
		this.addExtraPermission("far", UserRank.MOD);
		this.addExtraPermission("invisible", UserRank.MOD);
		this.addExtraPermission("spectate", UserRank.MOD);
		this.maxRadius = 200;
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		int radius = maxRadius;
		if (args.length > 0) {
			try {
				radius = Math.abs(Integer.parseInt(args[0]));
				if (radius > maxRadius && !sender.hasPermission("easterlyn.command.near.far")) {
					radius = maxRadius;
				}
			} catch (NumberFormatException e) {
				// Silently fail, fall through to default max radius
			}
		}

		Player player = (Player) sender;
		Location location = player.getLocation();
		boolean showSpectate = player.hasPermission("easterlyn.command.near.spectate");
		boolean showInvisible = player.hasPermission("easterlyn.command.near.invisible");
		double squared = Math.pow(radius, 2);

		StringBuilder builder = new StringBuilder(getLang().getValue("command.near.base"));

		for (Player target : location.getWorld().getPlayers()) {
			if (player.getUniqueId().equals(target.getUniqueId()) || !player.canSee(target)
					|| !showSpectate && target.getGameMode() == GameMode.SPECTATOR
					|| !showInvisible && target.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				continue;
			}
			double distanceSquared = location.distanceSquared(target.getLocation());
			if (distanceSquared > squared) {
				continue;
			}
			int distance = (int) Math.sqrt(distanceSquared);
			builder.append(getLang().getValue("command.near.player")
					.replace("{PLAYER}", target.getDisplayName())
					.replace("{DISTANCE}", String.valueOf(distance))).append(", ");
		}
		if (builder.charAt(builder.length() - 2) == ',') {
			builder.delete(builder.length() - 2, builder.length());
		} else {
			builder.append("none");
		}

		sender.sendMessage(builder.toString());
		return true;
	}

}
