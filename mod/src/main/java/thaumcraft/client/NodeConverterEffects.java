package thaumcraft.client;

import net.minecraft.world.phys.Vec3;
import thaumcraft.visnet.NodeConverterBlockEntity;

/** Original TileNodeConverter tick discharges; no random work is tied to render FPS. */
public final class NodeConverterEffects {
    public static void tick(NodeConverterBlockEntity tile){
        var level=tile.getLevel();if(level==null||!level.isClientSide)return;
        var pos=tile.getBlockPos();var random=level.random;var end=new Vec3(pos.getX()+.5,pos.getY()-.5,pos.getZ()+.5);
        if(random.nextBoolean())LegacyLightningParticle.node(new Vec3(pos.getX()+.25+random.nextFloat()*.5,pos.getY()+.5,pos.getZ()+.25+random.nextFloat()*.5),end);
        if(random.nextBoolean()&&tile.hasStabilizer())LegacyLightningParticle.node(new Vec3(pos.getX()+.25+random.nextFloat()*.5,pos.getY()-1.5,pos.getZ()+.25+random.nextFloat()*.5),end);
    }
    private NodeConverterEffects(){}
}
