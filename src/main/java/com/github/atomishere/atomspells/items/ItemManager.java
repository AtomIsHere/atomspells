package com.github.atomishere.atomspells.items;

import com.github.atomishere.atomspells.AtomSpells;
import com.github.atomishere.atomspells.spells.Spell;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemManager implements Listener {
    private final AtomSpells plugin;
    private final NamespacedKey spellItemKey;

    private final Map<NamespacedKey, SpellItem> spellItems = new HashMap<>();

    private final ItemStack emptySpellScroll;
    private final Map<NamespacedKey, Double> scrollSpawnChance = new HashMap<>();

    @SuppressWarnings("UnstableApiUsage")
    public ItemManager(AtomSpells plugin) {
        this.plugin = plugin;
        this.spellItemKey = new NamespacedKey(plugin, "spell_item");

        this.emptySpellScroll = new ItemStack(Material.PAPER);

        ItemMeta emptySpellScrollMeta = emptySpellScroll.getItemMeta();
        emptySpellScrollMeta.customName(Component.text("Empty Spell Scroll"));
        emptySpellScrollMeta.setRarity(ItemRarity.RARE);
        emptySpellScroll.setItemMeta(emptySpellScrollMeta);

        emptySpellScroll.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public void registerSpellItem(NamespacedKey spellKey, SpellItem item) {
        if(!plugin.getSpellRegistry().spellRegistered(spellKey)) {
            throw new IllegalArgumentException("Spell with key " + spellKey + " not registered!");
        }

        spellItems.put(spellKey, item);
    }

    public Optional<SpellItem> getSpellItem(NamespacedKey spellKey) {
        return Optional.ofNullable(spellItems.get(spellKey));
    }

    public Optional<Spell> getSpellFromItem(ItemStack item) {
        String spellId = item.getItemMeta().getPersistentDataContainer().get(spellItemKey, PersistentDataType.STRING);
        if(spellId == null) {
            return Optional.empty();
        }

        return plugin.getSpellRegistry().getSpell(NamespacedKey.fromString(spellId));
    }

    public boolean isSpellItem(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().has(spellItemKey);
    }

    public ItemStack getEmptySpellScroll() {
        return emptySpellScroll.clone();
    }

    public void setScrollSpawnChance(NamespacedKey lootTableKey, double chance) {
        scrollSpawnChance.put(lootTableKey, chance);
    }

    @EventHandler
    public void onGeneration(ChunkPopulateEvent event) {
        BlockState[] tileEntities = event.getChunk().getTileEntities();

        for(BlockState state : tileEntities) {
            if(state.getType() == Material.CHEST) {
                Chest chest = (Chest) state.getBlock().getState();
                LootTable lootTable = chest.getLootTable();

                if(lootTable != null && scrollSpawnChance.containsKey(lootTable.getKey())) {
                    double chance = scrollSpawnChance.get(lootTable.getKey());
                    if(Math.random() < chance) {
                        chest.getInventory().addItem(getEmptySpellScroll());
                    }
                }
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public Optional<ItemStack> createSpellItem(NamespacedKey spellKey) {
        SpellItem item = spellItems.get(spellKey);
        if(item == null) {
            return Optional.empty();
        }

        ItemStack spellItem = new ItemStack(Material.PAPER);
        ItemMeta spellItemMeta = spellItem.getItemMeta();

        spellItemMeta.customName(item.name());
        spellItemMeta.lore(item.lore());
        spellItemMeta.setRarity(ItemRarity.RARE);

        spellItemMeta.getPersistentDataContainer().set(spellItemKey, PersistentDataType.STRING, spellKey.toString());

        spellItem.setItemMeta(spellItemMeta);

        spellItem.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return Optional.of(spellItem);
    }

    public void createRecipes() {
        for(Map.Entry<NamespacedKey, SpellItem> spellItemEntry : spellItems.entrySet()) {
            ItemStack spellItem = createSpellItem(spellItemEntry.getKey()).orElseThrow(() -> new IllegalStateException("Spell item is null"));

            ShapedRecipe itemRecipe = new ShapedRecipe(new NamespacedKey(plugin, spellItemEntry.getKey().getKey() + "_item"), spellItem);
            spellItemEntry.getValue().recipeBuilder().accept(itemRecipe);

            Bukkit.getServer().addRecipe(itemRecipe);
        }
    }
}
