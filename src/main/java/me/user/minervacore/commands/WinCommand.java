package me.user.minervacore.commands;

import me.user.minervacore.MinervaCore;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class WinCommand implements CommandExecutor {

    private final MinervaCore plugin;

    public WinCommand(MinervaCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "관리자(OP) 권한이 필요합니다.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "사용법: /1등 [닉네임]");
            return true;
        }

        Player winner = Bukkit.getPlayer(args[0]);
        String winnerName = (winner != null) ? winner.getName() : args[0];

        String winTitle = ChatColor.GOLD + "" + ChatColor.BOLD + "1등 축하합니다 !";
        String winSub = ChatColor.YELLOW + "이게임을 야무지게 하셨군요";

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(winTitle, winSub, 10, 120, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        Bukkit.broadcastMessage(ChatColor.GREEN + "========================================");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "★ 최종 승리자: " + ChatColor.WHITE + winnerName + ChatColor.GOLD + " ★");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "이게임을 야무지게 하셨군요!");
        Bukkit.broadcastMessage(ChatColor.GREEN + "========================================");

        if (winner != null && winner.isOnline()) {
            new BukkitRunnable() {
                int count = 0;

                @Override
                public void run() {
                    if (count >= 10 || !winner.isOnline()) {
                        cancel();
                        return;
                    }
                    launchVictoryFirework(winner.getLocation());
                    count++;
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }

        return true;
    }

    private void launchVictoryFirework(Location loc) {
        if (loc.getWorld() == null) return;
        Firework fw = loc.getWorld().spawn(loc.clone().add(0, 1, 0), Firework.class);
        FireworkMeta fm = fw.getFireworkMeta();
        fm.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.YELLOW, Color.RED, Color.ORANGE, Color.PURPLE)
                .withFade(Color.WHITE)
                .withFlicker()
                .withTrail()
                .build());
        fm.setPower(1);
        fw.setFireworkMeta(fm);
    }
}
