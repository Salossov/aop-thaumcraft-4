package thaumcraft.crafting;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** A normal furnace recipe whose result is the first available ingot in a material tag. */
public final class TagSmeltingRecipe extends SmeltingRecipe {
    private final Ingredient input;
    private final ResourceLocation outputTag;
    private final int count;
    public TagSmeltingRecipe(Ingredient input,ResourceLocation outputTag,int count,float experience,int time){super("",CookingBookCategory.MISC,input,ItemStack.EMPTY,experience,time);this.input=input;this.outputTag=outputTag;this.count=count;}
    @Override public ItemStack getResultItem(HolderLookup.Provider lookup){return TagOutput.first(lookup,outputTag,count);}
    @Override public ItemStack assemble(SingleRecipeInput input,HolderLookup.Provider lookup){return getResultItem(lookup).copy();}
    @Override public boolean matches(SingleRecipeInput input,Level level){return !getResultItem(level.registryAccess()).isEmpty()&&super.matches(input,level);}
    @Override public RecipeSerializer<?> getSerializer(){return R3Recipes.TAG_SMELTING.get();}
    public static final class Serializer implements RecipeSerializer<TagSmeltingRecipe> {
        private static final MapCodec<TagSmeltingRecipe> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r->r.input),ResourceLocation.CODEC.fieldOf("result_tag").forGetter(r->r.outputTag),Codec.intRange(1,64).fieldOf("count").forGetter(r->r.count),Codec.FLOAT.fieldOf("experience").forGetter(TagSmeltingRecipe::getExperience),Codec.intRange(1,100000).fieldOf("cookingtime").forGetter(TagSmeltingRecipe::getCookingTime)).apply(i,TagSmeltingRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,TagSmeltingRecipe> STREAM=StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC,r->r.input,ResourceLocation.STREAM_CODEC,r->r.outputTag,ByteBufCodecs.VAR_INT,r->r.count,ByteBufCodecs.FLOAT,TagSmeltingRecipe::getExperience,ByteBufCodecs.VAR_INT,TagSmeltingRecipe::getCookingTime,TagSmeltingRecipe::new);
        @Override public MapCodec<TagSmeltingRecipe> codec(){return CODEC;}
        @Override public StreamCodec<RegistryFriendlyByteBuf,TagSmeltingRecipe> streamCodec(){return STREAM;}
    }
}
