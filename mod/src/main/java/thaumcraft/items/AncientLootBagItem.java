package thaumcraft.items;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import thaumcraft.loot.LegacyLoot;

public final class AncientLootBagItem extends Item {
    private final int rarity;
    public AncientLootBagItem(int rarity){super(new Properties().stacksTo(16).rarity(rarity==2?Rarity.RARE:rarity==1?Rarity.UNCOMMON:Rarity.COMMON));this.rarity=rarity;}
    public int rarity(){return rarity;}
    @Override public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level l,Player p,InteractionHand hand){var stack=p.getItemInHand(hand);if(l instanceof ServerLevel server){int rolls=8+l.random.nextInt(5);for(int i=0;i<rolls;i++)l.addFreshEntity(new ItemEntity(l,p.getX(),p.getY(),p.getZ(),LegacyLoot.generate(server,rarity,l.random)));l.playSound(null,p.blockPosition(),thaumcraft.registry.R2Sounds.COINS.get(),net.minecraft.sounds.SoundSource.PLAYERS,.75f,1);stack.shrink(1);}return InteractionResultHolder.sidedSuccess(stack,l.isClientSide);}
    @Override public void appendHoverText(ItemStack stack,TooltipContext c,List<Component> lines,TooltipFlag flag){super.appendHoverText(stack,c,lines,flag);lines.add(Component.translatable("tc.lootbag"));}
}
