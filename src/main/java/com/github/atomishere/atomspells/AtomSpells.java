package com.github.atomishere.atomspells;

import com.github.atomishere.atomspells.wand.SpellRegistry;
import com.github.atomishere.atomspells.wand.WandManager;
import com.github.atomishere.atomspells.wand.spells.ExplosionSpell;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AtomSpells extends JavaPlugin {
    private final ManaManager manaManager = new ManaManager(this);
    private final WandManager wandManager = new WandManager(this);

    private final SpellRegistry spellRegistry = new SpellRegistry();

    private BukkitTask manaTask;

    public SpellRegistry getSpellRegistry() {
        return spellRegistry;
    }

    @Override
    public void onEnable() {
        spellRegistry.registerSpell((byte) 0x0, new ExplosionSpell(manaManager));

        Bukkit.getPluginManager().registerEvents(wandManager, this);
        this.manaTask = Bukkit.getServer().getScheduler().runTaskTimer(this, manaManager, 0, 20);
    }

    @Override
    public void onDisable() {
        manaTask.cancel();
    }
}
