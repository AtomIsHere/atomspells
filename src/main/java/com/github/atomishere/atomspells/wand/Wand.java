package com.github.atomishere.atomspells.wand;

import com.github.atomishere.atomspells.spells.Spell;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Wand {
    public static final WandDataType WAND_DATA_TYPE = new WandDataType();

    private final Map<Byte, NamespacedKey> spells;

    public Wand() {
        this.spells = new HashMap<>();
    }

    private Wand(Map<Byte, NamespacedKey> spells) {
        this.spells = spells;
    }

    public void addSpell(byte spellTag, Spell spell) {
        if(spellTag < 0 || spellTag > 7) {
            throw new IllegalArgumentException("Spell tag must be between 0 and 7");
        }

        spells.put(spellTag, spell.getSpellId());
    }

    public void removeSpell(byte spellTag) {
        spells.remove(spellTag);
    }

    public Optional<NamespacedKey> getSpell(byte spellTag) {
        return Optional.ofNullable(spells.get(spellTag));
    }

    public static class WandDataType implements PersistentDataType<byte[], Wand> {
        private WandDataType() {}

        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<Wand> getComplexType() {
            return Wand.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(@NotNull Wand complex, @NotNull PersistentDataAdapterContext context) {
            ByteArrayDataOutput output = ByteStreams.newDataOutput();

            output.writeInt(complex.spells.size());
            for(Map.Entry<Byte, NamespacedKey> entry : complex.spells.entrySet()) {
                output.writeByte(entry.getKey());

                String spellId = entry.getValue().toString();
                byte[] spellIdBytes = spellId.getBytes(StandardCharsets.UTF_8);

                output.writeInt(spellIdBytes.length);
                output.write(spellIdBytes);
            }

            return output.toByteArray();
        }

        @Override
        public @NotNull Wand fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            ByteBuffer buffer = ByteBuffer.wrap(primitive);

            Map<Byte, NamespacedKey> spells = new HashMap<>();
            int spellsCount = buffer.getInt();

            for(int i = 0; i < spellsCount; i++) {
                byte spellTag = buffer.get();
                if(spellTag < 0 || spellTag > 7) {
                    throw new IllegalArgumentException("Spell tag must be between 0 and 7");
                }

                int spellIdLength = buffer.getInt();

                byte[] spellIdBytes = new byte[spellIdLength];
                buffer.get(spellIdBytes);

                String spellId = new String(spellIdBytes, StandardCharsets.UTF_8);
                spells.put(spellTag, NamespacedKey.fromString(spellId));
            }

            return new Wand(spells);
        }
    }
}
