package com.github.atomishere.atomspells;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public final class RayCaster {
    private RayCaster() {
        throw new AssertionError("No instances!");
    }

    public static RayResult castRay(Location startLocation, double maxDistance, boolean detectEntities, @Nullable Entity caster) {
        Vector direction = startLocation.getDirection();

        for (double i = 0; i <= maxDistance; i += 0.5) {
            Location currentPoint = startLocation.clone().add(direction.clone().multiply(i));

            currentPoint.getWorld().spawnParticle(Particle.DUST, currentPoint, 1, new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 255, 255), 1));

            Block block = currentPoint.getBlock();
            if(!block.isPassable()) {
                return new RayResult(true, block, null);
            }

            if(detectEntities) {
                Collection<Entity> nearbyEntities = startLocation.getWorld().getNearbyEntities(currentPoint, 0.5, 0.5, 0.5);
                for(Entity entity : nearbyEntities) {
                    if(entity != caster) {
                        return new RayResult(true, null, entity);
                    }
                }
            }
        }

        return new RayResult(false, null, null);
    }

    public static class RayResult {
        private final boolean hit;

        private final Block hitBlock;
        private final Entity hitEntity;

        private RayResult(boolean hit, @Nullable Block hitBlock, @Nullable Entity hitEntity) {
            this.hit = hit;
            this.hitBlock = hitBlock;
            this.hitEntity = hitEntity;
        }

        public boolean isHit() {
            return hit;
        }

        public Optional<Block> getHitBlock() {
            return Optional.ofNullable(hitBlock);
        }

        public Optional<Entity> getHitEntity() {
            return Optional.ofNullable(hitEntity);
        }
    }
}
