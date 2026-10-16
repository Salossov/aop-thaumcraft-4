package thaumcraft.items;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.api.*;
import thaumcraft.api.aspects.Aspect;

public final class ThaumicArmorItem extends ArmorItem implements IRepairable, IRunicArmor, IVisDiscountGear {
    public enum Kind { THAUMIUM, VOID, ROBE, FORTRESS, VOID_ROBE, TRAVELLER, HARNESS }
    private final Kind kind;

    public ThaumicArmorItem(Holder<ArmorMaterial> material, Type type, Kind kind, int durability) {
        super(material, type, properties(kind,durability));
        this.kind = kind;
    }

    public Kind kind() { return kind; }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flags){super.appendHoverText(stack,context,lines,flags);int discount=getVisDiscount(stack,null,null);if(discount>0)lines.add(net.minecraft.network.chat.Component.translatable("tc.visdiscount").append(": "+discount+"%").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));if(kind==Kind.HARNESS)lines.add(net.minecraft.network.chat.Component.translatable("tc.visdiscount").append(" (Aer): 5%").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));}
    @Override public net.minecraft.world.InteractionResult onItemUseFirst(ItemStack stack,net.minecraft.world.item.context.UseOnContext context){
        var level=context.getLevel();var pos=context.getClickedPos();var state=level.getBlockState(pos);
        if(kind==Kind.ROBE&&state.is(net.minecraft.world.level.block.Blocks.WATER_CAULDRON)){
            if(!level.isClientSide){stack.remove(net.minecraft.core.component.DataComponents.DYED_COLOR);net.minecraft.world.level.block.LayeredCauldronBlock.lowerFillLevel(state,level,pos);}
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.onItemUseFirst(stack,context);
    }
    private static Properties properties(Kind kind,int durability){
        var p=new Properties().durability(durability).rarity(Rarity.RARE);if(kind==Kind.ROBE)p.component(net.minecraft.core.component.DataComponents.DYED_COLOR,new net.minecraft.world.item.component.DyedItemColor(6961280,true));return p;
    }
    @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level,Player player,net.minecraft.world.InteractionHand hand){
        if(kind!=Kind.HARNESS)return super.use(level,player,hand);
        if(player instanceof net.minecraft.server.level.ServerPlayer server){
            int slot=hand==net.minecraft.world.InteractionHand.OFF_HAND?40:player.getInventory().selected;
            server.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,owner)->new thaumcraft.menu.HarnessMenu(id,inventory,slot),getName(player.getItemInHand(hand))),buf->buf.writeVarInt(slot));
        }
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide);
    }
    public int getRunicCharge(ItemStack stack) { return 0; }
    public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
        return switch (kind) {
            case ROBE -> getType() == Type.BOOTS ? 1 : 2;
            case VOID_ROBE -> 5;
            case HARNESS -> aspect != null && aspect.getTag().equals("aer") ? 5 : 2;
            default -> 0;
        };
    }

    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if(kind==Kind.VOID_ROBE)stack.remove(net.minecraft.core.component.DataComponents.DYED_COLOR);
        if (!level.isClientSide && (kind == Kind.VOID || kind == Kind.VOID_ROBE)
                && entity.tickCount % 20 == 0 && stack.isDamaged()) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }
}
