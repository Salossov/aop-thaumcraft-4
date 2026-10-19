package thaumcraft.addon;

import com.google.gson.*;
import java.util.*;
import net.minecraft.nbt.*;
import thaumcraft.api.book.*;
import thaumcraft.api.research.ResearchDefinition;

public final class BookData {
    private static final Gson GSON=new Gson();
    public static void encode(CompoundTag target,BookCatalog catalog){var root=new JsonObject();var categories=new JsonArray();catalog.categories().values().forEach(c->categories.add(GSON.toJsonTree(c)));var pages=new JsonArray();catalog.pages().values().forEach(p->pages.add(GSON.toJsonTree(p)));root.add("categories",categories);root.add("pages",pages);target.putString("addonBookCatalog",root.toString());}
    public static BookCatalog decode(CompoundTag data){if(!data.contains("addonBookCatalog"))return BookCatalog.EMPTY;var root=JsonParser.parseString(data.getString("addonBookCatalog")).getAsJsonObject();var cats=new LinkedHashMap<String,BookCategory>();var pages=new LinkedHashMap<String,BookPage>();for(var entry:root.getAsJsonArray("categories")){var c=GSON.fromJson(entry,BookCategory.class);cats.put(c.id(),c);}for(var entry:root.getAsJsonArray("pages")){var p=GSON.fromJson(entry,BookPage.class);PageTypes.validate(p);pages.put(p.id(),p);}return new BookCatalog(cats,pages);}
    public static BookCategory category(String id,JsonObject j){return new BookCategory(id,j.has("name")?j.get("name").getAsString():"category."+id.replace(':','.'),j.get("icon").getAsString(),j.has("background")?j.get("background").getAsString():"thaumcraft:textures/gui/gui_researchback.png",j.has("order")?j.get("order").getAsInt():100,j.has("required_research")?j.get("required_research").getAsString():"");}
    public static void researchDefaults(JsonObject j){for(var key:List.of("parents","text_pages","recipe_pages"))if(!j.has(key))j.add(key,new JsonArray());if(!j.has("aspects"))j.add("aspects",new JsonObject());for(var key:List.of("auto_unlock","concealed"))if(!j.has(key))j.addProperty(key,false);for(var key:List.of("complexity","column","row"))if(!j.has(key))j.addProperty(key,0);}
    private BookData(){}
}
