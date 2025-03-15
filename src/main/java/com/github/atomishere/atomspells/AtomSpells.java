package com.github.atomishere.atomspells;

import com.github.atomishere.atomspells.wand.SpellRegistry;
import com.github.atomishere.atomspells.wand.WandManager;
import com.github.atomishere.atomspells.wand.spells.ExplosionSpell;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AtomSpells extends JavaPlugin {
    private final ManaManager manaManager = new ManaManager(this);
    private final WandManager wandManager = new WandManager(this);
    private final ActionHud actionHud = new ActionHud();

    private final SpellRegistry spellRegistry = new SpellRegistry();

    private BukkitTask manaTask;
    private BukkitTask actionHudTask;

    public SpellRegistry getSpellRegistry() {
        return spellRegistry;
    }

    public ActionHud getActionHud() {
        return actionHud;
    }

    @Override
    public void onEnable() {
        spellRegistry.registerSpell((byte) 0x0, new ExplosionSpell(manaManager));
        actionHud.addElement(player -> Component.text("★ Mana: ")
                .append(Component.text(Math.round(manaManager.getMana(player))))
                .append(Component.text("/"))
                .append(Component.text(Math.round(manaManager.getMaxMana(player))))
                .color(NamedTextColor.AQUA)
        );

        Bukkit.getPluginManager().registerEvents(wandManager, this);
        this.manaTask = Bukkit.getServer().getScheduler().runTaskTimer(this, manaManager, 0, 20);
        this.actionHudTask = Bukkit.getServer().getScheduler().runTaskTimer(this, actionHud, 0, 1);
    }

    @Override
    public void onDisable() {
        actionHudTask.cancel();
        manaTask.cancel();
    }
}
