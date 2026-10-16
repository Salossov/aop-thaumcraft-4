package thaumcraft.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumcraft.registry.R2Content;

public final class RefillInkRecipe extends CustomRecipe {
    public RefillInkRecipe(CraftingBookCategory category) { super(category); }
    @Override public boolean matches(CraftingInput input,Level level) {
        boolean ink=false,dye=false;for(var stack:input.items()) {
            if(stack.isEmpty()) continue;
            if(stack.is(R2Content.SCRIBING_TOOLS)&&!ink) ink=true;
            else if(stack.is(Items.BLACK_DYE)&&!dye) dye=true;else return false;
        }return ink&&dye;
    }
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries) {
        for(var stack:input.items()) if(stack.is(R2Content.SCRIBING_TOOLS)) { var result=stack.copyWithCount(1);result.setDamageValue(0);return result; }return ItemStack.EMPTY;
    }
    @Override public boolean canCraftInDimensions(int width,int height) { return width*height>=2; }
    @Override public RecipeSerializer<?> getSerializer() { return R2Recipes.REFILL_INK.get(); }
}
