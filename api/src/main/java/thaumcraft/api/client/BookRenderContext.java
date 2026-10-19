package thaumcraft.api.client;

import java.util.List;
import net.minecraft.client.gui.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;

public interface BookRenderContext {
    GuiGraphics graphics();Font font();int x();int y();
    void item(ItemStack stack,int x,int y);
    void aspect(Aspect aspect,int amount,int x,int y);
    void recipe(String id,int x,int y);
    void link(Component text,String research,int x,int y);
    void tooltip(int x,int y,int width,int height,List<Component> lines);
    default void image(ResourceLocation texture,int x,int y,int width,int height){graphics().flush();com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();graphics().blit(texture,x()+x,y()+y,0,0,width,height,width,height);}
}
