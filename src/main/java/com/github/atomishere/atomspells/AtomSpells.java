package com.github.atomishere.atomspells;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AtomSpells extends JavaPlugin {
    private final ManaManager manaManager = new ManaManager(this);

    private BukkitTask manaTask;

    @Override
    public void onEnable() {
        this.manaTask = Bukkit.getServer().getScheduler().runTaskTimer(this, manaManager, 0, 20);
    }

    @Override
    public void onDisable() {
        manaTask.cancel();
    }
}
