package me.user.minervacore.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class TeamManager {

    private final Scoreboard mainScoreboard;
    private final Map<UUID, String> playerTeamMap = new HashMap<>();
    private final Map<String, ChatColor> teamColorMap = new HashMap<>();
    private final Map<String, String> teamKoreanNameMap = new HashMap<>();

    private static class ColorNamePair {
        ChatColor color;
        String koreanName;
        ColorNamePair(ChatColor color, String koreanName) {
            this.color = color;
            this.koreanName = koreanName;
        }
    }

    private final ColorNamePair[] TEAM_DATA = {
            new ColorNamePair(ChatColor.RED, "빨강팀"),
            new ColorNamePair(ChatColor.BLUE, "파랑팀"),
            new ColorNamePair(ChatColor.GREEN, "초록팀"),
            new ColorNamePair(ChatColor.YELLOW, "노랑팀"),
            new ColorNamePair(ChatColor.AQUA, "청록팀"),
            new ColorNamePair(ChatColor.LIGHT_PURPLE, "분홍팀"),
            new ColorNamePair(ChatColor.GOLD, "주황팀"),
            new ColorNamePair(ChatColor.DARK_PURPLE, "보라팀"),
            new ColorNamePair(ChatColor.WHITE, "하양팀"),
            new ColorNamePair(ChatColor.DARK_GREEN, "짙은초록팀")
    };

    public TeamManager() {
        this.mainScoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
    }

    public boolean isTeamModeActive() {
        // 활성화된 팀이 있고, 한 팀에 2명 이상인 팀이 하나라도 있으면 팀전으로 판별
        if (teamColorMap.isEmpty()) return false;
        Map<String, Integer> counts = new HashMap<>();
        for (String tId : playerTeamMap.values()) {
            counts.put(tId, counts.getOrDefault(tId, 0) + 1);
            if (counts.get(tId) >= 2) return true;
        }
        return false;
    }

    public void configureTeamsRandomly(int playersPerTeam) {
        clearCustomTeams();
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);

        int teamIndex = 0;
        while (!players.isEmpty()) {
            ColorNamePair cnp = TEAM_DATA[teamIndex % TEAM_DATA.length];
            String teamId = "T_" + (teamIndex + 1);
            teamColorMap.put(teamId, cnp.color);
            teamKoreanNameMap.put(teamId, cnp.koreanName);

            registerTeamOnAllBoards(teamId, cnp.color, cnp.koreanName);

            for (int i = 0; i < playersPerTeam && !players.isEmpty(); i++) {
                Player p = players.remove(0);
                addPlayerToTeamInternal(teamId, cnp.color, cnp.koreanName, p);
                p.sendMessage(cnp.color + "[팀] 당신은 [" + cnp.koreanName + "]에 배정되었습니다!");
            }
            teamIndex++;
        }
    }

    public void createManualTeam(List<Player> players) {
        for (Player p : players) {
            removePlayerFromCurrentTeam(p);
        }

        int teamIndex = (int) (System.currentTimeMillis() % 10000);
        ColorNamePair cnp = TEAM_DATA[new Random().nextInt(TEAM_DATA.length)];
        String teamId = "MT_" + teamIndex;
        teamColorMap.put(teamId, cnp.color);
        teamKoreanNameMap.put(teamId, cnp.koreanName);

        registerTeamOnAllBoards(teamId, cnp.color, cnp.koreanName);

        for (Player p : players) {
            addPlayerToTeamInternal(teamId, cnp.color, cnp.koreanName, p);
            p.sendMessage(cnp.color + "[팀] [" + cnp.koreanName + "]으로 지정되었습니다!");
        }
    }

    private void addPlayerToTeamInternal(String teamId, ChatColor color, String koreanName, Player p) {
        playerTeamMap.put(p.getUniqueId(), teamId);

        String formattedName = color + "[" + koreanName + "] " + p.getName();
        if (formattedName.length() > 40) formattedName = formattedName.substring(0, 40);
        p.setPlayerListName(formattedName);
        p.setDisplayName(color + p.getName() + ChatColor.RESET);

        addEntryToAllBoards(teamId, p.getName());
    }

    private void removePlayerFromCurrentTeam(Player p) {
        String existingTeam = playerTeamMap.remove(p.getUniqueId());
        if (existingTeam != null) {
            p.setPlayerListName(p.getName());
            p.setDisplayName(p.getName());
            removeEntryFromAllBoards(existingTeam, p.getName());
        }
    }

    public void syncPlayerToBoards(Player p) {
        String teamId = playerTeamMap.get(p.getUniqueId());
        if (teamId != null) {
            ChatColor color = teamColorMap.getOrDefault(teamId, ChatColor.WHITE);
            String koreanName = teamKoreanNameMap.getOrDefault(teamId, "팀");
            String formattedName = color + "[" + koreanName + "] " + p.getName();
            if (formattedName.length() > 40) formattedName = formattedName.substring(0, 40);
            p.setPlayerListName(formattedName);
            p.setDisplayName(color + p.getName() + ChatColor.RESET);

            registerTeamOnPlayerBoard(p.getScoreboard(), teamId, color, koreanName);
            Team t = p.getScoreboard().getTeam(teamId);
            if (t != null) {
                t.addEntry(p.getName());
            }
        }
    }

    public void syncAllTeamsToBoard(Scoreboard board) {
        for (Map.Entry<String, ChatColor> entry : teamColorMap.entrySet()) {
            String teamId = entry.getKey();
            String koreanName = teamKoreanNameMap.getOrDefault(teamId, "팀");
            registerTeamOnPlayerBoard(board, teamId, entry.getValue(), koreanName);
        }
        for (Map.Entry<UUID, String> entry : playerTeamMap.entrySet()) {
            Player target = Bukkit.getPlayer(entry.getKey());
            if (target != null && target.isOnline()) {
                Team t = board.getTeam(entry.getValue());
                if (t != null) {
                    t.addEntry(target.getName());
                }
            }
        }
    }

    private void registerTeamOnAllBoards(String name, ChatColor color, String koreanName) {
        registerTeamOnPlayerBoard(mainScoreboard, name, color, koreanName);
        for (Player p : Bukkit.getOnlinePlayers()) {
            registerTeamOnPlayerBoard(p.getScoreboard(), name, color, koreanName);
        }
    }

    private void registerTeamOnPlayerBoard(Scoreboard board, String name, ChatColor color, String koreanName) {
        if (board == null) return;
        Team team = board.getTeam(name);
        if (team == null) {
            team = board.registerNewTeam(name);
        }
        team.setColor(color);
        team.setPrefix(color + "[" + koreanName + "] ");
        team.setSuffix(ChatColor.RESET.toString());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    private void addEntryToAllBoards(String teamId, String playerName) {
        Team mainT = mainScoreboard.getTeam(teamId);
        if (mainT != null) mainT.addEntry(playerName);

        for (Player p : Bukkit.getOnlinePlayers()) {
            Team t = p.getScoreboard().getTeam(teamId);
            if (t != null) {
                t.addEntry(playerName);
            }
        }
    }

    private void removeEntryFromAllBoards(String teamId, String playerName) {
        Team mainT = mainScoreboard.getTeam(teamId);
        if (mainT != null) mainT.removeEntry(playerName);

        for (Player p : Bukkit.getOnlinePlayers()) {
            Team t = p.getScoreboard().getTeam(teamId);
            if (t != null) {
                t.removeEntry(playerName);
            }
        }
    }

    public boolean areInSameTeam(Player p1, Player p2) {
        String t1 = playerTeamMap.get(p1.getUniqueId());
        String t2 = playerTeamMap.get(p2.getUniqueId());
        return t1 != null && t1.equals(t2);
    }

    public String getPlayerTeam(Player p) {
        return playerTeamMap.get(p.getUniqueId());
    }

    public String getPlayerTeamKoreanName(Player p) {
        String t = getPlayerTeam(p);
        if (t != null && teamKoreanNameMap.containsKey(t)) {
            return teamKoreanNameMap.get(t);
        }
        return null;
    }

    public ChatColor getPlayerColor(Player p) {
        String t = getPlayerTeam(p);
        if (t != null && teamColorMap.containsKey(t)) {
            return teamColorMap.get(t);
        }
        return ChatColor.WHITE;
    }

    public void clearCustomTeams() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setPlayerListName(p.getName());
            p.setDisplayName(p.getName());
        }

        for (String teamId : new HashSet<>(teamColorMap.keySet())) {
            Team mainT = mainScoreboard.getTeam(teamId);
            if (mainT != null) mainT.unregister();

            for (Player p : Bukkit.getOnlinePlayers()) {
                Team t = p.getScoreboard().getTeam(teamId);
                if (t != null) t.unregister();
            }
        }

        playerTeamMap.clear();
        teamColorMap.clear();
        teamKoreanNameMap.clear();
    }
}
