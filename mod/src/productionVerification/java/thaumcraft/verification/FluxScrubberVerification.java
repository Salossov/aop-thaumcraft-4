package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.api.aspects.*;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.fluids.*;
import thaumcraft.production.*;
import thaumcraft.registry.*;

final class FluxScrubberVerification {
    private static final BlockPos POS=new BlockPos(1200,110,0);
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        for(var p:BlockPos.betweenClosed(POS.offset(-17,-2,-17),POS.offset(17,2,17))){level.getChunkAt(p);level.setBlock(p,Blocks.AIR.defaultBlockState(),2);}
        level.setBlockAndUpdate(POS,R4Content.FLUX_SCRUBBER.get().defaultBlockState());var tile=(FluxScrubberBlockEntity)level.getBlockEntity(POS);
        var inside=POS.offset(3,0,0);var boundary=POS.offset(16,0,0);
        level.setBlockAndUpdate(inside,R2Content.FLUX_GOO.get().defaultBlockState().setValue(FiniteFluxFluid.QUANTA,4));
        level.setBlockAndUpdate(boundary,R2Content.FLUX_GAS.get().defaultBlockState().setValue(FiniteFluxFluid.QUANTA,4));
        for(int i=0;i<2300;i++)tile.tick();
        check.accept(level.getBlockState(inside).getValue(FiniteFluxFluid.QUANTA)==4,"scrubber cannot destroy any flux without energized air vis");
        var saved=tile.saveWithoutMetadata(level.registryAccess());saved.putInt("power",5);tile.loadWithComponents(saved,level.registryAccess());
        for(int i=0;i<2300&&tile.storedCharges()==0;i++)tile.tick();
        check.accept(tile.storedCharges()==1&&tile.storedPower()==0&&level.getBlockState(inside).getValue(FiniteFluxFluid.QUANTA)==3,"five centivis removes exactly one finite flux quantum and earns one charge");
        check.accept(level.getBlockState(boundary).getValue(FiniteFluxFluid.QUANTA)==4,"strict original sixteen-block radius excludes the boundary cell");
        level.setBlockAndUpdate(inside,Blocks.AIR.defaultBlockState());
        long seed=0;while(true){level.random.setSeed(seed);if(level.random.nextInt(4)==0)break;seed++;}level.random.setSeed(seed);
        saved=tile.saveWithoutMetadata(level.registryAccess());saved.putInt("charges",4);saved.putInt("essentia",0);tile.loadWithComponents(saved,level.registryAccess());tile.tick();
        check.accept(tile.storedCharges()==0&&tile.getEssentiaAmount(null)==1&&tile.getEssentiaType(null)==Aspects.MAGIC,"four charges use the original one-in-four roll for one Praecantatio");
        check.accept(tile.isConnectable(Direction.DOWN)&&!tile.isConnectable(Direction.UP)&&!tile.canInputFrom(Direction.DOWN)&&tile.getSuctionAmount(null)==0,"scrubber exposes one output-only port without suction");
        var transport=level.getCapability(EssentiaCapabilities.TRANSPORT,POS,Direction.DOWN);check.accept(transport==tile,"scrubber is discoverable through the public essentia transport capability");
        check.accept(tile.takeEssentia(Aspects.AIR,1,Direction.DOWN)==0&&tile.takeEssentia(Aspects.MAGIC,1,Direction.UP)==0&&tile.getEssentiaAmount(null)==1,"wrong aspect or face cannot consume buffered output");
        check.accept(tile.takeEssentia(Aspects.MAGIC,7,Direction.DOWN)==1&&tile.getEssentiaAmount(null)==0,"output drains only the available Praecantatio without duplication");
        var auraPos=POS.offset(6,0,0);level.setBlockAndUpdate(auraPos,R2Content.AURA_NODE.get().defaultBlockState());var aura=(thaumcraft.blocks.AuraNodeBlockEntity)level.getBlockEntity(auraPos);aura.initialize(Map.of("aer",100),thaumcraft.api.nodes.NodeState.Type.NORMAL,thaumcraft.api.nodes.NodeState.Modifier.NONE);
        var nodePos=POS.offset(4,0,0);level.setBlockAndUpdate(nodePos,R3Content.ENERGIZED_NODE.get().defaultBlockState());var source=(thaumcraft.visnet.EnergizedNodeBlockEntity)level.getBlockEntity(nodePos);source.initialize(aura);VisNetHandler.addNode(level,source);tile.tick();
        check.accept(tile.storedPower()==10&&source.consumeVis(Aspects.AIR,10)==0,"actual energized node supplies ten centivis through the shared vis network exactly once per tick");
        level.setBlockAndUpdate(nodePos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(auraPos,Blocks.AIR.defaultBlockState());
        var packet=new thaumcraft.network.FluxScrubPayload(inside);var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),level.registryAccess());try{thaumcraft.network.FluxScrubPayload.CODEC.encode(buffer,packet);check.accept(thaumcraft.network.FluxScrubPayload.CODEC.decode(buffer).equals(packet)&&!buffer.isReadable(),"successful scrub particle position survives native packet serialization");}finally{buffer.release();}
        saved=tile.saveWithoutMetadata(level.registryAccess());saved.putInt("charges",3);saved.putInt("power",7);saved.putInt("essentia",2);tile.loadWithComponents(saved,level.registryAccess());tile.sync();
        var copy=new FluxScrubberBlockEntity(POS,tile.getBlockState());copy.loadWithComponents(tile.saveWithoutMetadata(level.registryAccess()),level.registryAccess());check.accept(copy.storedCharges()==3&&copy.storedPower()==7&&copy.getEssentiaAmount(null)==2,"scrubber conversion progress, power and output survive native NBT round trip");
        check.accept(level.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.parse("thaumcraft:fluxscrubber")).isPresent()&&thaumcraft.data.ThaumcraftData.get(level.getServer()).research().containsKey("FLUXSCRUB"),"original arcane recipe and research load together");
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check){level.getChunkAt(POS);var tile=(FluxScrubberBlockEntity)level.getBlockEntity(POS);check.accept(tile!=null&&tile.storedCharges()==3&&tile.storedPower()==7&&tile.getEssentiaAmount(null)==2,"scrubber state survives a real dedicated-server restart");}
}
