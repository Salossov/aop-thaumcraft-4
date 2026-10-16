package thaumcraft.verification;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.research.ResearchDefinition;
import thaumcraft.client.ThaumonomiconScreen;

/** Actual production tree, eldritch background, category tooltip and completion panel. */
final class RoundTwoBookPreview extends Screen {
    private ThaumonomiconScreen book;private java.lang.reflect.Method background,tooltip;
    private final ResearchDefinition next=definition("FOCUSPOUCH",List.of("ASPECTS"),8,5,true,"thaumcraft:focus_pouch");
    private boolean rendered;
    RoundTwoBookPreview(){super(Component.empty());}
    @Override protected void init(){try{
        var k=new PlayerKnowledge();k.completeResearch("ASPECTS");k.completeResearch("ELDRITCHMINOR");k.setPool("aer",20);k.setPool("aqua",20);
        var state=thaumcraft.network.ClientKnowledge.class.getDeclaredField("state");state.setAccessible(true);state.set(null,k);var definitions=thaumcraft.network.ClientKnowledge.class.getDeclaredField("definitions");definitions.setAccessible(true);definitions.set(null,Map.of("ASPECTS",definition("ASPECTS",List.of(),0,0,false,"thaumcraft:arcane_bellows"),"FOCUSPOUCH",next,"THAUMIUMCAPS",definition("THAUMIUMCAPS",List.of("FOCUSPOUCH"),9,6,false,"thaumcraft:thaumonomicon")));
        var inventory=new Inventory(null);book=new ThaumonomiconScreen(new thaumcraft.menu.ThaumonomiconMenu(0,inventory,UUID.randomUUID()),inventory,Component.empty());book.init(minecraft,width,height);var category=ThaumonomiconScreen.class.getDeclaredField("category");category.setAccessible(true);category.set(book,"ELDRITCH");var pan=ThaumonomiconScreen.class.getDeclaredField("panX");pan.setAccessible(true);pan.setDouble(book,-32);
        background=ThaumonomiconScreen.class.getDeclaredMethod("renderBg",GuiGraphics.class,float.class,int.class,int.class);background.setAccessible(true);tooltip=ThaumonomiconScreen.class.getDeclaredMethod("researchTooltip",GuiGraphics.class,ResearchDefinition.class,int.class,int.class);tooltip.setAccessible(true);
        thaumcraft.client.ResearchCompletionOverlay.accept("FOCUSPOUCH");
    }catch(Exception e){throw new RuntimeException(e);}}
    @Override public void render(GuiGraphics g,int x,int y,float partial){try{
        g.fill(0,0,width,height,0xff182026);background.invoke(book,g,partial,x,y);tooltip.invoke(book,g,next,width-160,160);
        var draw=thaumcraft.client.ResearchCompletionOverlay.class.getDeclaredMethod("draw",GuiGraphics.class);draw.setAccessible(true);draw.invoke(null,g);g.flush();rendered=true;
    }catch(Exception e){throw new RuntimeException(e);}}
    boolean rendered(){return rendered;}
    private static ResearchDefinition definition(String key,List<String> parents,int column,int row,boolean secondary,String icon){return new ResearchDefinition(key,"ELDRITCH",parents,secondary?Map.of("aer",5,"aqua",10,"praecantatio",15):Map.of(),1,false,false,column,row,List.of(),List.of(),icon,false,true,false,secondary,List.of(),List.of(),false,0,List.of(),List.of(),List.of(),List.of(),Map.of(),false,Map.of());}
}
