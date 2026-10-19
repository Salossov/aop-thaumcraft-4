package thaumcraft.crafting;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class R2Recipes {
    private static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.conditions.ICondition>> CONDITIONS=
            DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.CONDITION_CODECS,"thaumcraft");
    private static final net.neoforged.neoforge.registries.DeferredHolder<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.conditions.ICondition>,com.mojang.serialization.MapCodec<thaumcraft.api.crafting.MirrorsEnabledCondition>> MIRRORS=
            CONDITIONS.register("mirrors_enabled",()->thaumcraft.api.crafting.MirrorsEnabledCondition.CODEC);
    private static final DeferredRegister<RecipeType<?>> TYPES=DeferredRegister.create(Registries.RECIPE_TYPE,"thaumcraft");
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,"thaumcraft");
    public static final DeferredHolder<RecipeType<?>,RecipeType<thaumcraft.api.crafting.IArcaneRecipe>> ARCANE_TYPE=TYPES.register("arcane",()->new RecipeType<>() { public String toString() { return "thaumcraft:arcane"; } });
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<ArcaneRecipe>> ARCANE_SERIALIZER=SERIALIZERS.register("arcane",ArcaneRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<RefillInkRecipe>> REFILL_INK=SERIALIZERS.register("refill_ink",()->new SimpleCraftingRecipeSerializer<>(RefillInkRecipe::new));
    public static void register(IEventBus bus) { TYPES.register(bus);SERIALIZERS.register(bus);CONDITIONS.register(bus); }
    private R2Recipes() {}
}
