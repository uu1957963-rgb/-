package me.user.minervacore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "관리자(OP) 권한이 필요합니다.");
            return true;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            // 네더라이트 곡괭이 [효율 X / 내구성 III / 행운 7]
            ItemStack pickaxe = new ItemStack(Material.NETHERITE_PICKAXE);
            pickaxe.addUnsafeEnchantment(Enchantment.EFFICIENCY, 10);
            pickaxe.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
            pickaxe.addUnsafeEnchantment(Enchantment.FORTUNE, 7);
            pickaxe.addUnsafeEnchantment(Enchantment.MENDING, 1);

            // 겉날개
            ItemStack elytra = new ItemStack(Material.ELYTRA);
            elytra.addUnsafeEnchantment(Enchantment.UNBREAKING, 10);
            elytra.addUnsafeEnchantment(Enchantment.MENDING, 1);

            // 참나무 원목 16개
            ItemStack logs = new ItemStack(Material.OAK_LOG, 16);
            // 청금석 64개
            ItemStack lapis = new ItemStack(Material.LAPIS_LAZULI, 64);
            // 마법 부여대 5개
            ItemStack enchTable = new ItemStack(Material.ENCHANTING_TABLE, 5);
            // 책장 64개
            ItemStack bookshelf = new ItemStack(Material.BOOKSHELF, 64);
            // 숫돌 2개
            ItemStack grindstone = new ItemStack(Material.GRINDSTONE, 2);
            // 셜커 상자 1개
            ItemStack shulker = new ItemStack(Material.SHULKER_BOX, 1);
            // 모루 1개
            ItemStack anvil = new ItemStack(Material.ANVIL, 1);

            p.getInventory().addItem(pickaxe, elytra, logs, lapis, enchTable, bookshelf, grindstone, shulker, anvil);
            p.setLevel(200);
            p.sendMessage(ChatColor.GREEN + "[지급] 자원 채굴 및 제작 전용 아이템 세트와 200레벨이 지급되었습니다.");
        }

        sender.sendMessage(ChatColor.GOLD + "[완료] 전원에게 지정된 아이템 세트를 지급했습니다.");
        return true;
    }
}
