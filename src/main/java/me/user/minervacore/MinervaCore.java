package me.user.minervacore;

import me.user.minervacore.commands.*;
import me.user.minervacore.listeners.GameEventListener;
import me.user.minervacore.managers.RecipeManager;
import me.user.minervacore.managers.ScoreboardManager;
import me.user.minervacore.managers.TeamManager;
import me.user.minervacore.managers.TeleportManager;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.*;

public final class MinervaCore extends JavaPlugin implements Listener {

    private static MinervaCore instance;
    private TeamManager teamManager;
    private TeleportManager teleportManager;
    private ScoreboardManager scoreboardManager;
    private ResourceCommand resourceCommand;
    private GameEventListener gameEventListener;

    private boolean invulnerableMode = false;
    private boolean pvpAllowed = false;
    private boolean waitMode = false;
    private boolean resourceStarted = false;
    private boolean globalGlowEnabled = false;

    private final Set<UUID> permanentInvisPlayers = new HashSet<>();

    // 관리자(OP)만 사용 가능하고, 일반 유저가 쳤을 때 채팅창 반응/에러 메시지조차 안 뜨게 원천 차단할 명령어 목록
    private final Set<String> adminOnlyCommands = new HashSet<>(Arrays.asList(
            "/명령어", "/대기", "/청키", "/자원", "/자원시간", "/템", "/무적모드",
            "/1등", "/팀", "/초기화", "/발광", "/아이피", "/롤백",
            "/minervacore:명령어", "/minervacore:대기", "/minervacore:청키", "/minervacore:자원",
            "/minervacore:자원시간", "/minervacore:템", "/minervacore:무적모드",
            "/minervacore:1등", "/minervacore:팀", "/minervacore:초기화",
            "/minervacore:발광", "/minervacore:아이피", "/minervacore:롤백"
    ));

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        for (World world : getServer().getWorlds()) {
            world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
            world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        }

        this.teamManager = new TeamManager();
        this.teleportManager = new TeleportManager();
        this.scoreboardManager = new ScoreboardManager();
        this.scoreboardManager.setTeamManager(teamManager);

        this.gameEventListener = new GameEventListener(this, scoreboardManager, teamManager);
        this.resourceCommand = new ResourceCommand(this, teleportManager, teamManager);

        RecipeManager.registerAllRecipes(this);

        // 명령어 등록
        Objects.requireNonNull(getCommand("명령어")).setExecutor(new HelpCommand());
        Objects.requireNonNull(getCommand("대기")).setExecutor(new WaitCommand(this));
        Objects.requireNonNull(getCommand("청키")).setExecutor(new ChunkyCommand(this));
        Objects.requireNonNull(getCommand("팀")).setExecutor(new TeamCommand(teamManager));
        Objects.requireNonNull(getCommand("자원")).setExecutor(resourceCommand);
        Objects.requireNonNull(getCommand("템")).setExecutor(new ItemCommand());
        Objects.requireNonNull(getCommand("발광")).setExecutor(new GlowCommand());
        Objects.requireNonNull(getCommand("아이피")).setExecutor(new IpCommand(this));
        Objects.requireNonNull(getCommand("롤백")).setExecutor(new RollbackCommand());
        Objects.requireNonNull(getCommand("초기화")).setExecutor(new ResetCommand(this, resourceCommand, teamManager, teleportManager, scoreboardManager, gameEventListener));
        Objects.requireNonNull(getCommand("무적모드")).setExecutor(new InvulnerableCommand(this));
        Objects.requireNonNull(getCommand("1등")).setExecutor(new WinCommand(this));

        // 일반 유저 사용 가능 명령어 등록
        Objects.requireNonNull(getCommand("수선")).setExecutor(new MendingCommand());
        Objects.requireNonNull(getCommand("가시제거")).setExecutor(new RemoveThornsCommand());
        Objects.requireNonNull(getCommand("tp")).setExecutor(new TpCommand(teleportManager, teamManager));
        Objects.requireNonNull(getCommand("top")).setExecutor(new TopCommand());

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(gameEventListener, this);

        getLogger().info("MinervaCore 배틀 플러그인이 성공적으로 구동되었습니다.");
    }

    @Override
    public void onDisable() {
        if (resourceCommand != null) {
            resourceCommand.stopAllTasks();
        }
        saveConfig();
        getLogger().info("MinervaCore 플러그인이 비활성화되었습니다.");
    }

    public static MinervaCore getInstance() {
        return instance;
    }

    public boolean isInvulnerableMode() {
        return invulnerableMode;
    }

    public void setInvulnerableMode(boolean invulnerableMode) {
        this.invulnerableMode = invulnerableMode;
    }

    public boolean isPvpAllowed() {
        return pvpAllowed;
    }

    public void setPvpAllowed(boolean pvpAllowed) {
        this.pvpAllowed = pvpAllowed;
    }

    public boolean isWaitMode() {
        return waitMode;
    }

    public void setWaitMode(boolean waitMode) {
        this.waitMode = waitMode;
    }

    public boolean isResourceStarted() {
        return resourceStarted;
    }

    public void setResourceStarted(boolean resourceStarted) {
        this.resourceStarted = resourceStarted;
    }

    public boolean isGlobalGlowEnabled() {
        return globalGlowEnabled;
    }

    public void setGlobalGlowEnabled(boolean globalGlowEnabled) {
        this.globalGlowEnabled = globalGlowEnabled;
    }

    public void addPermanentInvisPlayer(UUID uuid) {
        permanentInvisPlayers.add(uuid);
    }

    public boolean isPermanentInvis(UUID uuid) {
        return permanentInvisPlayers.contains(uuid);
    }

    public void clearPermanentInvis() {
        permanentInvisPlayers.clear();
    }

    // [핵심] 일반 유저가 관리자 명령어 시도 시 채팅창에 어떤 반응도 안 뜨게(에러 메시지 포함) 완전 캔슬!
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return; // 관리자(OP)는 모든 명령어 정상 실행 및 메시지 확인

        String msg = event.getMessage().trim().toLowerCase();
        String cmd = msg.split(" ")[0];

        if (adminOnlyCommands.contains(cmd)) {
            event.setCancelled(true); // 일반 유저는 시도 자체 완전 무반응 취소!
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InetSocketAddress addr = player.getAddress();
        if (addr != null) {
            String ip = addr.getAddress().getHostAddress();
            getConfig().set("ips." + player.getName().toLowerCase(), ip);
            saveConfig();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && invulnerableMode) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player && !pvpAllowed) {
            event.setCancelled(true);
        }
    }
}
