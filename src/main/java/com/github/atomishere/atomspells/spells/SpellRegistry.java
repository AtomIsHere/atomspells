package com.github.atomishere.atomspells.spells;

import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpellRegistry {
    private final Map<NamespacedKey, Spell> spells = new HashMap<>();

    public void registerSpell(Spell spell) {
        spells.put(spell.getSpellId(), spell);
    }

    public boolean spellRegistered(NamespacedKey spellId) {
        return spells.containsKey(spellId);
    }

    public Optional<Spell> getSpell(NamespacedKey spellId) {
        return Optional.ofNullable(spells.get(spellId));
    }
}
