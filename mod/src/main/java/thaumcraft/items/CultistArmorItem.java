package thaumcraft.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import thaumcraft.api.*;
import thaumcraft.api.aspects.Aspect;

/** Original iron cultist equipment and fortress-material leader equipment. */
public final class CultistArmorItem extends ArmorItem implements IRepairable,IRunicArmor,IVisDiscountGear,IWarpingGear {
    public enum Kind { PLATE,ROBE,LEADER,BOOTS }
    private final Kind kind;
    public CultistArmorItem(Type type,Kind kind){super(kind==Kind.LEADER?thaumcraft.registry.R3Equipment.ARMOR_MATERIALS.get("fortress"):ArmorMaterials.IRON,type,new Properties().durability(type.getDurability(kind==Kind.LEADER?40:15)).rarity(kind==Kind.LEADER?Rarity.RARE:Rarity.UNCOMMON));this.kind=kind;}
    public Kind kind(){return kind;}
    @Override public int getRunicCharge(ItemStack stack){return 0;}
    @Override public int getVisDiscount(ItemStack stack,Player player,Aspect aspect){return kind==Kind.ROBE||kind==Kind.BOOTS?1:0;}
    @Override public int getWarp(ItemStack stack,Player player){return kind==Kind.ROBE||kind==Kind.BOOTS?1:0;}
    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack ingredient){return ingredient.is(Items.IRON_INGOT);}
    @Override public ResourceLocation getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,ArmorMaterial.Layer layer,boolean inner){String texture=switch(kind){case PLATE->net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(ResourceLocation.parse("thaumcraft:inhabited_zombie"))?"zombie_plate_armor":"cultist_plate_armor";case ROBE->"cultist_robe_armor";case LEADER->"cultist_leader_armor";case BOOTS->"cultistboots";};return ResourceLocation.parse("thaumcraft:textures/models/"+texture+".png");}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){if(getVisDiscount(stack,null,null)>0)lines.add(net.minecraft.network.chat.Component.translatable("tc.visdiscount").append(": 1%").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));super.appendHoverText(stack,context,lines,flag);}
}
