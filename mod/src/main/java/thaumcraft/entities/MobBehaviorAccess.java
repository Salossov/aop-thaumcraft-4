package thaumcraft.entities;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import thaumcraft.api.entities.MobBehaviorApi;
import thaumcraft.mixin.*;

public final class MobBehaviorAccess implements MobBehaviorApi.Access {
    private static List<WrappedGoal> entries(Mob mob) {
        var access=(MobGoalSelectorAccessor)mob;
        var result=new ArrayList<>(access.thaumcraft$goals().getAvailableGoals());
        result.addAll(new ArrayList<>(access.thaumcraft$targets().getAvailableGoals()));return result;
    }
    @Override public int redirectAvoidance(Mob mob,Class<? extends LivingEntity> from,Class<? extends LivingEntity> to) {
        int changed=0;
        for(var entry:entries(mob))if(entry.getGoal() instanceof AvoidEntityGoal<?> goal) {
            var access=(AvoidEntityClassAccessor)goal;
            if(access.thaumcraft$avoidClass()==from) {access.thaumcraft$avoidClass(to);changed++;}
        }
        return changed;
    }
    @Override public int redirectNearestTarget(Mob mob,Class<? extends Entity> from,Class<? extends Entity> to) {
        int changed=0;
        for(var entry:entries(mob))if(entry.getGoal() instanceof NearestAttackableTargetGoal<?> goal) {
            var access=(NearestTargetClassAccessor)goal;
            if(access.thaumcraft$targetClass()==from) {access.thaumcraft$targetClass(to);changed++;}
        }
        return changed;
    }
    @Override public void setSwell(Creeper creeper,int ticks) {((CreeperSwellAccessor)creeper).thaumcraft$swell(ticks);}
}
