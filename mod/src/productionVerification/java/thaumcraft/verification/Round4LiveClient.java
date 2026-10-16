package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import thaumcraft.client.*;
import thaumcraft.items.*;
import thaumcraft.registry.*;

final class Round4LiveClient {
    static int stage=Integer.getInteger("thaumcraft.round4.start",0),wait,frames,lastShot=-1;
    static boolean started,prepared,done;
    static final List<Object> observations=new ArrayList<>();static final Set<String> tooltipBounds=new HashSet<>();static int tooltipFrames;
    static final String[] names={"node-editor","ore-through-wall","converter","node-charging","focus-radial","arcane-tooltip","alchemy-gauge","inventory-models","golem-three-alembics","relay-crystals","construction","scan-golem","cursor-scanner","golem-equipment-world","wand-charging","staff-charging"};
    static void install(net.neoforged.bus.api.IEventBus bus){NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST,(RenderLevelStageEvent e)->{if(stage==1&&e.getStage()==RenderLevelStageEvent.Stage.AFTER_LEVEL)com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();});NeoForge.EVENT_BUS.addListener((RenderTooltipEvent.Pre e)->{if(stage!=5||!(e.getItemStack().getItem() instanceof CastingWandItem))return;int w=e.getComponents().stream().mapToInt(c->c.getWidth(e.getFont())).max().orElse(0),h=e.getComponents().stream().mapToInt(c->c.getHeight()).sum();var anchor=e.getTooltipPositioner().positionTooltip(e.getScreenWidth(),e.getScreenHeight(),e.getX(),e.getY(),w,h);tooltipFrames++;tooltipBounds.add(anchor.x()+","+anchor.y()+":"+w+"x"+h);});bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e)->e.enqueueWork(()->{var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;mc.options.skipMultiplayerWarning=true;mc.options.onboardAccessibility=false;mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0d);mc.options.save();org.lwjgl.glfw.GLFW.glfwSetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW,0);org.lwjgl.glfw.GLFW.glfwShowWindow(mc.getWindow().getWindow());}));NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{try{tick();}catch(Throwable t){fail(t);}});NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post e)->{if(Minecraft.getInstance().screen==null)render();});NeoForge.EVENT_BUS.addListener((ScreenEvent.Render.Post e)->render());}
    static void check(boolean value,String label){if(!value)throw new AssertionError(label);observations.add(label);}
    static void request()throws Exception{var mc=Minecraft.getInstance();mc.options.keyUse.setDown(false);mc.player.stopUsingItem();mc.setScreen(null);wait=frames=0;lastShot=-1;prepared=false;Files.writeString(Round4LiveScene.root().resolve("request.json"),Integer.toString(stage));}
    static void use(BlockPos p,Direction face){var mc=Minecraft.getInstance();mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(p.getCenter(),face,p,false));}
    static void tick()throws Exception{
        var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getOverlay()!=null)return;
        if(!started){if(++wait<60)return;started=true;mc.options.fov().set(70);mc.options.guiScale().set(2);mc.options.bobView().set(false);mc.options.hideGui=false;mc.options.gamma().set(.5);mc.resizeDisplay();request();return;}
        if(Files.exists(Round4LiveScene.root().resolve("server-failure.txt")))throw new AssertionError(Files.readString(Round4LiveScene.root().resolve("server-failure.txt")));
        var ready=Round4LiveScene.root().resolve("ready.json");if(!Files.exists(ready)||Integer.parseInt(Files.readString(ready))!=stage)return;wait++;
        mc.player.setYRot(stage==2?156:180);mc.player.yRotO=mc.player.getYRot();mc.player.setXRot(stage==8||stage==11||stage==13?20:stage==9?30:stage==3||stage==14||stage==15?1.5f:8);mc.player.xRotO=mc.player.getXRot();mc.player.setDeltaMovement(Vec3.ZERO);
        if(wait>=30&&!prepared){prepared=true;switch(stage){
            case 0->{check(mc.screen instanceof NodeEditorScreen,"node command opens real editor");var boxes=mc.screen.children().stream().filter(w->w instanceof EditBox).map(w->(EditBox)w).toList();for(var box:boxes)if(box.getWidth()==40){check(box.getValue().isEmpty(),"zero aspect input is empty");box.setValue("01250");check(box.getValue().equals("1250"),"aspect normalizes leading zeroes immediately");box.setValue("42");}var buttons=mc.screen.children().stream().filter(w->w instanceof Button).map(w->(Button)w).toList();var type=buttons.get(2);mc.screen.mouseClicked(type.getX()+6,type.getY()+6,0);mc.screen.mouseClicked(type.getX()+8,type.getY()+20+18+6,0);}
            case 1->{mc.options.keyUse.setDown(true);use(new BlockPos(0,66,0),Direction.SOUTH);mc.options.keyUse.setDown(false);}
            case 3,14,15->{mc.options.keyUse.setDown(true);use(Round4LiveScene.O.above(),Direction.SOUTH);}
            case 4->{mc.setScreen(new FocusRadialScreen(InteractionHand.MAIN_HAND));}
            case 5->{check(mc.screen instanceof ArcaneScreen,"arcane screen opened through server menu");moveToSlot(1);}
            case 6->{check(mc.screen instanceof AlchemyFurnaceScreen,"alchemy screen opened through server menu");}
            case 7->{mc.setScreen(new InventoryScreen(mc.player));check(!mc.player.containerMenu.getSlot(16).getItem().has(net.minecraft.core.component.DataComponents.DYED_COLOR),"fixed-colour void hood has no dye component");var move=MouseHandler.class.getDeclaredMethod("onMove",long.class,double.class,double.class);move.setAccessible(true);move.invoke(mc.mouseHandler,mc.getWindow().getWindow(),0d,0d);}
            case 12->{mc.setScreen(new InventoryScreen(mc.player));moveToSlot(9);var holding=ScanUi.class.getDeclaredMethod("holding");holding.setAccessible(true);check(!(boolean)holding.invoke(null),"client inventory ignores scanner merely selected in hotbar");}
            case 11->{check(mc.hitResult instanceof EntityHitResult,"golem can be targeted");mc.options.keyUse.setDown(true);mc.gameMode.interact(mc.player,((EntityHitResult)mc.hitResult).getEntity(),InteractionHand.MAIN_HAND);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);}
        }}
        if(stage==12&&wait>30)moveToSlot(9);
        if(stage==12&&wait==50){mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,36,0,net.minecraft.world.inventory.ClickType.PICKUP,mc.player);check(mc.player.containerMenu.getCarried().getItem() instanceof ThaumometerItem,"native inventory click picks scanner up onto cursor");}
        if(stage==5&&wait>30&&wait%10==0)moveToSlot(1);
        if(stage==4&&wait==55){int radius=16+9*5/2;mc.screen.mouseMoved(mc.screen.width/2,mc.screen.height/2-radius);org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(),mc.screen.width/2*mc.getWindow().getGuiScale(),(mc.screen.height/2-radius)*mc.getWindow().getGuiScale());}
        if(stage==4&&wait==70){mc.screen.mouseClicked(mc.screen.width/2,mc.screen.height/2-38,0);}
        if(stage==9&&wait>=40&&wait<=160&&wait%20==0){int shard=(wait-40)/20;mc.player.getInventory().selected=shard;use(Round4LiveScene.O,Direction.SOUTH);}
        if(stage==9&&wait>=55&&wait<=175&&wait%20==15){int expected=(wait-55)/20;var relay=(thaumcraft.visnet.VisRelayBlockEntity)mc.level.getBlockEntity(Round4LiveScene.O);check(relay.getAttunement()==(expected==6?-1:expected),"native crystal click syncs relay colour "+expected);check(relay.getRootSource()!=null,"relay automatically reconnects after colour change");observations.add(Map.of("shard",expected,"aspect",relay.attunementName().getString(),"parent",String.valueOf(relay.getParentPosition())));}
        if(stage==2&&wait%5==0){var converter=(thaumcraft.visnet.NodeConverterBlockEntity)mc.level.getBlockEntity(Round4LiveScene.O.above());float a=converter.progress(0),b=converter.progress(.5f),c=converter.progress(1);if(a!=c){if(Math.abs(b-(a+c)/2)>.001f||Math.abs(c-a)>1.001f)throw new AssertionError("converter sub-tick interpolation discontinuity");}observations.add(Map.of("tick",wait,"interpolation",List.of(a,b,c),"status",converter.status()));}
        int end=stage==8?540:stage==2||stage==9?190:stage==3||stage==5||stage==14||stage==15?150:stage==4?100:85;
        if(wait>end){if(stage==12)check(thaumcraft.network.ClientKnowledge.snapshot().scanned(thaumcraft.api.knowledge.PlayerKnowledge.ScanKind.OBJECT).contains("minecraft:diamond"),"cursor-held scanner completes inventory scan");if(stage==3||stage==14||stage==15)check(thaumcraft.api.aspects.Aspects.PRIMALS.stream().anyMatch(a->WandVis.get(mc.player.getMainHandItem(),a)>0),"native charging transferred vis for "+names[stage]);if(stage==11)check(thaumcraft.network.ClientKnowledge.snapshot().scanned(thaumcraft.api.knowledge.PlayerKnowledge.ScanKind.ENTITY).contains("thaumcraft:golem"),"native thaumometer interaction scans golem");if(stage==5){check(tooltipFrames>20,"native charging tooltip rendered repeatedly");check(tooltipBounds.size()==1,"charging tooltip anchor and dimensions remain fixed: "+tooltipBounds);var screen=(AbstractContainerScreen<?>)mc.screen;var w=screen.getMenu().getSlot(1).getItem();check(thaumcraft.items.WandVis.get(w,thaumcraft.api.aspects.Aspects.AIR)>0,"real charger updates vis while tooltip is open");}if(stage==8)check(Files.readString(Round4LiveScene.root().resolve("server-checks.json")).contains("48 essentia"),"three-alembic result recorded");
            if(++stage==Integer.getInteger("thaumcraft.round4.end",names.length)){done=true;Files.writeString(Round4LiveScene.root().resolve("result.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("finished",true,"observations",observations)));mc.stop();}else request();}
    }
    static void moveToSlot(int index)throws Exception{var mc=Minecraft.getInstance();var screen=(AbstractContainerScreen<?>)mc.screen;var left=AbstractContainerScreen.class.getDeclaredField("leftPos");var top=AbstractContainerScreen.class.getDeclaredField("topPos");left.setAccessible(true);top.setAccessible(true);var slot=screen.getMenu().getSlot(index);var move=MouseHandler.class.getDeclaredMethod("onMove",long.class,double.class,double.class);move.setAccessible(true);move.invoke(mc.mouseHandler,mc.getWindow().getWindow(),(left.getInt(screen)+slot.x+8)*mc.getWindow().getGuiScale(),(top.getInt(screen)+slot.y+8)*mc.getWindow().getGuiScale());}
    static void render(){if(done||!prepared)return;try{frames++;boolean capture=stage==4?frames<=26:wait>=40&&(wait/10!=lastShot)&&(stage==2||stage==3||stage==14||stage==15||stage==5||stage==9||wait==60||stage==8&&wait>510);if(!capture)return;lastShot=wait/10;var mc=Minecraft.getInstance();mc.renderBuffers().bufferSource().endBatch();try(var img=Screenshot.takeScreenshot(mc.getMainRenderTarget())){img.writeToFile(Round4LiveScene.root().resolve(String.format(java.util.Locale.ROOT,"%02d-%s-%03d.png",stage,names[stage],stage==4?frames:wait)));}}catch(Throwable t){fail(t);}}
    static void fail(Throwable t){if(done)return;done=true;t.printStackTrace();try{Files.writeString(Round4LiveScene.root().resolve("client-failure.txt"),t+" stage="+stage+" wait="+wait);}catch(Exception ignored){}Minecraft.getInstance().stop();}
}
