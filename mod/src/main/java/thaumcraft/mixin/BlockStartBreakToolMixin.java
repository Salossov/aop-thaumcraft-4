package thaumcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thaumcraft.api.world.IBlockStartBreakTool;

@Mixin(ServerPlayerGameMode.class)
public abstract class BlockStartBreakToolMixin {
    @WrapOperation(method="destroyBlock",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/common/CommonHooks;fireBlockBreak(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/GameType;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/neoforged/neoforge/event/level/BlockEvent$BreakEvent;"))
    private BlockEvent.BreakEvent thaumcraft$startBreak(Level level,GameType mode,ServerPlayer player,BlockPos pos,BlockState state,Operation<BlockEvent.BreakEvent> original,@Local(ordinal=0) LocalRef<BlockState> currentState) {
        var event=original.call(level,mode,player,pos,state);
        var stack=player.getMainHandItem();
        if(!event.isCanceled()&&stack.getItem() instanceof IBlockStartBreakTool tool) {
            boolean deferred=tool.onBlockStartBreak(stack,pos,player);
            // Forge 1.7.10 read this state after the item callback. NeoForge
            // captures it before the permission event; refresh the live local.
            currentState.set(level.getBlockState(pos));
            if(!deferred)return event;
            event.setCanceled(true);
            // The permission event has already finished. Restore client prediction
            // for this later cancellation, including the unchanged block entity.
            player.connection.send(new ClientboundBlockUpdatePacket(level,pos));
            var entity=level.getBlockEntity(pos);
            if(entity!=null&&entity.getUpdatePacket()!=null)player.connection.send(entity.getUpdatePacket());
        }
        return event;
    }
}
