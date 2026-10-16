package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import thaumcraft.api.aspects.*;
import thaumcraft.api.research.*;
import thaumcraft.blocks.*;
import thaumcraft.items.*;
import thaumcraft.knowledge.*;
import thaumcraft.menu.*;
import thaumcraft.registry.*;

/** Native inventory/knowledge/persistence acceptance in the dedicated QA profile. */
final class ResearchUiScene {
    static final BlockPos POS=new BlockPos(608,116,604);
    private static final List<String> checks=new ArrayList<>();
    private static void check(boolean pass,String label){if(!pass)throw new AssertionError(label);checks.add(label);}
    static void register(MinecraftServer server){
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event)->{if(event.getEntity() instanceof ServerPlayer player){try{prepare(player);verify(player);prepare(player);write(true,null);}catch(Throwable failure){failure.printStackTrace();write(false,failure.toString());}}});
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event)->server.halt(false));
    }
    private static void prepare(ServerPlayer player){
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();player.server.getPlayerList().op(player.getGameProfile());player.removeAllEffects();
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4),"execute in minecraft:overworld run tp @s 608 117 603 0 20");
        var level=player.serverLevel();level.setBlock(POS,R2Content.RESEARCH_TABLE.get().defaultBlockState().setValue(TableBlock.FACING,Direction.NORTH),2);level.setBlock(POS.north(),R2Content.RESEARCH_TABLE.get().defaultBlockState().setValue(TableBlock.FACING,Direction.SOUTH).setValue(TableBlock.SLAVE,true),2);
        player.getInventory().clearContent();player.getInventory().selected=0;player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(0));player.getInventory().setItem(0,new ItemStack(R2Content.BOOK.get()));
        KnowledgeService.mutate(player,k->{for(var key:List.copyOf(k.completedResearch()))k.forgetResearch(key);for(String key:List.of("BASICS","ASPECTS","RESEARCH","ALCHEMY","CRUCIBLE","RESEARCHER1","RESEARCHER2"))k.completeResearch(key);for(var aspect:Aspect.all().values())k.setPool(aspect.getTag(),100);return true;});
        player.inventoryMenu.sendAllDataToRemote();
    }
    static void supplies(ServerPlayer player){player.getInventory().setItem(1,new ItemStack(Items.PAPER,3));player.getInventory().setItem(2,new ItemStack(R2Content.SCRIBING_TOOLS.get()));player.inventoryMenu.sendAllDataToRemote();}
    static ResearchNote demo(){var grid=new LinkedHashMap<ResearchNote.Hex,ResearchNote.Entry>();for(int q=-2;q<=2;q++)for(int r=-2;r<=2;r++)if(Math.abs(q+r)<=2)grid.put(new ResearchNote.Hex(q,r),new ResearchNote.Entry("",0));grid.put(new ResearchNote.Hex(-2,0),new ResearchNote.Entry("ignis",1));grid.put(new ResearchNote.Hex(2,0),new ResearchNote.Entry("ignis",1));return new ResearchNote("NITOR",grid,false,0);}
    static TableBlockEntity table(ServerPlayer player){return (TableBlockEntity)player.serverLevel().getBlockEntity(POS);}
    static void openTable(ServerPlayer player){var table=table(player);table.setItem(0,new ItemStack(R2Content.SCRIBING_TOOLS.get()));var stack=new ItemStack(R2Content.NOTES.get());ResearchNotes.write(stack,demo());table.setItem(1,stack);player.openMenu(table);}
    private static void verify(ServerPlayer player){
        var book=new ThaumonomiconMenu(0,player.getInventory(),UUID.randomUUID());
        check(!book.createNote(player,"NITOR"),"creative book refuses a note with no paper or scribing tools");
        player.getInventory().setItem(1,new ItemStack(Items.PAPER,3));check(!book.createNote(player,"NITOR"),"paper alone cannot issue a note");
        player.getInventory().setItem(2,new ItemStack(R2Content.SCRIBING_TOOLS.get()));player.getInventory().getItem(2).setDamageValue(100);check(!book.createNote(player,"NITOR"),"exhausted ink cannot issue a note");
        supplies(player);check(book.createNote(player,"NITOR"),"a native book transaction delivers a note only with both materials");
        check(player.getInventory().getItem(1).getCount()==2&&player.getInventory().getItem(2).getDamageValue()==1,"successful issue consumes one paper and one ink");
        check(!book.createNote(player,"NITOR")&&player.getInventory().getItem(1).getCount()==2,"duplicate note requests preserve materials");
        var table=table(player);var stack=new ItemStack(R2Content.NOTES.get());ResearchNotes.write(stack,demo());table.setItem(0,new ItemStack(R2Content.SCRIBING_TOOLS.get()));table.setItem(1,stack);var menu=new ResearchMenu(0,player.getInventory(),table);
        check(menu.place(player,-1,0,"lux"),"known aspect can be placed on an editable note");var unfinished=ResearchNotes.read(stack);check(!unfinished.complete,"partial connection remains editable");
        var encoded=table.saveWithFullMetadata(player.registryAccess());var restored=new TableBlockEntity(POS,table.getBlockState());restored.loadWithComponents(encoded,player.registryAccess());check(ResearchNotes.read(restored.getItem(1)).grid.equals(unfinished.grid),"table NBT reload preserves the entire partial research graph");
        var removed=table.removeItemNoUpdate(1);table.setItem(1,removed);check(ResearchNotes.read(removed).grid.equals(unfinished.grid),"removing and reinserting an unfinished note loses no hexes");
        check(menu.place(player,0,0,"aer")&&menu.place(player,1,0,"lux"),"component chain joins both original anchors");var complete=ResearchNotes.read(removed);check(complete.complete,"connected note becomes a discovery");
        int ink=table.getItem(0).getDamageValue(),pool=KnowledgeService.get(player).pool(Aspects.AIR);check(!menu.place(player,0,0,"")&&!menu.place(player,-1,0,"ignis")&&table.getItem(0).getDamageValue()==ink&&KnowledgeService.get(player).pool(Aspects.AIR)==pool,"discovery rejects placement and erasure without spending ink or points");
        var completedCopy=removed.copy();table.setItem(1,ItemStack.EMPTY);table.setItem(1,completedCopy);check(ResearchNotes.read(table.getItem(1)).complete&&ResearchNotes.read(table.getItem(1)).grid.equals(complete.grid),"completed discovery remains intact when reinserted");
        var notes=(ResearchNotesItem)R2Content.NOTES.get();check(notes.getName(completedCopy).getString().equals(net.minecraft.network.chat.Component.translatable("item.discovery.name").getString()),"completed scroll uses Discovery display name");
        var tips=new ArrayList<net.minecraft.network.chat.Component>();notes.appendHoverText(completedCopy,Item.TooltipContext.of(player.level()),tips,TooltipFlag.Default.NORMAL);check(tips.stream().anyMatch(t->t.getString().equals(net.minecraft.network.chat.Component.translatable("tc.research_name.NITOR").getString())),"scroll tooltip includes its actual research title");
        KnowledgeService.mutate(player,k->{k.forgetResearch("RESEARCHER2");return true;});check(!menu.quickCombine(player,"lux"),"server refuses a forged quick-combine action without Research Mastery");KnowledgeService.mutate(player,k->{k.completeResearch("RESEARCHER2");return true;});
        int lux=KnowledgeService.get(player).pool(Aspects.LIGHT),air=KnowledgeService.get(player).pool(Aspects.AIR),fire=KnowledgeService.get(player).pool(Aspects.FIRE);check(menu.quickCombine(player,"lux")&&KnowledgeService.get(player).pool(Aspects.LIGHT)==lux+1&&KnowledgeService.get(player).pool(Aspects.AIR)==air-1&&KnowledgeService.get(player).pool(Aspects.FIRE)==fire-1,"Research Mastery quick synthesis spends both components and grants one Lux");
        var queue=new thaumcraft.client.ResearchNotificationQueue<Integer>(8,6000);for(int i=0;i<20;i++)queue.add(i,0);check(queue.rows().size()==8&&queue.remaining()==12,"notification queue retains all overflow rows and count");queue.advance(9001);check(queue.rows().getFirst().value()==1&&queue.rows().getLast().value()==8&&queue.remaining()==11&&queue.shift(9001)==8,"oldest notice exits alone and queue feeds a smooth one-row scroll");queue.advance(9251);check(queue.shift(9251)==0,"notification row settles after 250 ms");for(long now=15001;now<150000;now+=6001)queue.advance(now);check(queue.rows().isEmpty()&&queue.remaining()==0,"all queued notifications are eventually displayed and retired");queue.add(21,150001);check(queue.rows().getFirst().value()==21,"new gains still appear after the entire prior queue drains");
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),player.registryAccess());try{var ack=new thaumcraft.network.ResearchNoteCreatedPayload(42,"NITOR");thaumcraft.network.ResearchNoteCreatedPayload.CODEC.encode(buffer,ack);check(ack.equals(thaumcraft.network.ResearchNoteCreatedPayload.CODEC.decode(buffer))&&!buffer.isReadable(),"note-created acknowledgement retains container and research through native codec");}finally{buffer.release();}
    }
    private static void write(boolean pass,String error){try{var result=new LinkedHashMap<String,Object>();result.put("passed",pass);result.put("count",checks.size());result.put("checks",checks);if(error!=null)result.put("error",error);Files.writeString(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/research-ui-server.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception failure){throw new RuntimeException(failure);}}
}
