package me.user.minervacore.commands;

import me.user.minervacore.managers.TeamManager;
import me.user.minervacore.managers.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpCommand implements CommandExecutor {

    private final TeleportManager teleportManager;
    private final TeamManager teamManager;

    public TpCommand(TeleportManager teleportManager, TeamManager teamManager) {
        this.teleportManager = teleportManager;
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }

        if (!teleportManager.isResourceTime()) {
            player.sendMessage(ChatColor.RED + "자원 시간에만 사용 가능합니다.");
            return true;
        }

        if (teleportManager.hasUsedTeleport(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "이미 팀원 텔레포트 기회(1회)를 사용하셨습니다.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "사용법: /tp [팀원닉네임]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "해당 팀원을 찾을 수 없습니다.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "자신에게는 이동할 수 없습니다.");
            return true;
        }

        if (!teamManager.areInSameTeam(player, target)) {
            player.sendMessage(ChatColor.RED + "같은 팀원에게만 텔레포트할 수 있습니다.");
            return true;
        }

        player.teleport(target.getLocation());
        teleportManager.recordTeleport(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "[TP 완료] 팀원 " + target.getName() + "에게 텔레포트되었습니다. (자원 시간 1회 소모)");
        return true;
    }
}
