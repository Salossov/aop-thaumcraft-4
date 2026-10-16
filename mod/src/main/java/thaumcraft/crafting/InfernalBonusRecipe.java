package thaumcraft.crafting;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public record InfernalBonusRecipe(Ingredient ingredient,ItemStack result,int baseCount) implements Recipe<SingleRecipeInput> {
    public InfernalBonusRecipe{if(baseCount<0||baseCount>64||result.isEmpty())throw new IllegalArgumentException("Invalid infernal bonus");result=result.copyWithCount(1);}
    @Override public boolean matches(SingleRecipeInput input,Level level){return ingredient.test(input.item());}
    @Override public ItemStack assemble(SingleRecipeInput input,HolderLookup.Provider registries){return result.copy();}
    @Override public boolean canCraftInDimensions(int width,int height){return false;}
    @Override public ItemStack getResultItem(HolderLookup.Provider registries){return result.copy();}
    @Override public RecipeSerializer<?> getSerializer(){return R3Recipes.INFERNAL_BONUS_SERIALIZER.get();}
    @Override public RecipeType<?> getType(){return R3Recipes.INFERNAL_BONUS_TYPE.get();}
    @Override public boolean isSpecial(){return true;}
    public static final class Serializer implements RecipeSerializer<InfernalBonusRecipe>{private static final MapCodec<InfernalBonusRecipe> CODEC=RecordCodecBuilder.mapCodec(instance->instance.group(Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(InfernalBonusRecipe::ingredient),ItemStack.STRICT_CODEC.fieldOf("result").forGetter(InfernalBonusRecipe::result),Codec.intRange(0,64).optionalFieldOf("base_count",0).forGetter(InfernalBonusRecipe::baseCount)).apply(instance,InfernalBonusRecipe::new));private static final StreamCodec<RegistryFriendlyByteBuf,InfernalBonusRecipe> STREAM=StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC,InfernalBonusRecipe::ingredient,ItemStack.STREAM_CODEC,InfernalBonusRecipe::result,ByteBufCodecs.VAR_INT,InfernalBonusRecipe::baseCount,InfernalBonusRecipe::new);@Override public MapCodec<InfernalBonusRecipe> codec(){return CODEC;}@Override public StreamCodec<RegistryFriendlyByteBuf,InfernalBonusRecipe> streamCodec(){return STREAM;}}
}
