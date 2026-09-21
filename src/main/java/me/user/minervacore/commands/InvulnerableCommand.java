package me.user.minervacore.commands;

import me.user.minervacore.MinervaCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class InvulnerableCommand implements CommandExecutor {

    private final MinervaCore plugin;

    public InvulnerableCommand(MinervaCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "관리자(OP) 권한이 필요합니다.");
            return true;
        }

        plugin.setInvulnerableMode(false);
        Bukkit.broadcastMessage(ChatColor.RED + "========================================");
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "[무적 해제] 관리자에 의해 무적 모드가 강제로 해제되었습니다!");
        Bukkit.broadcastMessage(ChatColor.RED + "========================================");
        return true;
    }
}
