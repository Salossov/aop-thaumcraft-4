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

/** Native shaped crafting, preserving TC4's first-ingot output for nine of its own nuggets. */
public final class TagShapedRecipe extends ShapedRecipe {
    private final ResourceLocation outputTag;
    private final int count;
    public TagShapedRecipe(ShapedRecipePattern pattern,ResourceLocation outputTag,int count){super("",CraftingBookCategory.MISC,pattern,ItemStack.EMPTY);this.outputTag=outputTag;this.count=count;}
    @Override public ItemStack getResultItem(HolderLookup.Provider lookup){return TagOutput.first(lookup,outputTag,count);}
    @Override public boolean matches(CraftingInput input,Level level){return !getResultItem(level.registryAccess()).isEmpty()&&super.matches(input,level);}
    @Override public RecipeSerializer<?> getSerializer(){return R3Recipes.TAG_SHAPED.get();}
    public static final class Serializer implements RecipeSerializer<TagShapedRecipe> {
        private static final MapCodec<TagShapedRecipe> CODEC=RecordCodecBuilder.mapCodec(i->i.group(ShapedRecipePattern.MAP_CODEC.forGetter(r->r.pattern),ResourceLocation.CODEC.fieldOf("result_tag").forGetter(r->r.outputTag),Codec.intRange(1,64).optionalFieldOf("count",1).forGetter(r->r.count)).apply(i,TagShapedRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,TagShapedRecipe> STREAM=StreamCodec.composite(ShapedRecipePattern.STREAM_CODEC,r->r.pattern,ResourceLocation.STREAM_CODEC,r->r.outputTag,ByteBufCodecs.VAR_INT,r->r.count,TagShapedRecipe::new);
        @Override public MapCodec<TagShapedRecipe> codec(){return CODEC;}
        @Override public StreamCodec<RegistryFriendlyByteBuf,TagShapedRecipe> streamCodec(){return STREAM;}
    }
}
