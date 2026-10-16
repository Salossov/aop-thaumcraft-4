package thaumcraft.api.crafting;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;

/** Data-backed TC4 infusion. Research authorization belongs to the server-side ritual. */
public abstract class InfusionRecipe implements Recipe<InfusionInput> {
    private final String research;
    private final Ingredient input;
    private final List<Ingredient> components;
    private final ItemStack output;
    private final Map<String,Integer> aspects;
    private final int instability;
    public InfusionRecipe(String research,Ingredient input,List<Ingredient> components,ItemStack output,Map<String,Integer> aspects,int instability){
        if(research.isBlank()||output.isEmpty()||components.isEmpty()||components.size()>64||components.stream().anyMatch(Ingredient::isEmpty)||instability<0||instability>25)throw new IllegalArgumentException("Invalid infusion recipe");
        aspects.forEach((tag,n)->{if(Aspect.getAspect(tag)==null||n<=0||n>32767)throw new IllegalArgumentException("Invalid infusion essentia");});
        this.research=research;this.input=input;this.components=List.copyOf(components);this.output=output.copy();this.aspects=Collections.unmodifiableMap(new LinkedHashMap<>(aspects));this.instability=instability;
    }
    public String research(){return research;}
    public Ingredient input(){return input;}
    public List<Ingredient> components(){return components;}
    public List<Ingredient> components(ItemStack central){return components;}
    public Map<String,Integer> aspects(){return aspects;}
    public Map<String,Integer> aspects(ItemStack central){return aspects;}
    public int experienceCost(ItemStack central){return 0;}
    public boolean mayStart(net.minecraft.world.entity.player.Player player,ItemStack central){return true;}
    public int instability(){return instability;}
    public int instability(ItemStack central){return instability;}
    public ItemStack getRecipeOutput(){return output.copy();}
    public ItemStack getRecipeOutput(ItemStack central){return output.copy();}
    public boolean matches(InfusionInput contents){
        var requirements=components(contents.central());return input.test(contents.central())&&requirements.size()==contents.components().size()&&matchComponents(requirements,contents.components(),new boolean[requirements.size()],0);
    }
    protected static boolean matchComponents(List<Ingredient> required,List<ItemStack> supplied,boolean[] used,int n){
        if(n==required.size())return true;
        for(int i=0;i<supplied.size();i++)if(!used[i]&&required.get(n).test(supplied.get(i))){used[i]=true;if(matchComponents(required,supplied,used,n+1))return true;used[i]=false;}
        return false;
    }
    @Override public boolean matches(InfusionInput contents,Level level){return matches(contents);}
    @Override public ItemStack assemble(InfusionInput contents,HolderLookup.Provider registries){return getRecipeOutput(contents.central());}
    @Override public ItemStack getResultItem(HolderLookup.Provider registries){return getRecipeOutput();}
    @Override public boolean canCraftInDimensions(int w,int h){return false;}
    @Override public boolean isSpecial(){return true;}
    @Override public NonNullList<Ingredient> getIngredients(){var result=NonNullList.<Ingredient>create();result.add(input);result.addAll(components);return result;}
}
