package thaumcraft.menu;

import java.util.UUID;
import java.util.Map;
import java.util.LinkedHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchNote;
import thaumcraft.blocks.TableBlockEntity;
import thaumcraft.items.*;
import thaumcraft.knowledge.*;
import thaumcraft.network.ResearchActionPayload;
import thaumcraft.registry.R2Content;

public final class ResearchMenu extends AbstractContainerMenu implements ActionMenu {
    public final Container table;
    private final UUID token;
    private final Player owner;
    private final int[] clientBonuses=new int[TableBlockEntity.BONUS_ASPECTS.size()];
    private long lastSequence;
    public ResearchMenu(int id,Inventory inventory,RegistryFriendlyByteBuf data) { this(id,inventory,new SimpleContainer(11),data.readBlockPos(),data.readUUID()); }
    public ResearchMenu(int id,Inventory inventory,TableBlockEntity table) { this(id,inventory,table,table.getBlockPos(),UUID.randomUUID()); }
    private ResearchMenu(int id,Inventory inventory,Container table,net.minecraft.core.BlockPos pos,UUID token) {
        super(R2Content.RESEARCH_MENU.get(),id);this.table=table;this.token=token;owner=inventory.player;
        for(int i=0;i<clientBonuses.length;i++) {final int index=i;addDataSlot(new DataSlot(){@Override public int get(){return table instanceof TableBlockEntity tile?tile.bonus(TableBlockEntity.BONUS_ASPECTS.get(index)):clientBonuses[index];}@Override public void set(int value){clientBonuses[index]=Math.clamp(value,0,1);}});}
        addSlot(new Slot(table,0,14,10) { @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof thaumcraft.api.IScribeTools; } });
        addSlot(new Slot(table,1,70,10) { @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof ResearchNotesItem; } });
        for(int y=0;y<3;y++) for(int x=0;x<9;x++) addSlot(new Slot(inventory,9+y*9+x,48+x*18,175+y*18));
        for(int x=0;x<9;x++) addSlot(new Slot(inventory,x,48+x*18,233));
    }
    @Override public UUID token() { return token; }
    public int bonus(Aspect aspect) {if(table instanceof TableBlockEntity tile)return tile.bonus(aspect);int index=TableBlockEntity.BONUS_ASPECTS.indexOf(aspect);return index<0?0:clientBonuses[index];}
    private boolean spend(thaumcraft.api.knowledge.PlayerKnowledge state,Aspect aspect,Map<Aspect,Integer> reserved) {
        if(!state.knows(aspect))return false;if(state.pool(aspect)>0)return state.spend(aspect,1);
        int used=reserved.getOrDefault(aspect,0);if(bonus(aspect)<=used)return false;reserved.put(aspect,used+1);return true;
    }
    private void consume(Map<Aspect,Integer> reserved) {if(table instanceof TableBlockEntity tile&&!tile.consumeBonuses(reserved))throw new IllegalStateException("Research bonus reservation changed during a server transaction");}
    @Override public boolean stillValid(Player player) { return player==owner&&table.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0||index>=slots.size()) return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem()) return ItemStack.EMPTY;
        var stack=slot.getItem();var before=stack.copy();if(!(index<2?moveItemStackTo(stack,2,38,true):moveItemStackTo(stack,0,2,false))) return ItemStack.EMPTY;
        if(stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return before;
    }
    @Override public boolean accept(ServerPlayer player,ResearchActionPayload request) {
        if(!token.equals(request.token())||request.sequence()!=lastSequence+1||!stillValid(player)) return false;
        lastSequence=request.sequence();return switch(request.action()) { case 1->combine(player,request.first(),request.second());case 2->place(player,request.q(),request.r(),request.first());case 3->quickCombine(player,request.first());default->false; };
    }
    public boolean quickCombine(ServerPlayer player,String target){var aspect=Aspect.getAspect(target);var knowledge=KnowledgeService.get(player);if(aspect==null||aspect.isPrimal()||!knowledge.knows(aspect)||!knowledge.completedResearch().contains("RESEARCHER2"))return false;var parts=aspect.getComponents();return combine(player,parts.get(0).getTag(),parts.get(1).getTag());}
    public boolean combine(ServerPlayer player,String first,String second) {
        if(!stillValid(player)) return false;
        var a=Aspect.getAspect(first);var b=Aspect.getAspect(second);if(a==null||b==null) return false;
        var combo=Aspect.combine(a,b);
        var reserved=new LinkedHashMap<Aspect,Integer>();boolean success=KnowledgeService.mutate(player,k->{if(!spend(k,a,reserved)||!spend(k,b,reserved)) return false;if(combo!=null) ScanService.reward(k,combo,1);return true;});
        if(success){consume(reserved);broadcastChanges();}return success;
    }
    public boolean place(ServerPlayer player,int q,int r,String tag) {
        if(!stillValid(player)) return false;
        if(Math.abs(q)>4||Math.abs(r)>4) return false;var stack=table.getItem(1);var note=ResearchNotes.read(stack);
        if(!(stack.getItem() instanceof ResearchNotesItem)||note==null||note.complete||!usableInk(table.getItem(0))) return false;
        var hex=new ResearchNote.Hex(q,r);var old=note.grid.get(hex);var aspect=tag.isEmpty()?null:Aspect.getAspect(tag);
        if(old==null||old.type()==1||old.aspect().equals(tag)||(!tag.isEmpty()&&aspect==null)) return false;
        var k=KnowledgeService.get(player);if(aspect!=null&&(!k.knows(aspect)||k.pool(aspect)+bonus(aspect)<=0)) return false;
        var reserved=new LinkedHashMap<Aspect,Integer>();
        if(aspect!=null) {
            boolean free=k.completedResearch().contains("RESEARCHER2")&&player.getRandom().nextFloat()<0.1f;
            if(!free&&!KnowledgeService.mutate(player,state->spend(state,aspect,reserved)))return false;
            consume(reserved);
        } else {
            var removed=Aspect.getAspect(old.aspect());float chance=k.completedResearch().contains("RESEARCHER2")?0.5f:k.completedResearch().contains("RESEARCHER1")?0.25f:0;
            if(removed!=null&&player.getRandom().nextFloat()<chance)KnowledgeService.mutate(player,state->{state.addPool(removed,1);return true;});
        }
        note.grid.put(hex,new ResearchNote.Entry(tag,aspect==null?0:2));note.checkComplete(k.aspectPools().keySet());ResearchNotes.write(stack,note);
        table.getItem(0).setDamageValue(table.getItem(0).getDamageValue()+1);if(table instanceof TableBlockEntity tile)tile.changedAndSync();else table.setChanged();broadcastChanges();return true;
    }
    public static boolean usableInk(ItemStack stack) { return stack.getItem() instanceof thaumcraft.api.IScribeTools&&stack.getDamageValue()<stack.getMaxDamage(); }
}
