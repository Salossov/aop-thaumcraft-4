package thaumcraft.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import thaumcraft.entities.*;
import java.util.*;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;

public final class GolemBellItem extends Item {
    public GolemBellItem(){super(new Properties().stacksTo(1));}
    public static CompoundTag data(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
    public static ThaumicGolem selected(ItemStack stack,Level level){var tag=data(stack);if(!tag.hasUUID("Golem")||!level.dimension().location().toString().equals(tag.getString("Dimension")))return null;var entity=level instanceof ServerLevel server?server.getEntity(tag.getUUID("Golem")):level.getEntity(tag.getInt("GolemId"));return entity instanceof ThaumicGolem golem&&golem.isAlive()&&golem.getUUID().equals(tag.getUUID("Golem"))?golem:null;}
    public static List<GolemMarker> markers(ItemStack stack){var result=new ArrayList<GolemMarker>();for(var entry:data(stack).getList("Markers",Tag.TAG_COMPOUND)){var marker=GolemMarker.load((CompoundTag)entry);if(marker!=null)result.add(marker);}return result;}
    private static void storeMarkers(ItemStack stack,List<GolemMarker> markers){var list=new ListTag();markers.forEach(marker->list.add(marker.save()));CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->tag.put("Markers",list));}
    private static void clearSelection(ItemStack stack){CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{for(var key:List.of("Golem","GolemId","Dimension","Home","HomeFace","Markers"))tag.remove(key);});}
    private static void ring(Player player,net.minecraft.core.BlockPos pos){player.level().playSound(null,pos,net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,net.minecraft.sounds.SoundSource.PLAYERS,.7f,1+player.getRandom().nextFloat()*.1f);}
    @Override public InteractionResult interactLivingEntity(ItemStack stack,Player player,LivingEntity target,InteractionHand hand){if(!(target instanceof ThaumicGolem golem))return InteractionResult.PASS;if(!player.level().isClientSide){clearSelection(stack);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putUUID("Golem",golem.getUUID());tag.putInt("GolemId",golem.getId());tag.putString("Dimension",golem.level().dimension().location().toString());tag.putLong("Home",golem.home().asLong());tag.putByte("HomeFace",(byte)golem.homeFace().get3DDataValue());});storeMarkers(stack,golem.markers());if(player.getAbilities().instabuild)player.setItemInHand(hand,stack.copy());ring(player,golem.blockPosition());}return InteractionResult.sidedSuccess(player.level().isClientSide);}
    public static void changeMarkers(ItemStack stack,Player player,net.minecraft.core.BlockPos pos,net.minecraft.core.Direction face){
        if(player.level().isClientSide)return;var golem=selected(stack,player.level());if(data(stack).hasUUID("Golem")&&golem==null){clearSelection(stack);ring(player,pos);return;}
        var list=markers(stack);int found=-1;for(int i=0;i<list.size();i++){var m=list.get(i);if(m.dimension().equals(player.level().dimension())&&m.pos().equals(pos)&&m.face()==face){found=i;break;}}
        if(found>=0){var previous=list.remove(found);if(golem!=null&&golem.upgrade(4)>0&&!player.isShiftKeyDown()&&previous.color()<15){int color=previous.color()+1;list.add(new GolemMarker(player.level().dimension(),pos,face,color));thaumcraft.network.Notify.send(player,"tc.markerchange","color.minecraft."+net.minecraft.world.item.DyeColor.byId(color).getName(),0xffffff,false);}}
        else list.add(new GolemMarker(player.level().dimension(),pos,face,-1));
        storeMarkers(stack,list);if(golem!=null)golem.replaceMarkers(list);ring(player,pos);
    }
    public static void resetMarkers(Player player){var stack=player.getMainHandItem();if(!(stack.getItem() instanceof GolemBellItem))return;var golem=selected(stack,player.level());if(golem!=null){storeMarkers(stack,List.of());golem.replaceMarkers(List.of());ring(player,player.blockPosition());}}
    @Override public InteractionResult onItemUseFirst(ItemStack stack,UseOnContext context){if(context.getPlayer()!=null){var water=getPlayerPOVHitResult(context.getLevel(),context.getPlayer(),ClipContext.Fluid.SOURCE_ONLY);changeMarkers(stack,context.getPlayer(),water.getType()==HitResult.Type.BLOCK?water.getBlockPos():context.getClickedPos(),water.getType()==HitResult.Type.BLOCK?water.getDirection():context.getClickedFace());}return InteractionResult.sidedSuccess(context.getLevel().isClientSide);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){var stack=player.getItemInHand(hand);var hit=getPlayerPOVHitResult(level,player,ClipContext.Fluid.SOURCE_ONLY);if(hit.getType()==HitResult.Type.BLOCK)changeMarkers(stack,player,hit.getBlockPos(),hit.getDirection());return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}
    @Override public boolean onLeftClickEntity(ItemStack stack,Player player,Entity entity){if(entity instanceof ThaumicGolem golem&&golem.isAlive()){golem.dismantle(player);return true;}if(entity instanceof TravelingTrunk trunk&&trunk.isAlive()&&trunk.allowed(player)){trunk.dismantle(player);return true;}return false;}
}
