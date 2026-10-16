package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class ModelAuditClient {
    private static final List<JsonObject> captures=new ArrayList<>();private static List<JsonObject> scenes;private static int index,angle,wait,frames,ticks,menuTicks,revision;private static boolean ready,started,done,connecting;private static JsonObject prepared;
    private static List<Integer> recapture;private static int recapturePosition;
    private static net.minecraft.client.player.RemotePlayer cameraEntity;
    private static final class CaptureCamera extends net.minecraft.client.player.RemotePlayer {
        CaptureCamera(net.minecraft.client.multiplayer.ClientLevel level,com.mojang.authlib.GameProfile profile){super(level,profile);}
        @Override public float getViewYRot(float partial){return getYRot();}
        @Override public float getViewXRot(float partial){return getXRot();}
    }
    private static Path out(){return ModelAuditScene.root().resolve(System.getProperty("thaumcraft.modelAudit.output","port"));}
    static void install(){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ScreenEvent.Render.Post e)->{try{menu(e);}catch(Exception failure){throw new RuntimeException(failure);}});net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{try{tick(e);}catch(Exception failure){throw new RuntimeException(failure);}});}
    @SubscribeEvent public static void menu(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post event)throws Exception{
        if(!"model-audit-live".equals(System.getProperty("thaumcraft.r3.phase"))||started||connecting)return;var mc=Minecraft.getInstance();if(mc.player!=null||mc.getOverlay()!=null||++menuTicks<120)return;
        Files.createDirectories(out());Files.writeString(out().resolve("startup-screen.txt"),event.getScreen().getClass().getName());try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out().resolve("startup-screen.png"));}
        connecting=true;mc.execute(()->net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(),mc,net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("127.0.0.1:25584"),new net.minecraft.client.multiplayer.ServerData("Model audit","127.0.0.1:25584",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null));
    }
    private static void request()throws Exception{ready=false;wait=frames=0;var request=new JsonObject();request.addProperty("index",index);Files.writeString(ModelAuditScene.root().resolve("port-request.tmp"),request.toString());Files.move(ModelAuditScene.root().resolve("port-request.tmp"),ModelAuditScene.root().resolve("port-request.json"),StandardCopyOption.REPLACE_EXISTING);}
    private static void report(Throwable error)throws Exception{var r=new JsonObject();r.addProperty("finished",done&&error==null&&scenes!=null&&index>=scenes.size());r.addProperty("total_scenes",scenes==null?0:scenes.size());captures.sort(Comparator.comparingInt((JsonObject c)->c.get("index").getAsInt()).thenComparingInt(c->c.get("angle").getAsInt()));r.add("captures",new Gson().toJsonTree(captures));if(error!=null)r.addProperty("error",error.toString());Files.writeString(out().resolve("native-report.json"),new GsonBuilder().setPrettyPrinting().create().toJson(r));}
    private static void resume()throws Exception{var path=out().resolve("native-report.json");if(!Files.exists(path))return;var old=JsonParser.parseString(Files.readString(path)).getAsJsonObject();var saved=old.getAsJsonArray("captures");var selected=ModelAuditScene.root().resolve("port-recapture.json");if(Files.exists(selected)){recapture=JsonParser.parseString(Files.readString(selected)).getAsJsonArray().asList().stream().map(JsonElement::getAsInt).toList();if(recapture.isEmpty()||!old.get("finished").getAsBoolean()||saved.size()!=scenes.size()*4)throw new IllegalStateException("Recapture needs a complete baseline");for(var element:saved){var c=element.getAsJsonObject();if(!recapture.contains(c.get("index").getAsInt()))captures.add(c.deepCopy());}index=recapture.get(0);revision=3;return;}int complete=Math.min(25,saved.size()/4);for(int i=0;i<complete*4;i++){var capture=saved.get(i).getAsJsonObject();if(capture.get("index").getAsInt()!=i/4||capture.get("angle").getAsInt()!=i%4||!capture.has("revision")||capture.get("revision").getAsInt()!=3||!capture.has("yaw")||Math.abs(net.minecraft.util.Mth.wrapDegrees(capture.get("yaw").getAsFloat()-(135-i%4*90)))>.05)return;}for(int i=0;i<complete*4;i++)captures.add(saved.get(i).getAsJsonObject().deepCopy());index=complete;revision=3;report(null);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e)throws Exception{
        if(!"model-audit-live".equals(System.getProperty("thaumcraft.r3.phase"))||done)return;var mc=Minecraft.getInstance();if(mc.getOverlay()!=null)return;
        if(mc.player==null||mc.level==null)return;
        try{
            if(!started){if(++ticks<30)return;started=true;Files.createDirectories(out().resolve("screenshots"));scenes=JsonParser.parseString(Files.readString(ModelAuditScene.root().resolve("port-manifest.json"))).getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).toList();org.lwjgl.glfw.GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),960,720);mc.options.guiScale().set(1);mc.options.fov().set(70);mc.options.gamma().set(1d);mc.options.bobView().set(false);mc.options.hideGui=true;mc.resizeDisplay();mc.getToasts().clear();mc.setScreen(new Inset());request();return;}
            if(cameraEntity==null){thaumcraft.registry.R2Content.creative((stack,visibility)->{if(stack.is(thaumcraft.registry.R2Content.MAGIC_BOX.get()))throw new IllegalStateException("Magic Box must stay hidden from creative");});cameraEntity=new CaptureCamera(mc.level,mc.player.getGameProfile());mc.setCameraEntity(cameraEntity);resume();request();return;}
            var readyFile=ModelAuditScene.root().resolve("port-ready.json");if(!Files.exists(readyFile))return;try{prepared=JsonParser.parseString(Files.readString(readyFile)).getAsJsonObject();}catch(java.nio.file.NoSuchFileException|com.google.gson.JsonParseException transientWrite){return;}if(prepared.get("index").getAsInt()!=index)return;if(wait==0&&angle==0)mc.particleEngine.setLevel(mc.level);
            var tunePath=ModelAuditScene.root().resolve("camera-tune.json");var tune=Files.exists(tunePath)?JsonParser.parseString(Files.readString(tunePath)).getAsJsonObject():new JsonObject();int changed=tune.has("revision")?tune.get("revision").getAsInt():0;if(changed!=revision){revision=changed;index=angle=0;captures.clear();request();return;}
            var scene=scenes.get(index);String legacy=scene.get("legacy").getAsString();double y=legacy.equals("blockArcaneDoor")||legacy.equals("blockEssentiaReservoir")?65.45:65,dist=3.8;if(legacy.equals("blockAlchemyFurnace")||scene.get("group").getAsString().equals("assembled")&&legacy.equals("blockArcaneFurnace")){y=65.5;dist=8;}
            double theta=Math.toRadians(45+angle*90),x=.5+Math.sin(theta)*dist,z=.5+Math.cos(theta)*dist,feet=y-.3-cameraEntity.getEyeHeight()+(tune.has("feetOffset")?tune.get("feetOffset").getAsDouble():0);float pitch=tune.has("pitch")?tune.get("pitch").getAsFloat():18;cameraEntity.setPos(x,feet,z);cameraEntity.xo=cameraEntity.xOld=x;cameraEntity.yo=cameraEntity.yOld=feet;cameraEntity.zo=cameraEntity.zOld=z;cameraEntity.setYRot(135-angle*90);cameraEntity.yRotO=cameraEntity.getYRot();cameraEntity.setXRot(pitch);cameraEntity.xRotO=pitch;cameraEntity.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).copy());mc.setCameraEntity(cameraEntity);mc.player.setInvisible(true);mc.options.fovEffectScale().set(0d);
            if(index==18||index==36||index==150||index==151)mc.level.getBlockState(ModelAuditScene.CENTER).getBlock().animateTick(mc.level.getBlockState(ModelAuditScene.CENTER),mc.level,ModelAuditScene.CENTER,mc.level.random);if(++wait>=(index==20&&angle==0?120:index==0&&angle==0?80:12))ready=true;
        }catch(Throwable error){error.printStackTrace();done=true;report(error);mc.stop();}
    }
    private static final class Inset extends Screen {
        Inset(){super(Component.literal("Native model reference comparison"));}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){if(scenes==null||index>=scenes.size())return;var mc=Minecraft.getInstance();var scene=scenes.get(index);var item=BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:"+scene.get("modern").getAsString()));
            g.fill(780,520,952,712,0xe0202020);g.drawString(font,"Native inventory",795,530,0xffffffff,false);if(item!=Items.AIR){g.pose().pushPose();g.pose().translate(810,555,0);g.pose().scale(7,7,7);var stack=new ItemStack(item);if(item instanceof thaumcraft.items.AncientLootBlockItem loot)stack=thaumcraft.items.AncientLootBlockItem.stack((thaumcraft.blocks.AncientLootBlock)loot.getBlock(),scene.get("metadata").getAsInt());String legacy=scene.get("legacy").getAsString();if(legacy.startsWith("blockCosmeticDoubleSlab"))stack.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,new net.minecraft.world.item.component.BlockItemStateProperties(java.util.Map.of("type","double")));if(legacy.equals("blockCosmeticSolid")&&scene.get("metadata").getAsInt()==10)stack.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,new net.minecraft.world.item.component.BlockItemStateProperties(java.util.Map.of("powered","true")));if(legacy.equals("blockMetalDevice")&&scene.get("metadata").getAsInt()==6)stack.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,new net.minecraft.world.item.component.BlockItemStateProperties(java.util.Map.of("open","false")));if(!(legacy.equals("blockAiry")&&scene.get("metadata").getAsInt()==12))g.renderItem(stack,0,0);g.pose().popPose();}g.flush();
            if(!ready||done||++frames<3)return;
            try{
                if(mc.gameRenderer.getMainCamera().getPosition().distanceTo(cameraEntity.getEyePosition())>.05||Math.abs(net.minecraft.util.Mth.wrapDegrees(mc.gameRenderer.getMainCamera().getYRot()-cameraEntity.getYRot()))>.05)throw new IllegalStateException("Capture camera has not reached its requested pose");String name=String.format("%03d_%s_%02d_view%d.png",index,scene.get("legacy").getAsString(),scene.get("metadata").getAsInt(),angle);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out().resolve("screenshots").resolve(name));}
                var c=new JsonObject();c.addProperty("index",index);c.addProperty("angle",angle);c.addProperty("file","screenshots/"+name);c.add("prepared",prepared.deepCopy());var camera=mc.gameRenderer.getMainCamera();c.addProperty("camera",camera.getPosition().toString());c.addProperty("pitch",camera.getXRot());c.addProperty("yaw",camera.getYRot());c.addProperty("revision",revision);captures.add(c);report(null);ready=false;wait=frames=0;
                if(++angle>=4){angle=0;if(recapture!=null){index=++recapturePosition<recapture.size()?recapture.get(recapturePosition):scenes.size();}else ++index;if(index>=scenes.size()){done=true;report(null);mc.stop();}else request();}
            }catch(Throwable error){error.printStackTrace();done=true;try{report(error);}catch(Exception ignored){}mc.stop();}
        }
    }
}
