package com.github.atomishere.atomspells;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class ManaManager implements Runnable {
    private static final double DEFAULT_MAX_MANA = 100;

    // TODO: Replace with attributes
    private final NamespacedKey maxManaKey;
    private final NamespacedKey manaKey;

    public ManaManager(AtomSpells plugin) {
        this.maxManaKey = new NamespacedKey(plugin, "max_mana");
        this.manaKey = new NamespacedKey(plugin, "mana");
    }

    public double getMaxMana(Player player) {
        Double maxMana = player.getPersistentDataContainer().get(maxManaKey, PersistentDataType.DOUBLE);
        if(maxMana == null) {
            maxMana = DEFAULT_MAX_MANA;
            player.getPersistentDataContainer().set(maxManaKey, PersistentDataType.DOUBLE, maxMana);
        }

        return maxMana;
    }

    public double getMana(Player player) {
        Double mana = player.getPersistentDataContainer().get(manaKey, PersistentDataType.DOUBLE);
        if(mana == null) {
            mana = getMaxMana(player);
            player.getPersistentDataContainer().set(manaKey, PersistentDataType.DOUBLE, mana);
        }

        return mana;
    }

    public void setMana(Player player, double mana) {
        player.getPersistentDataContainer().set(manaKey, PersistentDataType.DOUBLE, mana);
    }

    public void setMaxMana(Player player, double maxMana) {
        player.getPersistentDataContainer().set(maxManaKey, PersistentDataType.DOUBLE, maxMana);
    }

    public void addMana(Player player, double mana) {
        addMana(player, mana, true);
    }

    public void addMana(Player player, double mana, boolean maxManaCheck) {
        double currentMana = getMana(player);
        double maxMana = getMaxMana(player);
        double newMana = currentMana + mana;

        if(maxManaCheck && newMana > maxMana) {
            newMana = maxMana;
        }

        player.getPersistentDataContainer().set(manaKey, PersistentDataType.DOUBLE, newMana);
    }

    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            double manaRegen = getMaxMana(player) * 0.02;
            addMana(player, manaRegen);
        }
    }
}
