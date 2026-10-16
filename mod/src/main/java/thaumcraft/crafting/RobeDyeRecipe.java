package thaumcraft.crafting;

import java.util.ArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumcraft.items.ThaumicArmorItem;

/** TC4's brightness-preserving mix uses the old sheep palette, not modern dye RGBs. */
public final class RobeDyeRecipe extends CustomRecipe {
    private static final float[][] PALETTE={{1,1,1},{.85f,.5f,.2f},{.7f,.3f,.85f},{.4f,.6f,.85f},{.9f,.9f,.2f},{.5f,.8f,.1f},{.95f,.5f,.65f},{.3f,.3f,.3f},{.6f,.6f,.6f},{.3f,.5f,.6f},{.5f,.25f,.7f},{.2f,.3f,.7f},{.4f,.3f,.2f},{.4f,.5f,.2f},{.6f,.2f,.2f},{.1f,.1f,.1f}};
    private final boolean voidRobe;
    public RobeDyeRecipe(CraftingBookCategory category,boolean voidRobe){super(category);this.voidRobe=voidRobe;}
    // Keep the old serializer readable, but fixed-colour void robes cannot consume dye.
    private boolean eligible(ItemStack stack){return !voidRobe&&stack.getItem() instanceof ThaumicArmorItem armor&&armor.kind()==ThaumicArmorItem.Kind.ROBE;}
    @Override public boolean matches(CraftingInput input,Level level){boolean robe=false,dye=false;for(var stack:input.items()){if(stack.isEmpty())continue;if(eligible(stack)){if(robe)return false;robe=true;}else if(stack.getItem() instanceof DyeItem)dye=true;else return false;}return robe&&dye;}
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries){
        ItemStack result=ItemStack.EMPTY;var dyes=new ArrayList<DyeItem>();
        for(var stack:input.items()){if(stack.isEmpty())continue;if(eligible(stack)){if(!result.isEmpty())return ItemStack.EMPTY;result=stack.copyWithCount(1);}else if(stack.getItem() instanceof DyeItem dye)dyes.add(dye);else return ItemStack.EMPTY;}
        if(result.isEmpty()||dyes.isEmpty())return ItemStack.EMPTY;
        int previous=DyedItemColor.getOrDefault(result,6961280),r=previous>>16&255,g=previous>>8&255,b=previous&255,brightness=Math.max(r,Math.max(g,b)),count=1;
        for(var dye:dyes){var rgb=PALETTE[dye.getDyeColor().getId()];int dr=(int)(rgb[0]*255),dg=(int)(rgb[1]*255),db=(int)(rgb[2]*255);r+=dr;g+=dg;b+=db;brightness+=Math.max(dr,Math.max(dg,db));count++;}
        r/=count;g/=count;b/=count;float average=(float)brightness/count,max=Math.max(r,Math.max(g,b));
        if(max>0){r=(int)(r*average/max);g=(int)(g*average/max);b=(int)(b*average/max);}
        result.set(DataComponents.DYED_COLOR,new DyedItemColor(r<<16|g<<8|b,true));return result;
    }
    @Override public boolean canCraftInDimensions(int width,int height){return width*height>=2;}
    @Override public RecipeSerializer<?> getSerializer(){return voidRobe?R3Recipes.VOID_ROBE_DYE.get():R3Recipes.ROBE_DYE.get();}
}
