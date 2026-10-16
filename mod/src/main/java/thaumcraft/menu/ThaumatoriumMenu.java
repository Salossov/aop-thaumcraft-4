package thaumcraft.menu;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.network.ResearchActionPayload;
import thaumcraft.production.ThaumatoriumBlockEntity;
import thaumcraft.registry.R3Content;
public final class ThaumatoriumMenu extends AbstractContainerMenu implements ActionMenu {
 public final BlockPos pos;
 private final Container input;
 private final Player owner;
 private final UUID token;
 private long sequence;
 public ThaumatoriumMenu(int id,Inventory inventory,RegistryFriendlyByteBuf data){this(id,inventory,new SimpleContainer(1),data.readBlockPos(),data.readUUID());}
 public ThaumatoriumMenu(int id,Inventory inventory,ThaumatoriumBlockEntity tile){this(id,inventory,tile,tile.getBlockPos(),UUID.randomUUID());}
 private ThaumatoriumMenu(int id,Inventory inventory,Container input,BlockPos pos,UUID token){super(R3Content.THAUMATORIUM_MENU.get(),id);this.pos=pos;this.input=input;this.owner=inventory.player;this.token=token;addSlot(new Slot(input,0,48,16));for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,8+col*18,84+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,142));}
 @Override public UUID token(){return token;}
 public ItemStack catalyst(){return input.getItem(0);}
 @Override public boolean stillValid(Player p){return p==owner&&input.stillValid(p);}
 @Override public boolean accept(ServerPlayer p,ResearchActionPayload request){if(!token.equals(request.token())||request.sequence()!=sequence+1||!stillValid(p)||!(input instanceof ThaumatoriumBlockEntity tile))return false;sequence=request.sequence();if(request.action()!=1)return false;var id=ResourceLocation.tryParse(request.first());return id!=null&&tile.toggle(p,id);}
 @Override public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();if(!(index==0?moveItemStackTo(stack,1,37,true):moveItemStackTo(stack,0,1,false)))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;}
}
