package me.user.minervacore.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }

        Location loc = player.getLocation();
        int highestY = player.getWorld().getHighestBlockYAt(loc);
        loc.setY(highestY + 1.0);
        player.teleport(loc);
        player.sendMessage(ChatColor.GREEN + "[TOP] 지상 가장 높은 블록으로 텔레포트되었습니다.");
        return true;
    }
}
