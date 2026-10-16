package thaumcraft.production;

import java.util.ArrayList;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.phys.Vec3;

public final class TubeBlock extends ProductionBlock {
    public TubeBlock(Kind kind) { super(kind); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); builder.add(PipeBlock.PROPERTY_BY_DIRECTION.values().toArray(BooleanProperty[]::new));
    }
    public static void updateConnections(TransportBlockEntity tile) {
        if (!(tile.getBlockState().getBlock() instanceof TubeBlock)) return;
        var next = tile.getBlockState();
        for (var entry : PipeBlock.PROPERTY_BY_DIRECTION.entrySet()) next = next.setValue(entry.getValue(), tile.connected(entry.getKey()));
        if (next != tile.getBlockState()) tile.getLevel().setBlockAndUpdate(tile.getBlockPos(), next);
    }
    /** Original block event 1: a colour flash and a venting burst on the client. */
    @Override protected boolean triggerEvent(BlockState state,Level level,BlockPos pos,int id,int value){
        if(id==1&&level.isClientSide){
            var aspects=new ArrayList<thaumcraft.api.aspects.Aspect>();
            for(var a:thaumcraft.api.aspects.Aspect.all().values())if(!a.isPrimal())aspects.add(a);
            int color=value<0||value>=aspects.size()?0xAAAAAA:aspects.get(value).getColor();
            var random=level.random;
            float rp=random.nextFloat()*360,ry=random.nextFloat()*360;
            double fx=-Math.sin(Math.toRadians(ry))*Math.cos(Math.toRadians(rp)),fz=Math.cos(Math.toRadians(ry))*Math.cos(Math.toRadians(rp)),fy=-Math.sin(Math.toRadians(rp));
            thaumcraft.client.LegacyParticles.vent(new Vec3(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5),new Vec3(fx/5,fy/5,fz/5),color,1);
            return true;
        }
        return super.triggerEvent(state,level,pos,id,value);
    }
}
