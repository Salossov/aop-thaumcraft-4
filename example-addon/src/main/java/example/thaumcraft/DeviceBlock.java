package example.thaumcraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
public final class DeviceBlock extends Block implements EntityBlock {
 public DeviceBlock(){super(Properties.of().strength(2));}
 public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new DeviceEntity(pos,state);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:(world,pos,s,be)->{if(be instanceof DeviceEntity device)device.tick();};}
 @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.phys.BlockHitResult hit){if(!level.isClientSide&&level.getBlockEntity(pos) instanceof DeviceEntity d)player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.thaumcraft_example.device",d.charge,d.store.amount()),true);return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);}
}
