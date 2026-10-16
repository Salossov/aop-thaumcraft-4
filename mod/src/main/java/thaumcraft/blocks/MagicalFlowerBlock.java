package thaumcraft.blocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
public final class MagicalFlowerBlock extends BushBlock {
    public record Wisp(net.minecraft.world.phys.Vec3 position,float red,float green,float blue) {}
    public static java.util.function.Consumer<Wisp> clientWisp=effect->{};
    public static final MapCodec<MagicalFlowerBlock> CODEC=simpleCodec(MagicalFlowerBlock::new);
    public MagicalFlowerBlock(){this(Properties.ofFullCopy(Blocks.DANDELION).lightLevel(s->8));}
    public MagicalFlowerBlock(BlockBehaviour.Properties properties){super(properties);}
    @Override protected MapCodec<? extends BushBlock> codec(){return CODEC;}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return Block.box(1.6,0,1.6,14.4,12.8,14.4);}
    @Override public int getFlammability(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos,net.minecraft.core.Direction face){return 100;}
    @Override public int getFireSpreadSpeed(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos,net.minecraft.core.Direction face){return 60;}
    @Override public void animateTick(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,net.minecraft.util.RandomSource random){
        if(random.nextInt(3)!=0)return;var r=level.random;
        float red=.3f+r.nextFloat()*.3f,green=.7f+r.nextFloat()*.3f,blue=.7f+r.nextFloat()*.3f;
        float x=(float)pos.getX()+.5f+(r.nextFloat()-r.nextFloat())*.1f,y=(float)pos.getY()+.5f+(r.nextFloat()-r.nextFloat())*.15f,z=(float)pos.getZ()+.5f+(r.nextFloat()-r.nextFloat())*.1f;
        clientWisp.accept(new Wisp(new net.minecraft.world.phys.Vec3(x,y,z),red,green,blue));
    }
}
