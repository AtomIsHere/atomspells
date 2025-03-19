package com.github.atomishere.atomspells.items;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;
import java.util.function.Consumer;

public record SpellItem(Component name, List<Component> lore, Consumer<ShapedRecipe> recipeBuilder) {
}
