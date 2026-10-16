package thaumcraft.crafting;
import java.util.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.crafting.*;
import thaumcraft.api.crafting.*;

/** Original TC4 enchantment upgrade costs, incompatibilities and one-level output. */
public final class InfusionEnchantmentRecipeData extends InfusionRecipe {
    private final Holder<Enchantment> enchantment;
    public InfusionEnchantmentRecipeData(String research,Holder<Enchantment> enchantment,List<Ingredient> ingredients,Map<String,Integer> aspects,int instability){
        super(research,Ingredient.of(Items.BOOK),ingredients,new ItemStack(Items.BOOK),aspects,instability);this.enchantment=enchantment;
    }
    public Holder<Enchantment> enchantment(){return enchantment;}
    private ItemEnchantments enchants(ItemStack central){return EnchantmentHelper.getEnchantmentsForCrafting(central);}
    @Override public boolean matches(InfusionInput input){
        var central=input.central();if(!central.isDamageableItem()||!enchantment.value().canEnchant(central))return false;
        var existing=enchants(central);if(existing.getLevel(enchantment)>=enchantment.value().getMaxLevel())return false;
        for(var e:existing.keySet())if(!e.equals(enchantment)&&!Enchantment.areCompatible(e,enchantment))return false;
        return input.components().size()==components().size()&&matchComponents(components(),input.components(),new boolean[components().size()],0);
    }
    @Override public ItemStack getRecipeOutput(ItemStack central){var output=central.copy();
        EnchantmentHelper.updateEnchantments(output,mutable->mutable.set(enchantment,enchants(central).getLevel(enchantment)+1));return output;
    }
    @Override public Map<String,Integer> aspects(ItemStack central){var existing=enchants(central);float modifier=existing.getLevel(enchantment);
        for(var e:existing.entrySet())if(!e.getKey().equals(enchantment))modifier+=e.getIntValue()*.1f;
        final float multiplier=modifier;var result=new LinkedHashMap<String,Integer>();aspects().forEach((tag,n)->result.put(tag,n+(int)(n*multiplier)));return result;
    }
    @Override public int instability(ItemStack central){int levels=0;for(var e:enchants(central).entrySet())levels+=e.getIntValue();return instability()+levels/2;}
    @Override public int experienceCost(ItemStack central){return Math.max(1,enchantment.value().getMinCost(1)/3)*(1+enchants(central).getLevel(enchantment));}
    public RecipeSerializer<?> getSerializer(){return R3Recipes.INFUSION_ENCHANTMENT.get();}
    public RecipeType<?> getType(){return R3Recipes.INFUSION_TYPE.get();}
    public static final class Serializer implements RecipeSerializer<InfusionEnchantmentRecipeData>{
        private static final MapCodec<InfusionEnchantmentRecipeData> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            Codec.STRING.fieldOf("research").forGetter(InfusionEnchantmentRecipeData::research),Enchantment.CODEC.fieldOf("enchantment").forGetter(InfusionEnchantmentRecipeData::enchantment),
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("components").forGetter(InfusionEnchantmentRecipeData::components),Codec.unboundedMap(Codec.STRING,Codec.INT).fieldOf("aspects").forGetter(InfusionEnchantmentRecipeData::aspects),
            Codec.INT.fieldOf("instability").forGetter(InfusionEnchantmentRecipeData::instability)).apply(i,InfusionEnchantmentRecipeData::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,InfusionEnchantmentRecipeData> STREAM=new StreamCodec<>(){
            public InfusionEnchantmentRecipeData decode(RegistryFriendlyByteBuf b){String research=b.readUtf(128);var enchantment=Enchantment.STREAM_CODEC.decode(b);int count=b.readVarInt();if(count<1||count>64)throw new IllegalArgumentException("Invalid ingredients");var ingredients=new ArrayList<Ingredient>();for(int j=0;j<count;j++)ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(b));int size=b.readVarInt();if(size<1||size>48)throw new IllegalArgumentException("Invalid aspects");var aspects=new LinkedHashMap<String,Integer>();for(int j=0;j<size;j++)aspects.put(b.readUtf(32),b.readVarInt());return new InfusionEnchantmentRecipeData(research,enchantment,ingredients,aspects,b.readVarInt());}
            public void encode(RegistryFriendlyByteBuf b,InfusionEnchantmentRecipeData r){b.writeUtf(r.research(),128);Enchantment.STREAM_CODEC.encode(b,r.enchantment);b.writeVarInt(r.components().size());for(var ingredient:r.components())Ingredient.CONTENTS_STREAM_CODEC.encode(b,ingredient);b.writeVarInt(r.aspects().size());r.aspects().forEach((tag,n)->{b.writeUtf(tag,32);b.writeVarInt(n);});b.writeVarInt(r.instability());}
        };
        public MapCodec<InfusionEnchantmentRecipeData> codec(){return CODEC;}
        public StreamCodec<RegistryFriendlyByteBuf,InfusionEnchantmentRecipeData> streamCodec(){return STREAM;}
    }
}
