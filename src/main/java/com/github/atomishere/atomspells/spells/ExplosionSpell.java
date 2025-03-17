package com.github.atomishere.atomspells.spells;

import com.github.atomishere.atomspells.ManaManager;
import com.github.atomishere.atomspells.RayCaster;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ExplosionSpell extends Spell {
    private final ManaManager manaManager;

    public ExplosionSpell(NamespacedKey spellId, ManaManager manaManager) {
        super(spellId, "Explosion Spell");

        this.manaManager = manaManager;
    }

    @Override
    public void performSpell(Player caster) {
        RayCaster.RayResult ray = RayCaster.at(caster.getEyeLocation())
                .maxDistance(10.0D)
                .showTrail(true)
                .entityFilter(entity -> entity != caster)
                .cast();

        if(!ray.isHit()) {
            caster.sendMessage("You didn't hit anything!");
            return;
        }

        if(manaManager.getMana(caster) < 10) {
            caster.sendMessage("You don't have enough mana!");
            return;
        }

        manaManager.setMana(caster, manaManager.getMana(caster) - 10);
        Location explosionPos;
        if(ray.getHitBlock().isPresent()) {
            explosionPos = ray.getHitBlock().get().getLocation();
        } else {
            //noinspection OptionalGetWithoutIsPresent
            explosionPos = ray.getHitEntity().get().getLocation();
        }

        explosionPos.getWorld().spawnParticle(Particle.EXPLOSION, explosionPos, 20, 0.5, 0.5, 0.5);
        explosionPos.getWorld().playSound(explosionPos, "entity.generic.explode", 1, 1);

        explosionPos.getWorld().getNearbyEntities(explosionPos, 3, 3, 3, entity -> entity != caster && entity instanceof LivingEntity)
                .stream()
                .map(entity -> (LivingEntity) entity)
                .forEach(entity -> entity.damage(10 * (1 - entity.getLocation().distance(explosionPos) / 3), caster));
    }
}
