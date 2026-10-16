package thaumcraft.verification;

import thaumcraft.client.ThaumonomiconScreen;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.api.research.ResearchDefinition;
import thaumcraft.registry.*;

/** Isolated loaded-resource, native GPU and sound-engine probe; never shipped. */
@EventBusSubscriber(modid="thaumcraft_r3_test",value=Dist.CLIENT)
public final class ReportedFixesClient {
    private static int ticks;
    private static Preview preview;
    private static SimpleSoundInstance playing;
    private static final List<String> checks=new ArrayList<>();
    private static final JsonObject result=new JsonObject();
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(!"reported-fixes-client".equals(System.getProperty("thaumcraft.r3.phase")))return;
        var mc=Minecraft.getInstance();if(mc.getOverlay()!=null)return;
        try{
            if(preview==null){
                if(mc.screen==null||mc.level!=null)return;
                org.lwjgl.glfw.GLFW.glfwRestoreWindow(mc.getWindow().getWindow());org.lwjgl.glfw.GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),854,480);mc.resizeDisplay();
                for(var eventName:List.of("page","cameraticks","cameraclack","learn","wand","ice","zap","craftstart","wind","wandfail")){
                    var sound=mc.getSoundManager().getSoundEvent(ResourceLocation.parse("thaumcraft:"+eventName));
                    require(sound!=null&&sound.getSound(RandomSource.create()).getLocation().getNamespace().equals("thaumcraft"),"loaded custom sound resolves to an OGG: "+eventName);
                }
                var audit=new JsonArray();for(var block:net.minecraft.core.registries.BuiltInRegistries.BLOCK){var id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);if(!id.getNamespace().equals("thaumcraft")||block.asItem()==net.minecraft.world.item.Items.AIR)continue;var row=new JsonObject();var stack=new ItemStack(block);var model=mc.getItemRenderer().getModel(stack,null,null,0);row.addProperty("id",id.toString());row.addProperty("name_key",stack.getDescriptionId());row.addProperty("translated",net.minecraft.client.resources.language.I18n.exists(stack.getDescriptionId()));row.addProperty("gui3d",model.isGui3d());row.addProperty("custom",model.isCustomRenderer());audit.add(row);}Files.writeString(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/block-model-audit.json"),new GsonBuilder().setPrettyPrinting().create().toJson(audit));
                RoundTwoClientChecks.run(mc,ReportedFixesClient::require);
                for(var block:R3Content.CRYSTAL_CLUSTERS){var model=mc.getItemRenderer().getModel(new ItemStack(block.get()),null,null,0);require(model.isCustomRenderer(),"cluster item uses three-dimensional renderer: "+block.getId());}
                for(var block:R4Content.CANDLES){var stack=new ItemStack(block.get());var model=mc.getItemRenderer().getModel(stack,null,null,0);require(model.getRenderPasses(stack,false).getFirst()==model,"candle inventory render pass retains UV wrapper: "+block.getId());}
                playing=SimpleSoundInstance.forUI(R2Sounds.PAGE.get(),1);mc.getSoundManager().play(playing);
                thaumcraft.client.ResearchNotifications.text("Gained 1 research point(s) for Perditio",0x111111,thaumcraft.api.aspects.Aspects.ENTROPY);
                preview=new Preview();mc.setScreen(preview);
            }
            if(++ticks==4)require(mc.getSoundManager().isActive(playing),"custom page OGG decoded and accepted by the native audio engine");
            if(ticks<30||ticks>30&&ticks<45)return;
            require(preview.rendered,"production icon, model and tooltip paths submitted to the GPU");require(mc.getMainRenderTarget().width>100&&mc.getMainRenderTarget().height>100,"native capture retains a visible framebuffer size");
            require(org.lwjgl.opengl.GL11.glGetError()==0,"native preview has no OpenGL errors");
            var root=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence");Files.createDirectories(root);
            try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(root.resolve(ticks==30?"reported-fixes-client.png":"round-two-book.png"));}
            if(ticks==30){mc.setScreen(new RoundTwoBookPreview());return;}
            require(mc.screen instanceof RoundTwoBookPreview screen&&screen.rendered(),"native eldritch tree and scroll-triggered completion panel rendered");result.addProperty("passed",true);finish(mc,root);
        }catch(Throwable failure){result.addProperty("passed",false);result.addProperty("error",failure.toString());failure.printStackTrace();finish(mc,Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence"));}
    }
    private static ResourceLocation texture(String name){return ResourceLocation.parse("thaumcraft:textures/"+name+".png");}
    private static void graphics(String name,Object... args){try{for(var method:Class.forName("thaumcraft.client.ResearchGraphics").getDeclaredMethods())if(method.getName().equals(name)&&method.getParameterCount()==args.length){method.setAccessible(true);method.invoke(null,args);return;}throw new IllegalArgumentException(name);}catch(Exception e){throw new RuntimeException(e);}}
    private static void blit(GuiGraphics g,ResourceLocation texture,int x,int y,float u,float v,int w,int h,int tw,int th){graphics("blit",g,texture,x,y,u,v,w,h,tw,th);}
    private static void forbidden(GuiGraphics g,int x,int y,int tick){graphics("forbidden",g,x,y,tick);}
    private static void require(boolean passed,String text){if(!passed)throw new AssertionError(text);checks.add(text);}
    private static void require(Boolean passed,String text){require(passed.booleanValue(),text);}
    private static void finish(Minecraft mc,Path root)throws Exception{result.add("checks",new Gson().toJsonTree(checks));result.addProperty("count",checks.size());Files.writeString(root.resolve("reported-fixes-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));mc.stop();}
    private static final class Preview extends Screen {
        private ThaumonomiconScreen book;
        private java.lang.reflect.Method tooltip;
        private boolean rendered;
        private final ResearchDefinition secondary=definition("FOCUSPOUCH",true,false,0),warp=definition("CRIMSON",false,true,3);
        private Preview(){super(Component.literal("Reported fixes"));}
        @Override protected void init(){
            var inventory=new Inventory(null);book=new ThaumonomiconScreen(new thaumcraft.menu.ThaumonomiconMenu(0,inventory,UUID.randomUUID()),inventory,Component.empty());book.init(minecraft,width,height);
            try{tooltip=ThaumonomiconScreen.class.getDeclaredMethod("researchTooltip",GuiGraphics.class,ResearchDefinition.class,int.class,int.class);tooltip.setAccessible(true);}catch(Exception e){throw new RuntimeException(e);}
        }
        @Override public void render(GuiGraphics g,int mx,int my,float partial){
            g.fill(0,0,width,height,0xff6888bd);
            g.drawString(font,"Crystal clusters and candle inventory models",8,8,0xffffffff,false);
            g.pose().pushPose();g.pose().translate(10,24,0);g.pose().scale(2,2,2);int i=0;
            for(var block:R3Content.CRYSTAL_CLUSTERS)g.renderItem(new ItemStack(block.get()),i++*20,0);
            i=0;for(var block:R4Content.CANDLES)g.renderItem(new ItemStack(block.get()),i++*18,22);g.pose().popPose();
            g.pose().pushPose();g.pose().translate(280,130,0);g.pose().scale(2,2,2);g.renderItem(new ItemStack(R2Content.WAND.get()),0,0);g.renderItem(new ItemStack(R3Equipment.STAFF.get()),18,0);var jar=new ItemStack(R3Content.WARDED_JAR.get());var tag=new net.minecraft.nbt.CompoundTag();var liquid=new net.minecraft.nbt.CompoundTag();liquid.putInt("aqua",64);tag.put("Essentia",liquid);tag.putString("AspectFilter","aqua");jar.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,net.minecraft.world.item.component.CustomData.of(tag));g.renderItem(jar,36,0);g.pose().popPose();
            g.drawString(font,"HUD after text flush",8,116,0xffffffff,false);
            g.pose().pushPose();g.pose().translate(260,30,0);g.pose().scale(3,3,3);int blockIndex=0;for(String id:List.of("thaumatorium_brainbox","wand_recharge_pedestal","item_grate"))g.renderItem(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:"+id))),blockIndex++*20,0);g.pose().popPose();
            g.pose().pushPose();g.pose().translate(8,130,0);g.pose().scale(.5f,.5f,1);blit(g,texture("gui/hud"),0,0,0,0,64,64,256,256);g.drawString(font,"23",68,0,0xffffffff,false);blit(g,texture("gui/hud"),72,0,72,0,16,42,256,256);g.pose().popPose();
            forbidden(g,120,154,ticks);g.drawString(font,"Warp glow",100,184,0xffffffff,false);
            try{tooltip.invoke(book,g,secondary,Math.min(180,width/2),110);tooltip.invoke(book,g,warp,Math.min(180,width/2),200);}catch(Exception e){throw new RuntimeException(e);}
            try{var method=thaumcraft.client.ResearchNotifications.class.getDeclaredMethod("text",GuiGraphics.class,Minecraft.class,long.class,int.class,int.class);method.setAccessible(true);method.invoke(null,g,minecraft,System.nanoTime()/1000000,width,height);}catch(Exception e){throw new RuntimeException(e);}
            g.flush();rendered=true;
        }
        private static ResearchDefinition definition(String key,boolean secondary,boolean special,int warp){return new ResearchDefinition(key,"THAUMATURGY",List.of("LOCKED"),Map.of("alienis",10,"auram",10,"praecantatio",10),1,false,false,0,0,List.of(),List.of(),"items/thaumonomicon",false,true,false,secondary,List.of(),List.of(),false,warp,List.of(),List.of(),List.of(),List.of(),Map.of(),special,Map.of());}
    }
}
