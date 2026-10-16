package thaumcraft.effects;

import java.util.Set;
import net.minecraft.world.effect.*;
import net.neoforged.neoforge.common.EffectCure;

public final class VisExhaustEffect extends MobEffect {
    public VisExhaustEffect() {super(MobEffectCategory.HARMFUL,0x664477);}
    // Cures are cleared on individual warp/flux applications, as in TC4.
    @Override public void fillEffectCures(Set<EffectCure> cures,MobEffectInstance instance) {super.fillEffectCures(cures,instance);}
}
