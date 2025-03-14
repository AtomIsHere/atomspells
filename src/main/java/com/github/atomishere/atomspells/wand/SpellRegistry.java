package com.github.atomishere.atomspells.wand;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// TODO: Make player spell assignments per player
public class SpellRegistry {
    private final Map<Byte, Spell> spells = new HashMap<>();

    public void registerSpell(byte spellTag, Spell spell) {
        if(spellTag < 0 || spellTag > 7) {
            throw new IllegalArgumentException("Spell tag must be between 0 and 7");
        }

        spells.put(spellTag, spell);
    }

    public Optional<Spell> getSpell(byte spellTag) {
        return Optional.ofNullable(spells.get(spellTag));
    }
}
