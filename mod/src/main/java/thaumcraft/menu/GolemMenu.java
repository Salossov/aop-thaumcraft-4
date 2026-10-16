package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.entities.ThaumicGolem;
import thaumcraft.registry.R3Content;

public final class GolemMenu extends AbstractContainerMenu {
    public final ThaumicGolem golem;private int page;
    private final ContainerData data=new SimpleContainerData(22);
    private final SimpleContainer view;
    public GolemMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer){this(id,inventory,entity(inventory,buffer.readVarInt()));}
    private static ThaumicGolem entity(Inventory inventory,int id){if(inventory.player.level().getEntity(id) instanceof ThaumicGolem golem)return golem;throw new IllegalArgumentException("Missing golem");}
    public GolemMenu(int id,Inventory inventory,ThaumicGolem golem){super(R3Content.GOLEM_MENU.get(),id);this.golem=golem;if(!inventory.player.level().isClientSide)golem.open();
        view=new SimpleContainer(6){@Override public ItemStack getItem(int slot){return golem.filters.getItem(slot+page*6);}@Override public void setItem(int slot,ItemStack stack){golem.filters.setItem(slot+page*6,stack);}@Override public boolean isEmpty(){return golem.filters.isEmpty();}};
        for(int i=0;i<6;i++){final int slot=i;addSlot(new Slot(view,i,100+i/2*28,16+i%2*31){@Override public boolean isActive(){return hasFilters()&&slot+page*6<count();}@Override public boolean mayPlace(ItemStack stack){return false;}@Override public boolean mayPickup(Player player){return false;}});}
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,8+col*18,84+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,142));addDataSlots(data);update();
    }
    public boolean hasFilters(){return java.util.Set.of(0,1,2,5,8).contains(golem.core());}
    public int count(){return data.get(0);}
    public int page(){return page;}
    public int color(int slot){return data.get(slot+4)-1;}
    public int upgrade(int type){return data.get(3)>>(type*2)&3;}
    public boolean toggle(int index){return (data.get(2)&1<<index)!=0;}
    private void update(){if(golem.level().isClientSide){page=data.get(1);return;}data.set(0,golem.filterSlots());data.set(1,page);data.set(2,golem.toggles());data.set(3,golem.upgradeMask());for(int i=0;i<18;i++)data.set(i+4,golem.color(i)+1);}
    @Override public void setData(int id,int value){super.setData(id,value);if(id==1)page=value;}
    @Override public void broadcastChanges(){update();super.broadcastChanges();}
    @Override public boolean stillValid(Player player){return golem.isAlive()&&player.level()==golem.level()&&player.distanceToSqr(golem)<64;}
    @Override public void clicked(int index,int button,ClickType type,Player player){if(!stillValid(player))return;if(index>=0&&index<6){if(!hasFilters()||index+page*6>=count())return;var selected=type==ClickType.SWAP&&button>=0&&button<9?player.getInventory().getItem(button):getCarried();var current=view.getItem(index);if(type==ClickType.THROW||selected.isEmpty()&&button==1)view.setItem(index,ItemStack.EMPTY);else if(!selected.isEmpty())view.setItem(index,selected.copyWithCount(golem.core()==0?Math.min(64,selected.getCount()):1));else if(golem.core()==0&&!current.isEmpty())view.setItem(index,current.copyWithCount(Math.max(1,Math.min(64,current.getCount()+(button==0?1:-1)))));broadcastChanges();return;}super.clicked(index,button,type,player);}
    @Override public boolean clickMenuButton(Player player,int button){if(!stillValid(player))return false;if(button==66&&page>0)page--;else if(button==67&&hasFilters()&&page<(count()-1)/6)page++;else if(button>=50&&button<=57){int toggle=button-50;if(toggle>=5&&upgrade(5)==0)return false;if(golem.core()==4&&upgrade(4)==0)return false;golem.toggle(toggle,!golem.toggle(toggle));}else if(button>=0&&button<36&&upgrade(4)>0){int slot=button%18;if(slot>=count())return false;int color=golem.color(slot)+(button<18?-1:1);golem.color(slot,color<-1?15:color>15?-1:color);}else return false;update();broadcastFullState();return true;}
    @Override public ItemStack quickMoveStack(Player player,int index){if(index<6||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();if(hasFilters()){for(int i=0;i<6&&i+page*6<count();i++)if(view.getItem(i).isEmpty()){view.setItem(i,stack.copyWithCount(golem.core()==0?Math.min(64,stack.getCount()):1));broadcastChanges();return ItemStack.EMPTY;}}if(!(index<33?moveItemStackTo(stack,33,42,false):moveItemStackTo(stack,6,33,false)))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return original;}
    @Override public void removed(Player player){if(!player.level().isClientSide)golem.close();super.removed(player);}
}
