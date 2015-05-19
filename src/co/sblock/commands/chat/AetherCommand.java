package co.sblock.commands.chat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.Users;
import co.sblock.utilities.player.DummyPlayer;

/**
 * SblockCommand for /aether, the command executed to make IRC chat mimic normal channels.
 * 
 * @author Jikoo
 */
public class AetherCommand extends SblockAsynchronousCommand {

	private static final ItemStack HOVER;
	private static final DummyPlayer SENDER;

	static {
		HOVER = new ItemStack(Material.WEB);
		ItemMeta hoverMeta = HOVER.getItemMeta();
		hoverMeta.setDisplayName(ChatColor.WHITE + "IRC Chat");
		hoverMeta.setLore(Arrays.asList(new String[] {
				ChatColor.GRAY + "Server: irc.freenode.net",
				ChatColor.GRAY + "Channel: #sblockserver" }));
		HOVER.setItemMeta(hoverMeta);

		SENDER = new DummyPlayer(Bukkit.getConsoleSender());
	}

	public AetherCommand() {
		super("aether");
		this.setDescription("For usage in console largely. Talks in #Aether.");
		this.setUsage("/aether <text>");
		this.setPermissionLevel("horrorterror");
		this.setPermissionMessage("The aetherial realm eludes your grasp once more.");

	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Hey Adam, stop faking empty IRC messages.");
			return true;
		}

		sendAether(args[0], StringUtils.join(args, ' ', 1, args.length));
		return true;
	}

	public static void sendAether(String name, String msg) {

		Channel aether = ChannelManager.getChannelManager().getChannel("#Aether");
		// set channel before and after to prevent @channel changing while also stripping invalid characters
		MessageBuilder builder = new MessageBuilder().setSender(ChatColor.WHITE + name)
				.setChannel(aether).setMessage(msg).setChannel(aether).setChannelClick("@# ")
				.setNameClick("@# ").setNameHover(HOVER);

		if (!builder.canBuild(false)) {
			return;
		}

		Message message = builder.toMessage();

		Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> Users.getGuaranteedUser(p.getUniqueId()).getSuppression());

		// CHAT: Verify that this does not cause concurrency issues (It totally does)
		SENDER.setDisplayName(name);

		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(false, SENDER, players, message));
	}
}
