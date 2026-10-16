package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class GolemFishing {
    private static final Item[] JUNK={Items.LEATHER_BOOTS,Items.LEATHER,Items.BONE,Items.GLASS_BOTTLE,Items.STRING,Items.FISHING_ROD,Items.BOWL,Items.STICK,Items.INK_SAC,Items.TRIPWIRE_HOOK,Items.ROTTEN_FLESH};
    private static final int[] JUNK_WEIGHTS={10,10,10,10,5,2,10,5,5,10,10};
    private static final Item[] RARE={Items.LILY_PAD,Items.NAME_TAG,Items.SADDLE,Items.BOW,Items.FISHING_ROD,Items.BOOK};
    private static final Item[] FISH={Items.COD,Items.SALMON,Items.TROPICAL_FISH,Items.PUFFERFISH};
    private static Item choose(Item[] values,int[] weights,net.minecraft.util.RandomSource random){int total=0;for(int weight:weights)total+=weight;int roll=random.nextInt(total);for(int i=0;i<weights.length;i++){roll-=weights[i];if(roll<0)return values[i];}return values[values.length-1];}
    public static ItemStack loot(ThaumicGolem golem,ServerLevel level,BlockPos target){
        float junk=.1f-golem.upgrade(5)*.025f,rare=.05f+golem.upgrade(4)*.0125f;
        for(var face:net.minecraft.core.Direction.Plane.HORIZONTAL){var pos=target.relative(face);if(!level.getFluidState(pos).is(FluidTags.WATER)||!level.isEmptyBlock(pos.above()))continue;junk-=.005f;rare+=.00125f;if(level.canSeeSky(pos.above())){junk-=.005f;rare+=.00125f;}for(int y=1;y<=3;y++)if(level.getFluidState(pos.below(y)).is(FluidTags.WATER))rare+=.001f;}
        junk=net.minecraft.util.Mth.clamp(junk,0,1);rare=net.minecraft.util.Mth.clamp(rare,0,1);float roll=level.random.nextFloat();ItemStack result;
        if(roll<junk){var item=choose(JUNK,JUNK_WEIGHTS,level.random);result=new ItemStack(item,item==Items.INK_SAC?10:1);if(item==Items.LEATHER_BOOTS||item==Items.FISHING_ROD)damage(result,.9f,level.random);}
        else if(roll<junk+rare){var item=RARE[level.random.nextInt(RARE.length)];result=new ItemStack(item);if(item==Items.BOW||item==Items.FISHING_ROD){damage(result,.25f,level.random);result=EnchantmentHelper.enchantItem(level.random,result,30,level.registryAccess(),java.util.Optional.empty());}else if(item==Items.BOOK)result=EnchantmentHelper.enchantItem(level.random,result,30,level.registryAccess(),java.util.Optional.empty());}
        else result=new ItemStack(choose(FISH,new int[]{60,25,2,13},level.random));
        if(golem.upgrade(2)>0){var input=new SingleRecipeInput(result);var recipe=level.getRecipeManager().getRecipeFor(RecipeType.SMELTING,input,level);if(recipe.isPresent())result=recipe.get().value().assemble(input,level.registryAccess());}
        return result;
    }
    private static void damage(ItemStack stack,float fraction,net.minecraft.util.RandomSource random){
        int maximum=stack.getMaxDamage(),limit=Math.max(1,(int)(fraction*maximum));
        stack.setDamageValue(Math.max(1,Math.min(limit,maximum-random.nextInt(random.nextInt(limit)+1))));
    }
    private GolemFishing(){}
}
