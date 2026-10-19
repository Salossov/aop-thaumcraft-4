package thaumcraft.verification;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectApi;
import thaumcraft.api.book.CatalogValidator;
import thaumcraft.api.crafting.*;
import thaumcraft.data.ThaumcraftData;

/** Release inventory, using the extracted 4.2.3.5 catalog rather than expected port counts alone. */
final class ReleaseInventoryVerification {
    static void run(ServerLevel level, BiConsumer<Boolean, String> check) throws Exception {
        var root = Path.of(System.getProperty("thaumcraft.r3.root"));
        for (String optional : List.of("thaumcraft_viewers", "jei", "emi")) {
            check.accept(!ModList.get().isLoaded(optional), "core runs without optional mod " + optional);
        }
        var originals = JsonParser.parseString(Files.readString(root.resolve(
            "analysis/thaumcraft4-4.2.3.5/r0/catalog/aspects.json"))).getAsJsonArray();
        var byConstant = new HashMap<String, String>();
        for (var entry : originals) {
            var row = entry.getAsJsonObject();
            byConstant.put(row.get("constant").getAsString(), row.get("tag").getAsString());
        }
        for (var entry : originals) {
            var row = entry.getAsJsonObject();
            String tag = row.get("tag").getAsString();
            var actual = Aspect.getAspect(tag);
            check.accept(actual != null && actual.getColor() == row.get("color").getAsInt(),
                "original aspect identity/color: " + tag);
            var expected = new ArrayList<String>();
            row.getAsJsonArray("components").forEach(c -> expected.add(byConstant.get(c.getAsString())));
            check.accept(actual.getComponents().stream().map(Aspect::getTag).toList().equals(expected),
                "original aspect components: " + tag);
        }
        var definitions = ThaumcraftData.get(level.getServer()).research();
        CatalogValidator.validate(definitions, ThaumcraftData.books(level.getServer()));
        check.accept(true, "live book catalog references and parent cycles validated");
        var sourceResearch = JsonParser.parseString(Files.readString(root.resolve(
            "analysis/thaumcraft4-4.2.3.5/r0/catalog/research.json"))).getAsJsonObject();
        var sourceKeys = new HashSet<String>();
        sourceResearch.getAsJsonArray("definitions").forEach(r -> sourceKeys.add(r.getAsJsonObject().get("key").getAsString()));
        check.accept(sourceKeys.containsAll(definitions.keySet()), "all active core research keys originate in TC4");
        var optionalMetalResearch = Set.of("CAP_silver", "PURELEAD", "PURESILVER", "PURETIN", "TRANSLEAD", "TRANSSILVER", "TRANSTIN");
        for (String key : sourceKeys) check.accept(definitions.containsKey(key) || optionalMetalResearch.contains(key),
            "original research present or conditional on external metal tags: " + key);
        for (var definition : definitions.values()) {
            check.accept(ThaumcraftData.books(level.getServer()).categories().containsKey(definition.category()),
                "active research category: " + definition.key());
        }
        var manifest = JsonParser.parseString(Files.readString(root.resolve(
            "analysis/comparison/manifest.json"))).getAsJsonObject();
        var absentDependencies = new ArrayList<String>();
        for (var entry : manifest.getAsJsonArray("stations")) {
            var row = entry.getAsJsonObject();
            String id = row.get("native_recipe").getAsString();
            var recipe = level.getRecipeManager().byKey(ResourceLocation.parse(id));
            if (id.equals("thaumcraft:void_robe_dye")) {
                check.accept(recipe.isEmpty(), "approved fixed-purple void robe has no dye recipe");
            } else if (recipe.isEmpty() && !row.getAsJsonArray("native_conditions").isEmpty()) {
                absentDependencies.add(id);
                check.accept(true, "conditional external-material recipe inactive: " + id);
            } else {
                check.accept(recipe.isPresent(), "original recipe registration available: " + row.get("id").getAsString() + " -> " + id);
            }
        }
        for (var holder : level.getRecipeManager().getRecipes()) {
            if (!holder.id().getNamespace().equals("thaumcraft")) continue;
            String research = holder.value() instanceof IArcaneRecipe r ? r.research()
                : holder.value() instanceof CrucibleRecipe r ? r.research()
                : holder.value() instanceof InfusionRecipe r ? r.research() : "";
            check.accept(research.isEmpty() || definitions.containsKey(research),
                "loaded recipe research resolves: " + holder.id());
        }
        var displayIds = new HashSet<ResourceLocation>();
        var kinds = EnumSet.noneOf(RecipeDisplay.Kind.class);
        for (var display : RecipeDisplays.recipes(level)) {
            check.accept(displayIds.add(display.id()), "unique effective recipe display: " + display.id());
            kinds.add(display.kind());
        }
        check.accept(kinds.equals(EnumSet.allOf(RecipeDisplay.Kind.class)), "core provides all seven display kinds without a viewer");
        check.accept(AspectApi.variants().size() == originals.size() * 5, "filled aspect variants available without integration");
        var summary = new LinkedHashMap<String, Object>();
        summary.put("original_research", sourceKeys.size());
        summary.put("active_research", definitions.size());
        summary.put("inactive_research", sourceKeys.stream().filter(k -> !definitions.containsKey(k)).sorted().toList());
        summary.put("original_recipe_registrations", manifest.getAsJsonArray("stations").size());
        summary.put("conditional_recipes_without_dependencies", absentDependencies);
        summary.put("approved_recipe_omissions", List.of("thaumcraft:void_robe_dye"));
        summary.put("blocks", BuiltInRegistries.BLOCK.keySet().stream().filter(id -> id.getNamespace().equals("thaumcraft")).count());
        summary.put("items", BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getNamespace().equals("thaumcraft")).count());
        summary.put("entity_types", BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(id -> id.getNamespace().equals("thaumcraft")).count());
        summary.put("block_ids", BuiltInRegistries.BLOCK.keySet().stream().filter(id -> id.getNamespace().equals("thaumcraft")).map(Object::toString).sorted().toList());
        summary.put("item_ids", BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getNamespace().equals("thaumcraft")).map(Object::toString).sorted().toList());
        summary.put("entity_ids", BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(id -> id.getNamespace().equals("thaumcraft")).map(Object::toString).sorted().toList());
        summary.put("recipe_displays", displayIds.size());
        summary.put("loaded_mods", ModList.get().getMods().stream().map(m -> m.getModId()).sorted().toList());
        Files.writeString(root.resolve("analysis/release-inventory.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(summary));
    }
}
