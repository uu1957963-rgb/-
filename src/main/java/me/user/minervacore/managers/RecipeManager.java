package me.user.minervacore.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeManager {

    public static void registerAllRecipes(JavaPlugin plugin) {
        // 기존 조합법들
        addShapeless(plugin, "custom_ender_pearl", new ItemStack(Material.ENDER_PEARL, 1), Material.RAW_COPPER);
        addShapeless(plugin, "custom_totem", new ItemStack(Material.TOTEM_OF_UNDYING, 1), Material.RAW_GOLD);
        addShapeless(plugin, "custom_exp_bottle_1", new ItemStack(Material.EXPERIENCE_BOTTLE, 1), Material.COBBLESTONE);
        addShapeless(plugin, "custom_exp_bottle_2", new ItemStack(Material.EXPERIENCE_BOTTLE, 2), Material.COBBLED_DEEPSLATE);
        addShapeless(plugin, "custom_ender_eye", new ItemStack(Material.ENDER_EYE, 1), Material.REDSTONE, Material.ENDER_PEARL);
        addShapeless(plugin, "custom_end_crystal", new ItemStack(Material.END_CRYSTAL, 1), Material.ENDER_EYE, Material.RAW_IRON);
        addShapeless(plugin, "custom_golden_carrot", new ItemStack(Material.GOLDEN_CARROT, 1), Material.RAW_GOLD, Material.RAW_IRON);
        addShapeless(plugin, "custom_golden_apple", new ItemStack(Material.GOLDEN_APPLE, 1), Material.RAW_GOLD, Material.RAW_IRON, Material.GOLDEN_CARROT);
        addShapeless(plugin, "custom_enchanted_golden_apple", new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1), Material.GOLDEN_APPLE, Material.GOLDEN_CARROT, Material.EMERALD);

        // 네더라이트 주괴 조합
        NamespacedKey netheriteKey = new NamespacedKey(plugin, "custom_netherite_ingot");
        ShapedRecipe netheriteRecipe = new ShapedRecipe(netheriteKey, new ItemStack(Material.NETHERITE_INGOT, 1));
        netheriteRecipe.shape("RDR", "DAD", "RDR");
        netheriteRecipe.setIngredient('R', Material.REDSTONE);
        netheriteRecipe.setIngredient('D', Material.DIAMOND);
        netheriteRecipe.setIngredient('A', Material.AMETHYST_SHARD);
        safeAddRecipe(netheriteRecipe, netheriteKey);

        // 네더라이트 방어구
        NamespacedKey chestKey = new NamespacedKey(plugin, "custom_netherite_chestplate");
        ShapedRecipe chestRecipe = new ShapedRecipe(chestKey, new ItemStack(Material.NETHERITE_CHESTPLATE, 1));
        chestRecipe.shape("N N", "NNN", "NNN");
        chestRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        safeAddRecipe(chestRecipe, chestKey);

        NamespacedKey legsKey = new NamespacedKey(plugin, "custom_netherite_leggings");
        ShapedRecipe legsRecipe = new ShapedRecipe(legsKey, new ItemStack(Material.NETHERITE_LEGGINGS, 1));
        legsRecipe.shape("NNN", "N N", "N N");
        legsRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        safeAddRecipe(legsRecipe, legsKey);

        NamespacedKey bootsKey = new NamespacedKey(plugin, "custom_netherite_boots");
        ShapedRecipe bootsRecipe = new ShapedRecipe(bootsKey, new ItemStack(Material.NETHERITE_BOOTS, 1));
        bootsRecipe.shape("N N", "N N", "   ");
        bootsRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        safeAddRecipe(bootsRecipe, bootsKey);

        // 네더라이트 주괴 -> 다이아몬드 4개
        addShapeless(plugin, "custom_diamonds_from_netherite", new ItemStack(Material.DIAMOND, 4), Material.NETHERITE_INGOT);

        // 심층암 조약돌 + 석탄 3개 -> 흑요석 5개
        NamespacedKey obsidianKey = new NamespacedKey(plugin, "custom_obsidian");
        ShapedRecipe obsidianRecipe = new ShapedRecipe(obsidianKey, new ItemStack(Material.OBSIDIAN, 5));
        obsidianRecipe.shape(" D ", "CCC", "   ");
        obsidianRecipe.setIngredient('D', Material.COBBLED_DEEPSLATE);
        obsidianRecipe.setIngredient('C', Material.COAL);
        safeAddRecipe(obsidianRecipe, obsidianKey);

        // 흑요석 + 금 원석 -> 발광석 1개
        addShapeless(plugin, "custom_glowstone_raw_gold", new ItemStack(Material.GLOWSTONE, 1), Material.OBSIDIAN, Material.RAW_GOLD);

        // 흑요석 6개 + 엔드 크리스탈 2개 + 다이아몬드 1개 -> 리스폰 정박기 1개
        NamespacedKey anchorKey = new NamespacedKey(plugin, "custom_respawn_anchor");
        ShapedRecipe anchorRecipe = new ShapedRecipe(anchorKey, new ItemStack(Material.RESPAWN_ANCHOR, 1));
        anchorRecipe.shape("OOO", "CDC", "OOO");
        anchorRecipe.setIngredient('O', Material.OBSIDIAN);
        anchorRecipe.setIngredient('C', Material.END_CRYSTAL);
        anchorRecipe.setIngredient('D', Material.DIAMOND);
        safeAddRecipe(anchorRecipe, anchorKey);

        // ==========================================
        // [신규 추가된 조합법]
        // ==========================================

        // 1. 네더라이트 주괴 2개 + 막대기 1개 -> 네더라이트 검 (NETHERITE_SWORD)
        NamespacedKey swordKey = new NamespacedKey(plugin, "custom_netherite_sword");
        ShapedRecipe swordRecipe = new ShapedRecipe(swordKey, new ItemStack(Material.NETHERITE_SWORD, 1));
        swordRecipe.shape(" N ", " N ", " S ");
        swordRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        swordRecipe.setIngredient('S', Material.STICK);
        safeAddRecipe(swordRecipe, swordKey);

        // 2. 네더라이트 주괴 2개 (위/아래) -> 브리즈 막대 1개 (BREEZE_ROD)
        NamespacedKey breezeKey = new NamespacedKey(plugin, "custom_breeze_rod");
        ShapedRecipe breezeRecipe = new ShapedRecipe(breezeKey, new ItemStack(Material.BREEZE_ROD, 1));
        breezeRecipe.shape(" N ", " N ", "   ");
        breezeRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        safeAddRecipe(breezeRecipe, breezeKey);

        // 3. 화로: 네더라이트 블록(NETHERITE_BLOCK) 제련 -> 리스폰 정박기 1개 (RESPAWN_ANCHOR)
        NamespacedKey furnaceAnchorKey = new NamespacedKey(plugin, "custom_furnace_respawn_anchor");
        if (Bukkit.getRecipe(furnaceAnchorKey) != null) {
            Bukkit.removeRecipe(furnaceAnchorKey);
        }
        FurnaceRecipe furnaceAnchorRecipe = new FurnaceRecipe(
                furnaceAnchorKey,
                new ItemStack(Material.RESPAWN_ANCHOR, 1),
                Material.NETHERITE_BLOCK,
                2.0f,
                200 // 10초(200틱) 제련 시간
        );
        Bukkit.addRecipe(furnaceAnchorRecipe);

        // 4. 철괴 8개 + 엔더 상자 1개 -> 매끄러운 돌 1개 (SMOOTH_STONE)
        NamespacedKey smoothStoneKey = new NamespacedKey(plugin, "custom_smooth_stone_box");
        ShapedRecipe smoothStoneRecipe = new ShapedRecipe(smoothStoneKey, new ItemStack(Material.SMOOTH_STONE, 1));
        smoothStoneRecipe.shape("III", "IEI", "III");
        smoothStoneRecipe.setIngredient('I', Material.IRON_INGOT);
        smoothStoneRecipe.setIngredient('E', Material.ENDER_CHEST);
        safeAddRecipe(smoothStoneRecipe, smoothStoneKey);
    }

    private static void addShapeless(JavaPlugin plugin, String keyName, ItemStack result, Material... ingredients) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);
        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        for (Material mat : ingredients) {
            recipe.addIngredient(mat);
        }
        Bukkit.addRecipe(recipe);
    }

    private static void safeAddRecipe(ShapedRecipe recipe, NamespacedKey key) {
        if (Bukkit.getRecipe(key) != null) {
            Bukkit.removeRecipe(key);
        }
        Bukkit.addRecipe(recipe);
    }
}
