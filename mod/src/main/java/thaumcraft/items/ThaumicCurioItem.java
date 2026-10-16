package thaumcraft.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.aspects.Aspects;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Original Baubles variants use the same necklace/ring/belt slots in Curios. */
public final class ThaumicCurioItem extends Item implements ICurioItem, IRunicArmor {
    public enum Kind {
        RING_MINOR(1), RING(5), RING_CHARGED(4), RING_HEALING(4),
        AMULET(8), AMULET_EMERGENCY(7), BELT(10), BELT_KINETIC(9),
        HOVER_GIRDLE(0), VIS_AMULET_LESSER(0), VIS_AMULET(0);
        final int shield;
        Kind(int shield) { this.shield = shield; }
    }
    private final Kind kind;
    public ThaumicCurioItem(Kind kind) {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE)); this.kind = kind;
    }
    public Kind kind() { return kind; }
    @Override public int getRunicCharge(ItemStack stack) { return kind.shield; }
    public boolean storesVis() { return kind == Kind.VIS_AMULET || kind == Kind.VIS_AMULET_LESSER; }
    public int capacity() { return kind == Kind.VIS_AMULET ? 25000 : 2500; }
    @Override public boolean canEquip(SlotContext context,ItemStack stack){if(kind!=Kind.VIS_AMULET)return true;if(context.entity() instanceof net.minecraft.server.level.ServerPlayer player)return thaumcraft.knowledge.KnowledgeService.get(player).completedResearch().contains("VISAMULET");return context.entity().level().isClientSide&&thaumcraft.network.ClientKnowledge.snapshot().completedResearch().contains("VISAMULET");}
    @Override public void curioTick(SlotContext context, ItemStack stack) {
        if (!(context.entity() instanceof Player player)) return;
        if (kind == Kind.HOVER_GIRDLE && !player.getAbilities().flying) {
            player.fallDistance = Math.max(0, player.fallDistance - .33f);
        }
        if (player.level().isClientSide || !storesVis() || player.tickCount % 5 != 0) return;
        var wand = player.getMainHandItem();
        if (wand.getItem() instanceof CastingWandItem) for (var aspect : Aspects.PRIMALS) {
            int amount = Math.min(5, Math.min(WandVis.get(stack, aspect), WandVis.capacity(wand) - WandVis.get(wand, aspect)));
            if (amount > 0) {
                WandVis.set(stack, aspect, WandVis.get(stack, aspect) - amount);
                WandVis.set(wand, aspect, WandVis.get(wand, aspect) + amount);
            }
        }
        var relay=thaumcraft.api.visnet.VisNetHandler.loadedNodes(player.level()).stream()
            .filter(node->node instanceof thaumcraft.visnet.VisRelayBlockEntity&&player.distanceToSqr(node.getBlockPos().getCenter())<26)
            .min(java.util.Comparator.comparingDouble(node->player.distanceToSqr(node.getBlockPos().getCenter()))).orElse(null);
        if(relay!=null)for(var aspect:Aspects.PRIMALS){int room=Math.min(5,capacity()-WandVis.get(stack,aspect));if(room>0){int amount=relay.consumeVis(aspect,room);if(amount>0)WandVis.set(stack,aspect,WandVis.get(stack,aspect)+amount);}}
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flags) {
        super.appendHoverText(stack, context, lines, flags);
        if (!storesVis()) return;
        lines.add(Component.translatable("item.capacity.text").append(" " + capacity() / 100).withStyle(ChatFormatting.GOLD));
        for (var aspect : Aspects.PRIMALS) lines.add(Component.literal(aspect.getName() + " " + WandVis.get(stack, aspect) / 100f).withStyle(ChatFormatting.AQUA));
    }
}
