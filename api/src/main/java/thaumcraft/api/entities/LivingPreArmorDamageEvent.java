package thaumcraft.api.entities;

import java.util.Objects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;

/** Damage entering armor calculation, after shield/invulnerability processing and
 * before armor, resistance, enchantments and absorption. Subscribe to BUS.
 * This event also precedes ISpecialArmor and includes sources bypassing armor.
 * Setting damage to zero skips the remaining damage calculation and armor wear.
 */
public final class LivingPreArmorDamageEvent extends Event {
    public static final IEventBus BUS=BusBuilder.builder().build();
    private final LivingEntity entity;
    private final DamageSource source;
    private final float originalDamage;
    private float damage;
    public LivingPreArmorDamageEvent(LivingEntity entity,DamageSource source,float damage) {
        this.entity=Objects.requireNonNull(entity);this.source=Objects.requireNonNull(source);
        this.originalDamage=damage;this.damage=damage;
    }
    public LivingEntity entity(){return entity;}
    public DamageSource source(){return source;}
    public float originalDamage(){return originalDamage;}
    public float damage(){return damage;}
    public void damage(float value) {
        if(!Float.isFinite(value)||value<0)throw new IllegalArgumentException("Invalid pre-armor damage");
        damage=value;
    }
}
