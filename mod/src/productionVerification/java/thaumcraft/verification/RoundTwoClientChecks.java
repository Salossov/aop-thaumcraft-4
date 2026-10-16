package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import thaumcraft.client.*;

final class RoundTwoClientChecks {
    static void run(Minecraft mc,BiConsumer<Boolean,String> check)throws Exception{
        for(var block:net.minecraft.core.registries.BuiltInRegistries.BLOCK){var id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);if(id.getNamespace().equals("thaumcraft")&&block.asItem()!=net.minecraft.world.item.Items.AIR)check.accept(net.minecraft.client.resources.language.I18n.exists(new ItemStack(block).getDescriptionId()),"registered block has a localized display name: "+id);}
        for(var block:List.of(thaumcraft.registry.R5Content.CARVED_STONE.get(),thaumcraft.registry.R5Content.ANCIENT_RUNIC_STONE.get(),thaumcraft.registry.R5Content.DECORATED_STONE.get())){
            var state=block.defaultBlockState();var model=mc.getBlockRenderer().getBlockModel(state);var quads=new ArrayList<net.minecraft.client.renderer.block.model.BakedQuad>();quads.addAll(model.getQuads(state,null,RandomSource.create(),ModelData.EMPTY,null));for(var face:Direction.values())quads.addAll(model.getQuads(state,face,RandomSource.create(),ModelData.EMPTY,null));
            float[] min={1,1,1},max={0,0,0};for(var quad:quads)for(int vertex=0;vertex<4;vertex++)for(int axis=0;axis<3;axis++){float value=Float.intBitsToFloat(quad.getVertices()[vertex*IQuadTransformer.STRIDE+axis]);min[axis]=Math.min(min[axis],value);max[axis]=Math.max(max[axis],value);}
            check.accept(quads.size()==6&&Arrays.equals(min,new float[]{0,0,0})&&Arrays.equals(max,new float[]{1,1,1}),"room stone geometry retains all six full-cube faces: "+block);
        }
        var resolve=thaumcraft.network.ClientNotify.class.getDeclaredMethod("resolve",thaumcraft.network.NotifyPayload.class);resolve.setAccessible(true);var notice=(net.minecraft.network.chat.Component)resolve.invoke(null,new thaumcraft.network.NotifyPayload("tc.addaspectpool","1|tc.aspect.aqua",0xffffff,false,false,true));check.accept(notice.getString().endsWith("Aqua"),"aspect notification names Aqua rather than Water");
        var warpTexts=new HashSet<String>();for(String key:List.of("tc.addwarp","tc.addwarpsticky","tc.addwarptemp")){var textNotice=(net.minecraft.network.chat.Component)resolve.invoke(null,new thaumcraft.network.NotifyPayload(key,"",0xaa00aa,false,false,false));warpTexts.add(textNotice.getString());}check.accept(warpTexts.size()==3,"permanent, sticky and temporary warp messages remain distinct");
        var field=ResearchNotifications.class.getDeclaredField("TEXT");field.setAccessible(true);var entries=(Deque<?>)field.get(null);entries.clear();for(int i=0;i<12;i++)ResearchNotifications.text("event-"+i,0xffffff);
        var text=entries.getFirst().getClass().getDeclaredMethod("text");text.setAccessible(true);var pendingField=ResearchNotifications.class.getDeclaredField("PENDING");pendingField.setAccessible(true);var pending=(Deque<?>)pendingField.get(null);check.accept(entries.size()==8&&pending.size()==4&&text.invoke(entries.getFirst()).equals("event-0")&&text.invoke(entries.getLast()).equals("event-7"),"first eight notifications display while four remain queued");var advance=ResearchNotifications.class.getDeclaredMethod("advance",long.class);advance.setAccessible(true);long future=System.nanoTime()/1000000+7000;advance.invoke(null,future);check.accept(entries.size()==4&&pending.isEmpty()&&text.invoke(entries.getFirst()).equals("event-8")&&text.invoke(entries.getLast()).equals("event-11"),"expired notification slots display the remaining batch in order");advance.invoke(null,future+7000);check.accept(entries.isEmpty(),"final notification batch expires normally");ResearchNotifications.text("fresh-after-expiry",0xffffff);check.accept(entries.size()==1,"new aspect gains still appear after the whole queue expires");entries.clear();pending.clear();
        ThirdRoundClientChecks.run(mc,check);
        var state=thaumcraft.network.ClientKnowledge.class.getDeclaredField("state");var definitions=thaumcraft.network.ClientKnowledge.class.getDeclaredField("definitions");state.setAccessible(true);definitions.setAccessible(true);var savedState=state.get(null);var savedDefinitions=definitions.get(null);
        try{
            var knowledge=new thaumcraft.api.knowledge.PlayerKnowledge();knowledge.completeResearch("ROOT");state.set(null,knowledge);definitions.set(null,Map.of("ROOT",definition("ROOT",List.of()),"NEXT",definition("NEXT",List.of("ROOT")),"LATER",definition("LATER",List.of("NEXT"))));
            var inventory=new Inventory(null);var book=new ThaumonomiconScreen(new thaumcraft.menu.ThaumonomiconMenu(0,inventory,UUID.randomUUID()),inventory,net.minecraft.network.chat.Component.empty());book.init(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());var visible=ThaumonomiconScreen.class.getDeclaredMethod("visible");visible.setAccessible(true);var keys=((List<?>)visible.invoke(book)).stream().map(value->((thaumcraft.api.research.ResearchDefinition)value).key()).collect(java.util.stream.Collectors.toSet());check.accept(keys.equals(Set.of("ROOT","NEXT")),"research tree shows completed and currently available nodes, hiding later descendants");
            var parent=ThaumonomiconScreen.class.getSuperclass();var scale=parent.getDeclaredField("interfaceScale");scale.setAccessible(true);scale.setDouble(book,.5);var scissor=parent.getDeclaredMethod("fittedScissor",GuiGraphics.class,int.class,int.class,int.class,int.class);scissor.setAccessible(true);var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());scissor.invoke(book,g,40,60,140,160);int[] box=new int[4];org.lwjgl.opengl.GL11.glGetIntegerv(org.lwjgl.opengl.GL11.GL_SCISSOR_BOX,box);double guiScale=mc.getWindow().getGuiScale();check.accept(box[0]==(int)(20*guiScale)&&box[2]==(int)(50*guiScale)&&box[3]==(int)(50*guiScale),"book clipping uses scaled physical coordinates");g.disableScissor();
        }finally{state.set(null,savedState);definitions.set(null,savedDefinitions);}
        check.accept(Arrays.stream(net.minecraft.client.renderer.ItemInHandRenderer.class.getDeclaredMethods()).anyMatch(method->method.getName().contains("keepScannerEquipped")),"native hand renderer contains the injected RMB equip-reset guard");
    }
    private static thaumcraft.api.research.ResearchDefinition definition(String key,List<String> parents){return new thaumcraft.api.research.ResearchDefinition(key,"BASICS",parents,Map.of(),1,false,false,0,0,List.of(),List.of(),"thaumcraft:thaumonomicon",false,true,false,false,List.of(),List.of(),false,0,List.of(),List.of(),List.of(),List.of(),Map.of(),false,Map.of());}
}
