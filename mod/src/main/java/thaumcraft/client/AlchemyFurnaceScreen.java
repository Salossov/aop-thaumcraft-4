package thaumcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.menu.AlchemyFurnaceMenu;

public final class AlchemyFurnaceScreen extends AbstractContainerScreen<AlchemyFurnaceMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/gui_alchemyfurnace.png");
    public AlchemyFurnaceScreen(AlchemyFurnaceMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Override protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int burn = menu.burnScaled(20); if (burn > 0) g.blit(TEXTURE, leftPos + 80, topPos + 46 - burn, 176, 20 - burn, 16, burn);
        int cook = menu.cookScaled(46); if (cook > 0) g.blit(TEXTURE, leftPos + 106, topPos + 59 - cook, 216, 46 - cook, 9, cook);
        int contents = menu.contentsScaled(48); if (contents > 0) g.blit(TEXTURE, leftPos + 61, topPos + 60 - contents, 200, 48 - contents, 8, contents);
        g.blit(TEXTURE, leftPos + 60, topPos + 8, 232, 0, 10, 55);
    }
    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {}
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) { super.render(g, mouseX, mouseY, partial); renderTooltip(g, mouseX, mouseY); }
}
