package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;
import thaumcraft.client.FocusRadialScreen;
import thaumcraft.items.*;
import thaumcraft.registry.*;

/** Drives Minecraft's actual input callbacks and receives the server's focus swaps. */
@EventBusSubscriber(modid="thaumcraft_r3_test",value=Dist.CLIENT)
public final class FocusSelectionLiveClient {
    private static int ticks,waited,waitStage;private static final List<String> checks=new ArrayList<>();private static KeyMapping focus;private static InputConstants.Key originalFocus,originalSwap;
    private static void check(boolean pass,String name){if(!pass)throw new AssertionError(name);checks.add(name);}
    private static void key(Minecraft mc,int key,int action){mc.keyboardHandler.keyPress(mc.getWindow().getWindow(),key,GLFW.glfwGetKeyScancode(key),action,0);}
    private static void bind(int key){focus.setKey(InputConstants.Type.KEYSYM.getOrCreate(key));KeyMapping.resetMapping();}
    private static void point(Minecraft mc,String target)throws Exception{var choices=WandFocus.choices(mc.player);int offset=WandFocus.get(wand(mc)).isEmpty()?0:1,index=offset;while(index-offset<choices.size()&&!choices.get(index-offset).stack().is(R3Equipment.FOCI.get(target).get()))index++;check(index-offset<choices.size(),"radial offers requested focus: "+target);int count=choices.size()+offset;
        var screen=mc.screen;var radiusMethod=FocusRadialScreen.class.getDeclaredMethod("radius");radiusMethod.setAccessible(true);var centerMethod=FocusRadialScreen.class.getDeclaredMethod("centerY");centerMethod.setAccessible(true);int radius=(int)radiusMethod.invoke(screen),center=(int)centerMethod.invoke(screen);double angle=index*Math.PI*2/count-Math.PI/2;
        double x=(screen.width/2+Math.cos(angle)*radius)*mc.getWindow().getScreenWidth()/screen.width,y=(center+Math.sin(angle)*radius)*mc.getWindow().getScreenHeight()/screen.height;
        var method=MouseHandler.class.getDeclaredMethod("onMove",long.class,double.class,double.class);method.setAccessible(true);method.invoke(mc.mouseHandler,mc.getWindow().getWindow(),x,y);method.invoke(mc.mouseHandler,mc.getWindow().getWindow(),x,y);
    }
    private static void mouse(Minecraft mc,int action)throws Exception{var method=MouseHandler.class.getDeclaredMethod("onPress",long.class,int.class,int.class,int.class);method.setAccessible(true);method.invoke(mc.mouseHandler,mc.getWindow().getWindow(),0,action,0);}
    private static boolean await(boolean ready,int stage){if(ready){waited=0;waitStage=0;return false;}if(waitStage!=stage){waitStage=stage;waited=0;}if(++waited<200){ticks--;return true;}return false;}
    private static ItemStack wand(Minecraft mc){return mc.player.getMainHandItem();}
    private static Collection<thaumcraft.network.BossStatusPayload> statuses()throws Exception{var field=thaumcraft.client.BossStatusBar.class.getDeclaredField("STATUS");field.setAccessible(true);return ((Map<java.util.UUID,thaumcraft.network.BossStatusPayload>)field.get(null)).values();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(!"focus-selection-live".equals(System.getProperty("thaumcraft.r3.phase")))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getOverlay()!=null||!mc.gameMode.hasInfiniteItems())return;if(ticks==0&&mc.screen!=null)return;
        try{
            if(++ticks==20){mc.getToasts().clear();var field=thaumcraft.client.FocusControls.class.getDeclaredField("FOCUS");field.setAccessible(true);focus=(KeyMapping)field.get(null);originalFocus=focus.getKey();originalSwap=mc.options.keySwapOffhand.getKey();mc.options.keySwapOffhand.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F));bind(GLFW.GLFW_KEY_F);check(WandFocus.supports(wand(mc)),"server delivered a focus-compatible held wand");key(mc,GLFW.GLFW_KEY_F,GLFW.GLFW_PRESS);}
            if(ticks==23){check(mc.screen instanceof FocusRadialScreen&&!focus.isDown(),"radial survives native setScreen resetting all key mappings; screen="+mc.screen+", keyDown="+focus.isDown()+", held="+wand(mc)+", offhand="+mc.player.getOffhandItem());}
            if(ticks==25){try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/focus-boss-status-early.png"));}}if(ticks==29){check(mc.screen instanceof FocusRadialScreen&&WandFocus.supports(wand(mc))&&mc.player.getOffhandItem().isEmpty(),"held F keeps radial open and does not trigger vanilla offhand swapping");point(mc,"frost");var selection=FocusRadialScreen.class.getDeclaredField("selection");selection.setAccessible(true);var radial=mc.screen;var entries=FocusRadialScreen.class.getDeclaredField("entries");entries.setAccessible(true);checks.add("release cursor="+mc.mouseHandler.xpos()+","+mc.mouseHandler.ypos()+", selection="+selection.get(radial)+", size="+radial.width+","+radial.height+", entries="+entries.get(radial));key(mc,GLFW.GLFW_KEY_F,GLFW.GLFW_RELEASE);checks.add("after release screen="+mc.screen+", selection="+selection.get(radial));}
            if(ticks==40){if(await(WandFocus.get(wand(mc)).is(R3Equipment.FOCI.get("frost").get()),40))return;check(mc.screen==null&&WandFocus.get(wand(mc)).is(R3Equipment.FOCI.get("frost").get())&&mc.player.getInventory().getItem(9).isEmpty(),"releasing F selects Frost and receives the authoritative inventory update; screen="+mc.screen+", focus="+WandFocus.get(wand(mc))+", slot9="+mc.player.getInventory().getItem(9)+", offhand="+mc.player.getOffhandItem());bind(GLFW.GLFW_KEY_RIGHT_BRACKET);key(mc,GLFW.GLFW_KEY_RIGHT_BRACKET,GLFW.GLFW_PRESS);}
            if(ticks==44){check(mc.screen instanceof FocusRadialScreen,"rebound right bracket opens the same stable radial");key(mc,GLFW.GLFW_KEY_A,GLFW.GLFW_RELEASE);check(mc.screen instanceof FocusRadialScreen,"releasing an unrelated key does not select or dismiss a focus");}
            if(ticks==50){point(mc,"fire");key(mc,GLFW.GLFW_KEY_RIGHT_BRACKET,GLFW.GLFW_RELEASE);}
            if(ticks==61){if(await(WandFocus.get(wand(mc)).is(R3Equipment.FOCI.get("fire").get()),61))return;check(mc.screen==null&&WandFocus.get(wand(mc)).is(R3Equipment.FOCI.get("fire").get())&&mc.player.getInventory().getItem(10).is(R3Equipment.FOCI.get("frost").get()),"releasing rebound bracket equips Fire and returns the previous Frost without duplication");key(mc,GLFW.GLFW_KEY_RIGHT_BRACKET,GLFW.GLFW_PRESS);}
            if(ticks==65||ticks==95){mc.getToasts().clear();}if(ticks==67){point(mc,"shock");try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/focus-selection-radial.png"));}check(org.lwjgl.opengl.GL11.glGetError()==0,"native radial renders without GL errors");mouse(mc,GLFW.GLFW_PRESS);mouse(mc,GLFW.GLFW_RELEASE);key(mc,GLFW.GLFW_KEY_RIGHT_BRACKET,GLFW.GLFW_RELEASE);}
            if(ticks==78){if(await(WandFocus.get(wand(mc)).is(R3Equipment.FOCI.get("shock").get()),78))return;var contents=FocusPouchItem.contents(mc.player.getInventory().getItem(2));check(WandFocus.get(wand(mc)).is(R3Equipment.FOCI.get("shock").get())&&contents.getFirst().is(R3Equipment.FOCI.get("fire").get()),"native click selects a pouch focus and returns the old focus into the pouch");key(mc,GLFW.GLFW_KEY_RIGHT_BRACKET,GLFW.GLFW_PRESS);}
            if(ticks==84){key(mc,GLFW.GLFW_KEY_ESCAPE,GLFW.GLFW_PRESS);key(mc,GLFW.GLFW_KEY_ESCAPE,GLFW.GLFW_RELEASE);key(mc,GLFW.GLFW_KEY_RIGHT_BRACKET,GLFW.GLFW_RELEASE);}
            if(ticks==94){check(mc.screen==null&&WandFocus.get(wand(mc)).is(R3Equipment.FOCI.get("shock").get()),"Escape cancels focus selection without changing the installed focus");check(statuses().size()==2,"two bosses retain independent status by their native boss-bar UUIDs");check(statuses().stream().anyMatch(s->s.health()>=160&&s.health()<200&&s.maxHealth()==200&&s.shield()>=30&&s.maxShield()==132&&s.rage()>=0),"boss status receives changed health, recharging shield and remaining anger from the server");}
            if(ticks==99){try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/focus-boss-status-bar.png"));}check(org.lwjgl.opengl.GL11.glGetError()==0,"combined health/shield/anger boss bars render without GL errors");finish(mc,true,null);}
        }catch(Throwable failure){failure.printStackTrace();finish(mc,false,failure.toString());}
    }
    private static void finish(Minecraft mc,boolean passed,String failure)throws Exception{
        if(originalFocus!=null){focus.setKey(originalFocus);mc.options.keySwapOffhand.setKey(originalSwap);KeyMapping.resetMapping();}
        var result=new LinkedHashMap<String,Object>();result.put("passed",passed);result.put("count",checks.size());result.put("checks",checks);if(failure!=null)result.put("error",failure);
        Files.writeString(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/focus-selection-live-client.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));mc.stop();
    }
}
