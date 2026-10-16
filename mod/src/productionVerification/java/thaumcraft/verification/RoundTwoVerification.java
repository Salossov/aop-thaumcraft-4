package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import thaumcraft.registry.*;

final class RoundTwoVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS,true,level.registryAccess());
        var tab=net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:thaumcraft"));
        var contents=tab.getDisplayItems();int aspects=thaumcraft.api.aspects.Aspect.all().size();
        for(var block:List.of(R3Content.WARDED_JAR.get(),R3Content.VOID_JAR.get())){
            var filled=contents.stream().filter(s->s.is(block.asItem())&&s.getOrDefault(DataComponents.BLOCK_ENTITY_DATA,CustomData.EMPTY).copyTag().contains("Essentia")).toList();
            check.accept(filled.size()==aspects,"creative filled jar variants cover every aspect: "+block);
            for(var stack:filled){var tile=new thaumcraft.production.TransportBlockEntity(BlockPos.ZERO,block.defaultBlockState());tile.loadWithComponents(stack.get(DataComponents.BLOCK_ENTITY_DATA).copyTag(),level.registryAccess());check.accept(tile.store.amount()==64&&tile.store.first()!=null,"filled jar loads 64 essentia: "+tile.store.first());}
        }
        for(var item:List.of(R3Content.ESSENTIA_PHIAL.get(),R3Content.ESSENTIA_CRYSTAL.get(),R2Content.WISP_ESSENCE.get()))check.accept(contents.stream().filter(s->s.is(item)).count()==aspects,"creative essentia/essence variants cover every aspect: "+item);
        var kinds=new HashSet<thaumcraft.items.CastingWandItem.Kind>();for(var stack:contents)if(stack.getItem() instanceof thaumcraft.items.CastingWandItem wand)kinds.add(wand.kind());check.accept(kinds.size()==3,"creative tab includes wand, staff and sceptre groups");
        var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);var scan=new ItemStack(R2Content.THAUMOMETER.get());player.setItemInHand(InteractionHand.MAIN_HAND,scan);player.setPos(400.5,250,400.5);player.setYRot(0);player.setXRot(-90);
        thaumcraft.knowledge.KnowledgeService.initialize(player);thaumcraft.knowledge.KnowledgeService.mutate(player,k->{for(var aspect:thaumcraft.api.aspects.Aspect.all().values())k.setPool(aspect.getTag(),15);return true;});
        check.accept(R2Content.THAUMOMETER.get().use(level,player,InteractionHand.MAIN_HAND).getResult().consumesAction()&&player.isUsingItem(),"RMB starts scanner while looking into air");
        for(int i=0;i<30;i++)R2Content.THAUMOMETER.get().onUseTick(level,player,scan,72000-i);
        check.accept(player.isUsingItem(),"scanner remains held while no target is present");
        var target=new BlockPos(400,251,403);level.setBlockAndUpdate(target,net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState());player.setXRot(0);player.xRotO=0;player.yRotO=0;player.xo=player.getX();player.yo=player.getY();player.zo=player.getZ();
        for(int i=0;i<20;i++)R2Content.THAUMOMETER.get().onUseTick(level,player,scan,71970-i);
        check.accept(!player.isUsingItem(),"acquiring a target completes scanning after 20 consecutive ticks");
        var key=thaumcraft.data.ThaumcraftData.objectScanKey(level.getServer(),level.getBlockState(target));check.accept(thaumcraft.knowledge.KnowledgeService.get(player).scanned(thaumcraft.api.knowledge.PlayerKnowledge.ScanKind.OBJECT).contains(key),"air-to-target scan commits knowledge through the normal server path");
        var nodePos=new BlockPos(408,251,403);check.accept(level.areEntitiesLoaded(new net.minecraft.world.level.ChunkPos(nodePos).toLong()),"node test chunk has loaded entity sections");for(var old:level.getEntitiesOfClass(ItemEntity.class,new AABB(nodePos).inflate(2)))old.discard();level.setBlockAndUpdate(nodePos,R2Content.AURA_NODE.get().defaultBlockState());var node=(thaumcraft.blocks.AuraNodeBlockEntity)level.getBlockEntity(nodePos);node.state.current.put("aer",25);node.state.current.put("terra",4);node.state.current.put("ordo",10);
        R2Content.AURA_NODE.get().playerWillDestroy(level,nodePos,level.getBlockState(nodePos),player);level.removeBlock(nodePos,false);
        var drops=level.getEntitiesOfClass(ItemEntity.class,new AABB(nodePos).inflate(2));var totals=new HashMap<String,Integer>();for(var drop:drops)for(var aspect:thaumcraft.items.WispEssenceItem.aspects(drop.getItem()).getAspects())totals.merge(aspect.getTag(),drop.getItem().getCount(),Integer::sum);
        check.accept(totals.equals(Map.of("aer",3,"ordo",2)),"node destruction drops original aspect essence quantities and skips aspects below 5: "+totals);
        check.accept(drops.stream().allMatch(drop->drop.getItem().is(R2Content.WISP_ESSENCE.get())&&thaumcraft.items.WispEssenceItem.aspects(drop.getItem()).visSize()==2),"each dropped essence item stores two units of its aspect");
    }
}
