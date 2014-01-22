package co.sblock.Sblock.Utilities.Captcha;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;

/**
 * @author Dublek, Jikoo
 */
public class CaptchaCommandListener implements CommandListener {

	/**
	 * Command used to convert an ItemStack into a Captchacard.
	 * 
	 * @param sender the CommandSender
	 * @param args the command arguments
	 * 
	 * @return true if successful
	 */
	@SblockCommand(description = "Captchalogues item in hand", usage = "/captcha")
	public boolean captcha(CommandSender sender, String[] args) {
		if (sender.isOp()) {
			Player p = (Player) sender;
			ItemStack item = p.getItemInHand();
			p.getInventory().remove(item);
			p.getInventory().addItem(Captcha.itemToCaptcha(item));
			return true;
		}
		return false;
	}

	/**
	 * Command used to convert a Captchacard into an ItemStack.
	 * 
	 * @param sender the CommandSender
	 * @param args the command arguments
	 * 
	 * @return true if successful
	 */
	@SblockCommand(description = "Uncaptchalogues item in hand", usage = "/uncaptcha")
	public boolean uncaptcha(CommandSender sender, String[] args) {
		if (sender.isOp()) {
			Player p = (Player) sender;
			ItemStack item = p.getItemInHand();
			if (item.getItemMeta().hasDisplayName()
					&& item.getItemMeta().getDisplayName().equals("Captchacard")) {
				p.getInventory().clear(p.getInventory().getHeldItemSlot());
				p.getInventory().addItem(Captcha.captchaToItem(item));
			}
			return true;
		}
		return false;
	}

	/**
	 * Command used to convert a Captchacard into a Punchcard.
	 * 
	 * @param sender the CommandSender
	 * @param args the command arguments
	 * 
	 * @return true if successful
	 */
	@SblockCommand(description = "Punches card in hand", usage = "/punchcard")
	public boolean punchcard(CommandSender sender, String[] args) {
		if (sender.isOp()) {
			Player p = (Player) sender;
			ItemStack item = p.getItemInHand();
			if (Captcha.isCaptchaCard(item)) {
				p.getInventory().clear(p.getInventory().getHeldItemSlot());
				p.getInventory().addItem(Captchadex.punchCard(item));
				return true;
			}
			sender.sendMessage(ChatColor.RED + "Item is not a captchacard!");
			return true;
		}
		return false;
	}

	/**
	 * Command used to convert a Punchcard into a Captchacard.
	 * 
	 * @param sender the CommandSender
	 * @param args the command arguments
	 * 
	 * @return true if successful
	 */
	@SblockCommand(description = "Gives player a captchadex", usage = "/captchadex")
	public boolean captchadex(CommandSender sender, String[] args) {
		if (sender.isOp())
			((Player) sender).getInventory().addItem(Captchadex.createCaptchadexBook((Player) sender));
		return true;
	}
}
