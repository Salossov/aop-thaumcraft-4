package thaumcraft.production;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.items.CastingWandItem;
import thaumcraft.registry.R3Content;

public class ProductionBlock extends Block implements EntityBlock {
    public enum Kind { CRUCIBLE, FURNACE, ALEMBIC, JAR, VOID_JAR, TUBE, VALVE, FILTER, RESTRICTED, ONEWAY, BUFFER, CENTRIFUGE, CRYSTALLIZER, RESERVOIR }
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public final Kind kind;
    // Original BlockMetalDevice shares this contact delay across crucibles.
    private int contactDelay;
    public ProductionBlock(Kind kind) {
        super(Properties.of().strength(kind == Kind.FURNACE ? 3.0f : 2.0f).sound(SoundType.METAL).noOcclusion());
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, kind==Kind.CRYSTALLIZER||kind==Kind.RESERVOIR?Direction.DOWN:Direction.NORTH).setValue(LIT, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, LIT); }
    @Override protected RenderShape getRenderShape(BlockState state) {return (kind==Kind.CENTRIFUGE||kind==Kind.ALEMBIC||kind==Kind.CRYSTALLIZER||kind==Kind.RESERVOIR)?RenderShape.ENTITYBLOCK_ANIMATED:RenderShape.MODEL;}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) {
        Direction face = kind == Kind.RESERVOIR || kind == Kind.CRYSTALLIZER || kind == Kind.ONEWAY ? c.getClickedFace() : c.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, face);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) {
            case CRUCIBLE -> new CrucibleBlockEntity(pos, state);
            case FURNACE -> new AlchemyFurnaceBlockEntity(pos, state);
            default -> new TransportBlockEntity(pos, state);
        };
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        BlockEntityType<?> expected = kind == Kind.CRUCIBLE ? R3Content.CRUCIBLE_ENTITY.get() : kind == Kind.FURNACE ? R3Content.FURNACE_ENTITY.get() : R3Content.TRANSPORT_ENTITY.get();
        return type == expected ? (world, pos, block, tile) -> ((EssentiaBlockEntity) tile).serverTick() : null;
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (kind) {
            case CRUCIBLE -> Shapes.or(box(0, 0, 0, 16, 3, 16), box(0, 3, 0, 2, 16, 16), box(14, 3, 0, 16, 16, 16), box(2, 3, 0, 14, 16, 2), box(2, 3, 14, 14, 16, 16));
            case JAR, VOID_JAR -> box(3, 0, 3, 13, 13, 13);
            case TUBE, VALVE, FILTER, RESTRICTED, ONEWAY, BUFFER -> tubeShape(state,level, pos);
            case RESERVOIR -> box(2, 0, 2, 14, 16, 14);
            default -> box(0, 0, 0, 16, 16, 16);
        };
    }
    private VoxelShape tubeShape(BlockState state,BlockGetter level, BlockPos pos) {
        double low=switch(kind){case BUFFER->4;case FILTER->5.5;case VALVE->6;default->6.5;};VoxelShape result = box(low,low,low,16-low,16-low,16-low);
        if(kind==Kind.VALVE)result=Shapes.or(result,switch(state.getValue(FACING)){case NORTH->Shapes.or(box(7,7,3,9,9,6),box(4,4,2,12,12,3));case SOUTH->Shapes.or(box(7,7,10,9,9,13),box(4,4,13,12,12,14));case WEST->Shapes.or(box(3,7,7,6,9,9),box(2,4,4,3,12,12));case EAST->Shapes.or(box(10,7,7,13,9,9),box(13,4,4,14,12,12));case UP->Shapes.or(box(7,10,7,9,13,9),box(4,13,4,12,14,12));case DOWN->Shapes.or(box(7,3,7,9,6,9),box(4,2,4,12,3,12));});
        if (level.getBlockEntity(pos) instanceof TransportBlockEntity tube) for (Direction d : Direction.values()) {
            if (!tube.connected(d)) continue;
            result = Shapes.or(result, box(d == Direction.WEST ? 0 : 7, d == Direction.DOWN ? 0 : 7, d == Direction.NORTH ? 0 : 7,
                    d == Direction.EAST ? 16 : 9, d == Direction.UP ? 16 : 9, d == Direction.SOUTH ? 16 : 9));
        }
        return result;
    }
    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) return;
        if (entity instanceof ItemEntity item && crucible.heat>150 && crucible.water()>0) {crucible.smelt(item);return;}
        if (++contactDelay<10) return;contactDelay=0;
        if(entity instanceof net.minecraft.world.entity.LivingEntity && crucible.heat>150 && crucible.water()>0){entity.hurt(level.damageSources().inFire(),1);level.playSound(null,pos,net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,net.minecraft.sounds.SoundSource.BLOCKS,.4f,2+level.random.nextFloat()*.4f);}
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EssentiaBlockEntity tile)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (tile instanceof CrucibleBlockEntity crucible && stack.is(Items.WATER_BUCKET)) {
            if (crucible.water() >= 1000) return ItemInteractionResult.CONSUME;
            if (!level.isClientSide) { crucible.fillWater(1000); if (!player.isCreative()) player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET))); }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if(tile instanceof TransportBlockEntity transport&&transport.useJar(stack,player,hand))return ItemInteractionResult.sidedSuccess(level.isClientSide);
        if (stack.getItem() instanceof CastingWandItem) {
            if (!level.isClientSide) {
                if (tile instanceof CrucibleBlockEntity crucible) { if (player.isShiftKeyDown()) crucible.empty(); }
                else if (tile instanceof TransportBlockEntity transport) transport.wandClick(hit.getDirection(), player.isShiftKeyDown());
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(R3Content.JAR_LABEL.get()) && tile instanceof TransportBlockEntity transport && transport.labelable()) {
            Aspect selected = R3Content.aspect(stack);
            if (selected == null) selected = tile.store.first();
            if (selected == null) return ItemInteractionResult.CONSUME;
            if (!level.isClientSide && transport.applyLabel(selected) && !player.isCreative()) stack.shrink(1);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (tile instanceof TransportBlockEntity transport && transport.usePhial(stack, player, hand)) return ItemInteractionResult.sidedSuccess(level.isClientSide);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (kind == Kind.FURNACE && player instanceof net.minecraft.server.level.ServerPlayer server && level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace) {
            server.openMenu(furnace); return InteractionResult.CONSUME;
        }
        if ((kind == Kind.JAR || kind == Kind.VOID_JAR || kind == Kind.ALEMBIC) && player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof TransportBlockEntity transport && transport.labelable()) {
            if (!level.isClientSide) transport.removeLabel(); return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<net.minecraft.network.chat.Component> lines, TooltipFlag flags) {
        if (kind != Kind.JAR && kind != Kind.VOID_JAR) return;
        var tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
        var contents = tag.getCompound("Essentia");
        for (var key : contents.getAllKeys()) { var aspect = Aspect.getAspect(key); if (aspect != null && contents.getInt(key) > 0) lines.add(net.minecraft.network.chat.Component.literal(aspect.getName() + " x" + contents.getInt(key)).withStyle(style -> style.withColor(aspect.getColor()))); }
        var filter = Aspect.getAspect(tag.getString("AspectFilter")); if (filter != null) lines.add(net.minecraft.network.chat.Component.translatable("thaumcraft.jar.filter", filter.getName()).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var drops = super.getDrops(state, params);
        if ((kind == Kind.JAR || kind == Kind.VOID_JAR) && params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof EssentiaBlockEntity tile) {
            var tag = tile.saveWithoutMetadata(params.getLevel().registryAccess());
            tag.putString("id", net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(tile.getType()).toString());
            for (var stack : drops) if (stack.is(asItem())) stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
        }
        return drops;
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace) Containers.dropContents(level, pos, furnace);
        super.onRemove(state, level, pos, next, moving);
    }
}
