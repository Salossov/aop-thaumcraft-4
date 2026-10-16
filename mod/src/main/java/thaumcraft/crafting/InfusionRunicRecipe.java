package thaumcraft.crafting;

import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.crafting.*;
import thaumcraft.equipment.RunicShield;
import thaumcraft.registry.R3Equipment;

/** Dynamic hardening recipe preserves all other components of the central item. */
public final class InfusionRunicRecipe extends InfusionRecipe {
    public InfusionRunicRecipe(){super("RUNICAUGMENTATION",Ingredient.of(Items.DIAMOND),List.of(Ingredient.of(Items.DIAMOND)),new ItemStack(Items.DIAMOND),Map.of("potentia",32),5);}
    @Override public List<Ingredient> components(ItemStack central){
        var items=new ArrayList<Ingredient>();items.add(Ingredient.of(Items.DIAMOND));
        for(int i=0;i<=RunicShield.capacity(central);i++)items.add(Ingredient.of(R3Equipment.SALIS_MUNDUS.get()));
        return List.copyOf(items);
    }
    @Override public Map<String,Integer> aspects(ItemStack central){int amount=(int)(32*Math.pow(2,RunicShield.capacity(central)));return Map.of("tutamen",amount/2,"praecantatio",amount/2,"potentia",amount);}
    @Override public int instability(ItemStack central){return 5+RunicShield.capacity(central)/2;}
    @Override public boolean matches(InfusionInput input){
        if(!(input.central().getItem() instanceof IRunicArmor))return false;
        var required=components(input.central());return required.size()==input.components().size()&&matchComponents(required,input.components(),new boolean[required.size()],0);
    }
    @Override public ItemStack getRecipeOutput(ItemStack central){var output=central.copy();RunicShield.harden(output);return output;}
    public RecipeType<?> getType(){return R3Recipes.INFUSION_TYPE.get();}
    public RecipeSerializer<?> getSerializer(){return R3Recipes.INFUSION_RUNIC.get();}
    public static final class Serializer implements RecipeSerializer<InfusionRunicRecipe>{
        // The rule is stateless; its cost and output are derived from the central item.
        // StreamCodec.unit would reject the separate instance created by JSON loading.
        private static final StreamCodec<RegistryFriendlyByteBuf,InfusionRunicRecipe> STREAM_CODEC =
            StreamCodec.of((buffer,recipe)->{},buffer->new InfusionRunicRecipe());
        public MapCodec<InfusionRunicRecipe> codec(){return MapCodec.unit(InfusionRunicRecipe::new);}
        public StreamCodec<RegistryFriendlyByteBuf,InfusionRunicRecipe> streamCodec(){return STREAM_CODEC;}
    }
}
