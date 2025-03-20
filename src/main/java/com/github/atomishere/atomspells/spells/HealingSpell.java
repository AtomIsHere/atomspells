package com.github.atomishere.atomspells.spells;

import com.github.atomishere.atomspells.AtomSpells;
import com.github.atomishere.atomspells.RayCaster;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class HealingSpell extends Spell {
    private final AtomSpells plugin;

    public HealingSpell(NamespacedKey spellId, AtomSpells plugin) {
        super(spellId, "Healing Circle");

        this.plugin = plugin;
    }

    @Override
    public void performSpell(Player caster) {
        RayCaster.RayResult ray = RayCaster.at(caster.getEyeLocation())
                .maxDistance(10.0D)
                .showTrail(true)
                .dustOptions(new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1))
                .detectEntities(false)
                .cast();

        if(!ray.isHit()) {
            caster.sendMessage("You didn't hit anything!");
            return;
        }

        if(plugin.getManaManager().getMana(caster) < 50) {
            caster.sendMessage("You don't have enough mana!");
            return;
        }

        plugin.getManaManager().setMana(caster, plugin.getManaManager().getMana(caster) - 10);
        HealingCircle circle = new HealingCircle(ray.getHitBlock().get().getLocation().add(0.5, 1, 0.5), 3);
        circle.spawn(plugin);
    }

    public static class HealingCircle implements Runnable {
        private final Location center;
        private final double radius;

        private BukkitTask task;
        private int ticksLeft = 200;

        public HealingCircle(Location center, double radius) {
            this.center = center;
            this.radius = radius;
        }

        public void spawn(AtomSpells plugin) {
            this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 0, 1);
        }

        public void spawnParticles() {
            double angleStep = Math.PI / 32;
            for(double angle = 0; angle < Math.PI * 2; angle += angleStep) {
                double xOffset = Math.cos(angle) * radius;
                double zOffset = Math.sin(angle) * radius;
                Location particleLocation = center.clone().add(xOffset, 0, zOffset);

                center.getWorld().spawnParticle(Particle.DUST, particleLocation, 1,
                        new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1));
            }
        }

        @Override
        public void run() {
            if(ticksLeft <= 0) {
                task.cancel();
                return;
            }

            spawnParticles();
            if(Bukkit.getServer().getCurrentTick() % 10 == 0) {
                center.getWorld().getNearbyEntities(center, 3, 3, 3, entity -> entity instanceof Player)
                        .stream()
                        .map(entity -> (Player) entity)
                        .forEach(player -> player.setHealth(Math.min(player.getHealth() + 1, player.getMaxHealth())));
            }

            ticksLeft--;
        }
    }
}
