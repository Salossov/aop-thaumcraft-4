package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.entities.*;
import thaumcraft.registry.*;
import thaumcraft.api.aspects.Aspects;

final class GolemDebugLiveScene {
    static final BlockPos O=new BlockPos(0,65,0);
    static Path root(){return Path.of(System.getProperty("thaumcraft.r3.root"),System.getProperty("thaumcraft.debug.output","analysis/golem-debug-2026-10-15/live"));}
    static ServerPlayer player;static int stage=-1,age;static ThaumicGolem golem;
    static final List<Object> checks=new ArrayList<>();static final List<BlockPos> wall=new ArrayList<>();static thaumcraft.production.ArcaneBoreBlockEntity bore;
    static void register(MinecraftServer server){
        try{Files.createDirectories(root());Files.deleteIfExists(root().resolve("request.json"));Files.deleteIfExists(root().resolve("ready.json"));}catch(Exception e){throw new RuntimeException(e);}
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e)->{if(e.getEntity() instanceof ServerPlayer p){player=p;server.getPlayerList().op(p.getGameProfile());}});
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e)->{if(e.getEntity()==player)server.halt(false);});
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e)->{if(e.getServer()!=server||player==null)return;try{
            age++;GolemAlchemyFishingVerification.tick(player,golem,stage,age);GolemReactionVerification.tick(player,golem,stage,age);if(stage>=0&&age%5==0){var entry=GolemDebugService.snapshot(golem);checks.add(Map.of("stage",stage,"age",age,"reason",entry.reason(),"nodes",entry.path().size(),"target",String.valueOf(entry.target()),"carried",golem.essentiaAmount));if(stage>=9&&stage<=14)checks.add(Map.of("bore",bore.getUpdateTag(player.registryAccess()).toString(),"valid",bore.valid(),"powered",bore.powered(),"base",String.valueOf(player.serverLevel().getBlockState(bore.basePosition())),"state",bore.getBlockState().toString()));Files.writeString(root().resolve("server-checks.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(checks));}
            if(stage>=9&&stage<=14&&age==160){long mined=wall.stream().filter(p->player.serverLevel().isEmptyBlock(p)).count();if(mined<1)throw new AssertionError("Bore did not mine in direction "+bore.facing());checks.add(Map.of("stage",stage,"mined",mined,"facing",bore.facing().getName(),"target",String.valueOf(bore.target())));}
            if(stage==8&&age==40)castPrimal();
            var request=root().resolve("request.json");if(!Files.exists(request))return;int next=Integer.parseInt(Files.readString(request).trim());if(next==stage)return;
            prepare(next);stage=next;age=0;Files.writeString(root().resolve("ready.json"),Integer.toString(stage));
        }catch(Throwable error){error.printStackTrace();try{Files.writeString(root().resolve("server-failure.txt"),error.toString());}catch(Exception ignored){}server.halt(false);}});
    }
    static void castPrimal(){
        var wand=new ItemStack(R3Equipment.WAND.get());var focus=new ItemStack(R3Equipment.FOCI.get("primal").get());thaumcraft.items.WandFocus.set(wand,focus);for(var a:Aspects.PRIMALS)thaumcraft.items.WandVis.set(wand,a,2500);
        player.setGameMode(GameType.SURVIVAL);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,wand);
        long before=System.currentTimeMillis();boolean cast=thaumcraft.items.FocusCasting.begin(player,net.minecraft.world.InteractionHand.MAIN_HAND);long after=System.currentTimeMillis();
        if(!cast)throw new AssertionError("Primal focus cast failed");boolean match=false;
        for(long window=before/200;window<=after/200;window++){var quote=thaumcraft.items.ThaumicFocusItem.primalCost(window*200);var costs=thaumcraft.items.WandFocus.costs(wand,player,quote);if(costs.entrySet().stream().allMatch(e->2500-thaumcraft.items.WandVis.get(wand,e.getKey())==e.getValue()))match=true;}
        if(!match)throw new AssertionError("Primal charge differed from live cost window");checks.add("Native primal cast deducted same 200 ms quote shown by tooltip");
    }
    static void prepare(int next){
        var l=player.serverLevel();player.closeContainer();player.getInventory().clearContent();for(var mob:l.getEntitiesOfClass(Mob.class,new AABB(O).inflate(30)))mob.discard();
        for(var p:BlockPos.betweenClosed(O.offset(-10,-4,-10),O.offset(10,7,10)))l.setBlock(p,p.getY()==64?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        l.setDayTime(6000);player.setGameMode(GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();player.teleportTo(-3.5,66,8.5);player.setYRot(180);player.setXRot(16);
        if(next<9){l.setBlockAndUpdate(O,R3Content.ALCHEMY_FURNACE.get().defaultBlockState());
        var cube=Round3Verification.transport(l,O.above(),R3Content.ALEMBIC.get());
        if(next==0||next==1)cube.store.setAspects(new thaumcraft.api.aspects.AspectList().add(Aspects.AIR,16));cube.sync();}
        var body=new ItemStack(R3Equipment.GOLEM_BODIES.get(GolemMaterial.THAUMIUM).get());CustomData.update(DataComponents.CUSTOM_DATA,body,t->t.putInt("core",6));
        golem=R3Entities.GOLEM.get().create(l);golem.initialize(GolemMaterial.THAUMIUM,player.getUUID(),O.south(),Direction.SOUTH,body);golem.moveTo(next<2?-5.5:.5,65,1.5);l.addFreshEntity(golem);
        if(next==0||next==1){var jar=O.west(7).north(2);Round3Verification.transport(l,jar,R3Content.WARDED_JAR.get());golem.mark(new GolemMarker(l.dimension(),jar,Direction.UP,-1),false);}
        if(next==1)for(int y=0;y<4;y++)for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(Math.abs(x)==2||Math.abs(z)==2)l.setBlockAndUpdate(O.offset(x,y,z),Blocks.STONE.defaultBlockState());
        if(next==3||next==4){golem.essentia=Aspects.WATER;golem.essentiaAmount=8;}
        if(next==4){var jarPos=O.west(5);var jar=Round3Verification.transport(l,jarPos,R3Content.WARDED_JAR.get());jar.store.filter(Aspects.AIR);jar.store.setAspects(new thaumcraft.api.aspects.AspectList().add(Aspects.AIR,64));jar.sync();golem.mark(new GolemMarker(l.dimension(),jarPos,Direction.UP,-1),false);}
        if(next==5)golem.setNoAi(true);
        if(next==6){golem.initialize(GolemMaterial.THAUMIUM,player.getUUID(),O.south(),Direction.SOUTH,new ItemStack(R3Equipment.GOLEM_BODIES.get(GolemMaterial.THAUMIUM).get()));}
        if(next==7){player.setGameMode(GameType.SURVIVAL);int slot=9;for(var key:List.of("void_robe_helmet","void_robe_chestplate","void_robe_leggings","thaumaturge_chestplate"))player.getInventory().setItem(slot++,new ItemStack(R3Equipment.ARMOR.get(key).get()));}
        if(next>=8)golem.setNoAi(true);
        if(next==8){player.setGameMode(GameType.SURVIVAL);player.getInventory().setItem(9,new ItemStack(R3Equipment.FOCI.get("primal").get()));}
        if(next>=9&&next<=14){
            var direction=Direction.values()[next-9];var head=O.above();boolean hanging=direction==Direction.DOWN;var basePos=head.relative(hanging?Direction.UP:Direction.DOWN);l.removeBlock(O,false);if(direction==Direction.DOWN)for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)l.removeBlock(O.offset(x,-1,z),false);
            l.setBlockAndUpdate(basePos,R3Content.BORE_BASE.get().defaultBlockState());l.setBlockAndUpdate(head,R3Content.BORE.get().defaultBlockState().setValue(thaumcraft.production.ArcaneBoreBlock.HANGING,hanging));bore=(thaumcraft.production.ArcaneBoreBlockEntity)l.getBlockEntity(head);
            bore.items.setItem(0,new ItemStack(R3Equipment.FOCI.get("excavation").get()));bore.items.setItem(1,new ItemStack(Items.DIAMOND_PICKAXE));
            var look=direction.getOpposite();player.setYRot(switch(look){case NORTH->180;case SOUTH->0;case WEST->90;case EAST->270;default->0;});player.setXRot(look==Direction.UP?-90:look==Direction.DOWN?90:0);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(R3Equipment.WAND.get()));
            player.gameMode.useItemOn(player,l,player.getMainHandItem(),net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(head.getCenter(),Direction.UP,head,false));
            if(bore.facing()!=direction)throw new AssertionError("Wand used clicked face instead of opposite look direction "+direction);
            checks.add("Wand config opposite look, independent of clicked UP face: "+direction);wall.clear();
            var centre=head.relative(direction,5);for(int a=-3;a<=3;a++)for(int b=-3;b<=3;b++){var pos=switch(direction.getAxis()){case X->centre.offset(0,a,b);case Y->centre.offset(a,0,b);case Z->centre.offset(a,b,0);};wall.add(pos);l.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());}
            l.setBlockAndUpdate(basePos.east(),Blocks.REDSTONE_BLOCK.defaultBlockState());golem.moveTo(-6.5,65,1.5);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.teleportTo(6.5,68,8.5);player.setYRot(140);player.setXRot(20);
        }
        if(next==15||next==16){l.setBlockAndUpdate(O,R3Content.VIS_RELAY.get().defaultBlockState());if(next==15)l.setBlockAndUpdate(O.west(3),R3Content.ENERGIZED_NODE.get().defaultBlockState());golem.moveTo(-6.5,65,1.5);player.teleportTo(.5,65,2.5);player.setYRot(180);player.setXRot(30);}
        for(var entity:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(O).inflate(30)))entity.discard();
        if(next>=17){golem.discard();golem=next>=21?GolemReactionVerification.prepare(player,next):GolemAlchemyFishingVerification.prepare(player,next);}
        player.inventoryMenu.broadcastChanges();
    }
}
