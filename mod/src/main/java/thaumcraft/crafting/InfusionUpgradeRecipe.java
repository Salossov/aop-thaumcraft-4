package thaumcraft.crafting;

import java.util.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import thaumcraft.api.crafting.InfusionRecipe;

/** TC4's NBT-output infusion recipes upgrade the existing item, including enchantments and durability. */
public final class InfusionUpgradeRecipe extends InfusionRecipe {
    private final String field;
    private final int value;
    public InfusionUpgradeRecipe(String research,Ingredient input,List<Ingredient> components,ItemStack preview,Map<String,Integer> aspects,int instability,String field,int value){
        super(research,input,components,preview,aspects,instability);
        if(!Set.of("goggles","mask","advanced").contains(field)||value<0||value>2)throw new IllegalArgumentException("Unknown TC4 infusion upgrade");
        this.field=field;this.value=value;
    }
    public String field(){return field;}
    public int value(){return value;}
    @Override public boolean matches(thaumcraft.api.crafting.InfusionInput contents){var tag=contents.central().getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return (!tag.contains(field)||tag.getInt(field)!=value)&&super.matches(contents);}
    @Override public ItemStack getRecipeOutput(ItemStack central){
        var output=central.copy();CustomData.update(DataComponents.CUSTOM_DATA,output,t->t.putInt(field,value));return output;
    }
    public RecipeType<?> getType(){return R3Recipes.INFUSION_TYPE.get();}
    public RecipeSerializer<?> getSerializer(){return R3Recipes.INFUSION_UPGRADE.get();}
    public static final class Serializer implements RecipeSerializer<InfusionUpgradeRecipe>{
        private static final MapCodec<InfusionUpgradeRecipe> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            Codec.STRING.fieldOf("research").forGetter(InfusionUpgradeRecipe::research),Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(InfusionUpgradeRecipe::input),
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("components").forGetter(InfusionUpgradeRecipe::components),ItemStack.STRICT_CODEC.fieldOf("result").forGetter(InfusionUpgradeRecipe::getRecipeOutput),
            Codec.unboundedMap(Codec.STRING,Codec.INT).fieldOf("aspects").forGetter(InfusionUpgradeRecipe::aspects),Codec.INT.fieldOf("instability").forGetter(InfusionUpgradeRecipe::instability),
            Codec.STRING.fieldOf("field").forGetter(InfusionUpgradeRecipe::field),Codec.INT.fieldOf("value").forGetter(InfusionUpgradeRecipe::value)
        ).apply(i,InfusionUpgradeRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,InfusionUpgradeRecipe> STREAM=new StreamCodec<>(){
            public InfusionUpgradeRecipe decode(RegistryFriendlyByteBuf b){
                var research=b.readUtf(128);var input=Ingredient.CONTENTS_STREAM_CODEC.decode(b);int count=b.readVarInt();if(count<1||count>64)throw new IllegalArgumentException("Invalid upgrade components");
                var components=new ArrayList<Ingredient>();for(int n=0;n<count;n++)components.add(Ingredient.CONTENTS_STREAM_CODEC.decode(b));
                var result=ItemStack.STREAM_CODEC.decode(b);int length=b.readVarInt();if(length<0||length>48)throw new IllegalArgumentException("Invalid upgrade aspects");
                var aspects=new LinkedHashMap<String,Integer>();for(int n=0;n<length;n++)if(aspects.put(b.readUtf(128),b.readVarInt())!=null)throw new IllegalArgumentException("Duplicate aspect");
                return new InfusionUpgradeRecipe(research,input,components,result,aspects,b.readVarInt(),b.readUtf(128),b.readVarInt());
            }
            public void encode(RegistryFriendlyByteBuf b,InfusionUpgradeRecipe recipe){
                b.writeUtf(recipe.research(),128);Ingredient.CONTENTS_STREAM_CODEC.encode(b,recipe.input());b.writeVarInt(recipe.components().size());for(var item:recipe.components())Ingredient.CONTENTS_STREAM_CODEC.encode(b,item);
                ItemStack.STREAM_CODEC.encode(b,recipe.getRecipeOutput());b.writeVarInt(recipe.aspects().size());recipe.aspects().forEach((tag,n)->{b.writeUtf(tag,128);b.writeVarInt(n);});
                b.writeVarInt(recipe.instability());b.writeUtf(recipe.field,32);b.writeVarInt(recipe.value);
            }
        };
        public MapCodec<InfusionUpgradeRecipe> codec(){return CODEC;}
        public StreamCodec<RegistryFriendlyByteBuf,InfusionUpgradeRecipe> streamCodec(){return STREAM;}
    }
}
