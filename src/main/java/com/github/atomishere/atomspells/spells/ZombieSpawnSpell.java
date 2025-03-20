package com.github.atomishere.atomspells.spells;

import com.github.atomishere.atomspells.AtomSpells;
import com.github.atomishere.atomspells.RayCaster;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ZombieSpawnSpell extends Spell implements Listener {
    private final AtomSpells plugin;
    private final List<ZombieSpawnCircle> circles = new ArrayList<>();

    public ZombieSpawnSpell(NamespacedKey spellId, AtomSpells plugin) {
        super(spellId, "zombie_spawn_spell");
        this.plugin = plugin;
    }

    @Override
    public void performSpell(Player caster) {
        if(plugin.getManaManager().getMana(caster) < 30) {
            caster.sendMessage("You don't have enough mana!");
            return;
        }

        RayCaster.RayResult ray = RayCaster.at(caster.getEyeLocation())
                .maxDistance(10.0D)
                .detectEntities(true)
                .entityFilter(entity -> entity != caster && entity instanceof LivingEntity)
                .showTrail(true)
                .cast();

        if(ray.getHitEntity().isEmpty()) {
            caster.sendMessage("You didn't hit anything!");
            return;
        }

        plugin.getManaManager().setMana(caster, plugin.getManaManager().getMana(caster) - 30);

        LivingEntity entity = (LivingEntity) ray.getHitEntity().get();

        if(circles.stream().anyMatch(circle -> circle.zombies.contains(entity.getUniqueId()))) {
            caster.sendMessage("You can't target that entity!");
            return;
        }

        ZombieSpawnCircle circle = new ZombieSpawnCircle(entity);
        circle.spawn(3);

        circles.add(circle);
        circle.setCancelTask(Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> {
            circle.zombies.forEach(uuid -> Optional.ofNullable(Bukkit.getEntity(uuid)).ifPresent(Entity::remove));
            circles.remove(circle);
        }, 20 * 10));
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if(event.getEntity() instanceof Zombie zombie) {
            circles.stream().filter(circle -> circle.containsZombie(zombie)).findFirst().ifPresent(circle -> {
                event.setCancelled(true);
                event.setTarget(circle.target);
            });
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Optional<ZombieSpawnCircle> targetCircle = circles.stream().filter(circle -> circle.target.equals(event.getEntity())).findFirst();
        if(targetCircle.isPresent()) {
            targetCircle.get().cancelDeletion();
            circles.remove(targetCircle.get());
            return;
        }

        circles.stream()
                .filter(circle -> circle.zombies.contains(event.getEntity().getUniqueId()))
                .findFirst()
                .ifPresent(zombieSpawnCircle -> zombieSpawnCircle.zombies.remove(event.getEntity().getUniqueId()));
    }

    public static class ZombieSpawnCircle {
        private final LivingEntity target;
        private final List<UUID> zombies = new ArrayList<>();
        private BukkitTask cancelTask = null;

        private World world;

        public ZombieSpawnCircle(LivingEntity target) {
            this.target = target;
            this.world = target.getWorld();
        }

        public void spawn(int amount) {
            Location center = target.getLocation();
            world = center.getWorld();

            double radius = 3.0;

            for(int i = 0; i < amount; i++) {
                double angle = 2 * Math.PI * i / amount;
                double xOffset = Math.cos(angle) * radius;
                double zOffset = Math.sin(angle) * radius;

                Location spawnLocation = center.clone().add(xOffset, 0, zOffset);
                spawnLocation = findValidSpawnPosition(spawnLocation).orElse(null);

                if(spawnLocation != null) {
                    Zombie zombie = world.spawn(spawnLocation, Zombie.class);
                    zombie.setAdult();

                    zombie.setTarget(target);
                    zombies.add(zombie.getUniqueId());
                }
            }
        }

        public void cancelDeletion() {
            if(cancelTask != null) {
                cancelTask.cancel();
                cancelTask = null;
            }
        }

        private void setCancelTask(BukkitTask task) {
            this.cancelTask = task;
        }

        private boolean containsZombie(Zombie zombie) {
            return zombies.contains(zombie.getUniqueId());
        }

        private Optional<Location> findValidSpawnPosition(Location location) {
            World world = location.getWorld();

            if(world == null) return Optional.empty();

            if(checkLocation(location)) {
                return Optional.of(location);
            }

            for(int yOffset = -5; yOffset < 10; yOffset++) {
                Location check = location.clone().add(0, yOffset, 0);
                if(checkLocation(check)) {
                    return Optional.of(check);
                }
            }

            return Optional.empty();
        }

        private boolean checkLocation(Location location) {
            Block block = location.getBlock();

            return !block.getType().isSolid() &&
                    !location.clone().add(0, 1, 0).getBlock().isSolid() &&
                    location.clone().add(0, -1, 0).getBlock().getType().isSolid();
        }
    }
}
