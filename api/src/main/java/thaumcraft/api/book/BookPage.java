package thaumcraft.api.book;

import com.google.gson.*;
import java.util.*;
import net.minecraft.resources.ResourceLocation;

/** Coordinates use a 139x199 logical page; x/y=-1 participate in vertical flow. */
public record BookPage(String id,String type,List<Element> elements,JsonObject data) {
    public record Element(String kind,String value,int x,int y,int width,int height,float scale,int count,boolean translate,List<String> tooltip){
        public Element {if(!Set.of("text","image","item","aspect","recipe","link","rule").contains(kind))throw new IllegalArgumentException("Unknown page element "+kind);if(x< -1||y< -1||x>139||y>199||width<1||width>139||height<1||height>199||!Float.isFinite(scale)||scale<=0||scale>4||count<0)throw new IllegalArgumentException("Invalid page geometry");tooltip=List.copyOf(tooltip);Objects.requireNonNull(value);}
    }
    public BookPage {ResourceLocation.parse(id);ResourceLocation.parse(type);elements=List.copyOf(elements);data=data==null?new JsonObject():data.deepCopy();}
    @Override public JsonObject data(){return data.deepCopy();}
    public static BookPage read(String id,JsonObject json){var elements=new ArrayList<Element>();if(json.has("elements"))for(var entry:json.getAsJsonArray("elements")){var e=entry.getAsJsonObject();var tips=new ArrayList<String>();if(e.has("tooltip"))e.getAsJsonArray("tooltip").forEach(t->tips.add(t.getAsString()));elements.add(new Element(str(e,"kind","text"),str(e,"value",""),num(e,"x",-1),num(e,"y",-1),num(e,"width",139),num(e,"height",9),e.has("scale")?e.get("scale").getAsFloat():1,num(e,"count",1),!e.has("translate")||e.get("translate").getAsBoolean(),tips));}return new BookPage(id,str(json,"type","thaumcraft:layout"),elements,json.has("data")?json.getAsJsonObject("data"):new JsonObject());}
    private static String str(JsonObject j,String k,String fallback){return j.has(k)?j.get(k).getAsString():fallback;}
    private static int num(JsonObject j,String k,int fallback){return j.has(k)?j.get(k).getAsInt():fallback;}
}
