package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.*;

/** Original metadata 0..3 destruction cascade; other eldritch stone survives. */
public final class EldritchStructure {
    private static final ThreadLocal<Boolean> REMOVING=ThreadLocal.withInitial(()->false);
    public static void removed(Level level,BlockPos pos,BlockState old,BlockState replacement){
        if(level.isClientSide||old.is(replacement.getBlock())||REMOVING.get())return;
        REMOVING.set(true);try{for(var p:BlockPos.betweenClosed(pos.offset(-3,-2,-3),pos.offset(3,2,3))){var s=level.getBlockState(p);if(s.is(R3Content.ELDRITCH_ALTAR.get())||s.is(R5Content.OBELISK.get())||s.is(R5Content.ELDRITCH_CAP.get())||s.is(R5Content.ELDRITCH_BODY.get()))level.removeBlock(p,false);}level.explode(null,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,1,Level.ExplosionInteraction.NONE);}finally{REMOVING.set(false);}
    }
    private EldritchStructure(){}
}
