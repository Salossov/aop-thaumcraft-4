package thaumcraft.api.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import thaumcraft.api.book.BookPage;

/** Client-only callback; context methods preserve native item/aspect tooltips and links. */
public interface BookPageRenderer {
    void render(BookPage page,BookRenderContext context);
    default boolean mouseClicked(BookPage page,double x,double y,int button){return false;}
    default void mouseMoved(BookPage page,double x,double y){}
}
