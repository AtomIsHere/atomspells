package com.github.atomishere.atomspells.wand;

import com.github.atomishere.atomspells.AtomSpells;
import com.github.atomishere.atomspells.spells.SpellKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class WandManager implements Listener {
    private final AtomSpells plugin;
    private final NamespacedKey wandTag;

    private final Map<UUID, List<Boolean>> casts = new HashMap<>();
    private final Map<UUID, BukkitTask> timeouts = new HashMap<>();

    public WandManager(AtomSpells plugin) {
        this.plugin = plugin;
        this.wandTag = new NamespacedKey(plugin, "wand");
    }

    public boolean isWand(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().has(wandTag);
    }

    public Optional<Wand> getWand(ItemStack item) {
        return Optional.ofNullable(item.getItemMeta().getPersistentDataContainer().get(wandTag, Wand.WAND_DATA_TYPE));
    }

    private ItemStack createTestWand() {
        ItemStack wandStack = new ItemStack(Material.STICK);

        Wand wand = new Wand();

        wand.addSpell((byte) 0x0, plugin.getSpellRegistry()
                .getSpell(SpellKeys.EXPLOSION_SPELL_KEY)
                .orElseThrow(() -> new IllegalStateException("Explosion spell is null")));
        wand.addSpell((byte) 0x1, plugin.getSpellRegistry()
                .getSpell(SpellKeys.HEALING_SPELL_KEY)
                .orElseThrow(() -> new IllegalStateException("Healing spell is null")));

        ItemMeta meta = wandStack.getItemMeta();
        meta.getPersistentDataContainer().set(wandTag, Wand.WAND_DATA_TYPE, wand);
        wandStack.setItemMeta(meta);

        return wandStack;
    }

    private byte convertToSpellTag(List<Boolean> clicks) {
        if(clicks.size() != 3) {
            throw new IllegalArgumentException("Clicks array must be of length 3");
        }

        byte spellTag = 0;
        for(byte i = 0; i < 3; i++) {
            if(clicks.get(i)) {
                spellTag |= 0x1 << i;
            }
        }

        return spellTag;
    }

    private void addClick(Player player, boolean click) {
        UUID playerUUID = player.getUniqueId();

        if(timeouts.containsKey(playerUUID)) {
            timeouts.get(playerUUID).cancel();
        }

        player.playSound(player.getLocation(), "block.lever.click", 1, 1);

        casts.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(click);
        displayClicks(player);
        timeouts.put(player.getUniqueId(), Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> casts.get(playerUUID).clear(), 20));
    }

    private void displayClicks(Player player) {
        List<Boolean> clicks = casts.get(player.getUniqueId());

        int emptySlots = 3 - clicks.size();

        TextComponent.Builder message = Component.text();
        for(boolean click : clicks) {
            if(click) {
                message.append(Component.text("<L> "));
            } else {
                message.append(Component.text("<R> "));
            }
        }

        for(int i = 0; i < emptySlots; i++) {
            message.append(Component.text("<> "));
        }

        plugin.getActionHud().sendMessage(player, message.build(), 20);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        ItemStack offItem = player.getInventory().getItemInOffHand();

        if(offItem.getItemMeta() != null && isWand(offItem) && event.getAction() == Action.RIGHT_CLICK_AIR) {
            WandMenu menu = new WandMenu(plugin);

            player.openInventory(menu.getInventory());
        } else if(item != null && item.getItemMeta() != null && isWand(item)) {
            Action action = event.getAction();

            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                addClick(player, false);
            } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                addClick(player, true);
            }

            List<Boolean> clicks = casts.get(player.getUniqueId());
            if(clicks.size() == 3) {
                Wand wand = getWand(item).orElseThrow(() -> new IllegalStateException("Wand is null"));

                byte spellTag = convertToSpellTag(clicks);
                player.sendMessage("Spell tag: " + spellTag);

                wand.getSpell(spellTag).ifPresentOrElse(
                        spellId -> plugin.getSpellRegistry()
                                .getSpell(spellId)
                                .orElseThrow(() -> new IllegalStateException("Spell is null"))
                                .performSpell(player),
                        () -> plugin.getActionHud()
                                .sendMessage(player, Component.text("No spell on that slot!")
                                        .color(NamedTextColor.RED), 20)
                );

                clicks.clear();
            }
        }
    }
}
