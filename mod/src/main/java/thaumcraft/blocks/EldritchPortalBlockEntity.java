package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.registry.*;
import thaumcraft.world.dim.EldritchTeleporter;
import thaumcraft.knowledge.KnowledgeService;

public final class EldritchPortalBlockEntity extends BlockEntity {
    public int openCount=-1;
    private int count;
    public EldritchPortalBlockEntity(BlockPos pos,BlockState state){super(R5Content.PORTAL_ENTITY.get(),pos,state);}
    public static void tick(Level level,BlockPos pos,BlockState state,EldritchPortalBlockEntity portal){
        ++portal.count;
        if(level.isClientSide){if(portal.openCount<30)++portal.openCount;if(portal.count%250==0)level.playLocalSound(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,R2Sounds.EVIL_PORTAL.get(),SoundSource.BLOCKS,1,1,false);return;}
        if(portal.count%5!=0||!(level instanceof ServerLevel server))return;
        for(var player:server.getEntitiesOfClass(ServerPlayer.class,new AABB(pos).inflate(.5,1,.5))){
            if(player.isPassenger()||player.isVehicle())continue;
            if(player.getPortalCooldown()>0){player.setPortalCooldown(100);continue;}
            var destination=server.getServer().getLevel(server.dimension()==EldritchTeleporter.OUTER?Level.OVERWORLD:EldritchTeleporter.OUTER);
            if(destination==null)continue;
            if(!EldritchTeleporter.prepare(destination,player.position()))continue;
            var origin=player.position();
            try{
                player.setPortalCooldown(100);
                if(player.changeDimension(EldritchTeleporter.transition(destination,player))!=null&&destination.dimension()==EldritchTeleporter.OUTER)KnowledgeService.mutate(player,k->k.completeResearch("ENTEROUTER"));
            }finally{EldritchTeleporter.releasePreparation(destination,origin);}
        }
    }
    public AABB renderBounds(){return new AABB(worldPosition).inflate(1);}
}
