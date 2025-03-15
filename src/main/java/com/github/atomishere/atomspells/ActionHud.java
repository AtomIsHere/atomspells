package com.github.atomishere.atomspells;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class ActionHud implements Runnable {
    private final List<ActionHudElement> elements = new ArrayList<>();
    private final Map<UUID, ActionHudMessage> messages = new HashMap<>();

    public void addElement(ActionHudElement element) {
        elements.add(element);
    }

    public void sendMessage(Player player, Component message, int ticks) {
        messages.put(player.getUniqueId(), new ActionHudMessage(message, ticks));
    }

    @Override
    public void run() {
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            if(messages.containsKey(player.getUniqueId())) {
                ActionHudMessage message = messages.get(player.getUniqueId());

                if(message.isExpired()) {
                    messages.remove(player.getUniqueId());
                } else {
                    message.tick();
                    player.sendActionBar(message.getMessage());
                }
            } else {
                TextComponent.Builder message = Component.text();

                int i = 0;
                for(ActionHudElement element : elements) {
                    message.append(element.getElement(player));

                    if(i < elements.size() - 1) {
                        message.append(Component.text(" "));
                    }

                    i++;
                }

                player.sendActionBar(message.build());
            }
        }
    }

    @FunctionalInterface
    public interface ActionHudElement {
        Component getElement(Player player);
    }

    public static class ActionHudMessage {
        private final Component message;
        private int ticksLeft;

        private ActionHudMessage(Component message, int ticksLeft) {
            this.message = message;
            this.ticksLeft = ticksLeft;
        }

        public Component getMessage() {
            return message;
        }

        public boolean isExpired() {
            return ticksLeft <= 0;
        }

        public void tick() {
            ticksLeft--;
        }
    }
}
