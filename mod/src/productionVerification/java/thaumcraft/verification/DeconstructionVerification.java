package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.api.aspects.*;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.menu.DeconstructionMenu;
import thaumcraft.production.DeconstructionTableBlockEntity;
import thaumcraft.registry.R4Content;

final class DeconstructionVerification {
    private static final BlockPos POS=new BlockPos(1240,110,0);
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        level.getChunkAt(POS);level.setBlockAndUpdate(POS,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(POS,R4Content.DECONSTRUCTOR.get().defaultBlockState());var tile=(DeconstructionTableBlockEntity)level.getBlockEntity(POS);
        check.accept(tile.canPlaceItem(0,new ItemStack(Items.COAL))&&!tile.canPlaceItem(0,new ItemStack(Items.DEBUG_STICK)),"deconstruction accepts actual aspected items and refuses unmapped modern items");
        tile.setItem(0,new ItemStack(Items.COAL,3));for(int i=0;i<39;i++)tile.serverTick();check.accept(tile.getItem(0).getCount()==3&&tile.breakTime()==1&&tile.pendingAspect()==null,"deconstruction preserves the input until the fortieth tick");
        long seed=0;while(true){level.random.setSeed(seed);if(level.random.nextInt(80)==0)break;seed++;}level.random.setSeed(seed);tile.serverTick();var aspect=tile.pendingAspect();
        check.accept(tile.getItem(0).getCount()==2&&aspect!=null&&aspect.isPrimal(),"completed forty-tick cycle consumes one real coal and produces one primary research aspect");
        for(int i=0;i<100;i++)tile.serverTick();check.accept(tile.getItem(0).getCount()==2&&tile.pendingAspect()==aspect&&tile.breakTime()==0,"unclaimed research aspect pauses all subsequent deconstruction");
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("01234567-1111-2222-3333-012345678902"),"DeconstructionQA"));player.setPos(POS.getX()+.5,POS.getY(),POS.getZ()+.5);KnowledgeService.initialize(player);KnowledgeService.mutate(player,k->{k.setPool(aspect.getTag(),999);return true;});
        var menu=(DeconstructionMenu)tile.createMenu(4,player.getInventory(),player);
        check.accept(menu.pendingAspect()==aspect&&menu.slots.size()==37&&menu.slots.getFirst().x==64&&menu.slots.getFirst().y==16,"native menu synchronizes the original aspect output and inventory slot layout");
        check.accept(!menu.clickMenuButton(player,0)&&tile.pendingAspect()==aspect,"unrecognized menu button cannot claim research");
        check.accept(menu.clickMenuButton(player,1)&&KnowledgeService.get(player).pool(aspect)==1000&&tile.pendingAspect()==null,"claiming through the actual menu adds exactly one research point above the ordinary pool cap");
        check.accept(!menu.clickMenuButton(player,1)&&KnowledgeService.get(player).pool(aspect)==1000,"repeated claim packets cannot duplicate a research point");
        tile.serverTick();check.accept(tile.breakTime()==39&&tile.getItem(0).getCount()==2,"deconstruction resumes only after output was claimed");
        var top=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,POS,Direction.UP);var side=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,POS,Direction.NORTH);
        check.accept(top!=null&&top.getSlots()==0&&side!=null&&side.getSlots()==1,"original top-face exclusion survives native sided item capability integration");
        check.accept(side.insertItem(0,new ItemStack(Items.DEBUG_STICK),false).is(Items.DEBUG_STICK),"automation cannot bypass the aspect eligibility check");
        var saved=tile.saveWithoutMetadata(level.registryAccess());saved.putString("Aspect","ignis");saved.putInt("BreakTime",0);tile.loadWithComponents(saved,level.registryAccess());tile.sync();var copy=new DeconstructionTableBlockEntity(POS,tile.getBlockState());copy.loadWithComponents(tile.saveWithoutMetadata(level.registryAccess()),level.registryAccess());
        check.accept(copy.pendingAspect()==Aspects.FIRE&&ItemStack.matches(copy.getItem(0),tile.getItem(0)),"unclaimed aspect and component-bearing input survive native NBT round trip");
        check.accept(level.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.parse("thaumcraft:deconstructor")).isPresent()&&thaumcraft.data.ThaumcraftData.get(level.getServer()).research().containsKey("DECONSTRUCTOR"),"original deconstruction arcane recipe and research both load");
        var list=new AspectList().add(Aspects.FIRE,7).add(Aspects.WATER,3);check.accept(list.size()==2&&list.visSize()==10,"API visSize totals units rather than counting distinct aspect types");
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check){level.getChunkAt(POS);var tile=(DeconstructionTableBlockEntity)level.getBlockEntity(POS);check.accept(tile!=null&&tile.pendingAspect()==Aspects.FIRE&&tile.getItem(0).is(Items.COAL)&&tile.getItem(0).getCount()==2,"deconstruction pending output and input survive a real dedicated-server restart");}
}
