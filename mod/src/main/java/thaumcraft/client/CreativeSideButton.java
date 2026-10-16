package thaumcraft.client;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
/** Concrete class outside the mixin package, usable by transformed vanilla screens. */
public final class CreativeSideButton extends Button {
    private final Supplier<ItemStack> icon;
    public CreativeSideButton(int x,int y,int width,int height,Component label,OnPress action,Supplier<Component> narration,Supplier<ItemStack> icon){super(x,y,width,height,label,action,n->narration.get().copy());this.icon=icon;}
    @Override public void renderWidget(GuiGraphics g,int mx,int my,float partial){int x=getX(),y=getY(),w=getWidth(),h=getHeight();g.fill(x,y,x+w,y+h,icon==null?0xff373737:0xff332a25);g.fill(x+1,y+1,x+w,y+h-1,icon==null?(isHoveredOrFocused()?0xffe3e3e3:0xffc6c6c6):(isHoveredOrFocused()?0xff877251:0xff514438));if(icon==null){g.fill(x+1,y+1,x+2,y+h-2,0xffffffff);g.fill(x+1,y+1,x+w,y+2,0xffffffff);g.fill(x+2,y+h-2,x+w,y+h-1,0xff555555);for(int line=0;line<3;line++){int yy=y+5+line*3;g.fill(x+6,yy,x+14,yy+1,0xff444444);}}else {g.pose().pushPose();g.pose().translate(x+4,y+1,0);g.pose().scale(.75f,.75f,1);g.renderItem(icon.get(),0,0);g.flush();g.pose().popPose();}}
}
