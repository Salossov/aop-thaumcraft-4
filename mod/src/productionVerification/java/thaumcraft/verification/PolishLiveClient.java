package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import thaumcraft.client.*;
import thaumcraft.visnet.NodeConverterBlockEntity;

/** Native screens and GPU captures on a dedicated server; records achieved frame rates separately. */
final class PolishLiveClient {
    private static final int[] SCENES={0,10,2,3,2,3,2,3,20};
    private static int step,wait,frames,firstFrameTick;
    private static boolean started,done,prepared;
    private static long frameStart,hudStart;
    private static double reached=-1;
    private static float halfSecond=-1;
    private static final Set<String> captures=new HashSet<>();
    private static final List<Object> checks=new ArrayList<>();
    private static java.lang.reflect.Field tagScale;
    private static Path root(){return Round4LiveScene.root();}
    private static int scene(){return SCENES[step];}
    private static int fps(){return step<2?60:step<4?30:step<6?60:step<8?144:60;}
    static void install(net.neoforged.bus.api.IEventBus bus){
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event)->event.enqueueWork(()->{
            var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;mc.options.skipMultiplayerWarning=true;mc.options.onboardAccessibility=false;mc.options.languageCode="en_us";
            mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0d);mc.options.enableVsync().set(false);mc.options.save();
            org.lwjgl.glfw.GLFW.glfwSetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW,0);org.lwjgl.glfw.GLFW.glfwShowWindow(mc.getWindow().getWindow());
        }));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event)->{try{tick();}catch(Throwable failure){fail(failure);}});
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Render.Post event)->{try{render();}catch(Throwable failure){fail(failure);}});
        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post event)->{try{if(Minecraft.getInstance().screen==null)render();}catch(Throwable failure){fail(failure);}});
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event)->{
            if(!started)return;
            try{var field=GogglesAspectOverlay.class.getDeclaredField("world");field.setAccessible(true);boolean cleared=field.get(null)==null&&tagScale.getFloat(null)==0;Files.writeString(root().resolve("logout-check.json"),"{\"passed\":"+cleared+"}");if(!cleared)throw new AssertionError("HUD retains a disconnected world");}catch(Exception failure){throw new RuntimeException(failure);}
        });
    }
    private static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks.add(text);}
    private static List<EditBox> boxes(){return Minecraft.getInstance().screen.children().stream().filter(w->w instanceof EditBox).map(w->(EditBox)w).toList();}
    private static List<Button> buttons(){return Minecraft.getInstance().screen.children().stream().filter(w->w instanceof Button).map(w->(Button)w).toList();}
    private static void request()throws Exception{
        var mc=Minecraft.getInstance();mc.setScreen(null);mc.options.setCameraType(CameraType.FIRST_PERSON);mc.options.framerateLimit().set(fps());wait=frames=0;frameStart=hudStart=0;reached=-1;halfSecond=-1;prepared=false;
        Files.writeString(root().resolve("request.json"),Integer.toString(scene()));
    }
    private static void tick()throws Exception{
        var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getOverlay()!=null)return;
        if(!started){if(++wait<50)return;started=true;mc.options.guiScale().set(2);mc.options.fov().set(70);mc.options.bobView().set(false);mc.options.hideGui=false;mc.resizeDisplay();tagScale=GogglesAspectOverlay.class.getDeclaredField("tagScale");tagScale.setAccessible(true);request();return;}
        var failure=root().resolve("server-failure.txt");if(Files.exists(failure))throw new AssertionError(Files.readString(failure));
        var ready=root().resolve("ready.json");if(!Files.exists(ready)||Integer.parseInt(Files.readString(ready))!=scene())return;
        wait++;mc.player.setYRot(scene()==2?156:180);mc.player.yRotO=mc.player.getYRot();mc.player.setXRot(scene()==3?1.5f:8);mc.player.xRotO=mc.player.getXRot();mc.player.setDeltaMovement(Vec3.ZERO);
        if(wait==20){prepared=true;mc.gui.getChat().clearMessages(false);
            if(scene()==0){
                check(mc.screen instanceof NodeEditorScreen,"native node editor opened by the server command");
                for(var box:boxes())if(box.getWidth()==40)check(box.getValue().isEmpty(),"zero aspect field stays empty");
                var amount=boxes().get(3);amount.setValue("01250");check(amount.getValue().equals("1250"),"native amount input normalizes 01250 to 1250");
                boxes().getFirst().setValue("");
            }
            if(scene()==20){mc.setScreen(new InventoryScreen(mc.player));for(int i=9;i<=11;i++){var stack=mc.player.getInventory().getItem(i);check((mc.getItemColors().getColor(stack,0)&0xffffff)==6961280,"void robe inventory layer is fixed purple: "+stack.getDescriptionId());check(!stack.has(net.minecraft.core.component.DataComponents.DYED_COLOR),"void robe has no ignored dye component");}Round4LiveClient.moveToSlot(10);}
        }
        if(scene()==0){
            if(wait==22){check(!buttons().getLast().active,"blank coordinate disables Create");buttons().getLast().onPress();check(mc.screen instanceof NodeEditorScreen,"blank coordinate callback cannot submit stale position");boxes().getFirst().setValue("-");}
            if(wait==24){check(!buttons().getLast().active,"unfinished negative coordinate disables Create");buttons().getLast().onPress();check(mc.screen instanceof NodeEditorScreen,"unfinished coordinate callback cannot submit");boxes().getFirst().setValue("17");}
            if(wait==26){check(buttons().getLast().active,"valid coordinates and a positive aspect enable Create");var type=buttons().get(2);mc.screen.mouseClicked(type.getX()+5,type.getY()+5,0);mc.screen.keyPressed(264,0,0);mc.screen.keyPressed(257,0,0);}
            if(wait==30){var modifier=buttons().get(3);mc.screen.mouseClicked(modifier.getX()+5,modifier.getY()+5,0);mc.screen.keyPressed(264,0,0);mc.screen.keyPressed(257,0,0);check(boxes().getFirst().getValue().equals("17")&&boxes().get(3).getValue().equals("1250"),"dropdown selection preserves coordinates and normalized amounts");}
            if(wait==55){var type=buttons().get(2);mc.screen.mouseClicked(type.getX()+5,type.getY()+5,0);}
        }
        if(scene()==3&&wait==25){check(mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&hit.getBlockPos().equals(Round4LiveScene.O.above()),"HUD aims at the actual node at target FPS "+fps());tagScale.setFloat(null,0);hudStart=System.nanoTime();}
        if(scene()==2&&wait>=40&&wait%5==0){var tile=(NodeConverterBlockEntity)mc.level.getBlockEntity(Round4LiveScene.O.above());float a=tile.progress(0),b=tile.progress(.5f),c=tile.progress(1);check(Math.abs(b-(a+c)/2)<.001&&Math.abs(c-a)<1.01,"converter interpolates within one native tick at target FPS "+fps()+", tick "+wait);}
        if(scene()==10&&wait==40){check(mc.level.getBlockState(Round4LiveScene.O.below()).is(thaumcraft.registry.R3Content.NODE_STABILIZER.get()),"native construction retains stabilizer below node");check(mc.level.getBlockState(Round4LiveScene.O.above()).is(thaumcraft.registry.R3Content.NODE_CONVERTER.get()),"native construction retains converter above node");}
        if(scene()==20&&wait==65){mc.setScreen(null);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);}
        if(wait>(scene()==0?80:scene()==20?100:scene()==2?140:90)){
            if(scene()==3){check(reached>150&&reached<750&&tagScale.getFloat(null)>.26,"HUD reaches its calibrated size in real elapsed time at target FPS "+fps());checks.add(Map.of("targetFps",fps(),"achievedFps",frames/((System.nanoTime()-frameStart)/1e9),"hudReachScale024Ms",reached,"scaleNear500ms",halfSecond));}
            if(++step==SCENES.length){done=true;Files.writeString(root().resolve("result.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("finished",true,"muted",mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).get()==0,"checks",checks)));mc.stop();}else request();
        }
    }
    private static void render()throws Exception{
        if(done||!prepared)return;long now=System.nanoTime();if(frameStart==0)frameStart=now;frames++;
        if(scene()==3&&hudStart!=0){float scale=tagScale.getFloat(null);double elapsed=(now-hudStart)/1e6;if(reached<0&&scale>=.24)reached=elapsed;if(halfSecond<0&&elapsed>=500)halfSecond=scale;}
        boolean capture=scene()==0?wait==40||wait==60:scene()==20?wait==45||wait==85:scene()==2?wait==80||wait==90||wait==120:wait==60;
        if(!capture)return;String name=step+"-scene-"+scene()+"-fps-"+fps()+"-tick-"+wait;if(!captures.add(name))return;
        var mc=Minecraft.getInstance();mc.renderBuffers().bufferSource().endBatch();try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(root().resolve(name+".png"));}
    }
    private static void fail(Throwable failure){if(done)return;done=true;failure.printStackTrace();try{Files.writeString(root().resolve("client-failure.txt"),failure+" step="+step+" wait="+wait);Files.writeString(root().resolve("partial-checks.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(checks));}catch(Exception ignored){}Minecraft.getInstance().stop();}
}
