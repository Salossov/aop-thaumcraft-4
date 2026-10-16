package thaumcraft.crafting;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class R3Recipes {
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "thaumcraft");
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "thaumcraft");
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<TagSmeltingRecipe>> TAG_SMELTING=SERIALIZERS.register("tag_smelting",TagSmeltingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<TagShapedRecipe>> TAG_SHAPED=SERIALIZERS.register("tag_shaped",TagShapedRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<RobeDyeRecipe>> ROBE_DYE=SERIALIZERS.register("robe_dye",()->new SimpleCraftingRecipeSerializer<>(category->new RobeDyeRecipe(category,false)));
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<RobeDyeRecipe>> VOID_ROBE_DYE=SERIALIZERS.register("void_robe_dye",()->new SimpleCraftingRecipeSerializer<>(category->new RobeDyeRecipe(category,true)));
    public static final DeferredHolder<RecipeType<?>,RecipeType<InfernalBonusRecipe>> INFERNAL_BONUS_TYPE=TYPES.register("infernal_bonus",()->new RecipeType<>(){public String toString(){return "thaumcraft:infernal_bonus";}});
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<InfernalBonusRecipe>> INFERNAL_BONUS_SERIALIZER=SERIALIZERS.register("infernal_bonus",InfernalBonusRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<thaumcraft.api.crafting.CrucibleRecipe>> CRUCIBLE_TYPE = TYPES.register("crucible", () -> new RecipeType<>() {
        public String toString() { return "thaumcraft:crucible"; }
    });
    public static final DeferredHolder<RecipeType<?>, RecipeType<thaumcraft.api.crafting.InfusionRecipe>> INFUSION_TYPE = TYPES.register("infusion", () -> new RecipeType<>() { public String toString(){return "thaumcraft:infusion";} });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfusionRecipeData>> INFUSION_SERIALIZER = SERIALIZERS.register("infusion", InfusionRecipeData.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrucibleRecipeData>> CRUCIBLE_SERIALIZER = SERIALIZERS.register("crucible", CrucibleRecipeData.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ArcaneShapelessRecipe>> ARCANE_SHAPELESS = SERIALIZERS.register("arcane_shapeless", ArcaneShapelessRecipe.Serializer::new);
    private static final DeferredRegister<net.neoforged.neoforge.common.crafting.IngredientType<?>> INGREDIENTS = DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.INGREDIENT_TYPES, "thaumcraft");
    public static final DeferredHolder<net.neoforged.neoforge.common.crafting.IngredientType<?>,net.neoforged.neoforge.common.crafting.IngredientType<RawOreIngredient>> RAW_ORE_INGREDIENT=INGREDIENTS.register("raw_ore_enabled",()->new net.neoforged.neoforge.common.crafting.IngredientType<>(RawOreIngredient.CODEC));
    public static final DeferredHolder<net.neoforged.neoforge.common.crafting.IngredientType<?>, net.neoforged.neoforge.common.crafting.IngredientType<BlueDyeIngredient>> BLUE_DYE = INGREDIENTS.register("blue_dye", () -> new net.neoforged.neoforge.common.crafting.IngredientType<>(BlueDyeIngredient.CODEC));
    public static final DeferredHolder<net.neoforged.neoforge.common.crafting.IngredientType<?>,net.neoforged.neoforge.common.crafting.IngredientType<CopperIngredient>> COPPER_INGREDIENT=INGREDIENTS.register("copper_enabled",()->new net.neoforged.neoforge.common.crafting.IngredientType<>(CopperIngredient.CODEC));
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<ArcaneWandRecipe>> WAND_ASSEMBLY=SERIALIZERS.register("wand_assembly",ArcaneWandRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<InfusionEnchantmentRecipeData>> INFUSION_ENCHANTMENT=SERIALIZERS.register("infusion_enchantment",InfusionEnchantmentRecipeData.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<InfusionRunicRecipe>> INFUSION_RUNIC=SERIALIZERS.register("infusion_runic",InfusionRunicRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<InfusionUpgradeRecipe>> INFUSION_UPGRADE=SERIALIZERS.register("infusion_upgrade",InfusionUpgradeRecipe.Serializer::new);
    public static void register(IEventBus bus) { TYPES.register(bus); SERIALIZERS.register(bus); INGREDIENTS.register(bus); }
    private R3Recipes() {}
}
