package thaumcraft.client;

import net.minecraft.world.phys.Vec3;
import thaumcraft.production.NitorBlockEntity;

/** TileNitor's two source chances: ALL 1/5 and 1/7, DECREASED 1/7 and 1/11, MINIMAL 1/9 and 1/15. */
final class NitorEffects {
    private NitorEffects(){}
    static void install(){NitorBlockEntity.clientTick=NitorEffects::tick;}
    private static void tick(NitorBlockEntity tile){
        var level=tile.getLevel();if(level==null)return;var r=level.random;var pos=tile.getBlockPos();
        var from=new Vec3((float)pos.getX()+.5f,(float)pos.getY()+.5f,(float)pos.getZ()+.5f);
        if(r.nextInt(9-LegacyParticles.particleCount(2))==0){
            var toward=new Vec3((float)pos.getX()+.3f+r.nextFloat()*.4f,(float)pos.getY()+.5f,(float)pos.getZ()+.3f+r.nextFloat()*.4f);
            VishroomEffects.nitor(from,toward,true);
        }
        if(r.nextInt(15-LegacyParticles.particleCount(4))==0){
            var toward=new Vec3((float)pos.getX()+.4f+r.nextFloat()*.2f,(float)pos.getY()+.5f,(float)pos.getZ()+.4f+r.nextFloat()*.2f);
            VishroomEffects.nitor(from,toward,false);
        }
    }
}
