package me.user.minervacore.commands;

import me.user.minervacore.MinervaCore;
import me.user.minervacore.listeners.GameEventListener;
import me.user.minervacore.managers.ScoreboardManager;
import me.user.minervacore.managers.TeamManager;
import me.user.minervacore.managers.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class ResetCommand implements CommandExecutor {

    private final MinervaCore plugin;
    private final ResourceCommand resourceCommand;
    private final TeamManager teamManager;
    private final TeleportManager teleportManager;
    private final ScoreboardManager scoreboardManager;
    private final GameEventListener gameEventListener;

    public ResetCommand(MinervaCore plugin, ResourceCommand resourceCommand, TeamManager teamManager, TeleportManager teleportManager, ScoreboardManager scoreboardManager, GameEventListener gameEventListener) {
        this.plugin = plugin;
        this.resourceCommand = resourceCommand;
        this.teamManager = teamManager;
        this.teleportManager = teleportManager;
        this.scoreboardManager = scoreboardManager;
        this.gameEventListener = gameEventListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "관리자(OP) 권한이 필요합니다.");
            return true;
        }

        if (resourceCommand != null) {
            resourceCommand.stopAllTasks();
        }

        plugin.setInvulnerableMode(false);
        plugin.setPvpAllowed(false);
        plugin.setWaitMode(false);
        plugin.setResourceStarted(false);
        plugin.clearPermanentInvis();
        plugin.setGlobalGlowEnabled(false);

        teleportManager.clearTeleportHistory();
        teleportManager.setResourceTime(false);
        teamManager.clearCustomTeams();
        scoreboardManager.resetKills();
        gameEventListener.resetGameStatus();

        for (World w : Bukkit.getWorlds()) {
            w.getWorldBorder().reset();
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.setGlowing(false);
            p.removePotionEffect(PotionEffectType.INVISIBILITY);

            AttributeInstance maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(20.0);
            }
            p.setHealth(20.0);
            p.setFoodLevel(20);
        }

        Bukkit.broadcastMessage(ChatColor.GREEN + "========================================");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[서버 전체 초기화 완료]");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "모든 타이머, 보더, 대기 모드, 상태, 팀, 킬수가 리셋되었습니다.");
        Bukkit.broadcastMessage(ChatColor.GREEN + "========================================");
        return true;
    }
}
