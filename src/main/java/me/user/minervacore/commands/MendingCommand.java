package me.user.minervacore.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MendingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "손에 아이템을 들고 사용해주세요.");
            return true;
        }

        item.addUnsafeEnchantment(Enchantment.MENDING, 1);
        player.sendMessage(ChatColor.GREEN + "[수선] 손에 든 아이템에 수선 인챈트가 부여되었습니다.");
        return true;
    }
}
