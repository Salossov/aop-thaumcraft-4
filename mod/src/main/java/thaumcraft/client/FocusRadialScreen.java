package thaumcraft.client;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.items.WandFocus;
import thaumcraft.network.FocusSelectPayload;

/** Original rotating focus wheel, icon hover boxes and upgrade-aware ordering. */
public final class FocusRadialScreen extends Screen {
    private record Entry(int slot,ItemStack stack){}
    private final InteractionHand hand;
    private final List<Entry> entries=new ArrayList<>();
    private int selection=-1;
    private boolean chosen;
    private long opened=System.nanoTime(),closing;
    private final Map<Integer,Float> hoverScale=new HashMap<>();
    private final FrameAnimation hoverClock=new FrameAnimation();
    public FocusRadialScreen(InteractionHand hand){super(Component.translatable("key.thaumcraft.focus"));this.hand=hand;}
    @Override protected void init(){entries.clear();if(minecraft.player==null)return;var wand=minecraft.player.getItemInHand(hand);var sorted=new TreeMap<String,Entry>();var current=WandFocus.get(wand);String currentKey=current.isEmpty()?"":sortKey(current);for(var entry:WandFocus.choices(minecraft.player))if(!sortKey(entry.stack()).equals(currentKey))sorted.put(sortKey(entry.stack()),new Entry(entry.slot(),entry.stack()));entries.addAll(sorted.values());}
    private static String sortKey(ItemStack stack){if(stack.getItem() instanceof ItemFocusBasic custom){var helper=custom.getSortingHelper(stack);if(helper!=null)return helper;}String prefix=stack.getItem() instanceof thaumcraft.items.ThaumicFocusItem focus?switch(focus.kind()){case FIRE->"AF";case EXCAVATION->"BE";case FROST->"BF";case SHOCK->"BL";case PORTABLE_HOLE->"BPH";case TRADE->"BT";case WARDING->"BWA";case PRIMAL->"FP";case HELLBAT->"HH";}:net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();var key=new StringBuilder(prefix);if(stack.getItem() instanceof ItemFocusBasic focus)for(int upgrade:focus.getAppliedUpgrades(stack))if(upgrade>=0)key.append(upgrade);return key.toString();}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void tick(){if(minecraft.player==null||!WandFocus.supports(minecraft.player.getItemInHand(hand)))onClose();}
    private int centerY(){int top=BossStatusBar.menuTop();return top==0?height/2:(top+height-26)/2;}
    private float animation(){float elapsed=(System.nanoTime()-(closing==0?opened:closing))/1_000_000f/300;return net.minecraft.util.Mth.clamp(closing==0?elapsed:1-elapsed,0,1);}
    private int radius(){return 16+entries.size()*5/2;}
    private void point(double mouseX,double mouseY){selection=-1;if(chosen)return;float scale=animation();for(int i=0;i<entries.size();i++){double angle=i*Math.PI*2/entries.size()-Math.PI/2*scale;double x=width/2+Math.cos(angle)*radius()*scale,y=centerY()+Math.sin(angle)*radius()*scale;if(Math.abs(mouseX-x)<=10*scale&&Math.abs(mouseY-y)<=10*scale){selection=i;break;}}}
    @Override public void mouseMoved(double x,double y){point(x,y);}
    @Override public boolean keyReleased(int key,int scan,int modifiers){if(FocusControls.FOCUS.matches(key,scan)){point(minecraft.mouseHandler.xpos()*width/minecraft.getWindow().getScreenWidth(),minecraft.mouseHandler.ypos()*height/minecraft.getWindow().getScreenHeight());choose();return true;}return super.keyReleased(key,scan,modifiers);}
    @Override public boolean mouseReleased(double x,double y,int button){if(FocusControls.FOCUS.matchesMouse(button)){point(x,y);choose();return true;}return super.mouseReleased(x,y,button);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){
        double seconds=hoverClock.seconds();
        float scale=animation();if(chosen&&scale<=0){onClose();return;}int cx=width/2,cy=centerY();point(mouseX,mouseY);
        float time=minecraft.player.tickCount+partial;
        for(int layer=0;layer<2;layer++){graphics.pose().pushPose();graphics.pose().translate(cx,cy,100);graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(time*.5f*(layer==0?1:-1)));int size=(int)(radius()*(layer==0?2.75f:2.55f)*scale);com.mojang.blaze3d.systems.RenderSystem.enableBlend();graphics.setColor(.5f,.5f,.5f,.5f);graphics.blit(ResearchGraphics.texture("misc/radial"+(layer==0?"":"2")),-size/2,-size/2,size,size,0,0,256,256,256,256);graphics.setColor(1,1,1,1);graphics.pose().popPose();}
        var current=WandFocus.get(minecraft.player.getItemInHand(hand));if(!current.isEmpty())graphics.renderItem(current,cx-8,cy-8);
        for(int i=0;i<entries.size();i++){double angle=i*Math.PI*2/entries.size()-Math.PI/2*scale;float hover=hoverScale.getOrDefault(i,1f);hover=FrameAnimation.hover(hover,i==selection?1.3f:1f,seconds);hoverScale.put(i,hover);graphics.pose().pushPose();graphics.pose().translate(cx+Math.cos(angle)*radius()*scale,cy+Math.sin(angle)*radius()*scale,200);graphics.pose().scale(scale*hover,scale*hover,1);graphics.renderItem(entries.get(i).stack,-8,-8);graphics.pose().popPose();}
        if(selection>=0)graphics.renderTooltip(font,entries.get(selection).stack,8,20);else if(!current.isEmpty()&&Math.abs(mouseX-cx)<10&&Math.abs(mouseY-cy)<10)graphics.renderTooltip(font,current,8,20);
    }
    private void choose(){if(chosen)return;chosen=true;if(selection>=0&&selection<entries.size())PacketDistributor.sendToServer(new FocusSelectPayload(hand==InteractionHand.OFF_HAND,entries.get(selection).slot));closing=System.nanoTime();}
    @Override public boolean mouseClicked(double x,double y,int button){if(button==0){point(x,y);choose();return true;}return super.mouseClicked(x,y,button);}
}
