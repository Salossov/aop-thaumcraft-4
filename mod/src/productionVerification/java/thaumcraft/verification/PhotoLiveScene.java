package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import thaumcraft.entities.*;
import thaumcraft.items.*;
import thaumcraft.registry.*;
import thaumcraft.production.InfernalFurnaceBlock;

/** Private flat-world fixtures; no external input or user world is used. */
final class PhotoLiveScene {
    static final BlockPos O=new BlockPos(0,65,0);
    static Path root(){return Path.of(System.getProperty("thaumcraft.r3.root"),System.getProperty("thaumcraft.photo.output"));}
    static ServerPlayer player;static int stage=-1,age,hops;static TravelingTrunk trunk;static double peak;static final List<String> checks=new ArrayList<>();
    static void check(boolean value,String label){if(!value)throw new AssertionError(label);checks.add(label);}
    static void register(MinecraftServer server){
        try{Files.createDirectories(root());Files.deleteIfExists(root().resolve("ready.json"));Files.deleteIfExists(root().resolve("request.json"));}catch(Exception e){throw new RuntimeException(e);}
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e)->{if(e.getEntity() instanceof ServerPlayer p){player=p;thaumcraft.knowledge.KnowledgeService.initialize(p);}});
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent e)->{if(e.getEntity() instanceof ServerPlayer p)player=p;});
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e)->{if(e.getEntity()==player)server.halt(false);});
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.PlayLevelSoundEvent e)->{if(stage>=20&&e.getSound()!=null)FocusReviewScene.sounds.add(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getKey(e.getSound().value()).toString());if(stage==3&&e.getSound()!=null&&e.getSound().value()==net.minecraft.sounds.SoundEvents.CHEST_CLOSE)hops++;});
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e)->{if(e.getServer()!=server||player==null)return;try{
            age++;if(stage>=70)FollowupReviewScene.tick(stage,age);else if(stage>=20)FocusReviewScene.tick(stage,age);if(stage==8&&age==55){player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(R2Content.GOGGLES.get()));player.inventoryMenu.broadcastChanges();}if(stage==10&&age==85){player.teleportTo(.5,68,.5);player.getAbilities().flying=true;player.onUpdateAbilities();}if(stage==3&&trunk!=null){peak=Math.max(peak,trunk.getY());if(age==100){check(peak>65.2,"trunk actually leaves ground while following: peak="+peak);check(hops>0,"physical following hop emits chest-closing sound: "+hops);}}
            var req=root().resolve("request.json");if(!Files.exists(req))return;int next=Integer.parseInt(Files.readString(req).trim());if(next==stage)return;
            prepare(next);stage=next;age=0;signal("ready.json",stage);Files.writeString(root().resolve("server-checks.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(checks));
        }catch(Throwable failure){failure.printStackTrace();try{Files.writeString(root().resolve("server-failure.txt"),failure.toString());}catch(Exception ignored){}server.halt(false);}});
    }
    static void signal(String name,int value)throws Exception{var dest=root().resolve(name);var temp=root().resolve(name+".tmp");Files.writeString(temp,Integer.toString(value));for(int attempt=0;;attempt++){try{Files.move(temp,dest,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);break;}catch(java.nio.file.AccessDeniedException busy){if(attempt==19)throw busy;Thread.sleep(5);}}}
    static float yaw(int scene){if(scene>=70)return FollowupReviewScene.yaw(scene);if(scene>=42&&scene<=45)return new float[]{0,180,-90,90}[scene-42];if(scene==18||scene==19)return 146;return scene>=4&&scene<=7?new float[]{0,180,-90,90}[scene-4]:scene==10?0:180;}
    static float pitch(int scene){if(scene>=70)return FollowupReviewScene.pitch(scene);if(scene>=42&&scene<=45)return 0;if(scene>=20)return scene==27?0:14;if(scene==18||scene==19)return 15;return scene>=4&&scene<=7?0:scene==8||scene==9?28:scene==10?0:scene==11?20:12;}
    static void prepare(int scene){
        var l=player.serverLevel();player.removeAllEffects();player.setHealth(player.getMaxHealth());player.closeContainer();player.getInventory().clearContent();for(var s:EquipmentSlot.values())if(s.getType()==EquipmentSlot.Type.HUMANOID_ARMOR)player.setItemSlot(s,ItemStack.EMPTY);
        for(var e:l.getEntitiesOfClass(Entity.class,new AABB(O).inflate(20),e->!(e instanceof net.minecraft.world.entity.player.Player)))e.discard();
        for(var p:BlockPos.betweenClosed(O.offset(-10,-1,-10),O.offset(10,6,10)))l.setBlock(p,p.getY()==64?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        for(var item:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(O).inflate(20)))item.discard();
        l.setDayTime(6000);l.setWeatherParameters(0,12000,false,false);player.setGameMode(GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();player.teleportTo(.5,65,4.5);player.setYRot(yaw(scene));player.setXRot(pitch(scene));
        if(scene==1||scene==2||scene==3||scene==18||scene==19){
            trunk=R3Entities.TRUNK.get().create(l);var packed=new ItemStack(R3Equipment.TRAVELING_TRUNK.get());if(scene==2||scene==19)net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,packed,t->t.putByte("upgrade",(byte)1));trunk.configure(packed,player);trunk.moveTo(.5,65,.5);l.addFreshEntity(trunk);
            if(scene!=3){trunk.toggleStay();if(scene==18||scene==19)player.teleportTo(2.5,65,3.5);else player.openMenu(trunk,b->b.writeVarInt(trunk.getId()));}else{hops=0;peak=65;player.teleportTo(.5,65,9.5);}
        }
        if(scene>=4&&scene<=7||scene>=42&&scene<=45){var facing=new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST}[scene>=42?scene-42:scene-4];for(int part=0;part<27;part++)if(part!=22)l.setBlock(O.offset(InfernalFurnaceBlock.offset(part)),R3Content.INFERNAL_FURNACE.get().defaultBlockState().setValue(InfernalFurnaceBlock.PART,part).setValue(InfernalFurnaceBlock.FACING,facing),2);double distance=scene>=42?1.2:4.5;player.teleportTo(.5+facing.getStepX()*distance,64,.5+facing.getStepZ()*distance);if(scene>=42)player.setGameMode(GameType.SPECTATOR);}
        if(scene==8||scene==9){l.setBlockAndUpdate(O,R3Content.VIS_RELAY.get().defaultBlockState());l.setBlockAndUpdate(O.west(3),R3Content.ENERGIZED_NODE.get().defaultBlockState());player.teleportTo(.5,65,2.5);if(scene==9){player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(R2Content.GOGGLES.get()));player.setItemInHand(InteractionHand.MAIN_HAND,wand());}}
        if(scene==10){player.teleportTo(.5,65,.5);player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(R3Equipment.ARMOR.get("hover_harness").get()));player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(R3Equipment.ARMOR.get("void_robe_helmet").get()));player.setItemSlot(EquipmentSlot.LEGS,new ItemStack(R3Equipment.ARMOR.get("void_robe_leggings").get()));}
        if(scene==11){player.setGameMode(GameType.SURVIVAL);player.getInventory().setItem(9,new ItemStack(R2Content.TAINTED_SOIL.get()));l.setBlockAndUpdate(O,R2Content.TAINTED_SOIL.get().defaultBlockState());var item=new net.minecraft.world.entity.item.ItemEntity(l,.5,66,.5,new ItemStack(R2Content.TAINTED_SOIL.get()));item.setNoGravity(true);l.addFreshEntity(item);player.teleportTo(2.5,65,3.5);player.setYRot(146);}
        if(scene==12||scene==15){for(int x=-2;x<=2;x++)for(int z=-3;z<=-1;z++){l.setBlockAndUpdate(O.offset(x,-2,z),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(O.offset(x,-1,z),Blocks.WATER.defaultBlockState());}l.setBlockAndUpdate(O,Blocks.STONE_BRICKS.defaultBlockState());l.setBlockAndUpdate(O.east(),Blocks.CHEST.defaultBlockState());var g=Round3Verification.golem(l,O,11,GolemMaterial.CLAY,false);g.setNoAi(true);g.moveTo(.5,66,.5);g.setYRot(180);g.yBodyRot=180;g.yHeadRot=180;var water=O.north(2).below();g.fishingTarget(water);l.addFreshEntity(new GolemBobber(l,g,water));player.teleportTo(-2.5,66,3.5);player.setYRot(-140);if(scene==15){player.teleportTo(3.5,66,2.5);player.setYRot(123);}}
        if(scene==13)player.setItemInHand(InteractionHand.MAIN_HAND,wand());
        if(scene==14){l.setBlockAndUpdate(O,thaumcraft.registry.R2Content.ARCANE_WORKBENCH.get().defaultBlockState());var table=(thaumcraft.blocks.TableBlockEntity)l.getBlockEntity(O);var w=wand();for(var a:thaumcraft.api.aspects.Aspects.PRIMALS)WandVis.set(w,a,1000);table.setItem(10,w);var recipe=(thaumcraft.crafting.ArcaneRecipe)l.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.parse("thaumcraft:wandcapgold")).orElseThrow().value();thaumcraft.knowledge.KnowledgeService.mutate(player,k->k.completeResearch(recipe.research()));player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(R3Equipment.ARMOR.get("void_robe_chestplate").get()));int width=recipe.pattern().width();for(int i=0;i<recipe.getIngredients().size();i++){var ing=recipe.getIngredients().get(i);if(!ing.isEmpty())table.setItem(i/width*3+i%width,ing.getItems()[0].copy());}player.inventoryMenu.broadcastChanges();player.openMenu(table);}
        if(scene>=70)FollowupReviewScene.prepare(scene);else if(scene>=20)FocusReviewScene.prepare(scene);player.inventoryMenu.broadcastChanges();
    }
    static ItemStack wand(){var w=new ItemStack(R3Equipment.WAND.get());w.set(WandComponents.PARTS.get(),new thaumcraft.api.wands.WandParts("greatwood","gold"));int n=0;for(var a:thaumcraft.api.aspects.Aspects.PRIMALS)WandVis.set(w,a,new int[]{237,55,337,150,1429,4123}[n++]);return w;}
    private PhotoLiveScene(){}
}
