package thaumcraft.blocks;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.items.*;
import thaumcraft.registry.R3Content;

public final class FocusActions {
    private record Mining(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,BlockPos pos,BlockState state,float progress){}
    private static final Map<UUID,Mining> MINING=new HashMap<>();
    private static final Map<UUID,Long> DIG_SOUND=new HashMap<>();
    private static final Map<UUID,Long> WARD_DELAY=new HashMap<>();
    public static boolean canChange(ServerPlayer player,BlockPos pos){var level=player.serverLevel();return level.hasChunkAt(pos)&&!level.isOutsideBuildHeight(pos)&&level.getWorldBorder().isWithinBounds(pos)&&level.mayInteract(player,pos)&&!player.blockActionRestricted(level,pos,player.gameMode.getGameModeForPlayer())&&!NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,pos,level.getBlockState(pos),player)).isCanceled();}
    public static void stop(ServerPlayer player){clearMining(player,true);}
    private static void clearMining(ServerPlayer player,boolean stopped){if(stopped)DIG_SOUND.remove(player.getUUID());var mining=MINING.remove(player.getUUID());if(mining!=null){net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(player.serverLevel(),null,player.getX(),player.getY(),player.getZ(),64,new thaumcraft.network.FocusDigPayload(player.getId(),mining.pos.getCenter(),false));var level=player.server.getLevel(mining.dimension);if(level!=null)progress(player,level,mining.pos,-1);}}
    private static void sparkle(net.minecraft.server.level.ServerLevel level,BlockPos pos,int color){net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(level,null,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,32,new thaumcraft.network.BlockSparklePayload(pos,color));}
    private static void progress(ServerPlayer player,net.minecraft.server.level.ServerLevel level,BlockPos pos,int stage){level.destroyBlockProgress(player.getId(),pos,stage);if(player.level()==level)player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket(player.getId(),pos,stage));}
    private static List<BlockPos> plane(ServerPlayer player,ItemStack focus,ThaumicFocusItem item,BlockHitResult hit){
        int size=item.isUpgradedWith(focus,FocusUpgradeType.architect)?1+item.getUpgradeLevel(focus,FocusUpgradeType.enlarge):0;var result=new ArrayList<BlockPos>();var level=player.serverLevel();var source=level.getBlockState(hit.getBlockPos());
        for(int a=-size;a<=size;a++)for(int b=-size;b<=size;b++){var pos=switch(hit.getDirection().getAxis()){case X->hit.getBlockPos().offset(0,a,b);case Y->hit.getBlockPos().offset(a,0,b);case Z->hit.getBlockPos().offset(a,b,0);};if(level.hasChunkAt(pos)&&level.getBlockState(pos).is(source.getBlock()))result.add(pos);}
        result.sort(Comparator.comparingDouble(pos->pos.distSqr(hit.getBlockPos())));return result;
    }
    public static boolean ward(ServerPlayer player,ItemStack wand,ItemStack focus,ThaumicFocusItem item,BlockHitResult hit){
        var level=player.serverLevel();long now=level.getGameTime();if(WARD_DELAY.getOrDefault(player.getUUID(),0L)>now)return false;WARD_DELAY.put(player.getUUID(),now+10);boolean changed=false;
        for(var pos:WandArea.blocks(player,wand,focus,item,hit)){
            if(level.getBlockEntity(pos) instanceof FocusMemoryBlockEntity tile){if(tile.ownedBy(player.getUUID())&&tile.getBlockState().is(R3Content.WARDED_BLOCK.get())){tile.restore();sparkle(level,pos,0xfc9e00);changed=true;}continue;}
            var state=level.getBlockState(pos);if(!canChange(player,pos)||state.isAir()||!state.isSolidRender(level,pos)||state.hasBlockEntity()||state.getDestroySpeed(level,pos)<0)continue;
            if(!WandFocus.consume(wand,player,item.getVisCost(focus),false))break;
            if(level.setBlockAndUpdate(pos,R3Content.WARDED_BLOCK.get().defaultBlockState())&&level.getBlockEntity(pos) instanceof FocusMemoryBlockEntity tile){tile.remember(state,player.getUUID(),120,1,null);WandFocus.consume(wand,player,item.getVisCost(focus),true);sparkle(level,pos,0xfc9e00);changed=true;}
        }if(changed)level.playSound(null,hit.getBlockPos(),thaumcraft.registry.R2Sounds.ZAP.get(),net.minecraft.sounds.SoundSource.PLAYERS,.25f,1);return changed;
    }
    public static boolean hole(ServerPlayer player,ItemStack wand,ItemStack focus,ThaumicFocusItem item,BlockHitResult hit){
        var level=player.serverLevel();var direction=hit.getDirection().getOpposite();int distance=0,max=33+item.getUpgradeLevel(focus,FocusUpgradeType.enlarge)*8;
        for(;distance<max;distance++){var pos=hit.getBlockPos().relative(direction,distance);if(!FocusMemoryBlockEntity.suitable(level,pos)||!canChange(player,pos))break;}
        if(distance==0)return false;var costs=new LinkedHashMap<>(item.getVisCost(focus));final int length=distance;costs.replaceAll((aspect,amount)->amount*length);
        if(!WandFocus.consume(wand,player,costs,false))return false;
        if(!FocusMemoryBlockEntity.createHole(level,hit.getBlockPos(),player.getUUID(),120+60*item.getUpgradeLevel(focus,FocusUpgradeType.extend),distance+1,direction))return false;
        WandFocus.consume(wand,player,costs,true);level.playSound(null,hit.getBlockPos(),net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,net.minecraft.sounds.SoundSource.PLAYERS,1,1);return true;
    }
    private static ItemStack lootTool(ServerPlayer player,ItemStack focus,ThaumicFocusItem item){var tool=new ItemStack(Items.NETHERITE_PICKAXE);var ench=player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);if(item.isUpgradedWith(focus,FocusUpgradeType.silktouch))tool.enchant(ench.getOrThrow(Enchantments.SILK_TOUCH),1);else {int fortune=item.getUpgradeLevel(focus,FocusUpgradeType.treasure);if(fortune>0)tool.enchant(ench.getOrThrow(Enchantments.FORTUNE),fortune);}if(item.isUpgradedWith(focus,FocusUpgradeType.dowsing))CustomData.update(DataComponents.CUSTOM_DATA,tool,tag->tag.putBoolean("ThaumcraftDowsing",true));return tool;}
    private static boolean harvest(ServerPlayer player,BlockPos pos,ItemStack focus,ThaumicFocusItem item){
        if(!canChange(player,pos))return false;var saved=player.getMainHandItem();player.setItemInHand(InteractionHand.MAIN_HAND,lootTool(player,focus,item));try{return player.gameMode.destroyBlock(pos);}finally{player.setItemInHand(InteractionHand.MAIN_HAND,saved);}
    }
    public static boolean excavate(ServerPlayer player,ItemStack wand,ItemStack focus,ThaumicFocusItem item,BlockHitResult hit){
        var level=player.serverLevel();var pos=hit.getBlockPos();var state=level.getBlockState(pos);float hardness=state.getDestroySpeed(level,pos);if(hardness<0||state.isAir()||!level.mayInteract(player,pos)){stop(player);return false;}
        if(!WandFocus.consume(wand,player,item.getVisCost(focus),false)){stop(player);player.stopUsingItem();return false;}
        long now=level.getGameTime();if(DIG_SOUND.getOrDefault(player.getUUID(),Long.MIN_VALUE)<=now){level.playSound(null,hit.getLocation().x,hit.getLocation().y,hit.getLocation().z,thaumcraft.registry.R2Sounds.RUMBLE.get(),net.minecraft.sounds.SoundSource.PLAYERS,.3f,1);DIG_SOUND.put(player.getUUID(),now+24);}
        var previous=MINING.get(player.getUUID());int potency=item.getUpgradeLevel(focus,FocusUpgradeType.potency);float speed=state.is(BlockTags.MINEABLE_WITH_PICKAXE)||state.is(net.neoforged.neoforge.common.Tags.Blocks.GLASS_BLOCKS)?.25f+.25f*potency:.05f+.1f*potency;if(state.is(Blocks.OBSIDIAN))speed*=3;
        float progress=previous!=null&&previous.dimension.equals(level.dimension())&&previous.pos.equals(pos)&&previous.state.equals(state)?previous.progress+speed:0;
        if(progress>=hardness&&harvest(player,pos,focus,item)){
            WandFocus.consume(wand,player,item.getVisCost(focus),true);clearMining(player,false);
            int extra=item.getUpgradeLevel(focus,FocusUpgradeType.enlarge);for(var side:Direction.values()){if(extra<=0)break;var neighbor=pos.relative(side);if(level.hasChunkAt(neighbor)&&level.getBlockState(neighbor).equals(state)&&WandFocus.consume(wand,player,item.getVisCost(focus),false)&&harvest(player,neighbor,focus,item)){WandFocus.consume(wand,player,item.getVisCost(focus),true);extra--;}}
            if(item.isUpgradedWith(focus,FocusUpgradeType.dowsing))ToolActions.scanOres(player,pos);return true;
        }
        if(player.tickCount%2==0||previous==null)net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(level,null,player.getX(),player.getY(),player.getZ(),64,new thaumcraft.network.FocusDigPayload(player.getId(),hit.getLocation(),true));
        if(previous!=null&&!previous.pos.equals(pos))progress(player,level,previous.pos,-1);MINING.put(player.getUUID(),new Mining(level.dimension(),pos.immutable(),state,progress));progress(player,level,pos,hardness<=0?9:Math.min(9,(int)(progress/hardness*9)));return false;
    }
    public static boolean exchange(ServerPlayer player,ItemStack wand,ItemStack focus,ThaumicFocusItem item,BlockHitResult hit){
        var level=player.serverLevel();var initial=level.getBlockState(hit.getBlockPos());if(initial.hasBlockEntity()||initial.isAir())return false;
        if(player.isShiftKeyDown()){CustomData.update(DataComponents.CUSTOM_DATA,focus,tag->tag.put("PickedBlock",NbtUtils.writeBlockState(initial)));WandFocus.set(wand,focus);return true;}
        var tag=focus.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();if(!tag.contains("PickedBlock"))return false;var replacement=NbtUtils.readBlockState(player.registryAccess().lookupOrThrow(Registries.BLOCK),tag.getCompound("PickedBlock"));if(replacement.isAir()||replacement.hasBlockEntity()||replacement.equals(initial)||replacement.getDestroySpeed(level,hit.getBlockPos())<0)return false;
        List<BlockPos> positions;if(item.isUpgradedWith(focus,FocusUpgradeType.architect))positions=WandArea.blocks(player,wand,focus,item,hit);else{
            var visited=new LinkedHashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(hit.getBlockPos());int limit=3+item.getUpgradeLevel(focus,FocusUpgradeType.enlarge);while(!queue.isEmpty()&&visited.size()<limit){var pos=queue.removeFirst();if(visited.contains(pos)||!level.hasChunkAt(pos)||!level.getBlockState(pos).equals(initial))continue;visited.add(pos.immutable());for(var side:Direction.values())queue.add(pos.relative(side));}positions=List.copyOf(visited);
        }
        boolean changed=false;for(var pos:positions){if(!canChange(player,pos)||level.getBlockEntity(pos)!=null||!WandFocus.consume(wand,player,item.getVisCost(focus),false))continue;
            int slot=-1;for(int i=0;i<player.getInventory().items.size();i++)if(player.getInventory().getItem(i).is(replacement.getBlock().asItem())){slot=i;break;}if(slot<0&&!player.isCreative())break;
            var old=level.getBlockState(pos);var snapshot=net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(),level,pos);
            if(!level.setBlock(pos,replacement,2))continue;if(net.neoforged.neoforge.event.EventHooks.onBlockPlace(player,snapshot,hit.getDirection())){snapshot.restore(3);continue;}
            var drops=Block.getDrops(old,level,pos,null,player,lootTool(player,focus,item));level.sendBlockUpdated(pos,old,replacement,3);
            if(!player.isCreative())player.getInventory().getItem(slot).shrink(1);WandFocus.consume(wand,player,item.getVisCost(focus),true);for(var drop:drops)if(!player.getInventory().add(drop))player.drop(drop,false);sparkle(level,pos,0xc0c0ff);level.levelEvent(net.minecraft.world.level.block.LevelEvent.PARTICLES_DESTROY_BLOCK,pos,Block.getId(old));changed=true;
        }if(changed)level.playSound(null,player.blockPosition(),thaumcraft.registry.R2Sounds.CRAFT_FINISH.get(),net.minecraft.sounds.SoundSource.PLAYERS,.25f,1);return changed;
    }
    private FocusActions(){}
}
