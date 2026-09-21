package me.user.minervacore.commands;

import me.user.minervacore.MinervaCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ChunkyCommand implements CommandExecutor {

    private final MinervaCore plugin;
    private BukkitTask currentGenTask = null;
    private boolean isGenerating = false;

    public ChunkyCommand(MinervaCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "관리자(OP) 권한이 필요합니다.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "========== [ 청키(청크 사전 생성) 시스템 ] ==========");
            sender.sendMessage(ChatColor.YELLOW + "/청키 시작 [반경블록] " + ChatColor.WHITE + "- 지정 반경 청크를 미리 생성하여 랙을 없앱니다.");
            sender.sendMessage(ChatColor.YELLOW + "/청키 중지 " + ChatColor.WHITE + "- 진행 중인 청크 생성을 중지합니다.");
            sender.sendMessage(ChatColor.GRAY + "예시: /청키 시작 1000 (1000x1000 영역 사전 로딩)");
            sender.sendMessage(ChatColor.GOLD + "================================================");
            return true;
        }

        if (args[0].equalsIgnoreCase("중지") || args[0].equalsIgnoreCase("취소") || args[0].equalsIgnoreCase("stop")) {
            if (!isGenerating || currentGenTask == null) {
                sender.sendMessage(ChatColor.RED + "현재 진행 중인 청키 작업이 없습니다.");
                return true;
            }
            currentGenTask.cancel();
            isGenerating = false;
            sender.sendMessage(ChatColor.GREEN + "[청키] 청크 사전 생성이 중지되었습니다.");
            return true;
        }

        if (args[0].equalsIgnoreCase("시작") || args[0].equalsIgnoreCase("start")) {
            if (isGenerating) {
                sender.sendMessage(ChatColor.RED + "이미 청크 생성이 진행 중입니다. (/청키 중지 로 취소 가능)");
                return true;
            }

            int radiusBlocks = 1000;
            if (args.length >= 2) {
                try {
                    radiusBlocks = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "반경은 숫자여야 합니다.");
                    return true;
                }
            }

            World world = (sender instanceof Player p) ? p.getWorld() : Bukkit.getWorlds().get(0);
            startPreGeneration(sender, world, radiusBlocks);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "알 수 없는 청키 명령어입니다. (/청키 로 확인)");
        return true;
    }

    private void startPreGeneration(CommandSender sender, World world, int radiusBlocks) {
        int chunkRadius = (radiusBlocks / 16) + 1;
        int minChunkX = -chunkRadius;
        int maxChunkX = chunkRadius;
        int minChunkZ = -chunkRadius;
        int maxChunkZ = chunkRadius;

        int totalChunks = (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
        isGenerating = true;

        Bukkit.broadcastMessage(ChatColor.GREEN + "[청키] 청크 사전 생성을 시작합니다! (총 " + totalChunks + "개 청크, 반경: " + radiusBlocks + "블록)");

        currentGenTask = new BukkitRunnable() {
            int currentX = minChunkX;
            int currentZ = minChunkZ;
            int loadedCount = 0;
            long lastBroadcastTime = System.currentTimeMillis();

            @Override
            public void run() {
                if (!isGenerating) {
                    cancel();
                    return;
                }

                // 1틱(0.05초)당 20개의 청크를 순차 생성하여 서버 튕김 없이 부드럽게 작업
                for (int i = 0; i < 20; i++) {
                    if (currentX > maxChunkX) {
                        isGenerating = false;
                        Bukkit.broadcastMessage(ChatColor.GOLD + "[청키 완료] " + totalChunks + "개의 청크 생성이 모두 완료되었습니다! (렉 없는 쾌적한 플레이 가능)");
                        cancel();
                        return;
                    }

                    if (!world.isChunkLoaded(currentX, currentZ)) {
                        world.loadChunk(currentX, currentZ, true);
                    }
                    loadedCount++;

                    currentZ++;
                    if (currentZ > maxChunkZ) {
                        currentZ = minChunkZ;
                        currentX++;
                    }
                }

                if (System.currentTimeMillis() - lastBroadcastTime >= 5000) { // 5초마다 진행도 브로드캐스트
                    lastBroadcastTime = System.currentTimeMillis();
                    double percent = ((double) loadedCount / totalChunks) * 100.0;
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "[청키 진행 중] " + String.format("%.1f", percent) + "% (" + loadedCount + "/" + totalChunks + " 청크)");
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
