package thaumcraft.crafting;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class R2Recipes {
    private static final DeferredRegister<RecipeType<?>> TYPES=DeferredRegister.create(Registries.RECIPE_TYPE,"thaumcraft");
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,"thaumcraft");
    public static final DeferredHolder<RecipeType<?>,RecipeType<thaumcraft.api.crafting.IArcaneRecipe>> ARCANE_TYPE=TYPES.register("arcane",()->new RecipeType<>() { public String toString() { return "thaumcraft:arcane"; } });
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<ArcaneRecipe>> ARCANE_SERIALIZER=SERIALIZERS.register("arcane",ArcaneRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<RefillInkRecipe>> REFILL_INK=SERIALIZERS.register("refill_ink",()->new SimpleCraftingRecipeSerializer<>(RefillInkRecipe::new));
    public static void register(IEventBus bus) { TYPES.register(bus);SERIALIZERS.register(bus); }
    private R2Recipes() {}
}
