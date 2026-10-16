package thaumcraft.items;

import net.minecraft.core.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.entities.*;

/** Separate native IDs replace TC4's six metadata values. */
public final class PrimalArrowItem extends ArrowItem {
    public final int element;
    public PrimalArrowItem(int element){super(new Properties());this.element=element;}
    @Override public AbstractArrow createArrow(Level l,ItemStack ammo,LivingEntity owner,ItemStack weapon){var arrow=new PrimalArrow(l,owner,ammo,element);return arrow;}
    @Override public Projectile asProjectile(Level l,Position p,ItemStack ammo,Direction face){var arrow=R3Entities.PRIMAL_ARROW.get().create(l);arrow.element(element);arrow.setPos(p.x(),p.y(),p.z());return arrow;}
}
