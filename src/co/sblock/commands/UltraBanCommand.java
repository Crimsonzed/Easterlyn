package co.sblock.commands;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.users.OfflineUser;
import co.sblock.users.UserManager;

/**
 * SblockCommand featuring a SuperBan along with several protection removal commands.
 * 
 * @author Jikoo
 */
public class UltraBanCommand extends SblockCommand {

	public UltraBanCommand() {
		super("ultraban");
		this.setDescription("YOU REALLY CAN'T ESCAPE THE RED MILES.");
		this.setUsage("/ultraban <target>");
		this.setPermission("group.horrorterror");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!Bukkit.dispatchCommand(sender, "sban " + StringUtils.join(args, ' '))) {
			// sban will return its own usage failure, no need to double message.
			return true;
		}

		Player p = Bukkit.getPlayer(args[0]);
		if (p != null) {
			OfflineUser victim = UserManager.getGuaranteedUser(p.getUniqueId());
			File file;
			try {
				file = new File(Sblock.getInstance().getUserDataFolder(), victim.getUUID().toString() + ".yml");
				if (file.exists()) {
					file.delete();
				}
			} catch (IOException e) {
				UserManager.getUserManager().getLogger().warning("Unable to delete data for " + victim.getUUID());
			}
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lwc admin purge " + p.getUniqueId());
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lwc admin purge " + args[0]);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ps delete " + args[0]);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (args.length < 2) {
			return super.tabComplete(sender, alias, args);
		}
		return ImmutableList.of();
	}
}
