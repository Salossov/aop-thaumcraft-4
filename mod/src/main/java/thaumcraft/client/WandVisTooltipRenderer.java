package thaumcraft.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import thaumcraft.items.WandVisTooltip;

public final class WandVisTooltipRenderer implements ClientTooltipComponent {
    private final WandVisTooltip data;
    public WandVisTooltipRenderer(WandVisTooltip data){this.data=data;}
    private int column(Font font){return font.width(Integer.toString(data.capacity()/100)+".00")+8;}
    @Override public int getWidth(Font font){return column(font)*6;}
    @Override public int getHeight(){return 11;}
    @Override public void renderImage(Font font,int x,int y,GuiGraphics g){int i=0;for(var aspect:thaumcraft.api.aspects.Aspects.PRIMALS){var value=String.format(java.util.Locale.ROOT,"%.2f",data.amounts().getOrDefault(aspect.getTag(),0)/100f);int right=x+(++i)*column(font)-5;g.drawString(font,value,right-font.width(value),y,0xff000000|aspect.getColor(),false);if(i<6)g.drawString(font,"|",right+2,y,0xff888888,false);}}
}
