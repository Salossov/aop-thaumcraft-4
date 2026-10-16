package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import thaumcraft.client.GolemDebugOverlay;

final class GolemDebugLiveClient {
    static long fpsFrames,fpsStart;
    static int stage=Integer.getInteger("thaumcraft.debug.start",0),wait;static final Set<String> primalCosts=new HashSet<>(),tooltipCosts=new HashSet<>();static boolean started,enabled,checked,done;static final List<Object> checks=new ArrayList<>();
    static final String[] reasons={"moving","partial_path","source_empty","no_markers","no_essentia_destination","no_ai","no_core","source_empty","no_ai","no_ai","no_ai","no_ai","no_ai","no_ai","no_ai","no_ai","no_ai","moving","working","working","no_water","reaction","reaction","reaction","reaction","reaction","reaction","reaction","reaction","no_ai"};
    static void install(net.neoforged.bus.api.IEventBus bus){
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e)->e.enqueueWork(()->{var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;mc.options.enableVsync().set(false);mc.options.framerateLimit().set(Integer.getInteger("thaumcraft.debug.fps",60));mc.options.skipMultiplayerWarning=true;mc.options.onboardAccessibility=false;mc.options.languageCode="ru_ru";mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0d);mc.options.save();org.lwjgl.glfw.GLFW.glfwSetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW,0);}));
        NeoForge.EVENT_BUS.addListener((RenderTooltipEvent.Pre e)->{if(stage==8&&e.getItemStack().is(thaumcraft.registry.R3Equipment.FOCI.get("primal").get()))tooltipCosts.add(((thaumcraft.items.ThaumicFocusItem)e.getItemStack().getItem()).getVisCost(e.getItemStack()).toString());});
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{try{tick();}catch(Throwable t){fail(t);}});
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Render.Post e)->{try{render();}catch(Throwable t){fail(t);}});
        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post e)->{try{if(Minecraft.getInstance().screen==null)render();}catch(Throwable t){fail(t);}});
    }
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks.add(message);}
    static void request()throws Exception{wait=0;enabled=checked=false;Files.writeString(GolemDebugLiveScene.root().resolve("request.json"),Integer.toString(stage));}
    static void tick()throws Exception{
        var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getOverlay()!=null)return;
        if(!started){if(++wait<40)return;started=true;mc.options.guiScale().set(2);mc.options.fov().set(70);mc.options.bobView().set(false);mc.resizeDisplay();request();return;}
        var failure=GolemDebugLiveScene.root().resolve("server-failure.txt");if(Files.exists(failure))throw new AssertionError(Files.readString(failure));
        var ready=GolemDebugLiveScene.root().resolve("ready.json");if(!Files.exists(ready)||Integer.parseInt(Files.readString(ready))!=stage)return;
        wait++;if(wait==35)mc.gui.getChat().clearMessages(false);mc.player.setYRot(stage>=9&&stage<=14?140:180);mc.player.yRotO=mc.player.getYRot();mc.player.setXRot(stage==29?0:stage>=15?30:stage>=9?20:16);mc.player.xRotO=mc.player.getXRot();mc.player.setDeltaMovement(Vec3.ZERO);
        if(!enabled&&wait>=5){enabled=true;mc.player.connection.sendCommand("thaumcraft debug golems on");}
        if(stage==7&&wait==20){mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));for(int slot=9;slot<13;slot++){var stack=mc.player.getInventory().getItem(slot);var model=mc.getItemRenderer().getModel(stack,mc.level,mc.player,0);var quads=model.getQuads(null,null,net.minecraft.util.RandomSource.create(0));checks.add(Map.of("item",stack.getDescriptionId(),"color",Integer.toHexString(mc.getItemColors().getColor(stack,0)),"tintIndices",quads.stream().map(q->q.getTintIndex()).distinct().toList()));}}
        if(stage==8&&wait==20)mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
        if(stage==8&&wait>=25&&wait<=65){Round4LiveClient.moveToSlot(9);var focus=mc.player.getInventory().getItem(9);var item=(thaumcraft.items.ThaumicFocusItem)focus.getItem();primalCosts.add(item.getVisCost(focus).toString());}
        if(stage>=9&&stage<=14&&wait>=20&&wait%10==0){var bore=(thaumcraft.production.ArcaneBoreBlockEntity)mc.level.getBlockEntity(GolemDebugLiveScene.O.above());if(bore.target()!=null){var axis=bore.aim(1).forward();var desired=bore.target().getCenter().subtract(bore.getBlockPos().getCenter()).normalize();if(wait>=50)check(axis.dot(desired)>.99,"bore emitter axis aligns with target: "+bore.facing());checks.add(Map.of("stage",stage,"progress",bore.miningProgress(.5f),"axis",axis.toString(),"target",bore.target().toString(),"spin",bore.spin(.5f)));}}
        if((stage==15||stage==16)&&wait==60){var relay=(thaumcraft.visnet.VisRelayBlockEntity)mc.level.getBlockEntity(GolemDebugLiveScene.O);check((relay.getRootSource()!=null)==(stage==15),"relay real connection state "+stage);check(mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&hit.getBlockPos().equals(GolemDebugLiveScene.O),"relay HUD actually targeted");}
        if((stage==7||stage==8)&&wait==30)check(mc.screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen,"actual survival inventory screen displayed");
        var entries=GolemDebugOverlay.entries();
        if(!checked&&wait>=15&&!entries.isEmpty()){
            var entry=entries.getFirst();boolean expected=stage>=21||stage==17||entry.reason().equals(reasons[stage])||stage==1&&entry.reason().equals("no_path");
            if(expected){checked=true;check(true,"received actual server AI reason: "+entry.reason()+" stage "+stage);checks.add(Map.of("stage",stage,"lines",entry.lines().stream().map(c->c.getString()).toList(),"nodes",entry.path().size(),"target",String.valueOf(entry.target())));
                if(stage==0){check(entry.target()!=null&&entry.path().size()>1,"real server route and work target reached native client");check(entry.target().y>entry.home().getY(),"elevated source target differs from walking approach");}
            }
        }
        if(wait==70){if(stage==8){check(primalCosts.size()>2,"primal tooltip costs change over live 200 ms windows");check(tooltipCosts.size()>2,"actual hovered primal tooltip rendered multiple cost windows");}check(checked,"scene diagnostic appeared stage "+stage);mc.player.connection.sendCommand("thaumcraft debug golems off");}
        if(wait==80){check(GolemDebugOverlay.entries().isEmpty(),"off command clears world debug immediately stage "+stage);mc.player.connection.sendCommand("thaumcraft debug golems");}
        if(wait==90)check(!GolemDebugOverlay.entries().isEmpty(),"bare command toggles debug back on stage "+stage);
        if(wait>(stage==17?135:stage>=9&&stage<=14?175:95)){mc.setScreen(null);if(++stage==Integer.getInteger("thaumcraft.debug.end",reasons.length)){done=true;Files.writeString(GolemDebugLiveScene.root().resolve("result.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("finished",true,"muted",mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).get()==0,"checks",checks,"targetFps",Integer.getInteger("thaumcraft.debug.fps",60),"achievedFps",fpsFrames/((System.nanoTime()-fpsStart)/1e9))));mc.stop();}else request();}
    }
    static void render()throws Exception{
        if(!done&&started&&Minecraft.getInstance().level!=null){if(fpsStart==0)fpsStart=System.nanoTime();fpsFrames++;}
        if(done||wait!=40&&wait!=60)return;var mc=Minecraft.getInstance();if(mc.level==null||!checked)return;var path=GolemDebugLiveScene.root().resolve(stage+"-"+reasons[stage]+"-"+wait+".png");if(Files.exists(path))return;
        mc.renderBuffers().bufferSource().endBatch();try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(path);}
    }
    static void fail(Throwable t){if(done)return;done=true;t.printStackTrace();try{Files.writeString(GolemDebugLiveScene.root().resolve("client-failure.txt"),t+" stage="+stage+" wait="+wait);}catch(Exception ignored){}Minecraft.getInstance().stop();}
}
