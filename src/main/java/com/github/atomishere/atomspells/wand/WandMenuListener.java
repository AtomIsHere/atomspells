package com.github.atomishere.atomspells.wand;

import com.github.atomishere.atomspells.items.ItemManager;
import com.github.atomishere.atomspells.spells.Spell;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class WandMenuListener implements Listener {
    private final ItemManager itemManager;
    private final WandManager wandManager;

    public WandMenuListener(ItemManager itemManager, WandManager wandManager) {
        this.itemManager = itemManager;
        this.wandManager = wandManager;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if(event.getInventory().getHolder() instanceof WandMenu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof WandMenu) {
            ItemStack clickedItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            if(clickedItem != null && !itemManager.isSpellItem(clickedItem)) {
                event.setCancelled(true);
            }

            int columnClicked = event.getSlot() % 9;
            if(columnClicked == 3 || columnClicked == 8) {
                if(cursorItem.getType() != Material.AIR && !itemManager.isSpellItem(cursorItem)) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getInventory().getHolder() instanceof WandMenu menu) {
            Wand wand = new Wand();

            for(int row = 0; row < 4; row++) {
                int spellSlot = row * 9 + 3;
                ItemStack spellItem = event.getInventory().getItem(spellSlot);
                if(spellItem != null && itemManager.isSpellItem(spellItem)) {
                    Spell spell = itemManager.getSpellFromItem(spellItem).orElseThrow(() -> new IllegalStateException("Spell is null"));
                    byte spellTag = (byte) (2 * row);

                    wand.addSpell(spellTag, spell);
                }

                spellSlot = row * 9 + 8;
                spellItem = event.getInventory().getItem(spellSlot);
                if(spellItem != null && itemManager.isSpellItem(spellItem)) {
                    Spell spell = itemManager.getSpellFromItem(spellItem).orElseThrow(() -> new IllegalStateException("Spell is null"));
                    byte spellTag = (byte) (2 * row + 1);

                    wand.addSpell(spellTag, spell);
                }
            }

            wandManager.makeWand(menu.getWandItem(), wand);
        }
    }
}
