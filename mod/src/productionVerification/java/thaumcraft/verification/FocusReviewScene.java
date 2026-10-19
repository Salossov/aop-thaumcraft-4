package thaumcraft.verification;

import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import thaumcraft.items.*;
import thaumcraft.entities.*;
import thaumcraft.registry.*;

/** Actual focus calls on the isolated photo server; the client verifies received effects and geometry. */
final class FocusReviewScene {
    static final String[] FOCI={"fire","frost","shock","excavation","trade","hellbat","portable_hole","warding","primal","pech"};
    static net.minecraft.world.entity.animal.Cow cow;static boolean cast;
    static final java.util.List<String> sounds=new java.util.ArrayList<>();
    static ItemStack wand(String focus){var w=PhotoLiveScene.wand();for(var aspect:thaumcraft.api.aspects.Aspects.PRIMALS)WandVis.set(w,aspect,WandVis.capacity(w));WandFocus.set(w,new ItemStack(focus.equals("pech")?R5Content.PECH_FOCUS.get():R3Equipment.FOCI.get(focus).get()));return w;}
    static void prepare(int scene){if(scene>=46&&scene<=55){PhotoLiveScene.player.setItemInHand(InteractionHand.MAIN_HAND,wand(FOCI[scene-46]));return;}if(scene>=42)return;
        var p=PhotoLiveScene.player;var l=p.serverLevel();var o=PhotoLiveScene.O;cast=false;sounds.clear();cow=null;
        p.setGameMode(GameType.SURVIVAL);p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();p.setYRot(180);p.yRotO=180;p.setXRot(scene==27?0:14);p.xRotO=p.getXRot();
        if(scene<=25||scene>=30&&scene<=35)return;
        p.setItemInHand(InteractionHand.MAIN_HAND,wand(scene==26?"excavation":scene==27?"hellbat":scene==28?"frost":scene==36?"warding":scene==37?"trade":scene==38?"fire":scene==39?"shock":scene==40?"primal":scene==41?"pech":"portable_hole"));
        if(scene==26)l.setBlockAndUpdate(o,Blocks.OBSIDIAN.defaultBlockState());
        if(scene==27){p.teleportTo(1.2,65,4.5);cow=EntityType.COW.create(l);cow.setNoAi(true);cow.moveTo(.5,65,-3.5);l.addFreshEntity(cow);}
        if(scene==28)for(var b:BlockPos.betweenClosed(o.offset(-3,0,-3),o.offset(3,4,-3)))l.setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
        if(scene==29)for(var b:BlockPos.betweenClosed(o.offset(-3,-1,-4),o.offset(3,3,0)))l.setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
        if(scene>=36){l.setBlockAndUpdate(o,Blocks.STONE.defaultBlockState());if(scene==37){var f=WandFocus.get(p.getMainHandItem());net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,f,t->t.put("PickedBlock",net.minecraft.nbt.NbtUtils.writeBlockState(Blocks.DIRT.defaultBlockState())));WandFocus.set(p.getMainHandItem(),f);p.getInventory().setItem(1,new ItemStack(Blocks.DIRT,64));}}
        p.inventoryMenu.broadcastChanges();
    }
    static void tick(int scene,int age){
        if(scene<26||scene>=30&&scene<=35||scene>=42)return;var p=PhotoLiveScene.player;var l=p.serverLevel();var o=PhotoLiveScene.O;
        if(age==20){cast=FocusCasting.begin(p,InteractionHand.MAIN_HAND);PhotoLiveScene.check(cast,"actual focus activation accepted, scene="+scene);if(scene==28)for(var projectile:l.getEntitiesOfClass(FocusProjectile.class,new AABB(o).inflate(12)))projectile.bounces=0;}
        if(scene==27&&age==22)PhotoLiveScene.check(l.getEntitiesOfClass(FocusFireBat.class,new AABB(o).inflate(16)).size()==1,"Hellbat selects a living target within original .8 aim padding and spawns a bat");
        if(scene==27&&age==170)PhotoLiveScene.check(cow.getHealth()<cow.getMaxHealth(),"summoned Hellbat reaches and damages the actual target");
        if(scene==26&&age==110){PhotoLiveScene.check(l.getBlockState(o).isAir(),"excavation actually harvests obsidian");FocusCasting.stop(p);p.stopUsingItem();PhotoLiveScene.check(sounds.contains("thaumcraft:rumble"),"excavation rumble sound emitted");}
        if(scene==29&&age==45){PhotoLiveScene.check(l.getBlockState(o).is(R3Content.PORTABLE_HOLE.get())&&l.getBlockState(o.north(3)).is(R3Content.PORTABLE_HOLE.get()),"portable hole propagates through a thick wall");PhotoLiveScene.check(sounds.contains("minecraft:entity.enderman.teleport"),"portable hole teleport sound emitted");}
        if(scene==29&&age==175)PhotoLiveScene.check(l.getBlockState(o).is(Blocks.STONE)&&l.getBlockState(o.north(3)).is(Blocks.STONE),"portable hole restores original wall after its duration");
        if(age==110&&(scene==38||scene==39)){FocusCasting.stop(p);p.stopUsingItem();}
        if(age==170&&scene>=36){String expected=scene==36?"thaumcraft:zap":scene==37?"thaumcraft:wand":scene==38?"thaumcraft:fireloop":scene==39?"thaumcraft:shock":"thaumcraft:ice";PhotoLiveScene.check(sounds.contains(expected),"actual focus sound delivered: "+expected+", scene="+scene);}
        if(age==175)try{java.nio.file.Files.writeString(PhotoLiveScene.root().resolve("focus-server-checks.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(PhotoLiveScene.checks));}catch(Exception e){throw new RuntimeException(e);}
    }
    private FocusReviewScene(){}
}
