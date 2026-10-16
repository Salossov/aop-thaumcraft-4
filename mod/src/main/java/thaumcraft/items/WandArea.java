package thaumcraft.items;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.blocks.FocusMemoryBlockEntity;

/** Original TC4 G / sneak-G dimensions are relative to the selected face. */
public final class WandArea {
    public static int maximum(ItemStack focus,ThaumicFocusItem item){return 3+item.getUpgradeLevel(focus,FocusUpgradeType.enlarge)*(item.kind()==ThaumicFocusItem.Kind.TRADE?2:1);}
    public static int size(ItemStack wand,String axis,int maximum){var tag=wand.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return tag.contains("area"+axis)?Math.clamp(tag.getInt("area"+axis),0,maximum):maximum;}
    public static boolean toggle(ServerPlayer player){var wand=player.getMainHandItem();var focus=WandFocus.get(wand);if(!(focus.getItem() instanceof ThaumicFocusItem item)||!item.isUpgradedWith(focus,FocusUpgradeType.architect))return false;int max=maximum(focus,item),x=size(wand,"x",max),y=size(wand,"y",max),z=size(wand,"z",max);CustomData.update(DataComponents.CUSTOM_DATA,wand,tag->{int dim=Math.clamp(tag.getInt("areadim"),0,item.kind()==ThaumicFocusItem.Kind.TRADE?2:3);if(player.isShiftKeyDown()){tag.putInt("areadim",(dim+1)%(item.kind()==ThaumicFocusItem.Kind.TRADE?3:4));}else{tag.putInt("areax",(dim==0||dim==1)?(x+1)%(max+1):x);tag.putInt("areaz",(dim==0||dim==2)?(z+1)%(max+1):z);tag.putInt("areay",(dim==0||dim==3)?(y+1)%(max+1):y);}});return true;}
    public static List<BlockPos> blocks(ServerPlayer player,ItemStack wand,ItemStack focus,ThaumicFocusItem item,BlockHitResult hit){
        if(!item.isUpgradedWith(focus,FocusUpgradeType.architect))return List.of(hit.getBlockPos());
        var level=player.serverLevel();var origin=hit.getBlockPos();var initial=level.getBlockState(origin);boolean trade=item.kind()==ThaumicFocusItem.Kind.TRADE;
        boolean restoring=level.getBlockEntity(origin) instanceof FocusMemoryBlockEntity;
        int max=maximum(focus,item),sx=size(wand,"x",max),sy=size(wand,"y",max),sz=size(wand,"z",max);
        var visited=new HashSet<BlockPos>();var result=new ArrayList<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(origin);
        while(!queue.isEmpty()&&visited.size()<12000){var pos=queue.removeFirst();if(!visited.add(pos))continue;int dx=Math.abs(pos.getX()-origin.getX()),dy=Math.abs(pos.getY()-origin.getY()),dz=Math.abs(pos.getZ()-origin.getZ());boolean within=switch(hit.getDirection().getAxis()){case Y->dx<=sx&&dz<=sz&&(trade?dy==0:dy<=sy);case Z->dx<=sz&&dy<=sx&&(trade?dz==0:dz<=sy);case X->dy<=sx&&dz<=sz&&(trade?dx==0:dx<=sy);};if(!within||!level.hasChunkAt(pos))continue;
            var state=level.getBlockState(pos);var tile=level.getBlockEntity(pos);if(trade){if(!state.equals(initial)||state.getDestroySpeed(level,pos)<0||Arrays.stream(Direction.values()).noneMatch(side->level.hasChunkAt(pos.relative(side))&&!level.getBlockState(pos.relative(side)).isSolidRender(level,pos.relative(side))))continue;}else if(restoring){if(!(tile instanceof FocusMemoryBlockEntity memory)||!memory.ownedBy(player.getUUID()))continue;}else if(tile!=null||!state.isSolidRender(level,pos)||state.isAir())continue;
            result.add(pos.immutable());for(var side:Direction.values())if(!trade||side.getAxis()!=hit.getDirection().getAxis())queue.add(pos.relative(side));
        }
        result.sort(Comparator.comparingDouble(pos->pos.distSqr(origin)));return result;
    }
    private WandArea(){}
}
