package thaumcraft.client;

import java.util.*;
import java.util.regex.Pattern;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

/** TC4's BR, LINE and IMG markup; pictures reserve their original scaled height. */
final class BookPageLayout {
    private static final Pattern INSERT=Pattern.compile("(?i)<LINE\\s*/?>|<IMG>(.*?)</IMG>",Pattern.DOTALL);
    private static final Pattern MARKER=Pattern.compile("\\u0001(\\d+)\\u0002");
    interface Element {int height();void draw(GuiGraphics g,Font font,int x,int y);}
    private record Text(FormattedCharSequence value) implements Element {
        public int height(){return 9;}public void draw(GuiGraphics g,Font font,int x,int y){g.drawString(font,value,x,y,ResearchGraphics.INK,false);}
    }
    private record Rule() implements Element {
        public int height(){return 9;}public void draw(GuiGraphics g,Font font,int x,int y){ThaumonomiconScreen.bookSprite(g,x+139/2-48,y+2,24,184,96,4);}
    }
    private record Picture(ResourceLocation texture,int u,int v,int width,int pixels,float scale) implements Element {
        public int height(){return Math.max(1,(int)Math.ceil(pixels*scale));}
        public void draw(GuiGraphics g,Font font,int x,int y){com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.pose().pushPose();g.pose().translate(x-3+139/2f-width*scale/2,y,0);g.pose().scale(scale,scale,1);g.blit(texture,0,0,u,v,width,pixels,256,256);g.pose().popPose();}
    }
    static List<Element> parse(Font font,String source) {
        // TCFontRenderer's wrapping retained the BR newline and added another.
        String text=source.replaceAll("(?i)<BR\\s*/?>","\n");var inserts=new ArrayList<Element>();var matcher=INSERT.matcher(text);var expanded=new StringBuffer();
        while(matcher.find()) {
            Element insert=new Rule();if(matcher.group(1)!=null){var parts=matcher.group(1).trim().split(":");try {
                if(parts.length!=7)throw new IllegalArgumentException();var id=ResourceLocation.fromNamespaceAndPath(parts[0],parts[1]);
                int u=Integer.parseInt(parts[2]),v=Integer.parseInt(parts[3]),w=Integer.parseInt(parts[4]),h=Integer.parseInt(parts[5]);float scale=Float.parseFloat(parts[6]);
                if(u<0||v<0||w<=0||h<=0||w>4096||h>4096||!Float.isFinite(scale)||scale<=0||scale>4)throw new IllegalArgumentException();
                insert=new Picture(id,u,v,w,h,scale);
            }catch(IllegalArgumentException bad){matcher.appendReplacement(expanded,"");continue;}}
            int index=inserts.size();inserts.add(insert);matcher.appendReplacement(expanded,"\n\u0001"+index+"\u0002\n");
        }
        matcher.appendTail(expanded);var result=new ArrayList<Element>();
        // The old wrapper kept one newline and consumed the next if adjacent.
        // BR followed by IMG therefore reserves one blank row, not three.
        String wrapped=Pattern.compile("\n+").matcher(expanded.toString()).replaceAll(match->"\n".repeat((match.group().length()+1)/2*2)).replaceAll("\n+$","");
        for(var line:wrapped.split("\n",-1)) {
            var marker=MARKER.matcher(line);if(marker.matches()){result.add(inserts.get(Integer.parseInt(marker.group(1))));continue;}
            line=line.replaceAll("<[^>]*>","");if(line.isEmpty()){result.add(new Text(FormattedCharSequence.EMPTY));continue;}
            // The original TCFontRenderer enabled Unicode glyphs for research text.
            var component=formatted(line);
            for(var row:font.split(component,139))result.add(new Text(row));
        }
        return List.copyOf(result);
    }
    static Component formatted(String text) {
        var base=net.minecraft.network.chat.Style.EMPTY;
        if(thaumcraft.config.ThaumcraftClientConfig.BOOK_FONT_STYLE.get()==thaumcraft.config.ThaumcraftClientConfig.BookFontStyle.UNICODE)base=base.withFont(ResourceLocation.withDefaultNamespace("uniform"));
        var result=Component.empty();var style=base;var run=new StringBuilder();
        for(int i=0;i<text.length();i++) {
            char ch=text.charAt(i);
            if(ch=='§'&&i+1<text.length()) {
                var format=net.minecraft.ChatFormatting.getByCode(text.charAt(++i));
                if(format!=null) {if(!run.isEmpty()){result.append(Component.literal(run.toString()).setStyle(style));run.setLength(0);}style=format==net.minecraft.ChatFormatting.RESET?base:style.applyLegacyFormat(format);}
            } else run.append(ch);
        }
        if(!run.isEmpty())result.append(Component.literal(run.toString()).setStyle(style));
        return result;
    }
    private BookPageLayout() {}
}
