package me.user.minervacore.listeners;

import me.user.minervacore.MinervaCore;
import me.user.minervacore.managers.ScoreboardManager;
import me.user.minervacore.managers.TeamManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;

public class GameEventListener implements Listener {

    private final MinervaCore plugin;
    private final ScoreboardManager scoreboardManager;
    private final TeamManager teamManager;
    private final Set<UUID> pendingSpectators = new HashSet<>();

    public GameEventListener(MinervaCore plugin, ScoreboardManager scoreboardManager, TeamManager teamManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
        this.teamManager = teamManager;
    }

    public void resetGameStatus() {
        pendingSpectators.clear();
        scoreboardManager.updateAllScoreboards();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.isWaitMode()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "[대기 모드] 대기 중에는 블록을 파괴할 수 없습니다!");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.isWaitMode()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "[대기 모드] 대기 중에는 블록을 설치할 수 없습니다!");
        }
    }

    @EventHandler
    public void onExpBottle(ExpBottleEvent event) {
        ProjectileSource source = event.getEntity().getShooter();
        if (source instanceof Player player) {
            int expToGive = event.getExperience();
            event.setExperience(0);
            player.giveExp(expToGive);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTotemResurrect(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player player) {
            boolean wasGlowing = player.isGlowing();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (wasGlowing || plugin.isGlobalGlowEnabled()) {
                    player.setGlowing(true);
                }
                if (plugin.isPermanentInvis(player.getUniqueId())) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, true));
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPermanentInvis(player.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, true));
            }, 1L);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.isResourceStarted()) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.RED + "[안내] 게임(자원시간)이 이미 시작되어 관전자 모드로 접속되었습니다.");
        }

        teamManager.syncPlayerToBoards(player);
        scoreboardManager.updateAllScoreboards();

        if (player.getGameMode() != GameMode.SPECTATOR) {
            ItemStack helmet = player.getInventory().getHelmet();
            if (helmet == null || helmet.getType() != Material.CARVED_PUMPKIN) {
                ItemStack strawHat = new ItemStack(Material.CARVED_PUMPKIN);
                ItemMeta meta = strawHat.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "밀짚모자 (네더라이트 투구 등급)");

                    NamespacedKey armorKey = new NamespacedKey(plugin, "strawhat_armor");
                    NamespacedKey toughKey = new NamespacedKey(plugin, "strawhat_toughness");
                    NamespacedKey kbKey = new NamespacedKey(plugin, "strawhat_kb");

                    meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(armorKey, 3.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));
                    meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(toughKey, 3.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));
                    meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(kbKey, 0.1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));

                    strawHat.setItemMeta(meta);
                }
                safeEnchant(strawHat, "protection", 4);
                safeEnchant(strawHat, "binding_curse", 1);
                safeEnchant(strawHat, "vanishing_curse", 1);
                player.getInventory().setHelmet(strawHat);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTakeDamageProtectionFix(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.getType() == Material.CARVED_PUMPKIN) {
            double originalDamage = event.getDamage();
            event.setDamage(originalDamage * 0.84);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDamageTeamCheck(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;
        Entity damager = event.getDamager();

        if (damager instanceof Player p) {
            attacker = p;
        } else if (damager instanceof Projectile proj) {
            ProjectileSource src = proj.getShooter();
            if (src instanceof Player p) attacker = p;
        } else if (damager instanceof TNTPrimed tnt) {
            Entity source = tnt.getSource();
            if (source instanceof Player p) attacker = p;
        } else if (damager instanceof EnderCrystal crystal) {
            Entity lastDamager = crystal.getLastDamageCause() instanceof EntityDamageByEntityEvent sub
                    ? sub.getDamager() : null;
            if (lastDamager instanceof Player p) {
                attacker = p;
            } else if (lastDamager instanceof Projectile proj && proj.getShooter() instanceof Player p) {
                attacker = p;
            }
        }

        if (attacker != null && !attacker.equals(victim)) {
            if (teamManager.areInSameTeam(victim, attacker)) {
                event.setCancelled(true);
                return;
            }
        }

        if (!plugin.isPvpAllowed() && attacker != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        Location deathLoc = victim.getLocation().getBlock().getLocation().add(0.5, 0.2, 0.5);

        if (killer != null && !killer.equals(victim)) {
            scoreboardManager.addKill(killer);
        }

        pendingSpectators.add(victim.getUniqueId());
        victim.sendMessage(ChatColor.RED + "[탈락] 사망하여 관전자 모드로 전환됩니다.");

        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        event.getDrops().clear();

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(victim);
            skullMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + victim.getName() + "의 머리");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "우클릭 시 최대 체력 2칸 증가 및 보급품 획득!");
            skullMeta.setLore(lore);
            skull.setItemMeta(skullMeta);
        }

        if (killer != null && !killer.equals(victim)) {
            HashMap<Integer, ItemStack> remain = killer.getInventory().addItem(skull);
            if (!remain.isEmpty()) {
                spawnStationaryItem(deathLoc, skull);
            } else {
                killer.sendMessage(ChatColor.GREEN + "[처치] " + victim.getName() + "의 머리가 인벤토리에 지급되었습니다!");
            }
        } else {
            drops.add(0, skull);
        }

        for (ItemStack item : drops) {
            if (item != null && item.getType() != Material.AIR) {
                spawnStationaryItem(deathLoc, item);
            }
        }

        scoreboardManager.updateAllScoreboards();
        // 자동 1등 폭죽 알림 제거 완료 (/1등 [닉네임] 명령어로만 수동 선언)
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.DARK_RED + "관전자 모드로 전환되었습니다.");
            scoreboardManager.updateAllScoreboards();
        }, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            scoreboardManager.updateAllScoreboards();
        }, 5L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = (event.getHand() == EquipmentSlot.HAND) ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();

        if (item.getType() != Material.PLAYER_HEAD) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().endsWith("의 머리")) {
            return;
        }

        event.setCancelled(true);

        int requiredSlots = 8;
        int emptySlots = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack invItem = player.getInventory().getItem(i);
            if (invItem == null || invItem.getType() == Material.AIR) {
                emptySlots++;
            }
        }

        if (emptySlots < requiredSlots) {
            player.sendMessage(ChatColor.RED + "[경고] 인벤토리에 최소 " + requiredSlots + "칸의 빈 공간이 필요합니다! (현재: " + emptySlots + "칸)");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        item.setAmount(item.getAmount() - 1);

        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            double currentBase = maxHealth.getBaseValue();
            maxHealth.setBaseValue(currentBase + 4.0);
            player.setHealth(Math.min(player.getHealth() + 4.0, maxHealth.getValue()));
        }

        player.getInventory().addItem(
                new ItemStack(Material.OBSIDIAN, 16),
                new ItemStack(Material.END_CRYSTAL, 16),
                new ItemStack(Material.ENDER_PEARL, 16),
                new ItemStack(Material.RESPAWN_ANCHOR, 8),
                new ItemStack(Material.GLOWSTONE, 16),
                new ItemStack(Material.TOTEM_OF_UNDYING, 8),
                new ItemStack(Material.GOLDEN_APPLE, 8),
                new ItemStack(Material.EXPERIENCE_BOTTLE, 32)
        );

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "[머리 사용] 최대 체력이 2칸 증가하고 강력한 전투 보급품을 획득했습니다!");
    }

    private void spawnStationaryItem(Location loc, ItemStack itemStack) {
        if (loc.getWorld() == null) return;
        Item item = loc.getWorld().dropItem(loc, itemStack);
        item.setVelocity(new Vector(0, 0, 0));
        item.setPickupDelay(0);
    }

    private void safeEnchant(ItemStack item, String key, int level) {
        NamespacedKey nKey = NamespacedKey.minecraft(key);
        Enchantment ench = Registry.ENCHANTMENT.get(nKey);
        if (ench == null) {
            ench = Enchantment.getByKey(nKey);
        }
        if (ench != null) {
            item.addUnsafeEnchantment(ench, level);
        }
    }
}
