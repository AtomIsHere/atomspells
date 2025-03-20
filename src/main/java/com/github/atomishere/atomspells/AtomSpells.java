package com.github.atomishere.atomspells;

import com.github.atomishere.atomspells.items.ItemManager;
import com.github.atomishere.atomspells.items.SpellItem;
import com.github.atomishere.atomspells.spells.ExplosionSpell;
import com.github.atomishere.atomspells.spells.HealingSpell;
import com.github.atomishere.atomspells.spells.SpellKeys;
import com.github.atomishere.atomspells.spells.SpellRegistry;
import com.github.atomishere.atomspells.wand.WandManager;
import com.github.atomishere.atomspells.wand.WandMenuListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.loot.LootTables;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;

public class AtomSpells extends JavaPlugin {
    public static final String PLUGIN_NAME = "AtomSpells";

    private final SpellRegistry spellRegistry = new SpellRegistry();
    private final ItemManager itemManager = new ItemManager(this);

    private final ManaManager manaManager = new ManaManager(this);
    private final WandManager wandManager = new WandManager(this);
    private final ActionHud actionHud = new ActionHud();


    private BukkitTask manaTask;
    private BukkitTask actionHudTask;

    public WandManager getWandManager() {
        return wandManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public ManaManager getManaManager() {
        return manaManager;
    }

    public SpellRegistry getSpellRegistry() {
        return spellRegistry;
    }

    public ActionHud getActionHud() {
        return actionHud;
    }

    private void registerSpells() {
        spellRegistry.registerSpell(new ExplosionSpell(SpellKeys.EXPLOSION_SPELL_KEY, manaManager));
        spellRegistry.registerSpell(new HealingSpell(SpellKeys.HEALING_SPELL_KEY, this));
    }

    private void registerSpellItems() {
        itemManager.registerSpellItem(SpellKeys.EXPLOSION_SPELL_KEY,
                new SpellItem(Component.text("Explosion Spell Scroll"),
                        Collections.emptyList(),
                        (shapedRecipe -> shapedRecipe.shape("BAB", "ACA", "BAB")
                                .setIngredient('A', Material.TNT)
                                .setIngredient('B', Material.GUNPOWDER)
                                .setIngredient('C', itemManager.getEmptySpellScroll()))));
        itemManager.registerSpellItem(SpellKeys.HEALING_SPELL_KEY,
                new SpellItem(Component.text("Healing Spell Scroll"),
                        Collections.emptyList(),
                        (shapedRecipe -> shapedRecipe.shape("BAB", "ACA", "BAB")
                                .setIngredient('A', Material.GOLDEN_APPLE)
                                .setIngredient('B', Material.GOLDEN_CARROT)
                                .setIngredient('C', itemManager.getEmptySpellScroll()))));
    }

    private void registerScrollSpawnChances() {
        itemManager.setScrollSpawnChance(LootTables.BURIED_TREASURE.getKey(), 0.5D);

        itemManager.setScrollSpawnChance(LootTables.ANCIENT_CITY.getKey(), 0.3D);

        itemManager.setScrollSpawnChance(LootTables.DESERT_PYRAMID.getKey(), 0.25D);
        itemManager.setScrollSpawnChance(LootTables.JUNGLE_TEMPLE.getKey(), 0.25D);

        itemManager.setScrollSpawnChance(LootTables.SIMPLE_DUNGEON.getKey(), 0.1D);
        itemManager.setScrollSpawnChance(LootTables.STRONGHOLD_CORRIDOR.getKey(), 0.15D);
        itemManager.setScrollSpawnChance(LootTables.STRONGHOLD_CROSSING.getKey(), 0.15D);
        itemManager.setScrollSpawnChance(LootTables.STRONGHOLD_LIBRARY.getKey(), 0.15D);
    }

    @Override
    public void onEnable() {
        registerSpells();
        registerSpellItems();

        itemManager.createRecipes();

        actionHud.addElement(player -> Component.text("★ Mana: ")
                .append(Component.text(Math.round(manaManager.getMana(player))))
                .append(Component.text("/"))
                .append(Component.text(Math.round(manaManager.getMaxMana(player))))
                .color(NamedTextColor.AQUA)
        );

        Bukkit.getPluginManager().registerEvents(wandManager, this);
        Bukkit.getPluginManager().registerEvents(itemManager, this);
        Bukkit.getPluginManager().registerEvents(new WandMenuListener(itemManager, wandManager), this);

        registerScrollSpawnChances();

        this.manaTask = Bukkit.getServer().getScheduler().runTaskTimer(this, manaManager, 0, 20);
        this.actionHudTask = Bukkit.getServer().getScheduler().runTaskTimer(this, actionHud, 0, 1);
    }

    @Override
    public void onDisable() {
        actionHudTask.cancel();
        manaTask.cancel();
    }
}
