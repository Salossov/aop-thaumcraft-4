package thaumcraft.items;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.DoorBlock;
import thaumcraft.blocks.*;
import thaumcraft.registry.R2Sounds;

/** TC4 keys bind to a block position and grant use (iron) or administration (gold). */
public final class ArcaneKeyItem extends Item {
    public final int rank;
    public ArcaneKeyItem(boolean gold){super(new Properties().rarity(Rarity.UNCOMMON));rank=gold?1:0;}
    public static boolean bound(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().contains("KeyPosition");}
    @Override public boolean isFoil(ItemStack stack){return bound(stack);}
    @Override public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext c){
        var l=c.getLevel();var player=c.getPlayer();var s=l.getBlockState(c.getClickedPos());if(player==null||!(s.getBlock() instanceof ArcaneDoorBlock||s.getBlock() instanceof ArcanePressurePlateBlock))return InteractionResult.sidedSuccess(l.isClientSide);if(l.isClientSide)return InteractionResult.SUCCESS;
        var p=OwnedBlockInteractions.lower(l,c.getClickedPos());if(!(l.getBlockEntity(p) instanceof OwnedBlockEntity tile))return InteractionResult.CONSUME;var id=player.getUUID();boolean door=s.getBlock() instanceof ArcaneDoorBlock;
        if(!bound(stack)){
            if(!tile.ownedBy(id)&&!(rank==0&&tile.administers(id)))return InteractionResult.CONSUME;
            var key=new ItemStack(this);CustomData.update(DataComponents.CUSTOM_DATA,key,t->{t.putLong("KeyPosition",p.asLong());t.putByte("KeyType",(byte)(door?0:1));});if(!player.getInventory().add(key))player.drop(key,false);if(!player.isCreative())stack.shrink(1);player.displayClientMessage(Component.translatable(door?"tc.key1":"tc.key2"),false);l.playSound(null,p,R2Sounds.KEY.get(),SoundSource.BLOCKS,1,.9f);player.swing(c.getHand());
        }else if(!tile.ownedBy(id)&&!(rank==0?tile.permits(id):tile.administers(id))){
            var tag=stack.get(DataComponents.CUSTOM_DATA).copyTag();if(tag.getLong("KeyPosition")!=p.asLong()){player.displayClientMessage(Component.translatable("tc.key7"),false);return InteractionResult.CONSUME;}
            tile.grant(id,rank);if(door&&l.getBlockEntity(p.above()) instanceof OwnedBlockEntity upper)upper.copyAccess(tile);if(!player.isCreative())stack.shrink(1);player.displayClientMessage(Component.translatable(door?"tc.key3":"tc.key5").append(rank==1?Component.translatable(door?"tc.key4":"tc.key6"):Component.empty()),false);l.playSound(null,p,R2Sounds.KEY.get(),SoundSource.BLOCKS,1,1.1f);player.swing(c.getHand());
        }else player.displayClientMessage(Component.translatable("tc.key8"),false);
        return InteractionResult.CONSUME;
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flags){super.appendHoverText(stack,context,lines,flags);if(!bound(stack))return;var tag=stack.get(DataComponents.CUSTOM_DATA).copyTag();var p=net.minecraft.core.BlockPos.of(tag.getLong("KeyPosition"));lines.add(Component.translatable("tc.key9").withStyle(ChatFormatting.DARK_PURPLE,ChatFormatting.ITALIC));lines.add(Component.translatable(tag.getByte("KeyType")==0?"tc.key10":"tc.key11").withStyle(ChatFormatting.DARK_PURPLE));lines.add(Component.literal("x"+p.getX()+", z"+p.getZ()+", y"+p.getY()).withStyle(ChatFormatting.DARK_PURPLE));}
}
