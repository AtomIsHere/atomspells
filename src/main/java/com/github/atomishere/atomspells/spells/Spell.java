package com.github.atomishere.atomspells.spells;

import org.bukkit.entity.Player;

public abstract class Spell { private final String name;

    protected Spell(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public abstract void performSpell(Player caster);
}
