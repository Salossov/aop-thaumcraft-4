package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.blocks.*;
import thaumcraft.production.EldritchAltarBlockEntity;
import thaumcraft.registry.*;
import thaumcraft.world.dim.*;
import thaumcraft.items.*;
import thaumcraft.knowledge.KnowledgeService;

final class OuterPortalVerification {
    private static final BlockPos POS=new BlockPos(6008,140,4104);
    private static final BlockPos DEST=new BlockPos(6008,50,4104);
    private static final UUID PLAYER=UUID.fromString("01234567-aaaa-bbbb-cccc-012345678931");
    static void prepare(ServerLevel level){for(var world:List.of(level,level.getServer().getLevel(EldritchTeleporter.OUTER))){var p=world==level?POS:DEST;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){int cx=(p.getX()>>4)+x,cz=(p.getZ()>>4)+z;world.setChunkForced(cx,cz,true);world.getChunk(cx,cz);}}}
    static boolean ready(ServerLevel level){for(var world:List.of(level,level.getServer().getLevel(EldritchTeleporter.OUTER))){var p=world==level?POS:DEST;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(!world.areEntitiesLoaded(ChunkPos.asLong((p.getX()>>4)+x,(p.getZ()>>4)+z)))return false;}return true;}
    static void release(ServerLevel level){for(var world:List.of(level,level.getServer().getLevel(EldritchTeleporter.OUTER))){var p=world==level?POS:DEST;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)world.setChunkForced((p.getX()>>4)+x,(p.getZ()>>4)+z,false);}}
    private static EldritchPortalBlockEntity portal(ServerLevel level,BlockPos pos){level.setBlock(pos.below(),R5Content.CARVED_STONE.get().defaultBlockState(),2);level.setBlock(pos.above(),R5Content.ELDRITCH_CAP.get().defaultBlockState(),2);level.setBlock(pos,R5Content.ELDRITCH_PORTAL.get().defaultBlockState(),2);return (EldritchPortalBlockEntity)level.getBlockEntity(pos);}
    private static void five(ServerLevel level,EldritchPortalBlockEntity tile){for(int i=0;i<5;i++)EldritchPortalBlockEntity.tick(level,tile.getBlockPos(),tile.getBlockState(),tile);}
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        try{
            var outer=level.getServer().getLevel(EldritchTeleporter.OUTER);var state=R5Content.ELDRITCH_PORTAL.get().defaultBlockState();portal(outer,DEST);
            check.accept(state.getDestroySpeed(outer,DEST)==-1&&state.getBlock().getExplosionResistance()==200000&&state.getLightEmission(outer,DEST)==15,"original portal is unbreakable, resistance200000 and light15");
            check.accept(state.getCollisionShape(outer,DEST).isEmpty()&&state.getShape(outer,DEST).isEmpty()&&!state.canBeReplaced()&&!state.isSolidRender(outer,DEST),"portal has no collision or selection and cannot be replaced");
            check.accept(net.minecraft.world.level.block.Block.getDrops(state,outer,DEST,outer.getBlockEntity(DEST)).isEmpty(),"portal produces no item drops");
            var origin=Vec3.atCenterOf(DEST);check.accept(DEST.equals(EldritchTeleporter.findPortal(outer,origin)),"cold search finds actual portal in registered Outer Lands");
            outer.removeBlock(DEST,false);check.accept(EldritchTeleporter.findPortal(outer,origin)==null,"removed portal is invalidated immediately instead of retaining an unusable cached destination");
            portal(outer,DEST);EldritchTeleporter.findPortal(outer,origin);
            EldritchTeleporter.expire(outer,(outer.getGameTime()/100+8)*100+1);check.accept(DEST.equals(EldritchTeleporter.findPortal(outer,origin)),"valid portal remains usable between original100tick eviction boundaries");outer.removeBlock(DEST,false);
            EldritchTeleporter.expire(outer,(outer.getGameTime()/100+8)*100);check.accept(EldritchTeleporter.findPortal(outer,origin)==null,"stale portal cache expires on100tick boundary and search finds none");
            portal(outer,DEST);
            var tieOrigin=new BlockPos(-8008,70,-4104);var a=tieOrigin.offset(-2,0,0);var b=tieOrigin.offset(2,0,0);portal(outer,b);portal(outer,a);check.accept(a.equals(EldritchTeleporter.findPortal(outer,Vec3.atCenterOf(tieOrigin))),"equal distance keeps original ascending X order even with negative chunk coordinates");
            var verticalOrigin=tieOrigin.offset(512,0,0);var low=verticalOrigin.below(4);var high=verticalOrigin.above(4);portal(outer,low);portal(outer,high);check.accept(high.equals(EldritchTeleporter.findPortal(outer,Vec3.atCenterOf(verticalOrigin))),"equal vertical distance retains original descending Y order");
            var borderOrigin=verticalOrigin.offset(512,0,0);outer.removeBlock(borderOrigin.east(128),false);outer.removeBlock(borderOrigin.east(129),false);portal(outer,borderOrigin.east(129));check.accept(EldritchTeleporter.findPortal(outer,Vec3.atCenterOf(borderOrigin))==null,"129block candidate is outside original inclusive128block range");portal(outer,borderOrigin.east(128));check.accept(borderOrigin.east(128).equals(EldritchTeleporter.findPortal(outer,Vec3.atCenterOf(borderOrigin))),"exact128block candidate remains within original search");
            var distanceOrigin=borderOrigin.offset(512,0,0);var tall=distanceOrigin.above(50);var near=distanceOrigin.east(8);portal(outer,tall);portal(outer,near);check.accept(near.equals(EldritchTeleporter.findPortal(outer,Vec3.atCenterOf(distanceOrigin))),"portal selection includes vertical distance rather than nearest horizontal column");
            outer.setBlock(DEST.above(),Blocks.AIR.defaultBlockState(),3);check.accept(outer.getBlockState(DEST).isAir(),"removing upper eldritch support destroys portal");portal(outer,DEST);outer.setBlock(DEST.below(),Blocks.AIR.defaultBlockState(),3);check.accept(outer.getBlockState(DEST).isAir(),"removing lower eldritch support destroys portal");portal(outer,DEST);
            var player=new ServerPlayer(level.getServer(),level,new GameProfile(UUID.randomUUID(),"OuterPortalQA"),net.minecraft.server.level.ClientInformation.createDefault());
            var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);var channel=new io.netty.channel.embedded.EmbeddedChannel(connection);new net.minecraft.server.network.ServerGamePacketListenerImpl(level.getServer(),connection,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false)){@Override public void send(net.minecraft.network.protocol.Packet<?> packet){} @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){}};player.setGameMode(GameType.SURVIVAL);player.moveTo(POS.getCenter());KnowledgeService.initialize(player);
            level.setBlock(POS,Blocks.AIR.defaultBlockState(),2);level.setBlock(POS,R3Content.ELDRITCH_ALTAR.get().defaultBlockState(),2);level.setBlock(POS.above(2),R5Content.OBELISK.get().defaultBlockState(),2);level.setBlock(POS.above(),R2Content.AURA_NODE.get().defaultBlockState(),2);var node=(AuraNodeBlockEntity)level.getBlockEntity(POS.above());node.initialize(Map.of("aer",100),thaumcraft.api.nodes.NodeState.Type.DARK,thaumcraft.api.nodes.NodeState.Modifier.NONE);
            var altar=(EldritchAltarBlockEntity)level.getBlockEntity(POS);altar.eyes=4;var wand=new ItemStack(R2Content.WAND.get());wand.set(WandComponents.PARTS.get(),new thaumcraft.api.wands.WandParts("silverwood","gold"));for(var aspect:thaumcraft.api.aspects.Aspects.PRIMALS)WandVis.set(wand,aspect,10000);
            var maze=LabyrinthData.get(level);for(var cell:maze.snapshot().keySet())if(Math.abs(cell.x()-(POS.getX()>>4))<=30&&Math.abs(cell.z()-(POS.getZ()>>4))<=30)maze.remove(cell.x(),cell.z());
            check.accept(!altar.activate(player,wand)&&!maze.inRange(POS.getX()>>4,POS.getZ()>>4,29,29),"unresearched player cannot allocate a labyrinth");KnowledgeService.mutate(player,k->k.completeResearch("OCULUS"));
            check.accept(!altar.activate(player,wand)&&maze.inRange(POS.getX()>>4,POS.getZ()>>4,29,29)&&WandVis.get(wand,thaumcraft.api.aspects.Aspects.AIR)==10000&&!altar.open,"first activation allocates original maze without opening or consuming vis");
            WandVis.set(wand,thaumcraft.api.aspects.Aspects.FIRE,0);check.accept(!altar.activate(player,wand)&&WandVis.get(wand,thaumcraft.api.aspects.Aspects.AIR)==10000&&!altar.open,"insufficient vis leaves all other aspects untouched");WandVis.set(wand,thaumcraft.api.aspects.Aspects.FIRE,10000);
            check.accept(altar.activate(player,wand)&&altar.open&&level.getBlockState(POS.above()).is(R5Content.ELDRITCH_PORTAL.get())&&WandVis.get(wand,thaumcraft.api.aspects.Aspects.AIR)==0,"second funded activation replaces dark node with portal and consumes100vis per primal");
            check.accept(!altar.activate(player,wand),"opened altar cannot consume vis or create another portal");
            var source=(EldritchPortalBlockEntity)level.getBlockEntity(POS.above());player.moveTo(Vec3.atCenterOf(POS.above()));player.setPortalCooldown(2);level.addFreshEntity(player);five(level,source);check.accept(player.serverLevel()==level&&player.getPortalCooldown()==100,"standing in portal refreshes active cooldown to100ticks");
            var boat=net.minecraft.world.entity.EntityType.BOAT.create(level);boat.moveTo(player.position());level.addFreshEntity(boat);player.startRiding(boat,true);player.setPortalCooldown(0);five(level,source);check.accept(player.serverLevel()==level&&player.getPortalCooldown()==0,"mounted player is ignored without changing cooldown");player.stopRiding();boat.discard();player.moveTo(Vec3.atCenterOf(POS.above()));
            var passenger=net.minecraft.world.entity.EntityType.COW.create(level);passenger.moveTo(player.position());level.addFreshEntity(passenger);passenger.startRiding(player,true);five(level,source);check.accept(player.isVehicle()&&player.serverLevel()==level&&player.getPortalCooldown()==0,"player carrying a passenger is ignored by original portal filter");player.ejectPassengers();passenger.discard();
            EldritchTeleporter.findPortal(outer,player.position());EldritchTeleporter.findPortal(level,player.position());five(level,source);check.accept(player.serverLevel()==outer&&player.getPortalCooldown()==100,"actual native player travels from overworld to Outer Lands with100tick cooldown");check.accept(KnowledgeService.get(player).completedResearch().contains("ENTEROUTER"),"successful native entry grants ENTEROUTER research");check.accept(player.getY()==DEST.getY()&&Math.abs(player.getX()-DEST.getX()-.5)==1&&Math.abs(player.getZ()-DEST.getZ()-.5)==1&&player.getDeltaMovement().equals(Vec3.ZERO),"destination uses original random diagonal offset, portal Y and zero velocity");
            player.moveTo(Vec3.atCenterOf(DEST));var destination=(EldritchPortalBlockEntity)outer.getBlockEntity(DEST);five(outer,destination);check.accept(player.serverLevel()==outer&&player.getPortalCooldown()==100,"arrival cooldown prevents immediate return");player.setPortalCooldown(0);five(outer,destination);check.accept(player.serverLevel()==level&&player.getPortalCooldown()==100,"actual exit returns to overworld portal");check.accept(player.getY()==POS.getY()+1,"destination cache is isolated between dimensions sharing the same X/Z chunk");player.discard();channel.finishAndReleaseAll();
            altar.counter=79;altar.setChanged();check.accept(!altar.saveWithoutMetadata(level.registryAccess()).contains("Counter")&&altar.saveWithoutMetadata(level.registryAccess()).getBoolean("Open"),"persistent altar stores open state while original spawn timer remains transient");check.accept(source.openCount==-1&&source.renderBounds().getXsize()==3,"server does not advance client opening animation and portal has3block render bounds");
            for(var p:List.of(a,b,low,high,borderOrigin.east(128),borderOrigin.east(129),tall,near))outer.removeBlock(p,false);
        }finally{release(level);}
    }
    static void prepareSave(ServerLevel level,BiConsumer<Boolean,String> check){try{
        level.setBlock(POS,Blocks.AIR.defaultBlockState(),2);level.setBlock(POS,R3Content.ELDRITCH_ALTAR.get().defaultBlockState(),2);
        level.setBlock(POS.above(2),R5Content.OBELISK.get().defaultBlockState(),2);level.setBlock(POS.above(),R5Content.ELDRITCH_PORTAL.get().defaultBlockState(),2);
        var altar=(EldritchAltarBlockEntity)level.getBlockEntity(POS);altar.eyes=4;altar.open=true;altar.counter=79;altar.setChanged();
        var maze=LabyrinthData.get(level);if(!maze.inRange(POS.getX()>>4,POS.getZ()>>4,29,29))maze.generate(POS.getX()>>4,POS.getZ()>>4,11,11,81234);
        check.accept(level.getBlockEntity(POS.above()) instanceof EldritchPortalBlockEntity&&altar.open,"independent portal save prepares an open altar, actual portal and SavedData maze");
    }finally{release(level);}}
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check){try{var altar=(EldritchAltarBlockEntity)level.getBlockEntity(POS);check.accept(altar!=null&&altar.open&&altar.eyes==4&&altar.counter==0,"actual server restart preserves altar eyes/open but resets original counter");var portal=level.getBlockEntity(POS.above());check.accept(portal instanceof EldritchPortalBlockEntity tile&&tile.openCount==-1,"actual portal tile survives restart with fresh opening timer");check.accept(LabyrinthData.get(level).inRange(POS.getX()>>4,POS.getZ()>>4,29,29),"activated maze survives real SavedData reload");}finally{release(level);}}
    private OuterPortalVerification(){}
}
