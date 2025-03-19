package com.github.atomishere.atomspells.wand;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.atomishere.atomspells.AtomSpells;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WandMenu implements InventoryHolder {
    private static final String LEFT_HEAD_ID = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2M1ODMyMWQ0YmZmYmVjMmRkZjY2YmYzOGNmMmY5ZTlkZGYzZmEyZjEzODdkYzdkMzBjNjJiNGQwMTBjOCJ9fX0=";
    private static final String RIGHT_HEAD_ID = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2NiODgyMjVlZTRhYjM5ZjdjYmY1ODFmMjJjYmYwOGJkY2MzMzg4NGYxZmY3NDc2ODkzMTI4NDE1MTZjMzQ1In19fQ==";

    private final Inventory inventory;

    private final ItemStack leftHead;
    private final ItemStack rightHead;

    public WandMenu(AtomSpells plugin) {
        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("Wand"));

        this.leftHead = createHead(LEFT_HEAD_ID);
        this.rightHead = createHead(RIGHT_HEAD_ID);

        int invenPos = 0;
        for(byte i = 0; i < 8; i++) {
            ItemStack[] heads = createHeads(i);

            for(ItemStack head : heads) {
                inventory.setItem(invenPos, head);
                invenPos++;
            }

            invenPos++;

            if(i % 2 == 0) {
                inventory.setItem(invenPos, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
                invenPos++;
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private static ItemStack createHead(String id) {
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", id));

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        meta.setPlayerProfile(profile);
        head.setItemMeta(meta);

        return head;
    }

    private ItemStack[] createHeads(byte spellTag) {
        ItemStack[] heads = new ItemStack[3];

        for(int i = 0; i < 3; i++) {
            byte slot = (byte) (0x1 << i);

            if((spellTag & slot) == slot) {
                heads[i] = leftHead.clone();
            } else {
                heads[i] = rightHead.clone();
            }
        }

        return heads;
    }
}
