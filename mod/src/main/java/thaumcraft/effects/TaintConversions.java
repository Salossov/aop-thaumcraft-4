package thaumcraft.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import thaumcraft.entities.*;

public final class TaintConversions {
    public static void death(LivingDeathEvent event) {
        var dead=event.getEntity();if(!(dead.level() instanceof ServerLevel level)||dead instanceof TaintedMob||!dead.hasEffect(TaintEffects.FLUX_TAINT)) return;
        EntityType<? extends Mob> type=dead instanceof Creeper?R2Entities.TAINT_CREEPER.get():dead instanceof Sheep?R2Entities.TAINT_SHEEP.get():dead instanceof Cow?R2Entities.TAINT_COW.get():dead instanceof Pig?R2Entities.TAINT_PIG.get():dead instanceof Chicken?R2Entities.TAINT_CHICKEN.get():dead instanceof Villager?R2Entities.TAINT_VILLAGER.get():R2Entities.THAUMIC_SLIME.get();
        var converted=type.create(level);if(converted==null) throw new IllegalStateException("Cannot create taint conversion");
        converted.moveTo(dead.getX(),dead.getY(),dead.getZ(),dead.getYRot(),dead.getXRot());
        if(converted instanceof ThaumicSlime slime) slime.size((int)(1+Math.min(dead.getMaxHealth()/10,6)));
        level.addFreshEntity(converted);
        // Normal death processing preserves modern player respawn, drops and death events.
    }
    private TaintConversions() {}
}
