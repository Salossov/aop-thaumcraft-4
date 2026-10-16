package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Drives the real creative screen in an isolated LAN test world. */
@EventBusSubscriber(modid="thaumcraft_r3_test",value=Dist.CLIENT)
public final class ThirdRoundLiveClient {
    private static int ticks;private static boolean windSound;private static CreativeModeInventoryScreen screen;private static final List<String> checks=new ArrayList<>();
    @SubscribeEvent public static void sound(net.neoforged.neoforge.client.event.sound.PlaySoundEvent event){if(event.getSound()!=null&&event.getSound().getLocation().toString().equals("thaumcraft:wind"))windSound=true;}
    private static void check(boolean pass,String message){if(!pass)throw new AssertionError(message);checks.add(message);}
    private static java.lang.reflect.Field field(Class<?> type,String name)throws Exception{var result=type.getDeclaredField(name);result.setAccessible(true);return result;}
    private static void refresh()throws Exception{var method=CreativeModeInventoryScreen.class.getDeclaredMethod("refreshSearchResults");method.setAccessible(true);method.invoke(screen);}
    private static long wands(){return screen.getMenu().items.stream().filter(s->s.getItem() instanceof thaumcraft.items.CastingWandItem).count();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(!"round-three-live".equals(System.getProperty("thaumcraft.r3.phase")))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getOverlay()!=null||!mc.gameMode.hasInfiniteItems())return;
        try{
            if(++ticks==25){org.lwjgl.glfw.GLFW.glfwRestoreWindow(mc.getWindow().getWindow());org.lwjgl.glfw.GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),1050,700);mc.resizeDisplay();var tab=net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:thaumcraft"));check(tab.hasSearchBar(),"Thaumcraft tab exposes the native creative search box");field(CreativeModeInventoryScreen.class,"selectedTab").set(null,tab);screen=new CreativeModeInventoryScreen(mc.player,mc.level.enabledFeatures(),true);mc.setScreen(screen);check(wands()==3,"native creative screen starts with three collapsed wand-kind groups");}
            if(ticks==35){var search=(EditBox)field(CreativeModeInventoryScreen.class,"searchBox").get(screen);search.setValue("zzzz-no-such-item");refresh();check(screen.getMenu().items.isEmpty(),"native search rejects nonexistent names");search.setValue("Silverwood");refresh();check(screen.getMenu().items.stream().anyMatch(s->s.getItem() instanceof thaumcraft.items.CastingWandItem&&thaumcraft.items.WandMaterials.parts(s).rod().contains("silverwood")),"native search finds material-specific wand variants");search.setValue("");refresh();
                int index=0;while(!(screen.getMenu().items.get(index).getItem() instanceof thaumcraft.items.CastingWandItem))index++;int rows=Math.max(1,(screen.getMenu().items.size()+8)/9-5);float scroll=Math.min(1,(index/9)/(float)rows);field(CreativeModeInventoryScreen.class,"scrollOffs").setFloat(screen,scroll);screen.getMenu().scrollTo(scroll);int left=(int)field(net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class,"leftPos").get(screen),top=(int)field(net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class,"topPos").get(screen);var slot=screen.getMenu().slots.stream().limit(45).filter(s->s.getItem().getItem() instanceof thaumcraft.items.CastingWandItem).findFirst().orElseThrow();long before=wands();screen.mouseClicked(left+slot.x+8,top+slot.y+8,1);screen.mouseReleased(left+slot.x+8,top+slot.y+8,1);check(wands()>before,"real RMB expands a group into its component-preserving item stacks");check(screen.getMenu().getCarried().isEmpty(),"group RMB leaves the creative cursor empty");
                check(screen.getMenu().slots.stream().limit(45).anyMatch(s->s.getItem().getItem() instanceof thaumcraft.items.CastingWandItem),"expanded group remains visible at the clicked scroll position");var mode=(net.minecraft.client.gui.components.Button)field(CreativeModeInventoryScreen.class,"tcGroupButton").get(screen);for(int i=0;i<3;i++)mode.onPress();check(wands()>100,"group mode switch exposes all original variants");mode.onPress();check(wands()==3,"returning to type mode collapses the groups");search.setValue("capped");refresh();check(wands()==3,"search groups all matching capped implements by kind");var first=screen.getMenu().slots.stream().limit(45).filter(s->s.getItem().getItem() instanceof thaumcraft.items.CastingWandItem).findFirst().orElseThrow();screen.mouseClicked(left+first.x+8,top+first.y+8,1);screen.mouseReleased(left+first.x+8,top+first.y+8,1);check(wands()>3,"filtered group expands through real RMB input");
                for(int i=0;i<12;i++)thaumcraft.client.ResearchNotifications.text("Queue test "+i,0xffffff);
            }
            if(ticks==45){var root=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence");try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(root.resolve("round-three-creative.png"));}check(org.lwjgl.opengl.GL11.glGetError()==0,"live creative screen renders without GL errors");}
            if(ticks==55){mc.setScreen(null);mc.player.connection.sendCommand("give @s thaumcraft:elemental_sword");}
            if(ticks==70){check(mc.player.getMainHandItem().is(thaumcraft.registry.R3Equipment.TOOLS.get("elemental_sword").get()),"native give command delivered Zephyr sword to the held slot");mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND);}
            if(ticks==95){check(mc.player.isUsingItem()&&windSound,"holding RMB triggers Zephyr use and the server wind sound in the real client");var particles=(Map<?,?>)field(net.minecraft.client.particle.ParticleEngine.class,"particles").get(mc.particleEngine);check(particles.values().stream().anyMatch(value->((Collection<?>)value).stream().anyMatch(p->p.getClass().getSimpleName().equals("WindSpiral"))),"native particle engine contains the Zephyr spiral during RMB use");var root=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence");try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(root.resolve("round-three-zephyr.png"));}check(org.lwjgl.opengl.GL11.glGetError()==0,"Zephyr smoke spiral renders without GL errors");mc.options.keyUse.setDown(false);mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);var result=new LinkedHashMap<String,Object>();result.put("passed",true);result.put("checks",checks);result.put("count",checks.size());Files.writeString(root.resolve("round-three-live-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));mc.stop();}
        }catch(Throwable failure){failure.printStackTrace();var result=new LinkedHashMap<String,Object>();result.put("passed",false);result.put("error",failure.toString());result.put("checks",checks);Files.writeString(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/round-three-live-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));mc.stop();}
    }
}
