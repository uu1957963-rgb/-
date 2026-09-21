package me.user.minervacore.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RemoveThornsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR || !item.containsEnchantment(Enchantment.THORNS)) {
            player.sendMessage(ChatColor.RED + "손에 가시 인챈트가 붙은 방어구를 들어주세요.");
            return true;
        }

        item.removeEnchantment(Enchantment.THORNS);
        player.sendMessage(ChatColor.GREEN + "[가시 제거] 손에 든 방어구에서 가시 인챈트가 제거되었습니다.");
        return true;
    }
}
