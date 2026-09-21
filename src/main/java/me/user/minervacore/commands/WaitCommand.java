package me.user.minervacore.commands;

import me.user.minervacore.MinervaCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WaitCommand implements CommandExecutor {

    private final MinervaCore plugin;

    public WaitCommand(MinervaCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "관리자(OP) 권한이 필요합니다.");
            return true;
        }

        World world = (sender instanceof Player p) ? p.getWorld() : Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();
        border.setSize(20.0); // 20x20 월드보더

        plugin.setWaitMode(true);
        plugin.setInvulnerableMode(true);
        plugin.setPvpAllowed(false);

        Bukkit.broadcastMessage(ChatColor.YELLOW + "========================================");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[대기 모드 시작]");
        Bukkit.broadcastMessage(ChatColor.WHITE + "월드보더가 " + ChatColor.RED + "20x20" + ChatColor.WHITE + " 크기로 축소되었습니다.");
        Bukkit.broadcastMessage(ChatColor.RED + "블록 설치 및 파괴가 불가능합니다. (자원 시작 시 해제)");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "========================================");
        return true;
    }
}
