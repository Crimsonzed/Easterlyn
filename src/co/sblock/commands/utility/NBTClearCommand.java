package co.sblock.commands.utility;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.utilities.inventory.InventoryUtils;

/**
 * SblockCommand for removing NBT tags from an item. Attempts to preserve standard meta.
 * 
 * @author Jikoo
 */
public class NBTClearCommand extends SblockCommand {

	public NBTClearCommand() {
		super("clearnbt");
		this.setDescription("Clear some NBT tags off an item. Preserves most meta.");
		this.setUsage("Run /clearnbt while holding an item");
		this.setAliases("cleannbt");
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		player.setItemInHand(InventoryUtils.cleanNBT(player.getItemInHand()));
		player.sendMessage(Color.GOOD + "NBT cleared!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
