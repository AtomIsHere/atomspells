package com.github.atomishere.atomspells;

import com.github.atomishere.atomspells.wand.WandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AtomSpells extends JavaPlugin {
    private final ManaManager manaManager = new ManaManager(this);
    private final WandManager wandManager = new WandManager(this);

    private BukkitTask manaTask;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(wandManager, this);
        this.manaTask = Bukkit.getServer().getScheduler().runTaskTimer(this, manaManager, 0, 20);
    }

    @Override
    public void onDisable() {
        manaTask.cancel();
    }
}
