package thaumcraft.menu;

import java.util.*;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import thaumcraft.blocks.TableBlockEntity;
import thaumcraft.crafting.R2Recipes;
import thaumcraft.items.*;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.registry.R2Content;

public final class ArcaneMenu extends AbstractContainerMenu {
    public final Container table;
    private final Player owner;
    private final ResultContainer result=new ResultContainer();
    private record Match(RecipeHolder<? extends Recipe<CraftingInput>> recipe,CraftingInput.Positioned input,Map<String,Integer> cost) {}
    public ArcaneMenu(int id,Inventory inventory,RegistryFriendlyByteBuf data) { this(id,inventory,new SimpleContainer(11));data.readBlockPos(); }
    public ArcaneMenu(int id,Inventory inventory,Container table) {
        super(R2Content.ARCANE_MENU.get(),id);this.table=table;owner=inventory.player;
        addSlot(new Slot(result,0,160,64) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return stillValid(player)&&match()!=null; }
            @Override public ItemStack remove(int amount) { return craft(owner); }
            @Override public Optional<ItemStack> tryRemove(int amount,int maximum,Player player) {
                refresh();var preview=result.getItem(0);
                if(amount<=0||preview.isEmpty()||maximum<preview.getCount()||!mayPickup(player)) return Optional.empty();
                var output=craft(player);return output.isEmpty()?Optional.empty():Optional.of(output);
            }
            @Override public void onTake(Player player,ItemStack stack) { stack.onCraftedBy(player.level(),player,stack.getCount());if(player instanceof net.minecraft.server.level.ServerPlayer server)thaumcraft.knowledge.WarpService.crafted(server,stack); }
        });
        addSlot(new Slot(table,10,160,24) { @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof CastingWandItem&&!WandMaterials.staff(stack); } @Override public int getMaxStackSize() { return 1; } });
        for(int y=0;y<3;y++) for(int x=0;x<3;x++) addSlot(new Slot(table,y*3+x,40+x*24,40+y*24));
        for(int y=0;y<3;y++) for(int x=0;x<9;x++) addSlot(new Slot(inventory,9+y*9+x,16+x*18,151+y*18));
        for(int x=0;x<9;x++) addSlot(new Slot(inventory,x,16+x*18,209));
        refresh();
    }
    @Override public boolean stillValid(Player player) { return player==owner&&table.stillValid(player); }
    /** A fill is planned entirely on copies; no craft, wand debit or dropped remainder occurs here. */
    public boolean fill(ServerPlayer player,thaumcraft.api.crafting.ArcaneTransferRequest request){
        if(player!=owner||request.menuId()!=containerId||request.stateId()!=getStateId()||!stillValid(player))return false;
        var display=thaumcraft.api.crafting.RecipeDisplays.recipes(player.level()).stream().filter(d->d.id().equals(request.displayId())).findFirst().orElse(null);
        if(display==null||display.kind()!=thaumcraft.api.crafting.RecipeDisplay.Kind.ARCANE||display.recipeId()==null||display.inputs().size()!=9||!KnowledgeService.get(player).completedResearch().containsAll(display.research()))return false;
        var holder=player.server.getRecipeManager().byKey(display.recipeId()).orElse(null);
        if(holder==null||!(holder.value() instanceof thaumcraft.api.crafting.IArcaneRecipe recipe))return false;
        var inventory=new ArrayList<ItemStack>();var grid=new ArrayList<ItemStack>();
        for(int n=0;n<36;n++)inventory.add(player.getInventory().getItem(n).copy());for(int n=0;n<9;n++)grid.add(table.getItem(n).copy());
        var required=new ArrayList<java.util.function.Predicate<ItemStack>>();for(var ingredient:display.inputs())required.add(ingredient.isEmpty()?null:ingredient::test);
        var plan=thaumcraft.api.crafting.TransferPlanner.plan(inventory,grid,required,new thaumcraft.api.crafting.TransferPlanner.Stacks<ItemStack>(){
            public boolean empty(ItemStack s){return s.isEmpty();}public int count(ItemStack s){return s.getCount();}public int limit(ItemStack s){return s.getMaxStackSize();}
            public boolean same(ItemStack a,ItemStack b){return ItemStack.isSameItemSameComponents(a,b);}public ItemStack count(ItemStack s,int n){return n==0?ItemStack.EMPTY:s.copyWithCount(n);}public ItemStack empty(){return ItemStack.EMPTY;}
        },request.maximum());
        if(plan.isEmpty())return false;var value=plan.get();
        var input=CraftingInput.ofPositioned(3,3,value.grid()).input();if(!recipe.matches(input,player.level())||!recipe.mayCraft(player,input))return false;
        for(int n=0;n<36;n++)player.getInventory().setItem(n,value.inventory().get(n));for(int n=0;n<9;n++)table.setItem(n,value.grid().get(n));
        player.getInventory().setChanged();table.setChanged();broadcastChanges();return true;
    }
    private float discount() {return WandVis.discount(owner);}
    private Match match() {
        if(!(owner instanceof ServerPlayer player)||!stillValid(owner)) return null;
        var items=new ArrayList<ItemStack>();for(int i=0;i<9;i++) items.add(table.getItem(i));var input=CraftingInput.ofPositioned(3,3,items);
        var normal=player.server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input.input(),player.level());
        if(normal.isPresent()) return new Match(normal.get(),input,Map.of());
        var arcane=player.server.getRecipeManager().getRecipeFor(R2Recipes.ARCANE_TYPE.get(),input.input(),player.level());
        if(arcane.isEmpty())return null;var recipe=arcane.get().value();
        if(!recipe.research().isEmpty()&&!KnowledgeService.get(player).completedResearch().contains(recipe.research())||!recipe.mayCraft(player,input.input()))return null;
        var cost=arcane.get().value().vis(input.input());if(!WandVis.consume(table.getItem(10),cost,owner,false)) return null;
        return new Match(arcane.get(),input,cost);
    }
    public ItemStack craft(Player player) {
        if(player!=owner) return ItemStack.EMPTY;var match=match();if(match==null) return ItemStack.EMPTY;
        var crafted=match.recipe.value().assemble(match.input.input(),player.level().registryAccess());
        if(crafted.isEmpty()) return ItemStack.EMPTY;
        var remaining=match.recipe.value().getRemainingItems(match.input.input());
        if(!match.cost.isEmpty()&&!WandVis.consume(table.getItem(10),match.cost,owner,true)) return ItemStack.EMPTY;
        for(int y=0;y<match.input.input().height();y++) for(int x=0;x<match.input.input().width();x++) {
            int index=(y+match.input.top())*3+x+match.input.left();var old=table.getItem(index);
            if(!old.isEmpty()) table.removeItem(index,1);var remainder=remaining.get(y*match.input.input().width()+x);
            if(remainder.isEmpty()) continue;var current=table.getItem(index);
            if(current.isEmpty()) table.setItem(index,remainder.copy());
            else if(ItemStack.isSameItemSameComponents(current,remainder)&&current.getCount()+remainder.getCount()<=current.getMaxStackSize()) current.grow(remainder.getCount());
            else { var leftover=remainder.copy();if(!player.getInventory().add(leftover)&&!leftover.isEmpty()) player.drop(leftover,false); }
        }
        table.setChanged();refresh();return crafted;
    }
    private void refresh() {
        if(owner instanceof ServerPlayer) { var match=match();result.setItem(0,match==null?ItemStack.EMPTY:match.recipe.value().assemble(match.input.input(),owner.level().registryAccess())); }
    }
    @Override public void broadcastChanges() { refresh();super.broadcastChanges(); }
    @Override public void slotsChanged(Container container) { refresh();super.slotsChanged(container); }
    @Override public boolean canTakeItemForPickAll(ItemStack stack,Slot slot) { return slot.container!=result&&super.canTakeItemForPickAll(stack,slot); }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0||index>=slots.size()) return ItemStack.EMPTY;
        if(index==0) {
            refresh();var preview=result.getItem(0);if(preview.isEmpty()||!fitsPlayerInventory(preview)) return ItemStack.EMPTY;
            var crafted=craft(player);if(crafted.isEmpty()) return ItemStack.EMPTY;var returned=crafted.copy();
            moveItemStackTo(crafted,11,47,true);if(!crafted.isEmpty()) player.drop(crafted,false);slots.get(0).onTake(player,returned);return returned;
        }
        var slot=slots.get(index);if(!slot.hasItem()) return ItemStack.EMPTY;var stack=slot.getItem();var before=stack.copy();
        boolean moved=index<11?moveItemStackTo(stack,11,47,true):stack.getItem() instanceof CastingWandItem?moveItemStackTo(stack,1,2,false):
            index<38?moveItemStackTo(stack,38,47,false):moveItemStackTo(stack,11,38,false);
        if(!moved) return ItemStack.EMPTY;if(stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();refresh();return before;
    }
    private boolean fitsPlayerInventory(ItemStack stack) {
        int room=0;for(int i=11;i<47;i++) { var current=slots.get(i).getItem();if(current.isEmpty()) room+=stack.getMaxStackSize();else if(ItemStack.isSameItemSameComponents(stack,current)) room+=Math.max(0,current.getMaxStackSize()-current.getCount()); }return room>=stack.getCount();
    }
}
