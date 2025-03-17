package com.github.atomishere.atomspells.spells;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public abstract class Spell {
    private final NamespacedKey spellId;
    private final String name;

    protected Spell(NamespacedKey spellId, String name) {
        this.spellId = spellId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public NamespacedKey getSpellId() {
        return spellId;
    }


    public abstract void performSpell(Player caster);
}
