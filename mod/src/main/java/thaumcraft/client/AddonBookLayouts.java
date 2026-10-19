package thaumcraft.client;

import java.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.book.*;
import thaumcraft.api.client.*;
import thaumcraft.api.aspects.Aspect;

/** Logical-page layout. The fitted screen supplies the same transform as legacy pages. */
final class AddonBookLayouts {
    interface ContextFactory {BookRenderContext create(GuiGraphics g,int x,int y,BookPage page);}
    record Positioned(BookPage.Element element,int x,int y){}
    record Canvas(BookPage page,List<Positioned> elements,int height,ContextFactory context) implements BookPageLayout.Element {
        @Override public void draw(GuiGraphics g,Font font,int x,int y){var c=context.create(g,x,y,page);var custom=BookRenderers.get(page.type());if(custom!=null){custom.render(page,c);return;}for(var p:elements){var e=p.element();int xx=p.x(),yy=p.y();String value=e.translate()?I18n.get(e.value()):e.value();switch(e.kind()){
            case "text"->g.drawString(font,BookPageLayout.formatted(value),x+xx,y+yy,ResearchGraphics.INK,false);
            case "image"->c.image(ResourceLocation.parse(e.value()),xx,yy,Math.max(1,(int)(e.width()*e.scale())),Math.max(1,(int)(e.height()*e.scale())));
            case "item"->{var item=BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(e.value()));if(item.isPresent())c.item(new ItemStack(item.get(),Math.max(1,e.count())),xx,yy);}
            case "aspect"->c.aspect(Aspect.getAspect(e.value()),e.count(),xx,yy);
            case "recipe"->c.recipe(e.value(),xx,yy);
            case "link"->c.link(Component.translatable("tc.research_name."+e.value()),e.value(),xx,yy);
            case "rule"->g.fill(x+xx,y+yy,x+xx+e.width(),y+yy+1,ResearchGraphics.INK);
        }if(!e.tooltip().isEmpty())c.tooltip(xx,yy,e.width(),e.height(),e.tooltip().stream().map(Component::translatable).map(t->(Component)t).toList());}}
    }
    static List<Canvas> layout(BookPage page,Font font,int firstHeight,ContextFactory context){
        if(!page.type().equals("thaumcraft:layout"))return List.of(new Canvas(page,List.of(),firstHeight,context));
        var pages=new ArrayList<Canvas>();var elements=new ArrayList<Positioned>();int cursor=0,capacity=firstHeight;
        for(var e:page.elements()){
            var parts=new ArrayList<BookPage.Element>();if(e.kind().equals("text")){String text=e.translate()?I18n.get(e.value()):e.value();for(var line:font.getSplitter().splitLines(BookPageLayout.formatted(text),Math.min(e.width(),139-Math.max(0,e.x())),net.minecraft.network.chat.Style.EMPTY)){String value=line.getString();parts.add(new BookPage.Element("text",value,e.x(),e.y(),e.width(),9,1,e.count(),false,e.tooltip()));}}else parts.add(e);
            int manualLine=0;for(var part:parts){int h=part.kind().equals("item")||part.kind().equals("aspect")?16:part.kind().equals("recipe")?Math.max(160,part.height()):part.kind().equals("image")?(int)Math.ceil(part.height()*part.scale()):part.height();
                if(part.y()<0&&cursor+h>capacity&&!elements.isEmpty()){pages.add(new Canvas(page,List.copyOf(elements),capacity,context));elements.clear();cursor=0;capacity=199;}
                int xx=Math.max(0,part.x()),yy=part.y()<0?cursor:part.y()+manualLine;elements.add(new Positioned(part,xx,yy));if(part.y()<0)cursor+=h;else manualLine+=h;
            }
        }
        if(!elements.isEmpty()||pages.isEmpty())pages.add(new Canvas(page,List.copyOf(elements),capacity,context));return List.copyOf(pages);
    }
    private AddonBookLayouts(){}
}
