package me.user.minervacore.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ScoreboardManager {

    private final Map<UUID, Integer> killsMap = new HashMap<>();
    private TeamManager teamManager;
    private int rainbowIndex = 0;

    private final ChatColor[] RAINBOW_COLORS = {
            ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN,
            ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE
    };

    public void setTeamManager(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    public void addKill(Player player) {
        killsMap.put(player.getUniqueId(), getKills(player) + 1);
        updateAllScoreboards();
    }

    public int getKills(Player player) {
        return killsMap.getOrDefault(player.getUniqueId(), 0);
    }

    public void resetKills() {
        killsMap.clear();
        updateAllScoreboards();
    }

    public void updateScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = player.getScoreboard();
        if (board.equals(manager.getMainScoreboard())) {
            board = manager.getNewScoreboard();
            player.setScoreboard(board);
        }

        if (teamManager != null) {
            teamManager.syncAllTeamsToBoard(board);
        }

        Objective obj = board.getObjective("MinervaBoard");
        String title = ChatColor.WHITE + "🗡 " + ChatColor.RED + "" + ChatColor.BOLD + "처치 점수";
        if (obj == null) {
            obj = board.registerNewObjective("MinervaBoard", Criteria.DUMMY, title);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            obj.setDisplayName(title);
        }

        for (String entry : new ArrayList<>(board.getEntries())) {
            board.resetScores(entry);
        }

        int scoreIndex = 15;

        // [요청 반영] 맨 위에 레인보우 qground.labmc.kr 서버 주소 표시
        String rainbowDomain = getRainbowText("qground.labmc.kr");
        Score domainScore = obj.getScore(rainbowDomain);
        domainScore.setScore(scoreIndex--);

        Score blankTop = obj.getScore(ChatColor.GRAY + "-----------------");
        blankTop.setScore(scoreIndex--);

        // 사진 스타일: '생존 [닉네임]' 또는 '사망 [닉네임]'
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player p : players) {
            if (scoreIndex <= 1) break;
            boolean isAlive = (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) && !p.isDead();
            String status = isAlive ? ChatColor.GREEN + "생존 " : ChatColor.RED + "사망 ";
            ChatColor nameColor = (teamManager != null) ? teamManager.getPlayerColor(p) : ChatColor.WHITE;

            String line = status + nameColor + p.getName();
            if (line.length() > 40) line = line.substring(0, 40);

            Score s = obj.getScore(line);
            s.setScore(getKills(p));
            scoreIndex--;
        }
    }

    private String getRainbowText(String text) {
        StringBuilder sb = new StringBuilder();
        ChatColor[] colors = {ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.AQUA, ChatColor.LIGHT_PURPLE};
        for (int i = 0; i < text.length(); i++) {
            sb.append(colors[(i + rainbowIndex) % colors.length]).append(text.charAt(i));
        }
        return sb.toString();
    }

    public void updateAllScoreboards() {
        rainbowIndex = (rainbowIndex + 1) % RAINBOW_COLORS.length;
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateScoreboard(p);
        }
    }
}
