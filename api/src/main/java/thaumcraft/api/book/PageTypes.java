package thaumcraft.api.book;

import com.google.gson.JsonObject;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

/** Register common data validation at startup; the renderer is registered separately on the client. */
public final class PageTypes {
    private static final Map<ResourceLocation,Consumer<JsonObject>> TYPES=new LinkedHashMap<>();
    static{TYPES.put(ResourceLocation.parse("thaumcraft:layout"),data->{});}
    public static synchronized void register(ResourceLocation id,Consumer<JsonObject> validator){if(TYPES.putIfAbsent(id,Objects.requireNonNull(validator))!=null)throw new IllegalArgumentException("Duplicate page type "+id);}
    public static void validate(BookPage page){var validator=TYPES.get(ResourceLocation.parse(page.type()));if(validator==null)throw new IllegalArgumentException("Unknown page type "+page.type());validator.accept(page.data());}
    private PageTypes(){}
}
