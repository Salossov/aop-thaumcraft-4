package thaumcraft.verification;

import com.google.gson.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.api.aspects.*;
import thaumcraft.config.ThaumcraftClientConfig;
import thaumcraft.production.*;
import thaumcraft.registry.*;

/** Isolated native scene: holding a filled jar must not change placed jar labels. */
public final class JarLabelClientVerification {
    private static final String[] NAMES={"empty-hand-mono","main-hand-mono","cleared-hand-mono","dropped-mono","off-hand-mono","empty-hand-color","main-hand-color","dropped-color","main-hand-empty-mono","clear-after-empty-mono","main-hand-empty-color","clear-after-empty-color","main-hand-stack-color","clear-after-stack-color","main-hand-empty-stack-color","clear-after-empty-stack-color"};
    private static int ticks,stage=-1,wait;
    private static volatile boolean ready;
    private static boolean done,originalColor;
    private static boolean opening;
    private static ItemStack filled,different;
    private static String lastScreen="";
    private static final JsonArray captures=new JsonArray();
    private static final int TOTAL_STAGES=NAMES.length+32;
    private static String captureName(){
        if(stage<NAMES.length)return NAMES[stage];
        int view=(stage-NAMES.length)/4;
        return "direction-"+new String[]{"south","west","north","east"}[view/2]+(view%2==0?"-front":"-oblique")+new String[]{"-empty","-same","-different","-cleared"}[(stage-NAMES.length)%4];
    }
    static void install(net.neoforged.bus.api.IEventBus bus){
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event)->event.enqueueWork(()->{
            var mc=Minecraft.getInstance();mc.options.onboardAccessibility=false;mc.options.pauseOnLostFocus=false;mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0d);mc.options.save();
            org.lwjgl.glfw.GLFW.glfwSetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW,0);
            org.lwjgl.glfw.GLFW.glfwShowWindow(mc.getWindow().getWindow());org.lwjgl.glfw.GLFW.glfwRestoreWindow(mc.getWindow().getWindow());
            System.out.println("JAR_LABELS_VERIFICATION_READY_MUTED");
        }));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(JarLabelClientVerification::tick);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(JarLabelClientVerification::screen);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.RenderHandEvent event)->{
            if(Boolean.getBoolean("thaumcraft.jarLabels.inspectHand"))event.getPoseStack().translate(0,.25,0);
        });
    }
    private static Path output(){return Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/jar-labels",System.getProperty("thaumcraft.jarLabels.output","baseline"));}
    public static void screen(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post event){
        var mc=Minecraft.getInstance();String name=event.getScreen().getClass().getName();
        if(name.equals(lastScreen))return;lastScreen=name;
        try{Files.createDirectories(output());Files.writeString(output().resolve("screen.txt"),name+"\n"+event.getScreen().children().stream().filter(c->c instanceof net.minecraft.client.gui.components.AbstractWidget).map(c->((net.minecraft.client.gui.components.AbstractWidget)c).getMessage().getString()).toList());try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(output().resolve("startup.png"));}}
        catch(Exception error){throw new RuntimeException(error);}
        if(event.getScreen() instanceof net.minecraft.client.gui.screens.BackupConfirmScreen){
            // This profile contains an isolated QA-world copy, never a player save.
            event.getScreen().children().stream().filter(c->c instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().equals(net.minecraft.network.chat.Component.translatable("selectWorld.backupJoinSkipButton").getString())).map(c->(net.minecraft.client.gui.components.Button)c).findFirst().ifPresent(b->mc.execute(b::onPress));
        }
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(done||!"jar-labels-client".equals(System.getProperty("thaumcraft.r3.phase")))return;
        var mc=Minecraft.getInstance();ticks++;
        try{
            if(mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen){mc.screen.onClose();return;}
            if(ticks%100==0){
                Files.createDirectories(output());
                Files.writeString(output().resolve("screen.txt"),mc.screen==null?"none":mc.screen.getClass().getName());
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(output().resolve("startup.png"));}
            }
            if(ticks>3600)throw new AssertionError("Jar scene timed out at stage "+stage);
            if(!opening&&ticks>100&&mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen){opening=true;mc.createWorldOpenFlows().openWorld("jar-labels-qa",()->mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen()));return;}
            if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return;
            if(stage==-1&&mc.player.tickCount<40)return;
            if(stage==-1){
                stage=0;originalColor=ThaumcraftClientConfig.COLORED_JAR_LABELS.get();ThaumcraftClientConfig.COLORED_JAR_LABELS.set(false);
                Files.createDirectories(output());mc.options.guiScale().set(2);mc.options.bobView().set(false);mc.options.fov().set(70);mc.options.pauseOnLostFocus=false;mc.options.hideGui=false;mc.resizeDisplay();
                var server=mc.getSingleplayerServer();server.execute(()->{
                    var player=server.getPlayerList().getPlayer(mc.player.getUUID());var level=player.serverLevel();
                    for(var drop:level.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(-8,118,-8,12,128,10)))drop.discard();
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);level.setDayTime(6000);level.setWeatherParameters(100000,0,false,false);
                    player.setGameMode(GameType.CREATIVE);player.getInventory().clearContent();player.getInventory().selected=0;
                    player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(R2Content.GOGGLES.get()));
                    thaumcraft.knowledge.KnowledgeService.mutate(player,k->{k.discover(Aspects.TOOL);return true;});
                    for(int x=-5;x<=9;x++)for(int z=-4;z<=7;z++){
                        level.setBlock(new BlockPos(x,119,z),Blocks.SMOOTH_QUARTZ.defaultBlockState(),3);
                        for(int y=120;y<=124;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
                    }
                    for(int x:new int[]{0,2,4}){
                        var pos=new BlockPos(x,120,0);level.setBlock(pos,R3Content.WARDED_JAR.get().defaultBlockState().setValue(ProductionBlock.FACING,Direction.SOUTH),3);
                        var jar=(TransportBlockEntity)level.getBlockEntity(pos);jar.store.setAspects(new AspectList().add(Aspects.TOOL,64));jar.store.filter(Aspects.TOOL);jar.sync();
                        filled=new ItemStack(R3Content.WARDED_JAR.get());filled.set(DataComponents.BLOCK_ENTITY_DATA,CustomData.of(jar.saveWithFullMetadata(level.registryAccess())));
                    }
                    different=filled.copy();var differentTag=filled.get(DataComponents.BLOCK_ENTITY_DATA).copyTag();var fire=new net.minecraft.nbt.CompoundTag();fire.putInt(Aspects.FIRE.getTag(),64);differentTag.put("Essentia",fire);differentTag.putString("AspectFilter",Aspects.FIRE.getTag());different.set(DataComponents.BLOCK_ENTITY_DATA,CustomData.of(differentTag));
                    player.teleportTo(2.5,120,4.5);player.connection.teleport(2.5,120,4.5,180,16.5f);player.inventoryMenu.sendAllDataToRemote();ready=true;
                });return;
            }
            if(!ready)return;
            mc.setScreen(null);
            if(++wait<(stage==0?140:25))return;
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(output().resolve(captureName()+".png"));}
            var capture=new JsonObject();capture.addProperty("name",captureName());capture.addProperty("held",mc.player.getMainHandItem().toString());capture.addProperty("offhand",mc.player.getOffhandItem().toString());capture.addProperty("gl_error",org.lwjgl.opengl.GL11.glGetError());captures.add(capture);
            if(++stage==TOTAL_STAGES){finish(mc,true,null);return;}
            ready=false;wait=0;
            ThaumcraftClientConfig.COLORED_JAR_LABELS.set(stage>=5&&stage!=8&&stage!=9);
            mc.getSingleplayerServer().execute(()->{
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                for(int slot=0;slot<player.getInventory().items.size();slot++)player.getInventory().setItem(slot,ItemStack.EMPTY);
                player.getInventory().setItem(0,stage==1||stage==6?filled.copy():stage==8||stage==10?new ItemStack(R3Content.WARDED_JAR.get()):stage==12?filled.copyWithCount(64):stage==14?new ItemStack(R3Content.WARDED_JAR.get(),64):ItemStack.EMPTY);
                if(stage>=NAMES.length){
                    int view=(stage-NAMES.length)/4;var face=new Direction[]{Direction.SOUTH,Direction.WEST,Direction.NORTH,Direction.EAST}[view/2];
                    double angle=Math.toRadians(view/2*90+(view%2==0?0:40));
                    double dx=-Math.sin(angle)*4,dz=Math.cos(angle)*4;
                    if((stage-NAMES.length)%4==0){
                        for(var drop:player.serverLevel().getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(-8,118,-8,12,128,10)))drop.discard();
                        for(var pos:java.util.List.of(new BlockPos(0,120,0),new BlockPos(2,120,0),new BlockPos(4,120,0),new BlockPos(2,120,-2),new BlockPos(2,120,2)))player.serverLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),3);
                        for(int offset:new int[]{-2,0,2}){
                            var pos=new BlockPos(2+(face.getAxis()==Direction.Axis.Z?offset:0),120,face.getAxis()==Direction.Axis.X?offset:0);
                            player.serverLevel().setBlock(pos,R3Content.WARDED_JAR.get().defaultBlockState().setValue(ProductionBlock.FACING,face),3);
                            var jar=(TransportBlockEntity)player.serverLevel().getBlockEntity(pos);jar.store.setAspects(new AspectList().add(Aspects.TOOL,64));jar.store.filter(Aspects.TOOL);jar.sync();
                        }
                    }
                    player.connection.teleport(2.5+dx,120,.5+dz,(float)(180+Math.toDegrees(angle)),16.5f);
                    player.getInventory().setItem(0,switch((stage-NAMES.length)%4){case 1->filled.copyWithCount(64);case 2->different.copyWithCount(64);default->ItemStack.EMPTY;});
                }
                player.setItemSlot(EquipmentSlot.OFFHAND,stage==4?filled.copy():ItemStack.EMPTY);
                if(stage==3||stage==7){var drop=new ItemEntity(player.serverLevel(),6,120,1,filled.copy());drop.setPickUpDelay(32767);drop.setNoGravity(true);drop.setDeltaMovement(0,0,0);player.serverLevel().addFreshEntity(drop);}
                player.inventoryMenu.sendAllDataToRemote();ready=true;
            });
        }catch(Throwable error){error.printStackTrace();finish(mc,false,error.toString());}
    }
    private static void finish(Minecraft mc,boolean passed,String error){
        done=true;ThaumcraftClientConfig.COLORED_JAR_LABELS.set(originalColor);
        try{Files.createDirectories(output());var result=new JsonObject();result.addProperty("passed",passed);result.add("captures",captures);if(error!=null)result.addProperty("error",error);Files.writeString(output().resolve("client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}
        catch(Exception failure){throw new RuntimeException(failure);}
        mc.stop();
    }
}
