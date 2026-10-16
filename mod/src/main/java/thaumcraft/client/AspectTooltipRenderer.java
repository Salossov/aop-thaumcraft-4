package thaumcraft.client;
import java.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import thaumcraft.api.aspects.Aspect;
public final class AspectTooltipRenderer implements ClientTooltipComponent {
    private final List<Map.Entry<String,Integer>> values;
    public AspectTooltipRenderer(AspectTooltipData data){values=data.values().entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).toList();}
    @Override public int getWidth(Font font){return Math.min(8,values.size())*20;}
    @Override public int getHeight(){return ((values.size()+7)/8)*20+3;}
    @Override public void renderImage(Font font,int x,int y,GuiGraphics g){var knowledge=thaumcraft.network.ClientKnowledge.snapshot();for(int i=0;i<values.size();i++){var e=values.get(i);var aspect=Aspect.getAspect(e.getKey());if(aspect==null)continue;int xx=x+i%8*20,yy=y+3+i/8*20;ResearchGraphics.aspectHighlight(g,xx-2,yy-2);if(knowledge.knows(aspect))ResearchGraphics.aspect(g,aspect,xx,yy,1);else ResearchGraphics.blit(g,ResearchGraphics.texture("aspects/_unknown"),xx,yy,16,16,0,0,32,32,32,32);g.pose().pushPose();g.pose().translate(xx+16,yy+10,1);g.pose().scale(.5f,.5f,1);var n=Integer.toString(e.getValue());g.drawString(font,n,-font.width(n),0,0xffffffff,true);g.flush();g.pose().popPose();}}
}
