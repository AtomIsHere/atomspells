package com.github.atomishere.atomspells.spells;

import com.github.atomishere.atomspells.AtomSpells;
import org.bukkit.NamespacedKey;

public final class SpellKeys {
    public static final NamespacedKey HEALING_SPELL_KEY;
    public static final NamespacedKey EXPLOSION_SPELL_KEY;

    static {
        HEALING_SPELL_KEY = createSpellKey("healing_spell");
        EXPLOSION_SPELL_KEY = createSpellKey("explosion_spell");
    }

    public static NamespacedKey createSpellKey(String id) {
        return new NamespacedKey(AtomSpells.PLUGIN_NAME.toLowerCase(), id);
    }

    private SpellKeys() {
        throw new AssertionError("No instances!");
    }
}
