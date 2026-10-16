package thaumcraft.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import thaumcraft.api.aspects.*;

public final class EssentiaResonatorItem extends Item {
    public EssentiaResonatorItem() { super(new Properties().stacksTo(1)); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel(); var face = context.getClickedFace();
        var transport = level.getCapability(EssentiaCapabilities.TRANSPORT, context.getClickedPos(), face);
        if (transport == null || context.getPlayer() == null) return InteractionResult.PASS;
        if (!level.isClientSide) {
            Aspect essence = transport.getEssentiaType(face), demand = transport.getSuctionType(face);
            context.getPlayer().displayClientMessage(Component.translatable("thaumcraft.resonator.reading", essence == null ? "-" : essence.getName(), transport.getEssentiaAmount(face), demand == null ? "-" : demand.getName(), transport.getSuctionAmount(face)).withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
