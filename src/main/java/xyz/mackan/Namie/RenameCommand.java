package xyz.mackan.Namie;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RenameCommand implements CommandExecutor {
	// TODO: Rewrite this to use NMS to get rid of the italic names.
	@Override
	public boolean onCommand (CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED+"You can only use this command as a player.");
			return true;
		}

		if (args.length <= 0) return false;

		Player p = (Player) sender;

		ItemStack itemInMainHand = p.getInventory().getItemInMainHand();

		if (itemInMainHand == null || itemInMainHand.getType() == Material.AIR) {
			p.sendMessage(ChatColor.RED+"You're not holding an item!");
			return true;
		}

		String initialName = String.join(" ", args);

		String formattedName = FormatUtil.formatString(p, initialName);
		ItemMeta meta = itemInMainHand.getItemMeta();

		itemInMainHand.setItemMeta(meta);

		p.sendMessage("Item name set to "+formattedName);

		return true;
	}
}
