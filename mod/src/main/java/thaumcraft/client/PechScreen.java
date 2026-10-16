package thaumcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.menu.PechMenu;
import thaumcraft.registry.R2Sounds;
import thaumcraft.entities.PechTrades;
import java.util.*;
import net.minecraft.world.item.ItemStack;

public final class PechScreen extends FittedContainerScreen<PechMenu> {
    private final List<PechTrades.Offer> examples;
    private int selectedTier=1;
    public PechScreen(PechMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=PechMenu.PREVIEW_WIDTH+175;imageHeight=232;examples=PechTrades.inventory(inventory.player.level(),menu.pech.variant());}
    @Override protected void renderBg(GuiGraphics graphics,float partial,int x,int y){
        int tradeLeft=leftPos+PechMenu.PREVIEW_WIDTH;
        graphics.blit(ResearchGraphics.texture("gui/gui_pech"),tradeLeft,topPos,0,0,175,imageHeight);
        if(menu.ready())graphics.blit(ResearchGraphics.texture("gui/gui_pech"),tradeLeft+67,topPos+24,176,0,25,25);
        graphics.fill(leftPos,topPos,leftPos+116,topPos+imageHeight,0xff37332d);
        graphics.fill(leftPos+2,topPos+2,leftPos+114,topPos+imageHeight-2,0xffc6c6c6);
        graphics.drawWordWrap(font,Component.translatable("tc.pech.possible_rewards"),leftPos+8,topPos+8,100,0x404040);
        int cycle=(int)(minecraft.level.getGameTime()/40);
        for(int tier=1;tier<=5;tier++){
            int yy=topPos+35+(tier-1)*29;
            graphics.fill(leftPos+5,yy,leftPos+111,yy+27,tier==selectedTier?0xff6c8661:0xff8b8b8b);
            graphics.fill(leftPos+6,yy+1,leftPos+110,yy+26,tier==selectedTier?0xffc6d6b9:0xffdedede);
            graphics.drawString(font,Component.translatable("tc.pech.reward_tier",tier),leftPos+9,yy+3,0x404040,false);
            var pool=pool(tier);
            for(int icon=0;icon<4&&!pool.isEmpty();icon++)graphics.renderItem(pool.get(Math.floorMod(cycle+icon,pool.size())),leftPos+10+icon*23,yy+11);
        }
        graphics.drawWordWrap(font,Component.translatable("tc.pech.random_result"),leftPos+8,topPos+184,100,0x404040);
    }
    @Override protected void renderLabels(GuiGraphics graphics,int x,int y){}
    @Override protected void renderFitted(GuiGraphics graphics,int x,int y,float partial){
        super.renderFitted(graphics,x,y,partial);renderTooltip(graphics,x,y);
        int relativeX=x-leftPos,relativeY=y-topPos;
        if(relativeX>=5&&relativeX<111&&relativeY>=35&&relativeY<180){
            int tier=(relativeY-35)/29+1,icon=(relativeX-10)/23;var pool=pool(tier);
            if(relativeY>=35+(tier-1)*29+11&&relativeX>=10&&icon<4&&!pool.isEmpty()){
                var item=pool.get(Math.floorMod((int)(minecraft.level.getGameTime()/40)+icon,pool.size()));
                var lines=new ArrayList<>(getTooltipFromItem(minecraft,item));lines.add(Component.translatable("tc.pech.example_only").withStyle(net.minecraft.ChatFormatting.GRAY));
                if(tier>=4)lines.add(Component.translatable("tc.pech.dungeon_alternative").withStyle(net.minecraft.ChatFormatting.GRAY));
                fittedTooltip(graphics,lines,x,y);
            }
        }
    }
    private List<ItemStack> pool(int tier){return examples.stream().filter(offer->offer.tier()==tier).map(PechTrades.Offer::stack).toList();}
    @Override protected boolean mouseClickedFitted(double x,double y,int button){
        if(button==0&&x-leftPos>=5&&x-leftPos<111&&y-topPos>=35&&y-topPos<180){selectedTier=(int)(y-topPos-35)/29+1;return true;}
        if(button==0&&x-leftPos-PechMenu.PREVIEW_WIDTH>=67&&x-leftPos-PechMenu.PREVIEW_WIDTH<92&&y-topPos>=24&&y-topPos<49&&menu.ready()){
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);minecraft.player.playSound(R2Sounds.PECH_DICE.get(),.5f,.95f+minecraft.level.random.nextFloat()*.1f);return true;
        }
        return super.mouseClickedFitted(x,y,button);
    }
}
