package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import thaumcraft.client.*;

/** Captures the actual game renderer and dispatches clicks through Minecraft's MouseHandler. */
final class PhotoLiveClient {
    static int scene=Integer.getInteger("thaumcraft.photo.start",0),wait;static boolean started,done,prepared;static final Set<String> captures=new HashSet<>();static final List<String> checks=new ArrayList<>();
    static Path root(){return PhotoLiveScene.root();}
    static void install(net.neoforged.bus.api.IEventBus bus){
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e)->e.enqueueWork(()->{var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;mc.options.skipMultiplayerWarning=true;mc.options.onboardAccessibility=false;mc.options.languageCode="en_us";mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0d);mc.options.enableVsync().set(false);mc.options.framerateLimit().set(60);mc.options.save();org.lwjgl.glfw.GLFW.glfwSetWindowAttrib(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW,0);}));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{try{tick();}catch(Throwable t){fail(t);}});
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Render.Post e)->{try{capture();}catch(Throwable t){fail(t);}});
        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post e)->{try{if(Minecraft.getInstance().screen==null)capture();}catch(Throwable t){fail(t);}});
    }
    static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks.add(text);try{Files.writeString(root().resolve("client-checks.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(checks));}catch(Exception e){throw new RuntimeException(e);}}
    static void request()throws Exception{var mc=Minecraft.getInstance();if(mc.player!=null)mc.player.closeContainer();mc.setScreen(null);mc.options.setCameraType(CameraType.FIRST_PERSON);wait=0;prepared=false;PhotoLiveScene.signal("request.json",scene);}
    static ResearchNotificationQueue<?> queue()throws Exception{var f=ResearchNotifications.class.getDeclaredField("TEXT");f.setAccessible(true);return (ResearchNotificationQueue<?>)f.get(null);}
    static void click(double x,double y)throws Exception{var mc=Minecraft.getInstance();var w=mc.getWindow();var move=MouseHandler.class.getDeclaredMethod("onMove",long.class,double.class,double.class);move.setAccessible(true);move.invoke(mc.mouseHandler,w.getWindow(),x*w.getScreenWidth()/w.getGuiScaledWidth(),y*w.getScreenHeight()/w.getGuiScaledHeight());var press=MouseHandler.class.getDeclaredMethod("onPress",long.class,int.class,int.class,int.class);press.setAccessible(true);press.invoke(mc.mouseHandler,w.getWindow(),0,1,0);press.invoke(mc.mouseHandler,w.getWindow(),0,0,0);}
    static void tick()throws Exception{
        var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getOverlay()!=null)return;
        if(!started){if(++wait<50)return;started=true;mc.options.guiScale().set(2);mc.options.fov().set(70);mc.options.bobView().set(false);mc.resizeDisplay();request();return;}
        if(Files.exists(root().resolve("server-failure.txt")))throw new AssertionError(Files.readString(root().resolve("server-failure.txt")));
        var ready=root().resolve("ready.json");if(!Files.exists(ready)||Integer.parseInt(Files.readString(ready))!=scene)return;
        wait++;if(scene>=70)FollowupReviewClient.tick(scene,wait);else if(scene>=20)FocusReviewClient.tick(scene,wait);mc.options.keyShift.setDown(scene==13||scene==10&&wait>=60&&wait<85);mc.player.setYRot(scene==11?146:scene==12?-140:scene==15?123:PhotoLiveScene.yaw(scene));mc.player.yRotO=mc.player.getYRot();mc.player.setXRot(PhotoLiveScene.pitch(scene));mc.player.xRotO=mc.player.getXRot();mc.player.setDeltaMovement(Vec3.ZERO);
        if(wait==15){prepared=true;mc.gui.getChat().clearMessages(false);
            if(scene==0){queue().clear();for(int n=0;n<12;n++){ResearchNotifications.text("Gained "+(n+1)+" research point(s) for Aer",thaumcraft.api.aspects.Aspects.AIR);ResearchNotifications.aspect(thaumcraft.api.aspects.Aspects.AIR);}}
            if(scene==1||scene==2){check(mc.screen instanceof TrunkScreen,"native trunk inventory opens with "+(scene==1?3:4)+" rows");var menu=(thaumcraft.menu.TrunkMenu)mc.player.containerMenu;check(menu.trunk.owner()==mc.player,"native trunk owner UUID reaches client for owner caption");}
        if(scene==10){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);var method=net.minecraft.client.player.AbstractClientPlayer.class.getDeclaredMethod("getPlayerInfo");method.setAccessible(true);var info=(net.minecraft.client.multiplayer.PlayerInfo)method.invoke(mc.player);var field=info.getClass().getDeclaredField("skinLookup");field.setAccessible(true);var skin=info.getSkin();var testImage=new com.mojang.blaze3d.platform.NativeImage(64,32,false);for(int py=0;py<32;py++)for(int px=0;px<64;px++)testImage.setPixelRGBA(px,py,0xffffff00);var testCape=net.minecraft.resources.ResourceLocation.parse("thaumcraft_photo_test:cape");mc.getTextureManager().register(testCape,new net.minecraft.client.renderer.texture.DynamicTexture(testImage));field.set(info,(java.util.function.Supplier<net.minecraft.client.resources.PlayerSkin>)()->new net.minecraft.client.resources.PlayerSkin(skin.texture(),skin.textureUrl(),testCape,null,skin.model(),skin.secure()));check(mc.player.getSkin().capeTexture()!=null,"native cape layer has an actual test texture with equipped harness");}
            if(scene==4){var state=mc.level.getBlockState(PhotoLiveScene.O);check(state.getBlock()==thaumcraft.registry.R3Content.INFERNAL_FURNACE.get()&&state.getValue(thaumcraft.production.InfernalFurnaceBlock.PART)==13,"furnace core is lava-model part 13, no structure block");}
            if(scene==12||scene==15){var gs=mc.level.getEntitiesOfClass(thaumcraft.entities.ThaumicGolem.class,new net.minecraft.world.phys.AABB(PhotoLiveScene.O).inflate(6));check(gs.size()==1&&gs.getFirst().core()==11,"native fishing golem carries synced core 11");var rod=mc.getItemRenderer().getModel(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FISHING_ROD),mc.level,gs.getFirst(),0);check(!rod.getQuads(null,null,net.minecraft.util.RandomSource.create()).isEmpty(),"native fishing rod has actual generated item quads");}
            if(scene==11){var stack=new net.minecraft.world.item.ItemStack(thaumcraft.registry.R2Content.TAINTED_SOIL.get());check((mc.getItemColors().getColor(stack,0)&0xffffff)==0x8a21b5,"tainted soil inventory tint is purple");}
            if(scene==13)mc.options.keyShift.setDown(true);
        }
        if(scene==11&&wait==60)mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
        if(scene==8&&(wait==35||wait==75))check(thaumcraft.equipment.FortressArmor.goggles(mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD))==(wait==75),"relay visibility fixture has goggles="+(wait==75));
        if(scene==14&&wait==35){check(mc.screen instanceof thaumcraft.client.ArcaneScreen,"native arcane workbench screen opened");var menu=(thaumcraft.menu.ArcaneMenu)mc.player.containerMenu;check(menu.getSlot(0).hasItem(),"native arcane table previews craftable fractional-vis recipe");check(Math.abs(thaumcraft.items.WandVis.discount(mc.player)-.05f)<.0001,"actual client has robe discount of five percent for fractional UI cost");}
        if(scene==13&&wait==35){Files.writeString(root().resolve("wand-client.txt"),mc.player.getMainHandItem().save(mc.level.registryAccess()).toString());check(thaumcraft.items.WandVis.get(mc.player.getMainHandItem(),thaumcraft.api.aspects.Aspects.AIR)==237,"client receives exactly 2.37 stored vis");}
        if(scene==0&&(wait==47||wait==67||wait==75)){var f=ResearchNotifications.class.getDeclaredField("LEAVING");f.setAccessible(true);var leaving=(java.util.List<?>)f.get(null);check(wait==75?leaving.isEmpty():!leaving.isEmpty(),"native notification slide remains visible mid-animation and ends on time, tick="+wait);}
        if(scene==0){
            if(wait==35)mc.setScreen(new ChatScreen(""));
            if(wait==45){int sw=mc.getWindow().getGuiScaledWidth(),sh=mc.getWindow().getGuiScaledHeight()-14;check(queue().rows().size()==8&&queue().remaining()==4,"twelve notifications preserve visible/pending FIFO");click(sw-mc.font.width("Gained 4 research point(s) for Aer")*.5-18,sh-14-3*8+3);check(queue().rows().size()==8&&queue().remaining()==3,"native chat X click dismisses exactly one row and refills from pending");}
            if(wait==65){int sw=mc.getWindow().getGuiScaledWidth(),sh=mc.getWindow().getGuiScaledHeight()-14;click(sw-20,sh-8*queue().rows().size()-12);check(queue().rows().isEmpty()&&queue().remaining()==0,"native chat Clean all click clears visible and pending notifications");}
        }
        if((scene==1||scene==2)&&wait==55){var s=mc.screen;click((s.width-176)/2+116,(s.height-200)/2+5);}
        if(scene==8||scene==9){if(wait==25)check(mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&hit.getBlockPos().equals(PhotoLiveScene.O),"native crosshair targets actual vis relay, wand="+(scene==9));}
        if(scene==10){mc.player.setYRot(wait>=60?55:0);mc.player.yRotO=mc.player.getYRot();mc.player.setYBodyRot(0);mc.player.yBodyRotO=0;if(wait==60)mc.player.setShiftKeyDown(true);if(wait==85)mc.player.setShiftKeyDown(false);}
        if(scene==129&&!Files.exists(root().resolve("natural-spawn-result.json")))return;
        if(wait>(scene>=120?320:scene==3?125:scene==10?105:scene>=26&&(scene<=29||scene>=36&&scene<=41)?185:85)){
            mc.options.keyShift.setDown(false);mc.player.setShiftKeyDown(false);if((scene=nextScene(scene))==Integer.getInteger("thaumcraft.photo.end",16)){done=true;Files.writeString(root().resolve("result.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("finished",true,"muted",true,"checks",checks)));mc.stop();}else request();
        }
    }
    static int nextScene(int current){var configured=System.getProperty("thaumcraft.photo.scenes","");if(configured.isEmpty())return current+1;var scenes=Arrays.stream(configured.split(",")).map(Integer::parseInt).toList();int next=scenes.indexOf(current)+1;return next==scenes.size()?Integer.getInteger("thaumcraft.photo.end",16):scenes.get(next);}
    static void capture()throws Exception{
        if(done||!prepared)return;
        boolean take=scene==0?Set.of(15,16,20,28,40,44,46,47,50,64,66,67,70,75).contains(wait):scene==1||scene==2?wait==40||wait==75:scene==3?wait==30||wait==60:scene==10?wait==40||wait==75||wait==95:wait==50||scene==11&&wait==70||scene==8&&wait==75;
        if(scene>=26&&(scene<=29||scene>=36))take=java.util.Set.of(25,35,50,65,90,130,170).contains(wait);if(scene==108)take=wait>=26&&wait<=64;if(scene>=120)take=java.util.Set.of(150,250,310).contains(wait);if(!take)return;String name=String.format("%02d-tick-%03d",scene,wait);if(!captures.add(name))return;var mc=Minecraft.getInstance();mc.renderBuffers().bufferSource().endBatch();try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(root().resolve(name+".png"));}
    }
    static void fail(Throwable t){if(done)return;done=true;t.printStackTrace();try{Files.writeString(root().resolve("client-failure.txt"),t+" scene="+scene+" wait="+wait);}catch(Exception ignored){}Minecraft.getInstance().stop();}
    private PhotoLiveClient(){}
}
