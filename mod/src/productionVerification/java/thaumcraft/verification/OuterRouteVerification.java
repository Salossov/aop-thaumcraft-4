package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.blocks.*;
import thaumcraft.entities.*;
import thaumcraft.items.*;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.production.EldritchAltarBlockEntity;
import thaumcraft.registry.*;
import thaumcraft.world.dim.*;

/** Scripted native survival callbacks. Not a claim of ordinary player combat/balance acceptance. */
final class OuterRouteVerification {
    private final net.minecraft.server.MinecraftServer server;
    private final ServerLevel overworld,outer;
    private final List<String> checks=new ArrayList<>();
    private final Map<ServerLevel,Set<Long>> forced=new HashMap<>();
    private final Path result=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/full_port/outer-route.json");
    private final BlockPos origin;
    private ServerPlayer player;private io.netty.channel.embedded.EmbeddedChannel channel;
    private EldritchAltarBlockEntity altar;private BlockPos portal,keyPos,lockPos;
    private List<LabyrinthData.Position> keyCells,bossCells;
    private final ArrayDeque<BlockPos> walking=new ArrayDeque<>();
    private int ticks,stage,stageTicks,walked,attacks,mined;private final int replay=Integer.getInteger("thaumcraft.qa.routeReplay",0);private boolean done,navigationOnly;
    private final Set<String> encountered=new LinkedHashSet<>();
    OuterRouteVerification(net.minecraft.server.MinecraftServer server){
        this.server=server;overworld=server.overworld();outer=server.getLevel(EldritchTeleporter.OUTER);
        int attempt=0;try{if(Files.exists(result))attempt=com.google.gson.JsonParser.parseString(Files.readString(result)).getAsJsonObject().get("attempt").getAsInt();}catch(Exception ignored){}
        try(var files=Files.newDirectoryStream(result.getParent(),"outer-route*.json")){for(var file:files){var row=com.google.gson.JsonParser.parseString(Files.readString(file)).getAsJsonObject();if(row.has("attempt"))attempt=Math.max(attempt,row.get("attempt").getAsInt());}}catch(Exception ignored){}
        origin=new BlockPos((5200+(replay>0?replay:attempt+1)*64)*16+8,200,5200*16+8);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)force(overworld,(origin.getX()>>4)+x,(origin.getZ()>>4)+z);
        NeoForge.EVENT_BUS.addListener(this::tick);
    }
    private void check(boolean value,String label){if(!value)throw new AssertionError(label);checks.add(label);thaumcraft.Thaumcraft.LOGGER.info("Outer route: {}",label);}
    private void force(ServerLevel level,int x,int z){long p=ChunkPos.asLong(x,z);if(forced.computeIfAbsent(level,k->new HashSet<>()).add(p))level.setChunkForced(x,z,true);level.getChunk(x,z);}
    private boolean ready(){for(var entry:forced.entrySet())for(long pos:entry.getValue())if(!entry.getKey().areEntitiesLoaded(pos))return false;return true;}
    private void next(){stage++;stageTicks=0;}
    private void setup(){
        check(server.isDedicatedServer()&&outer!=null,"real dedicated server and registered Outer Lands");
        if(replay==0){
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){overworld.setBlock(origin.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);for(int y=0;y<5;y++)overworld.setBlock(origin.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);}
        overworld.setBlock(origin,R3Content.ELDRITCH_ALTAR.get().defaultBlockState(),2);overworld.setBlock(origin.above(2),R5Content.OBELISK.get().defaultBlockState(),2);overworld.setBlock(origin.above(),R2Content.AURA_NODE.get().defaultBlockState(),2);
        ((AuraNodeBlockEntity)overworld.getBlockEntity(origin.above())).initialize(Map.of("aer",100),thaumcraft.api.nodes.NodeState.Type.DARK,thaumcraft.api.nodes.NodeState.Modifier.NONE);
        }
        altar=(EldritchAltarBlockEntity)overworld.getBlockEntity(origin);
        player=new ServerPlayer(server,overworld,new GameProfile(UUID.randomUUID(),"OuterRouteQA"),ClientInformation.createDefault());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);channel=new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(server,connection,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false)){
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet){}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){}
        };
        player.setGameMode(GameType.SURVIVAL);player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000);player.setHealth(2000);player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(30);player.moveTo(origin.getCenter().add(2,0,0));KnowledgeService.initialize(player);KnowledgeService.mutate(player,k->k.completeResearch("OCULUS"));overworld.addFreshEntity(player);
        if(replay==0){
        var eyes=new ItemStack(R3Equipment.ELDRITCH_EYE.get(),4);player.setItemInHand(InteractionHand.MAIN_HAND,eyes);
        var hit=new BlockHitResult(origin.getCenter(),Direction.EAST,origin,false);
        for(int n=0;n<4;n++)player.gameMode.useItemOn(player,overworld,eyes,InteractionHand.MAIN_HAND,hit);
        check(eyes.isEmpty()&&altar.eyes==4,"four native eye interactions consume four eyes");
        var wand=new ItemStack(R2Content.WAND.get());wand.set(WandComponents.PARTS.get(),new thaumcraft.api.wands.WandParts("silverwood","gold"));for(var aspect:thaumcraft.api.aspects.Aspects.PRIMALS)WandVis.set(wand,aspect,10000);player.setItemInHand(InteractionHand.MAIN_HAND,wand);
        player.gameMode.useItemOn(player,overworld,wand,InteractionHand.MAIN_HAND,hit);
        check(altar.open&&overworld.getBlockState(origin.above()).is(R5Content.ELDRITCH_PORTAL.get()),"funded native wand interaction opens original obelisk");
        check(thaumcraft.api.aspects.Aspects.PRIMALS.stream().allMatch(a->WandVis.get(wand,a)==0),"all six original100vis costs consumed");
        }else check(altar!=null&&altar.open,"replay uses unchanged saved obelisk and original allocated maze");
        var maze=LabyrinthData.get(outer);var start=new LabyrinthData.Position(origin.getX()>>4,origin.getZ()>>4);
        check(maze.cell(start.x(),start.z()).feature==1,"allocated maze portal cell matches overworld obelisk chunk");
        keyCells=NativeMazeRoute.cells(maze,start,c->c.feature==6,false);bossCells=NativeMazeRoute.cells(maze,keyCells.getLast(),c->c.feature>=2&&c.feature<=5,true);
        for(var cell:maze.snapshot().keySet())if(Math.abs(cell.x()-start.x())<30&&Math.abs(cell.z()-start.z())<30)force(outer,cell.x(),cell.z());
        // These are all real custom-generator chunks; no portal, key or lock is placed by the fixture.
        portal=EldritchTeleporter.findPortal(outer,origin.getCenter());check(portal!=null,"custom generator produces a discoverable return portal without a fixture portal");
        var keyCell=keyCells.getLast();keyPos=new BlockPos(keyCell.x()*16+8,53,keyCell.z()*16+8);
        var bossCell=bossCells.getLast();var chunk=outer.getChunk(bossCell.x(),bossCell.z());
        lockPos=chunk.getBlockEntities().entrySet().stream().filter(e->e.getValue() instanceof EldritchLockBlockEntity).map(Map.Entry::getKey).findFirst().orElseThrow(()->new AssertionError("Generated boss entrance lacks lock"));
        check(outer.getBlockEntity(lockPos) instanceof EldritchLockBlockEntity,"custom generator produces the reachable runic lock");
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        next();
    }
    private Set<LabyrinthData.Position> allowed(List<LabyrinthData.Position> cells){
        var result=new HashSet<LabyrinthData.Position>();var maze=LabyrinthData.get(outer);int cx=origin.getX()>>4,cz=origin.getZ()>>4;
        for(var p:maze.snapshot().keySet())if(Math.abs(p.x()-cx)<30&&Math.abs(p.z()-cz)<30){var c=maze.cell(p.x(),p.z());if(c.feature<2||c.feature>5||cells.contains(p))result.add(p);}
        return result;
    }
    private void walkTo(BlockPos preferred,List<LabyrinthData.Position> cells){var start=NativeMazeRoute.nearest(outer,player.blockPosition());var goal=NativeMazeRoute.nearest(outer,preferred);player.moveTo(start.getX()+.5,start.getY(),start.getZ()+.5);walking.addAll(NativeMazeRoute.walk(outer,start,goal,allowed(cells)));}
    private void tick(ServerTickEvent.Post event){
        if(done||event.getServer()!=server)return;
        try{
            if(++ticks>4200)throw new AssertionError("Native route timed out at stage "+stage+" after "+stageTicks+" ticks, walked="+walked+" actors="+encountered);
            stageTicks++;if(player!=null){player.doTick();player.heal(10);player.getFoodData().setFoodLevel(20);}
            if(stage==0){if(ready())setup();return;}
            if(stage==1){if(!ready())return;if(replay==0)check(!outer.getEntitiesOfClass(PermanentItemEntity.class,new AABB(keyPos).inflate(4)).isEmpty(),"generated key room contains a real floating key entity");player.moveTo(origin.above().getCenter());player.setPortalCooldown(0);next();return;}
            if(stage==2){if(player.serverLevel()!=outer)return;check(KnowledgeService.get(player).completedResearch().contains("ENTEROUTER"),"native portal ticker transfers player and awards ENTEROUTER");walkTo(keyPos,keyCells);next();return;}
            if(stage==3||stage==5){if(!walking.isEmpty()){var p=walking.remove();checkMove(p);return;}if(stage==3){var items=outer.getEntitiesOfClass(PermanentItemEntity.class,new AABB(keyPos).inflate(5));for(var item:items)if(item.getItem().is(R5Content.RUNIC_KEY.get())){player.moveTo(item.position());item.playerTouch(player);}if(replay>0&&player.getInventory().countItem(R5Content.RUNIC_KEY.get())==0){navigationOnly=true;check(true,"saved failed route already consumed its generated key; replay validates navigation only, without inventing a replacement");}else check(player.getInventory().countItem(R5Content.RUNIC_KEY.get())==1,"native item pickup grants exactly one generated key");next();}else{if(navigationOnly){check(true,"saved failed maze now has a collision-validated route from key room to original lock");finish(null);}else{useKey();next();}}return;}
            if(stage==4){var start=NativeMazeRoute.nearest(outer,player.blockPosition());player.moveTo(start.getX()+.5,start.getY(),start.getZ()+.5);walking.addAll(NativeMazeRoute.interact(outer,start,lockPos,allowed(bossCells),player));next();return;}
            if(stage==6){if(outer.getBlockEntity(lockPos)!=null)return;check(stageTicks>=95,"natural block ticker completes original101tick keyed opening");next();return;}
            if(stage==7){combat();return;}
            if(stage==8){player.moveTo(portal.getCenter());player.setPortalCooldown(0);next();return;}
            if(stage==9){if(player.serverLevel()!=overworld)return;check(player.blockPosition().distSqr(origin.above())<16,"native return portal ticker returns to original overworld obelisk");check(player.getInventory().countItem(R3Equipment.PRIMAL_PEARL.get())>=1,"real boss-death pearl survives dimension return in inventory");finish(null);}
        }catch(Throwable failure){finish(failure);}
    }
    private void checkMove(BlockPos feet){
        for(var p:List.of(feet.above(),feet))for(int obstruction=0;obstruction<4&&outer.getBlockState(p).is(R5Content.ANCIENT_STONE.get());obstruction++){
            var hit=outer.clip(new ClipContext(player.getEyePosition(),p.getCenter(),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,player));
            var exposed=hit.getBlockPos();
            check(outer.getBlockState(exposed).is(R5Content.ANCIENT_STONE.get())&&player.getEyePosition().distanceToSqr(hit.getLocation())<=Math.pow(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE),2),"original crust obstacle is reachable with native mining line of sight: "+exposed+" feet="+player.blockPosition());
            if(!player.gameMode.destroyBlock(exposed))throw new AssertionError("Native survival mining refused crust at "+exposed);mined++;
        }
        if(!NativeMazeRoute.bodyClear(outer,feet))throw new AssertionError("Generated corridor became blocked at "+feet);player.moveTo(feet.getX()+.5,feet.getY(),feet.getZ()+.5);player.setDeltaMovement(Vec3.ZERO);walked++;}
    private void useKey(){
        double reach=player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        var hit=outer.clip(new ClipContext(player.getEyePosition(),lockPos.getCenter(),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,player));
        check(player.getEyePosition().distanceToSqr(lockPos.getCenter())<=reach*reach&&hit.getBlockPos().equals(lockPos),"collision-validated route reaches the lock within native interaction range and line of sight");
        int slot=-1;for(int n=0;n<player.getInventory().getContainerSize();n++)if(player.getInventory().getItem(n).is(R5Content.RUNIC_KEY.get())){slot=n;break;}
        if(slot<0)throw new AssertionError("Picked-up key missing from native inventory");
        var key=player.getInventory().removeItem(slot,1);player.setItemInHand(InteractionHand.MAIN_HAND,key);
        player.gameMode.useItemOn(player,outer,key,InteractionHand.MAIN_HAND,hit);
        check(key.isEmpty()&&((EldritchLockBlockEntity)outer.getBlockEntity(lockPos)).count==0,"native key interaction consumes the generated key and starts the lock");player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
    }
    private void combat(){
        var area=new AABB(lockPos).inflate(50);var mobs=outer.getEntitiesOfClass(Mob.class,area,m->m.isAlive()&&(m instanceof ThaumcraftBoss||m instanceof CultistPortal||m instanceof Taintacle||m instanceof Cultist));
        for(var mob:mobs)encountered.add(mob.getType().builtInRegistryHolder().key().location().toString());
        if(stageTicks%20==0&&!mobs.isEmpty()){
            var mob=mobs.getFirst();if(mob instanceof CultistPortal invasion&&invasion.getStage()<13){for(var minion:mobs)if(minion instanceof Cultist&&! (minion instanceof CultistLeader)){player.moveTo(minion.position().add(1,0,0));player.attack(minion);attacks++;break;}}
            else{player.moveTo(mob.position().add(2,0,0));player.setDeltaMovement(Vec3.ZERO);player.attack(mob);attacks++;}
        }
        if(mobs.isEmpty()&&stageTicks>30){
            if(player.getInventory().countItem(R3Equipment.PRIMAL_PEARL.get())>0){check(!encountered.isEmpty(),"native survival attacks complete arena actors, real death callbacks and pearl pickup: "+encountered);next();return;}
            var pearls=outer.getEntitiesOfClass(ItemEntity.class,area,i->i.getItem().is(R3Equipment.PRIMAL_PEARL.get()));
            if(pearls.isEmpty())throw new AssertionError("Completed native encounter produced no collectible pearl");
            for(var pearl:pearls){player.moveTo(pearl.position());pearl.playerTouch(player);}
            if(player.getInventory().countItem(R3Equipment.PRIMAL_PEARL.get())==0)return;
            check(true,"native survival attacks complete arena death callbacks and pearl pickup");next();
        }
    }
    private void finish(Throwable failure){
        if(done)return;done=true;
        try{if(player!=null)player.discard();if(channel!=null)channel.finishAndReleaseAll();for(var entry:forced.entrySet())for(long pos:entry.getValue())entry.getKey().setChunkForced(ChunkPos.getX(pos),ChunkPos.getZ(pos),false);server.saveEverything(false,true,true);
            var record=new LinkedHashMap<String,Object>();record.put("passed",failure==null);record.put("attempt",(origin.getX()/16-5200)/64);record.put("count",checks.size());record.put("checks",checks);record.put("time",java.time.Instant.now().toString());record.put("origin",origin.toShortString());record.put("ticks",ticks);record.put("walked_blocks",walked);record.put("mined_original_crust",mined);record.put("replay_saved_attempt",replay);record.put("navigation_only",navigationOnly);record.put("native_survival_attacks",attacks);record.put("encountered",encountered);record.put("manual_acceptance","pending");record.put("fixture_limits",List.of("Native server callbacks with a headless ServerPlayer and prepared research/vis/eyes.","Collision-validated block-step routes and native crust destruction callbacks, not live client movement or timed mining input.","Combat fixture has2000HP, scripted healing and base attack30; survival balance remains manual QA."));if(failure!=null){record.put("error",failure.toString());record.put("stage",stage);thaumcraft.Thaumcraft.LOGGER.error("Outer route failed",failure);}Files.writeString(result,new GsonBuilder().setPrettyPrinting().create().toJson(record)+"\n");
        }catch(Exception e){throw new RuntimeException(e);}finally{server.halt(false);}
    }
}
