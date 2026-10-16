package thaumcraft.production;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.*;

/** Position in the original two-layer structure; the upper middle cell stays untouched. */
public final class AdvancedAlchemyBlock extends BaseEntityBlock {
    public static final IntegerProperty PART=IntegerProperty.create("part",0,17),LIGHT=IntegerProperty.create("light",0,15);
    private static boolean replacing;
    public AdvancedAlchemyBlock(){super(Properties.of().strength(3,17).sound(SoundType.METAL).noOcclusion().lightLevel(s->s.getValue(LIGHT)));registerDefaultState(stateDefinition.any().setValue(PART,4).setValue(LIGHT,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new AdvancedAlchemyBlock());}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PART,LIGHT);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
    public static BlockPos offset(int part){return new BlockPos(part%3-1,part/9,part/3%3-1);}
    public static BlockPos core(BlockPos p,BlockState s){return p.subtract(offset(s.getValue(PART)));}
    public static boolean nozzle(int part){return part==1||part==3||part==5||part==7;}
    public static BlockState original(int part){if(part==4)return R3Content.ALCHEMY_FURNACE.get().defaultBlockState();if(part<9)return R4Content.ADVANCED_CONSTRUCT.get().defaultBlockState();var o=offset(part);return (o.getX()!=0&&o.getZ()!=0?R3Content.ALEMBIC.get():R3Content.ALCHEMICAL_CONSTRUCT.get()).defaultBlockState();}
    public static boolean fits(Level l,BlockPos center){for(int part=0;part<18;part++){if(part==13)continue;var p=center.offset(offset(part));if(!l.hasChunkAt(p)||!l.getBlockState(p).is(original(part).getBlock()))return false;}return true;}
    public static boolean activate(ServerPlayer player,ItemStack wand,BlockPos clicked){
        var l=player.serverLevel();if(!thaumcraft.knowledge.KnowledgeService.get(player).completedResearch().contains("ADVALCHEMYFURNACE"))return false;
        for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++){var center=clicked.offset(x,y,z);if(!l.hasChunkAt(center)||!l.getBlockState(center).is(R3Content.ALCHEMY_FURNACE.get()))continue;
            if(!fits(l,center))return false;for(int part=0;part<18;part++)if(part!=13&&!l.mayInteract(player,center.offset(offset(part))))return false;
            if(!thaumcraft.items.WandVis.consume(wand,java.util.Map.of("ignis",50,"aqua",50,"ordo",50),player,true))return false;
            replacing=true;try{for(int part=0;part<18;part++)if(part!=13)l.setBlock(center.offset(offset(part)),R4Content.ADVANCED_FURNACE.get().defaultBlockState().setValue(PART,part),2);}finally{replacing=false;}
            for(int part=0;part<18;part++)if(part!=13){var p=center.offset(offset(part));l.sendBlockUpdated(p,l.getBlockState(p),l.getBlockState(p),3);net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(l,null,p.getX(),p.getY(),p.getZ(),32,new thaumcraft.network.BlockSparklePayload(p,-9999));}
            l.playSound(null,center,R2Sounds.CRAFT_FINISH.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);return true;
        }return false;
    }
    public static void restore(Level l,BlockPos center,BlockPos skip){if(replacing||l.isClientSide)return;replacing=true;try{for(int part=0;part<18;part++){if(part==13)continue;var p=center.offset(offset(part));if(p.equals(skip)||!l.hasChunkAt(p))continue;var s=l.getBlockState(p);if(s.is(R4Content.ADVANCED_FURNACE.get())&&core(p,s).equals(center))l.setBlockAndUpdate(p,original(part));}}finally{replacing=false;}}
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&!replacing&&!l.isClientSide){var center=core(p,s);if(p.equals(center))restore(l,center,p);else if(l.hasChunkAt(center)&&l.getBlockEntity(center) instanceof AdvancedAlchemyBlockEntity tile)tile.destroy=true;}super.onRemove(s,l,p,next,moving);}
    @Override protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){super.onPlace(s,l,p,old,moving);if(!l.isClientSide&&s.getValue(PART)!=4)l.scheduleTick(p,this,40);}
    @Override protected void tick(BlockState s,net.minecraft.server.level.ServerLevel l,BlockPos p,net.minecraft.util.RandomSource random){var center=core(p,s);if(l.hasChunkAt(center)){var c=l.getBlockState(center);if(!c.is(this)||c.getValue(PART)!=4){l.setBlockAndUpdate(p,original(s.getValue(PART)));return;}}l.scheduleTick(p,this,100);}
    @Override protected java.util.List<ItemStack> getDrops(BlockState s,LootParams.Builder p){return java.util.List.of(new ItemStack(original(s.getValue(PART)).getBlock()));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){int part=s.getValue(PART);return part==4?new AdvancedAlchemyBlockEntity(p,s):nozzle(part)?new AdvancedAlchemyNozzleBlockEntity(p,s):null;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,R4Content.ADVANCED_FURNACE_ENTITY.get(),(level,pos,state,tile)->tile.serverTick());}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.block();}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(PART)==4&&!(c instanceof EntityCollisionContext ec&&ec.getEntity() instanceof LivingEntity)?box(0,0,0,16,11.2,16):Shapes.block();}
    @Override protected boolean hasAnalogOutputSignal(BlockState s){return nozzle(s.getValue(PART));}
    @Override protected int getAnalogOutputSignal(BlockState s,Level l,BlockPos p){return l.getBlockEntity(p) instanceof AdvancedAlchemyNozzleBlockEntity n&&n.furnace()!=null&&n.furnace().store.amount()>0?1:0;}
    @Override protected void entityInside(BlockState s,Level l,BlockPos p,Entity e){if(!l.isClientSide&&s.getValue(PART)==4&&e instanceof ItemEntity item&&l.getBlockEntity(p) instanceof AdvancedAlchemyBlockEntity tile&&tile.process(item.getItem())){item.getItem().shrink(1);if(item.getItem().isEmpty())item.discard();l.playSound(null,p,R2Sounds.BUBBLE.get(),net.minecraft.sounds.SoundSource.BLOCKS,.2f,1+l.random.nextFloat()*.4f);}}
    public static java.util.function.BiConsumer<Level,BlockPos> bubbles=(l,p)->{};
    @Override public void animateTick(BlockState s,Level l,BlockPos p,net.minecraft.util.RandomSource r){if(s.getValue(PART)==4&&l.getBlockEntity(p) instanceof AdvancedAlchemyBlockEntity tile&&tile.store.amount()>0)bubbles.accept(l,p);}
}
