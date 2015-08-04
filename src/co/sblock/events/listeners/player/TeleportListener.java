package co.sblock.events.listeners.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.users.OfflineUser;
import co.sblock.users.Region;
import co.sblock.users.Users;

/**
 * Listener for PlayerTeleportEvents.
 * 
 * @author Jikoo
 */
public class TeleportListener implements Listener {

	/**
	 * The event handler for PlayerTeleportEvents.
	 * <p>
	 * This method is for events that are guaranteed to be completed.
	 * 
	 * @param event the PlayerTeleportEvent
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getCause() != TeleportCause.SPECTATE || event.getPlayer().hasPermission("sblock.felt")) {
			return;
		}
		for (Player player : event.getTo().getWorld().getPlayers()) {
			if (!player.getLocation().equals(event.getTo())) {
				continue;
			}
			if (player.getGameMode() == GameMode.SPECTATOR && player.getVehicle() != null
					&& player.getVehicle() instanceof Player) {
				player = (Player) player.getVehicle();
			}
			if (Users.getGuaranteedUser(player.getUniqueId()).getSpectatable()) {
				return;
			}
			event.setCancelled(true);
			event.getPlayer().sendMessage(Color.BAD_PLAYER + player.getDisplayName() + Color.BAD
					+ " has disallowed spectating! You'll need to send a tpa.");
			return;
		}
	}

	/**
	 * The event handler for PlayerTeleportEvents.
	 * <p>
	 * This method is for events that are guaranteed to be completed.
	 * 
	 * @param event the PlayerTeleportEvent
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleportHasOccurred(final PlayerTeleportEvent event) {
		// People keep doing stupid stuff like /home while falling from spawn
		event.getPlayer().setFallDistance(0);

		if (event.getTo().getWorld().equals(event.getFrom().getWorld())) {
			return;
		}

		final UUID uuid = event.getPlayer().getUniqueId();

		Bukkit.getScheduler().runTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					// Player has logged out.
					return;
				}
				OfflineUser user = Users.getGuaranteedUser(uuid);
				// Update region
				Region target;
				if (player.getWorld().getName().equals("Derspit")) {
					target = user.getDreamPlanet();
				} else {
					target = Region.getRegion(event.getTo().getWorld().getName());
				}
				user.updateCurrentRegion(target);
			}
		});
	}
}
