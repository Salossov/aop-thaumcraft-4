package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.registry.R5Content;

public final class EldritchTrapBlockEntity extends BlockEntity {
    // Original countdown is deliberately not saved: a loaded trap starts at 20.
    private int count=20;
    public EldritchTrapBlockEntity(BlockPos pos,BlockState state){super(R5Content.TRAP_ENTITY.get(),pos,state);}
    public int countdown(){return count;}
    public void serverTick(){if(!(level instanceof ServerLevel server)||count-->0)return;count=10+level.random.nextInt(25);var centre=Vec3.atCenterOf(worldPosition);var player=level.getNearestPlayer(centre.x,centre.y,centre.z,3,false);if(player==null)return;player.hurt(level.damageSources().magic(),2);if(level.random.nextBoolean()){int warp=1+level.random.nextInt(2);if(player instanceof ServerPlayer target)thaumcraft.knowledge.WarpService.add(target,0,0,warp);}PacketDistributor.sendToPlayersNear(server,null,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),32,new thaumcraft.network.InfusionZapPayload(centre,player.getEyePosition()));}
}
