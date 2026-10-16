package thaumcraft.mixin;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import thaumcraft.client.CreativeWandGroups;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeWandScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow private static CreativeModeTab selectedTab;
    @Shadow private float scrollOffs;
    @Shadow private EditBox searchBox;
    @Shadow protected abstract void refreshSearchResults();
    @Unique private final CreativeWandGroups tcGroups=new CreativeWandGroups();
    @Unique private final thaumcraft.client.CreativeSections tcSections=new thaumcraft.client.CreativeSections();
    @Unique private Button tcGroupButton;
    @Unique private final java.util.List<Button> tcSectionButtons=new java.util.ArrayList<>();
    @Unique private record TcAnchor(ItemStack stack,int offset,int row){}
    protected CreativeWandScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu,Inventory inventory,Component title){super(menu,inventory,title);}
    @Unique private boolean tcTab(){return net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.getKey(selectedTab).equals(net.minecraft.resources.ResourceLocation.parse("thaumcraft:thaumcraft"));}
    @Inject(method="init",at=@At("RETURN")) private void tcInit(CallbackInfo ci){
        tcSectionButtons.clear();tcGroupButton=addRenderableWidget(new thaumcraft.client.CreativeSideButton(leftPos-20,topPos+4,20,18,Component.literal("≡"),button->{var anchor=tcAnchor();tcGroups.next();tcModeTooltip();refreshSearchResults();tcRestore(anchor);},()->Component.translatable("thaumcraft.creative.group.current",tcGroups.modeName()),null));
        tcGroupButton.visible=tcTab();tcModeTooltip();
        for(int section=0;section<8;section++){final int value=section;var label=Component.translatable("thaumcraft.creative.section."+section);var button=addRenderableWidget(new thaumcraft.client.CreativeSideButton(leftPos-20,topPos+24+section*14,20,14,label,b->tcJump(value),()->label,()->thaumcraft.client.CreativeSectionIcons.section(value)));button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(label));button.visible=tcTab();tcSectionButtons.add(button);}tcSearchLayout();
    }
    @Unique private void tcSearchLayout(){if(tcTab()){searchBox.setX(leftPos+82);searchBox.setWidth(88);searchBox.setHint(Component.translatable("itemGroup.search"));}}
    @Unique private void tcJump(int section){searchBox.setValue("");refreshSearchResults();int row=tcSections.headers().entrySet().stream().filter(e->e.getValue()==section).mapToInt(java.util.Map.Entry::getKey).findFirst().orElse(0);int rows=Math.max(0,(menu.items.size()+8)/9-5);scrollOffs=rows==0?0:Math.clamp(row/(float)rows,0,1);menu.scrollTo(scrollOffs);}
    @Redirect(method="renderTabButton",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/CreativeModeTab;getIconItem()Lnet/minecraft/world/item/ItemStack;")) private ItemStack tcIcon(CreativeModeTab tab){return net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab).equals(net.minecraft.resources.ResourceLocation.parse("thaumcraft:thaumcraft"))?thaumcraft.client.CreativeSectionIcons.tab():tab.getIconItem();}
    @Unique private void tcModeTooltip(){tcGroupButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("thaumcraft.creative.group.current",tcGroups.modeName())));}
    @Unique private int tcRow(){return Math.max(0,Math.round(scrollOffs*Math.max(0,(menu.items.size()+8)/9-5)));}
    @Unique private boolean tcBlank(Slot slot){return tcTab()&&slot!=null&&slot.index>=0&&slot.index<45&&tcSections.placeholder(tcRow()*9+slot.index);}
    @Override protected void renderSlot(GuiGraphics g,Slot slot){if(!tcBlank(slot))super.renderSlot(g,slot);}
    @Override protected boolean isHovering(int x,int y,int w,int h,double mouseX,double mouseY){if(w==16&&h==16&&tcTab())for(int i=0;i<45;i++){var slot=menu.slots.get(i);if(slot.x==x&&slot.y==y&&tcBlank(slot))return false;}return super.isHovering(x,y,w,h,mouseX,mouseY);}
    @Inject(method="mouseClicked",at=@At("HEAD"),cancellable=true) private void tcBlankClick(double x,double y,int button,CallbackInfoReturnable<Boolean> cir){if(tcTab())for(int i=0;i<45;i++){var slot=menu.slots.get(i);if(tcBlank(slot)&&x>=leftPos+slot.x-1&&x<leftPos+slot.x+17&&y>=topPos+slot.y-1&&y<topPos+slot.y+17){cir.setReturnValue(true);return;}}}
    @Unique private TcAnchor tcAnchor(){int row=tcRow();for(int i=row*9;i<menu.items.size()&&i<(row+5)*9;i++)if(!menu.items.get(i).isEmpty())return new TcAnchor(menu.items.get(i).copy(),i/9-row,row);return new TcAnchor(ItemStack.EMPTY,0,row);}
    @Unique private void tcRestore(TcAnchor anchor){int row=anchor.row();if(!anchor.stack().isEmpty()){int found=-1;for(int i=0;i<menu.items.size();i++)if(ItemStack.isSameItemSameComponents(anchor.stack(),menu.items.get(i))){found=i;break;}if(found<0)for(int i=0;i<menu.items.size();i++)if(tcGroups.sameGroup(anchor.stack(),menu.items.get(i))){found=i;break;}if(found>=0)row=found/9-anchor.offset();}int rows=Math.max(0,(menu.items.size()+8)/9-5);scrollOffs=rows==0?0:Math.clamp(row/(float)rows,0,1);menu.scrollTo(scrollOffs);}
    @Inject(method="selectTab",at=@At("RETURN")) private void tcSelected(CreativeModeTab tab,CallbackInfo ci){if(tcGroupButton!=null)tcGroupButton.visible=tcTab();for(var button:tcSectionButtons)button.visible=tcTab();tcSearchLayout();}
    @Inject(method="refreshSearchResults",at=@At("RETURN")) private void tcFilter(CallbackInfo ci){
        if(!tcTab())return;var grouped=tcSections.project(tcGroups.project(List.copyOf(menu.items)),!searchBox.getValue().isEmpty());menu.items.clear();menu.items.addAll(grouped);scrollOffs=0;menu.scrollTo(0);
    }
    @Inject(method="slotClicked",at=@At("HEAD"),cancellable=true) private void tcExpand(Slot slot,int slotId,int button,ClickType click,CallbackInfo ci){
        if(tcTab()&&slotId>=0&&slotId<45&&tcSections.placeholder(tcRow()*9+slotId)){ci.cancel();return;}
        if(tcTab()&&slot!=null&&slotId>=0&&slotId<45&&button==1&&click==ClickType.PICKUP){var stack=slot.getItem().copy();if(tcGroups.toggle(stack)){refreshSearchResults();int index=0;while(index<menu.items.size()&&!tcGroups.sameGroup(stack,menu.items.get(index)))index++;int rows=Math.max(0,(menu.items.size()+8)/9-5);scrollOffs=rows==0?0:Math.clamp((index/9-slotId/9)/(float)rows,0,1);menu.scrollTo(scrollOffs);ci.cancel();}}
    }
    @Inject(method="getTooltipFromContainerItem",at=@At("RETURN")) private void tcTooltip(ItemStack stack,CallbackInfoReturnable<List<Component>> cir){
        if(tcTab()&&getSlotUnderMouse()!=null&&getSlotUnderMouse().index>=0&&getSlotUnderMouse().index<45&&tcGroups.count(stack)>1)cir.getReturnValue().add(tcGroups.description(stack).copy().withStyle(net.minecraft.ChatFormatting.GOLD));
    }
    @Inject(method="renderBg",at=@At("RETURN")) private void tcSectionBands(GuiGraphics g,float partial,int mouseX,int mouseY,CallbackInfo ci){
        if(!tcTab())return;int firstRow=tcRow();for(var header:tcSections.headers().entrySet()){
            int row=header.getKey()-firstRow;if(row<0||row>=5)continue;var slot=menu.slots.get(row*9);int x=leftPos+slot.x-1,y=topPos+slot.y-1;
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.blit(net.minecraft.resources.ResourceLocation.parse("thaumcraft:textures/gui/creative_section_"+header.getValue()+".png"),x,y,0,0,163,18,163,18);
            
        }
    }
    @Inject(method="render",at=@At("TAIL")) private void tcBadges(GuiGraphics g,int mouseX,int mouseY,float partial,CallbackInfo ci){
        if(!tcTab())return;g.pose().pushPose();g.pose().translate(0,0,310);var title=selectedTab.getDisplayName();float titleScale=Math.min(1,77f/Math.max(1,font.width(title)));g.pose().pushPose();g.pose().translate(leftPos+8,topPos+6,0);g.pose().scale(titleScale,titleScale,1);g.drawString(font,title,0,0,selectedTab.getLabelColor(),false);g.pose().popPose();int firstRow=tcRow();for(var header:tcSections.headers().entrySet()){int row=header.getKey()-firstRow;if(row<0||row>=5)continue;var slot=menu.slots.get(row*9);int x=leftPos+slot.x-1,y=topPos+slot.y-1;var label=Component.translatable("thaumcraft.creative.section."+header.getValue());float scale=Math.min(1,132f/Math.max(1,font.width(label)));g.pose().pushPose();g.pose().translate(x+10,y+(18-font.lineHeight*scale)/2,0);g.pose().scale(scale,scale,1);g.drawString(font,label,0,0,0xffffe0a0,false);g.pose().popPose();}for(int i=0;i<45;i++){var slot=menu.slots.get(i);if(tcGroups.collapsed(slot.getItem())){String count="+"+(tcGroups.count(slot.getItem())-1);g.pose().pushPose();g.pose().translate(leftPos+slot.x+17,topPos+slot.y+11,0);g.pose().scale(.5f,.5f,1);g.drawString(font,count,-font.width(count),0,0xffffff55,true);g.pose().popPose();}}g.pose().popPose();
    }
}
