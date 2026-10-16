package thaumcraft.menu;

import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.entities.*;
import thaumcraft.registry.*;

/** Original one offered item and four result slots; the pack remains on the Pech. */
public final class PechMenu extends AbstractContainerMenu {
    public static final int PREVIEW_WIDTH=120;
    public static java.util.function.BiFunction<ServerLevel,Player,ItemStack> dungeonReward=thaumcraft.loot.PechDungeonRewards::generate;
    public final Pech pech;private final Player customer;public final SimpleContainer contents=new SimpleContainer(5);
    public PechMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer){this(id,inventory,(Pech)inventory.player.level().getEntity(buffer.readVarInt()));}
    public PechMenu(int id,Inventory inventory,Pech pech){super(R5Content.PECH_MENU.get(),id);this.pech=Objects.requireNonNull(pech);customer=inventory.player;if(!customer.level().isClientSide)pech.trading=true;addSlot(new Slot(contents,0,PREVIEW_WIDTH+36,29));for(int row=0;row<2;row++)for(int col=0;col<2;col++)addSlot(new Slot(contents,1+col+row*2,PREVIEW_WIDTH+106+col*18,20+row*18){@Override public boolean mayPlace(ItemStack stack){return false;}});for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+col+row*9,PREVIEW_WIDTH+8+col*18,84+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,PREVIEW_WIDTH+8+col*18,142));}
    public boolean ready(){return pech.valued(contents.getItem(0))&&java.util.stream.IntStream.range(1,5).allMatch(i->contents.getItem(i).isEmpty());}
    @Override public boolean clickMenuButton(Player player,int button){if(button!=0||player!=customer||!stillValid(player)||!ready()||!(player.level() instanceof ServerLevel level))return false;int value=pech.value(contents.getItem(0));var random=level.random;if(random.nextInt(100)<=value/2){pech.tamed(false);pech.playSound(R2Sounds.PECH_TRADE.get(),.4f,1);}if(random.nextInt(5)==0)value+=random.nextInt(3);else if(random.nextBoolean())value-=random.nextInt(3);var inventory=PechTrades.inventory(level,pech.variant());while(value>0){int tier=Math.min(5,Math.max((value+1)/2,random.nextInt(value)+1));value-=tier;if(tier==1&&random.nextBoolean()&&pech.loot.stream().anyMatch(s->!s.isEmpty())){var positions=new ArrayList<Integer>();for(int i=0;i<9;i++)if(!pech.loot.get(i).isEmpty())positions.add(i);var stack=pech.loot.get(positions.get(random.nextInt(positions.size())));mergeResult(stack.copyWithCount(1));stack.shrink(1);continue;}if(tier>=4&&random.nextBoolean()){var stack=dungeonReward.apply(level,player);if(stack.isEmpty()){value+=tier;continue;}mergeResult(stack);continue;}PechTrades.Offer offer;do{offer=inventory.get(random.nextInt(inventory.size()));}while(offer.tier()!=tier);var stack=offer.stack().copy();stack.getItem().onCraftedBy(stack,level,player);mergeResult(stack);}contents.removeItem(0,1);broadcastChanges();return true;}
    /** Results ignore input restrictions just like original ContainerPech.mergeItemStack. */
    private void mergeResult(ItemStack stack){if(stack.isStackable())for(int i=1;i<5&&!stack.isEmpty();i++){var existing=contents.getItem(i);if(!existing.isEmpty()&&ItemStack.isSameItemSameComponents(stack,existing)){int count=Math.min(stack.getCount(),stack.getMaxStackSize()-existing.getCount());existing.grow(count);stack.shrink(count);contents.setChanged();}}for(int i=1;i<5&&!stack.isEmpty();i++)if(contents.getItem(i).isEmpty()){contents.setItem(i,stack.copy());stack.setCount(0);}}
    @Override public boolean stillValid(Player player){return player==customer&&pech.isAlive()&&pech.tamed()&&player.distanceToSqr(pech)<=64;}
    @Override public ItemStack quickMoveStack(Player player,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();if(!moveItemStackTo(stack,index<5?5:0,index<5?41:1,index<5))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return original;}
    @Override public void removed(Player player){super.removed(player);pech.trading=false;if(!player.level().isClientSide)for(int i=0;i<5;i++){var stack=contents.removeItemNoUpdate(i);if(!stack.isEmpty()){var drop=player.drop(stack,false);if(drop!=null)drop.getPersistentData().putBoolean("PechDrop",true);}}}
}
