package com.github.atomishere.atomspells;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public final class RayCaster {
    private final Location startLocation;

    private double maxDistance = 1.0D;
    private boolean detectEntities = true;

    private boolean showTrail = false;
    private Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 255, 255), 1);

    private Predicate<Entity> entityFilter = entity -> true;
    private Predicate<Block> blockFilter = block -> true;

    public RayCaster maxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    public RayCaster detectEntities(boolean detectEntities) {
        this.detectEntities = detectEntities;
        return this;
    }

    public RayCaster showTrail(boolean showTrail) {
        this.showTrail = showTrail;
        return this;
    }

    public RayCaster dustOptions(Particle.DustOptions dustOptions) {
        this.dustOptions = dustOptions;
        return this;
    }

    public RayCaster entityFilter(Predicate<Entity> entityFilter) {
        this.entityFilter = entityFilter;
        return this;
    }

    public RayCaster blockFilter(Predicate<Block> blockFilter) {
        this.blockFilter = blockFilter;
        return this;
    }

    public RayResult cast() {
        Vector direction = startLocation.getDirection();

        for (double i = 0; i <= maxDistance; i += 0.5) {
            Location currentPoint = startLocation.clone().add(direction.clone().multiply(i));

            if(showTrail) {
                currentPoint.getWorld().spawnParticle(Particle.DUST, currentPoint, 1, dustOptions);
            }

            Block block = currentPoint.getBlock();
            if(!block.isPassable() && blockFilter.test(block)) {
                return new RayResult(true, block, null);
            }

            if(detectEntities) {
                RayResult result = startLocation.getWorld()
                        .getNearbyEntities(currentPoint, 0.5, 0.5, 0.5)
                        .stream()
                        .filter(entityFilter)
                        .findFirst()
                        .map(entity -> new RayResult(true, null, entity))
                        .orElse(null);
                if(result != null) {
                    return result;
                }
            }
        }

        return new RayResult(false, null, null);
    }

    private RayCaster(Location startLocation) {
        this.startLocation = startLocation;
    }

    public static RayCaster at(Location location) {
        return new RayCaster(location);
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
