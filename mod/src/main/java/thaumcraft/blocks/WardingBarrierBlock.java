package thaumcraft.blocks;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R4Content;

/** Invisible protection passes players and items, while powered stones release mobs. */
public final class WardingBarrierBlock extends Block {
    public WardingBarrierBlock(){super(Properties.of().noOcclusion().noLootTable().strength(-1,3600000).replaceable());}
    private BlockPos source(BlockGetter l,BlockPos p){for(int y=1;y<=2;y++)if(l.getBlockState(p.below(y)).is(R4Content.PAVING_WARDING.get()))return p.below(y);return null;}
    @Override public net.minecraft.world.level.pathfinder.PathType getBlockPathType(BlockState s,BlockGetter l,BlockPos p,net.minecraft.world.entity.Mob mob){var origin=source(l,p);return origin!=null&&l instanceof Level level&&!level.hasNeighborSignal(origin)?net.minecraft.world.level.pathfinder.PathType.BLOCKED:net.minecraft.world.level.pathfinder.PathType.OPEN;}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var source=source(l,p);return source!=null&&l instanceof Level level&&!level.hasNeighborSignal(source)&&c instanceof EntityCollisionContext entity&&entity.getEntity() instanceof LivingEntity&&!(entity.getEntity() instanceof Player)?Shapes.block():Shapes.empty();}
    @Override protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){if(!l.isClientSide)l.scheduleTick(p,this,100);}
    @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(source(l,p)==null)l.removeBlock(p,false);else l.scheduleTick(p,this,100);}
}
