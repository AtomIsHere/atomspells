package com.github.atomishere.atomspells.items;

import com.github.atomishere.atomspells.AtomSpells;
import com.github.atomishere.atomspells.spells.Spell;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemManager {
    private final AtomSpells plugin;
    private final NamespacedKey spellItemKey;

    private final Map<NamespacedKey, SpellItem> spellItems = new HashMap<>();

    public ItemManager(AtomSpells plugin) {
        this.plugin = plugin;
        this.spellItemKey = new NamespacedKey(plugin, "spell_item");
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
