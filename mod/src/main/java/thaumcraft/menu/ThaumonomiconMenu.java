package thaumcraft.menu;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchNote;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.items.*;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.network.ResearchActionPayload;
import thaumcraft.registry.R2Content;

public final class ThaumonomiconMenu extends AbstractContainerMenu implements ActionMenu {
    private final UUID token;
    private final Player owner;
    private long lastSequence;
    public ThaumonomiconMenu(int id,Inventory inventory,RegistryFriendlyByteBuf data) { this(id,inventory,data.readUUID()); }
    public ThaumonomiconMenu(int id,Inventory inventory,UUID token) { super(R2Content.BOOK_MENU.get(),id);this.token=token;owner=inventory.player; }
    @Override public UUID token() { return token; }
    @Override public boolean stillValid(Player player) { return player==owner&&(player.getMainHandItem().is(R2Content.BOOK.get())||player.getOffhandItem().is(R2Content.BOOK.get())); }
    @Override public ItemStack quickMoveStack(Player player,int index) { return ItemStack.EMPTY; }
    @Override public boolean accept(ServerPlayer player,ResearchActionPayload request) {
        if(!token.equals(request.token())||request.sequence()!=lastSequence+1||!stillValid(player)||request.action()!=0) return false;
        lastSequence=request.sequence();return createNote(player,request.first());
    }
    public boolean createNote(ServerPlayer player,String key) {
        if(!stillValid(player)) return false;
        var definition=ThaumcraftData.get(player.server).research().get(key);var knowledge=KnowledgeService.get(player);
        if(definition==null||definition.autoUnlock()||!definition.available(knowledge)) return false;
        if(definition.secondary()) {
            boolean completed=KnowledgeService.mutate(player,state->{
                if(!definition.available(state)||definition.aspects().entrySet().stream().anyMatch(e->!state.knows(Aspect.getAspect(e.getKey()))||state.pool(Aspect.getAspect(e.getKey()))<e.getValue()))return false;
                definition.aspects().forEach((tag,amount)->state.spend(Aspect.getAspect(tag),amount));return state.completeResearch(key);
            });
            if(completed)net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,new thaumcraft.network.ResearchCompletePayload(key));return completed;
        }
        var inventory=player.getInventory();int paper=-1,ink=-1;
        for(int slot=0;slot<inventory.items.size();slot++) {
            var stack=inventory.getItem(slot);var note=ResearchNotes.read(stack);
            if(stack.getItem() instanceof ResearchNotesItem&&note!=null&&note.key.equals(key)) return false;
            if(paper<0&&stack.is(Items.PAPER)) paper=slot;if(ink<0&&ResearchMenu.usableInk(stack)) ink=slot;
        }
        if(paper<0||ink<0) return false;
        var anchors=definition.aspects().keySet().stream().map(Aspect::getAspect).toList();if(anchors.isEmpty()) return false;
        var note=ResearchNote.create(key,definition.complexity(),anchors,new java.util.Random(player.getRandom().nextLong()));
        var stack=new ItemStack(R2Content.NOTES.get());ResearchNotes.write(stack,note);
        inventory.getItem(paper).shrink(1);var tools=inventory.getItem(ink);tools.setDamageValue(tools.getDamageValue()+1);
        if(!inventory.add(stack)) player.drop(stack,false);inventory.setChanged();player.inventoryMenu.sendAllDataToRemote();
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,new thaumcraft.network.ResearchNoteCreatedPayload(containerId,key));return true;
    }
}
