package me.user.minervacore.commands;

import me.user.minervacore.MinervaCore;
import me.user.minervacore.managers.TeamManager;
import me.user.minervacore.managers.TeleportManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceCommand implements CommandExecutor {

    private final MinervaCore plugin;
    private final TeleportManager teleportManager;
    private final TeamManager teamManager;
    private BukkitTask currentTimerTask = null;
    private BukkitTask borderShrinkTask = null;
    private BukkitTask waitTask = null;
    private BukkitTask meteorCountdownTask = null;

    public ResourceCommand(MinervaCore plugin, TeleportManager teleportManager, TeamManager teamManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
        this.teamManager = teamManager;
    }

    public void stopAllTasks() {
        if (currentTimerTask != null && !currentTimerTask.isCancelled()) currentTimerTask.cancel();
        if (borderShrinkTask != null && !borderShrinkTask.isCancelled()) borderShrinkTask.cancel();
        if (waitTask != null && !waitTask.isCancelled()) waitTask.cancel();
        if (meteorCountdownTask != null && !meteorCountdownTask.isCancelled()) meteorCountdownTask.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "관리자(OP) 권한이 필요합니다.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "사용법 1: /" + label + " [시간] [보더크기]");
            sender.sendMessage(ChatColor.YELLOW + "사용법 2: /" + label + " [시간] [보더크기] [X] [Y] [Z] (지정 좌표로 전원 이동 후 시작)");
            sender.sendMessage(ChatColor.GRAY + "예시: /자원 10분 1000 0 100 0");
            return true;
        }

        stopAllTasks();

        int customBorderSize = 1000;
        Double tpX = null, tpY = null, tpZ = null;

        if (args.length >= 5) {
            try {
                customBorderSize = Integer.parseInt(args[1]);
                tpX = Double.parseDouble(args[2]);
                tpY = Double.parseDouble(args[3]);
                tpZ = Double.parseDouble(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "보더 및 좌표는 숫자여야 합니다.");
                return true;
            }
        } else if (args.length >= 2) {
            try {
                customBorderSize = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }

        int totalSeconds = parseTimeToSeconds(args[0]);
        if (totalSeconds <= 0) {
            sender.sendMessage(ChatColor.RED + "시간 형식이 올바르지 않습니다. (예: 10분, 5m, 30초 등)");
            return true;
        }

        World mainWorld = (sender instanceof Player p) ? p.getWorld() : Bukkit.getWorlds().get(0);

        plugin.setWaitMode(false);
        plugin.setResourceStarted(true);
        mainWorld.setDifficulty(Difficulty.PEACEFUL);

        WorldBorder border = mainWorld.getWorldBorder();
        if (tpX != null) {
            border.setCenter(tpX, tpZ);
        }
        border.setSize(customBorderSize);

        if (tpX != null && tpY != null && tpZ != null) {
            Location tpLoc = new Location(mainWorld, tpX + 0.5, tpY, tpZ + 0.5);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.teleport(tpLoc);
            }
            Bukkit.broadcastMessage(ChatColor.GOLD + "[이동] 전원이 지정된 시작 좌표(" + tpX.intValue() + ", " + tpY.intValue() + ", " + tpZ.intValue() + ")로 이동되었습니다.");
        }

        teleportManager.setResourceTime(true);
        plugin.setInvulnerableMode(true);
        plugin.setPvpAllowed(false);

        String mainTitle = ChatColor.GREEN + "" + ChatColor.BOLD + "자원시간 입니다";
        String subTitle = ChatColor.DARK_GREEN + "난이도: 평화로움 | 자원시간 종료 시 결투 시작";

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(mainTitle, subTitle, 10, 70, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        Bukkit.broadcastMessage(ChatColor.GREEN + "[자원 시작] " + ChatColor.YELLOW + "월드보더 크기: " + customBorderSize + "x" + customBorderSize);

        currentTimerTask = new BukkitRunnable() {
            int remaining = totalSeconds;

            @Override
            public void run() {
                if (remaining <= 0) {
                    onResourceTimeEnd(mainWorld);
                    cancel();
                    return;
                }

                ChatColor timeColor = (remaining <= 10) ? ChatColor.RED : ChatColor.GREEN;
                String actionBarMessage = ChatColor.DARK_GREEN + "자원 남은시간: " + timeColor + ChatColor.BOLD + formatSeconds(remaining);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
                }

                if (remaining == 10 || (remaining <= 5 && remaining >= 1)) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(ChatColor.YELLOW + "" + ChatColor.BOLD + remaining + "초 후 결투 시작!", ChatColor.RED + "전투 준비를 하세요!", 0, 20, 5);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    }
                }

                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }

    private void onResourceTimeEnd(World world) {
        teleportManager.setResourceTime(false);
        plugin.setInvulnerableMode(false);
        plugin.setPvpAllowed(true);

        world.setDifficulty(Difficulty.HARD);

        for (Player p : Bukkit.getOnlinePlayers()) {
            stripResourceItems(p);
            p.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "자원 시간 종료!", ChatColor.YELLOW + "전투가 시작됩니다! (난이도: 어려움)", 10, 80, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }

        applySoloInvisibility();
        matchAndTeleport1v1(world);
        startBorderShrink(world);
    }

    private void applySoloInvisibility() {
        if (teamManager.isTeamModeActive()) return;

        List<Player> alive = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                alive.add(p);
            }
        }

        if (alive.isEmpty()) return;

        Collections.shuffle(alive);
        int targetCount = Math.min(3, alive.size());

        for (int i = 0; i < targetCount; i++) {
            Player invisiblePlayer = alive.get(i);
            plugin.addPermanentInvisPlayer(invisiblePlayer.getUniqueId());
            invisiblePlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, true));
            invisiblePlayer.sendMessage(ChatColor.LIGHT_PURPLE + "[은신] 당신은 영구 투명화 상태가 되었습니다! (데미지나 우유를 마셔도 풀리지 않습니다)");
        }
        Bukkit.broadcastMessage(ChatColor.YELLOW + "[알림] 개인전 특수 효과로 3명의 플레이어가 무한 투명화되었습니다!");
    }

    private void stripResourceItems(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) continue;

            if (item.getType() == Material.ELYTRA || item.getType() == Material.FIREWORK_ROCKET) {
                player.getInventory().setItem(i, null);
            } else if (item.getType() == Material.NETHERITE_PICKAXE) {
                // /템으로 지급된 초과 인챈트 곡괭이(효율 > 5 또는 행운 > 3)만 인챈트 제거!
                // 유저가 제작대/모루로 직접 만든 일반 곡괭이는 인챈트 절대 보존!
                int eff = item.getEnchantmentLevel(Enchantment.EFFICIENCY);
                int fort = item.getEnchantmentLevel(Enchantment.FORTUNE);
                if (eff > 5 || fort > 3) {
                    for (Enchantment ench : new ArrayList<>(item.getEnchantments().keySet())) {
                        item.removeEnchantment(ench);
                    }
                }
            }
        }
        player.sendMessage(ChatColor.RED + "[안내] 겉날개/폭죽이 회수되었으며 지급된 네더라이트 곡괭이의 인챈트가 제거되었습니다.");
    }

    private void matchAndTeleport1v1(World world) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);

        List<PlayerPair> pairs = new ArrayList<>();
        Set<Player> matched = new HashSet<>();

        for (int i = 0; i < players.size(); i++) {
            Player p1 = players.get(i);
            if (matched.contains(p1)) continue;

            Player bestOpponent = null;
            for (int j = i + 1; j < players.size(); j++) {
                Player p2 = players.get(j);
                if (!matched.contains(p2) && !teamManager.areInSameTeam(p1, p2)) {
                    bestOpponent = p2;
                    break;
                }
            }

            if (bestOpponent != null) {
                matched.add(p1);
                matched.add(bestOpponent);
                pairs.add(new PlayerPair(p1, bestOpponent));
            }
        }

        for (Player p : players) {
            if (!matched.contains(p)) {
                pairs.add(new PlayerPair(p, null));
            }
        }

        Random rand = new Random();
        double borderSize = world.getWorldBorder().getSize();
        Location center = world.getWorldBorder().getCenter();
        int half = (int) (borderSize / 2) - 30;
        if (half < 10) half = 10;

        for (PlayerPair pair : pairs) {
            int rx = (int) center.getX() + rand.nextInt(half * 2) - half;
            int rz = (int) center.getZ() + rand.nextInt(half * 2) - half;
            int highestY = world.getHighestBlockYAt(rx, rz);
            Location arenaLoc = new Location(world, rx + 0.5, highestY + 1.0, rz + 0.5);

            pair.p1.teleport(arenaLoc);
            pair.p1.sendMessage(ChatColor.GOLD + "[전장 이동] 2인 결투 장소로 텔레포트되었습니다!");

            if (pair.p2 != null) {
                Location oppLoc = arenaLoc.clone().add(5, 0, 5);
                oppLoc.setY(world.getHighestBlockYAt(oppLoc) + 1.0);
                pair.p2.teleport(oppLoc);
                pair.p2.sendMessage(ChatColor.GOLD + "[전장 이동] 상대(" + pair.p1.getName() + ")와의 결투 장소로 텔레포트되었습니다!");
            }
        }
    }

    private void startBorderShrink(World world) {
        WorldBorder border = world.getWorldBorder();
        borderShrinkTask = new BukkitRunnable() {
            @Override
            public void run() {
                double currentSize = border.getSize();
                if (currentSize <= 20.0) {
                    border.setSize(20.0);
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "[월드보더] 20x20 최종 구역에 도달했습니다!");
                    Bukkit.broadcastMessage(ChatColor.GOLD + "★ 5분 뒤 최종 보강된 심층암 결투장으로 강제 이동됩니다! ★");

                    start5MinuteCountdown(world);
                    cancel();
                    return;
                }

                // [요청 반영] 1분마다 200블록씩 빠르게 수축! (수축 시간 25초로 신속하게 이동)
                double targetSize = Math.max(20.0, currentSize - 200.0);
                border.setSize(targetSize, 25);
                Bukkit.broadcastMessage(ChatColor.RED + "[월드보더] 자기장이 200블록 수축합니다! (현재: " + (int) targetSize + "x" + (int) targetSize + ")");
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // 1200틱 = 60초(1분)
    }

    private void start5MinuteCountdown(World world) {
        waitTask = new BukkitRunnable() {
            int remainingSeconds = 300; // 5분 = 300초

            @Override
            public void run() {
                if (remainingSeconds <= 0) {
                    buildFinalArenaAndTeleport(world);
                    cancel();
                    return;
                }

                if (remainingSeconds == 180 || remainingSeconds == 60 || remainingSeconds == 30 || remainingSeconds <= 10) {
                    Bukkit.broadcastMessage(ChatColor.RED + "[경고] 최종 결투장 이동까지 " + ChatColor.YELLOW + remainingSeconds + "초" + ChatColor.RED + " 남았습니다!");
                }

                remainingSeconds--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void buildFinalArenaAndTeleport(World world) {
        Location center = world.getWorldBorder().getCenter();
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int minY = world.getMinHeight();
        int floorY = minY + 1;
        int arenaHeight = 22;
        int half = 10;

        for (int x = cx - half; x <= cx + half; x++) {
            for (int z = cz - half; z <= cz + half; z++) {
                Block floorBlock = world.getBlockAt(x, floorY, z);
                floorBlock.setType(Material.REINFORCED_DEEPSLATE);

                boolean isEdge = (x == cx - half || x == cx + half || z == cz - half || z == cz + half);

                for (int y = floorY + 1; y <= floorY + arenaHeight && y < world.getMaxHeight(); y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (isEdge || y == floorY + arenaHeight) {
                        b.setType(Material.REINFORCED_DEEPSLATE);
                    } else {
                        if (b.getType() != Material.AIR) {
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
        }

        Location arenaCenter = new Location(world, cx + 0.5, floorY + 1.0, cz + 0.5);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                p.teleport(arenaCenter);
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
            }
        }

        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "[최종 결투] 보강된 심층암 전장으로 텔레포트되었습니다! 끝까지 살아남으세요!");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcastMessage(ChatColor.RED + "[경고] 메테오가 20초마다 떨어집니다 (설치 블록 파괴 & 관통 피해)");
            startMeteorStorm(world, cx, cz, floorY, half, arenaHeight);
        }, 40L);
    }

    private void startMeteorStorm(World world, int cx, int cz, int floorY, int half, int arenaHeight) {
        if (meteorCountdownTask != null && !meteorCountdownTask.isCancelled()) meteorCountdownTask.cancel();

        meteorCountdownTask = new BukkitRunnable() {
            int second = 20;

            @Override
            public void run() {
                if (second > 0) {
                    String bar = ChatColor.RED + "☄ 메테오 투하까지: " + ChatColor.YELLOW + ChatColor.BOLD + second + "초";
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar));
                    }
                    second--;
                } else {
                    executeMeteorImpact(world, cx, cz, floorY, half, arenaHeight);
                    second = 20;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void executeMeteorImpact(World world, int cx, int cz, int floorY, int half, int arenaHeight) {
        Location centerLoc = new Location(world, cx + 0.5, floorY + 2.0, cz + 0.5);
        world.playSound(centerLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, centerLoc, 8, 5, 2, 5);
        world.spawnParticle(Particle.LAVA, centerLoc, 80, 8, 2, 8);

        String impactBar = ChatColor.DARK_RED + "" + ChatColor.BOLD + "💥 메테오 폭격 직격! (-100 관통 데미지 & 블록 파괴)";
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(impactBar));
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                Location loc = p.getLocation();
                if (Math.abs(loc.getBlockX() - cx) <= half && Math.abs(loc.getBlockZ() - cz) <= half) {
                    p.damage(100.0);
                }
            }
        }

        for (int x = cx - half + 1; x <= cx + half - 1; x++) {
            for (int z = cz - half + 1; z <= cz + half - 1; z++) {
                for (int y = floorY + 1; y < floorY + arenaHeight; y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() != Material.AIR && b.getType() != Material.REINFORCED_DEEPSLATE) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }
    }

    private static class PlayerPair {
        Player p1;
        Player p2;
        PlayerPair(Player p1, Player p2) { this.p1 = p1; this.p2 = p2; }
    }

    private int parseTimeToSeconds(String input) {
        int seconds = 0;
        boolean matched = false;
        Matcher minMatcher = Pattern.compile("(\\d+)(?:분|m)").matcher(input);
        if (minMatcher.find()) { seconds += Integer.parseInt(minMatcher.group(1)) * 60; matched = true; }
        Matcher secMatcher = Pattern.compile("(\\d+)(?:초|s)").matcher(input);
        if (secMatcher.find()) { seconds += Integer.parseInt(secMatcher.group(1)); matched = true; }
        if (!matched) {
            try { seconds = Integer.parseInt(input); } catch (NumberFormatException ignored) {}
        }
        return seconds;
    }

    private String formatSeconds(int totalSec) {
        int m = totalSec / 60;
        int s = totalSec % 60;
        if (m > 0 && s > 0) return m + "분 " + s + "초";
        if (m > 0) return m + "분";
        return s + "초";
    }
}
