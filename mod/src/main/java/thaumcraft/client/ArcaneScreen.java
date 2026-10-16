package thaumcraft.client;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import thaumcraft.api.aspects.Aspects;
import thaumcraft.crafting.R2Recipes;
import thaumcraft.effects.TaintEffects;
import thaumcraft.items.WandVis;
import thaumcraft.menu.ArcaneMenu;
import thaumcraft.network.ClientKnowledge;
import thaumcraft.registry.R2Content;

public final class ArcaneScreen extends FittedContainerScreen<ArcaneMenu> {
    private static final int[][] POS={{72,21},{24,43},{24,102},{72,124},{120,102},{120,43}};
    public ArcaneScreen(ArcaneMenu menu,Inventory inventory,Component title) {super(menu,inventory,title);imageWidth=190;imageHeight=234;}
    @Override protected void renderLabels(GuiGraphics g,int x,int y) {}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.blit(ResearchGraphics.texture("gui/gui_arcaneworkbench"),leftPos,topPos,0,0,imageWidth,imageHeight,256,256);if(minecraft.level==null)return;
        var stacks=new ArrayList<ItemStack>();for(int i=0;i<9;i++)stacks.add(menu.table.getItem(i));var input=CraftingInput.ofPositioned(3,3,stacks).input();
        var recipe=minecraft.level.getRecipeManager().getRecipeFor(R2Recipes.ARCANE_TYPE.get(),input,minecraft.level);if(recipe.isEmpty()||!recipe.get().value().research().isEmpty()&&!ClientKnowledge.snapshot().completedResearch().contains(recipe.get().value().research()))return;
        var wand=menu.table.getItem(10);var amounts=recipe.get().value().vis(input);var cost=WandVis.cost(wand,amounts,minecraft.player);boolean sufficient=WandVis.consume(wand,amounts,minecraft.player,false);
        for(int i=0;i<6;i++) {var aspect=Aspects.PRIMALS.get(i);int amount=cost.getOrDefault(aspect,0);if(amount<=0)continue;int x=leftPos+POS[i][0]-8,y=topPos+POS[i][1]-8;
            ResearchGraphics.aspect(g,aspect,x,y,WandVis.get(wand,aspect)>=amount?1:0.4f);g.pose().pushPose();g.pose().translate(x+8,y+16,0);g.pose().scale(0.5f,0.5f,1);g.drawCenteredString(font,String.format(java.util.Locale.ROOT,"%.2f",amount/100f),0,0,ResearchGraphics.WHITE);g.pose().popPose();}
        if(!sufficient) {g.drawCenteredString(font,Component.translatable("thaumcraft.arcane.insufficient_vis"),leftPos+95,topPos+138,0xffee6e6e);g.renderFakeItem(recipe.get().value().getResultItem(minecraft.level.registryAccess()),leftPos+160,topPos+64);}
    }
    @Override protected void renderFitted(GuiGraphics g,int x,int y,float partial) {renderBackground(g,x,y,partial);super.renderFitted(g,x,y,partial);renderTooltip(g,x,y);}
}
