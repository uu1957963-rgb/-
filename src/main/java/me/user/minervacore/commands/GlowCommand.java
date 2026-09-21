package me.user.minervacore.commands;

import me.user.minervacore.MinervaCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GlowCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "관리자(OP) 권한이 필요합니다.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "사용법: /발광 [닉네임 / all]");
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            MinervaCore.getInstance().setGlobalGlowEnabled(true);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGlowing(true);
            }
            Bukkit.broadcastMessage(ChatColor.GOLD + "[발광] 전원에게 발광 효과가 부여되었습니다.");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "해당 플레이어를 찾을 수 없습니다.");
            return true;
        }

        target.setGlowing(!target.isGlowing());
        sender.sendMessage(ChatColor.GREEN + target.getName() + "의 발광 상태가 " + (target.isGlowing() ? "켜짐" : "꺼짐") + "으로 변경되었습니다.");
        return true;
    }
}
