package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.menu.ArcaneBoreMenu;
public final class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/gui/gui_arcanebore.png");
    public ArcaneBoreScreen(ArcaneBoreMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageHeight=141;}
    @Override protected void renderLabels(GuiGraphics graphics,int x,int y){}
    @Override protected void renderBg(GuiGraphics graphics,float partial,int x,int y){graphics.blit(TEXTURE,leftPos,topPos,0,0,176,141);var pick=menu.bore.items.getItem(1);if(!pick.isEmpty()&&pick.getDamageValue()+1>=pick.getMaxDamage())graphics.blit(TEXTURE,leftPos+74,topPos+18,184,0,16,16);graphics.pose().pushPose();graphics.pose().translate(leftPos+112,topPos+8,0);graphics.pose().scale(.5f,.5f,1);graphics.drawString(font,Component.translatable("thaumcraft.bore.width",1+(menu.bore.area+2)*2),0,0,0xffffff,false);graphics.drawString(font,Component.translatable("thaumcraft.bore.speed",menu.bore.speed),0,10,0xffffff,false);if(menu.bore.fortune>0)graphics.drawString(font,Component.translatable("enchantment.minecraft.fortune").append(" "+menu.bore.fortune),4,34,0xeec44a,false);if(menu.bore.enchant(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH)>0||menu.bore.upgrade(thaumcraft.api.wands.FocusUpgradeType.silktouch)>0)graphics.drawString(font,Component.translatable("enchantment.minecraft.silk_touch"),4,44,0x8080ff,false);graphics.pose().popPose();}
    @Override public void render(GuiGraphics graphics,int x,int y,float partial){super.render(graphics,x,y,partial);renderTooltip(graphics,x,y);}
}
