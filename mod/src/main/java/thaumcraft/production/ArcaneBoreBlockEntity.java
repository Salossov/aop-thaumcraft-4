package thaumcraft.production;

import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.items.*;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import thaumcraft.api.aspects.*;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.api.wands.*;
import thaumcraft.items.ThaumicFocusItem;

public final class ArcaneBoreBlockEntity extends BlockEntity implements MenuProvider,IEssentiaTransport,thaumcraft.api.world.IArcaneBore {
    private static final GameProfile PROFILE=new GameProfile(UUID.nameUUIDFromBytes("FakeThaumcraftBore".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"FakeThaumcraftBore");
    public final SimpleContainer items=new SimpleContainer(2){@Override public void setChanged(){super.setChanged();ArcaneBoreBlockEntity.this.changed();}@Override public boolean canPlaceItem(int slot,ItemStack stack){return slot==0?stack.getItem() instanceof ThaumicFocusItem focus&&focus.kind()==ThaumicFocusItem.Kind.EXCAVATION:stack.getItem() instanceof PickaxeItem;}};
    public final InvWrapper handler=new InvWrapper(items);
    // Client visual state is continuous across target and redstone updates.
    private float visualYaw,previousYaw,visualPitch,previousPitch,spin,previousSpin;
    private boolean visualReady;
    public record Aim(float yaw,float pitch){
        public Vec3 forward(){double y=Math.toRadians(yaw),p=Math.toRadians(pitch);return new Vec3(-Math.sin(p)*Math.cos(y),Math.cos(p),Math.sin(p)*Math.sin(y));}
    }
    public static Aim aimAt(Vec3 direction){
        double horizontal=Math.sqrt(direction.x*direction.x+direction.z*direction.z);
        return new Aim((float)Math.toDegrees(Math.atan2(direction.z,-direction.x)),90-(float)Math.toDegrees(Math.atan2(direction.y,horizontal)));
    }
    private Aim desiredAim(){return aimAt(target==null?Vec3.atLowerCornerOf(facing().getNormal()):target.getCenter().subtract(worldPosition.getCenter()));}
    public Aim aim(float partial){return visualReady?new Aim(net.minecraft.util.Mth.lerp(partial,previousYaw,visualYaw),net.minecraft.util.Mth.lerp(partial,previousPitch,visualPitch)):desiredAim();}
    public float spin(float partial){return net.minecraft.util.Mth.lerp(partial,previousSpin,spin);}
    public boolean working(){return !base()&&target!=null&&powered()&&valid()&&baseTile()!=null;}
    private void clientTick(){
        var wanted=desiredAim();
        if(!visualReady){visualYaw=wanted.yaw;visualPitch=wanted.pitch;visualReady=true;}
        previousYaw=visualYaw;previousPitch=visualPitch;previousSpin=spin;
        visualYaw+=net.minecraft.util.Mth.wrapDegrees(wanted.yaw-visualYaw)*.35f;visualPitch+=(wanted.pitch-visualPitch)*.35f;
        if(working()){
            if(delay>0)delay--;
            spin+=Math.min(10,(float)target.getCenter().distanceTo(worldPosition.getCenter())/6);
        }else{float remainder=spin%90;if(remainder>.001f)spin+=Math.min(10,90-remainder);}
        if(spin>3600){spin-=3600;previousSpin-=3600;}
    }
    private BlockPos target,last;
    private BlockState expected;
    private int delay,spiral,repairTick,totalDelay;
    private float radius,increment,boost;
    private final Map<Aspect,Integer> repairVis=new HashMap<>();
    public int speed,fortune,area;
    public ArcaneBoreBlockEntity(BlockPos pos,BlockState state){super(thaumcraft.registry.R3Content.BORE_ENTITY.get(),pos,state);}
    public boolean base(){return ((ArcaneBoreBlock)getBlockState().getBlock()).base;}
    public Direction facing(){return getBlockState().getValue(ArcaneBoreBlock.FACING);}
    public BlockPos target(){return target;}
    public float miningProgress(float partial){return target==null||totalDelay<=0?0:Math.clamp(1-(delay-(working()?partial:0))/(float)totalDelay,0,1);}
    public float boost(){return boost;}
    public boolean powered(){return level.hasNeighborSignal(worldPosition)||level.hasNeighborSignal(basePosition());}
    @Override public thaumcraft.api.world.IArcaneBore.State inspectBore(){var pick=items.getItem(1);boolean hasPick=items.canPlaceItem(1,pick),hasFocus=items.canPlaceItem(0,items.getItem(0)),nearBroken=!pick.isEmpty()&&pick.getDamageValue()+1==pick.getMaxDamage();return new thaumcraft.api.world.IArcaneBore.State(hasPick,hasFocus,nearBroken,powered()&&hasFocus&&hasPick&&pick.isDamageableItem()&&!nearBroken,1+(area+2)*2,speed,pick.is(thaumcraft.registry.R3Equipment.TOOLS.get("elemental_pickaxe").get()),enchant(Enchantments.FORTUNE),enchant(Enchantments.SILK_TOUCH)>0);}
    public BlockPos basePosition(){return worldPosition.relative(getBlockState().getValue(ArcaneBoreBlock.HANGING)?Direction.UP:Direction.DOWN);}
    private ArcaneBoreBlockEntity baseTile(){return level.hasChunkAt(basePosition())&&level.getBlockEntity(basePosition()) instanceof ArcaneBoreBlockEntity tile&&tile.base()?tile:null;}
    public int upgrade(FocusUpgradeType type){return items.getItem(0).getItem() instanceof ItemFocusBasic focus?focus.getUpgradeLevel(items.getItem(0),type):0;}
    public int enchant(net.minecraft.resources.ResourceKey<Enchantment> key){return level==null?0:EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key),items.getItem(1));}
    public boolean valid(){var pick=items.getItem(1);return items.canPlaceItem(0,items.getItem(0))&&items.canPlaceItem(1,pick)&&pick.isDamageableItem()&&pick.getDamageValue()+1<pick.getMaxDamage();}
    public void changed(){setChanged();if(level!=null){speed=upgrade(FocusUpgradeType.potency)+enchant(Enchantments.EFFICIENCY);fortune=Math.max(upgrade(FocusUpgradeType.treasure),enchant(Enchantments.FORTUNE));area=upgrade(FocusUpgradeType.enlarge);level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}}
    public void resetAim(){target=null;expected=null;last=null;radius=0;increment=0;delay=totalDelay=0;changed();}
    @Override public void setLevel(Level level){super.setLevel(level);speed=upgrade(FocusUpgradeType.potency)+enchant(Enchantments.EFFICIENCY);fortune=Math.max(upgrade(FocusUpgradeType.treasure),enchant(Enchantments.FORTUNE));area=upgrade(FocusUpgradeType.enlarge);}
    public static void tick(Level level,BlockPos pos,BlockState state,ArcaneBoreBlockEntity tile){if(level.isClientSide){if(!tile.base())tile.clientTick();}else if(level instanceof ServerLevel server&&!tile.base())tile.serverTick(server);}
    private void serverTick(ServerLevel server){var base=baseTile();if(boost<20){boost+=VisNetHandler.drainVis(server,worldPosition,thaumcraft.api.aspects.Aspects.ENTROPY,100)/5f;if(boost<20&&base!=null&&base.draw())boost+=20;}
        if(++repairTick%5==0)repair(server);
        if(base==null||!powered()||!valid())return;if(--delay>0)return;
        if(target!=null){var pos=target;target=null;if(server.hasChunkAt(pos)&&server.getBlockState(pos).equals(expected)&&harvest(server,pos,base)){if(boost>0)boost=Math.max(0,boost-1);}}findTarget(server);setChanged();
    }
    private boolean harvest(ServerLevel server,BlockPos pos,ArcaneBoreBlockEntity base){var player=FakePlayerFactory.get(server,PROFILE);player.moveTo(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,0,0);if(!server.mayInteract(player,pos)||!server.getWorldBorder().isWithinBounds(pos))return false;ItemStack pick=items.getItem(1),tool=pick.copy();if(fortune>enchant(Enchantments.FORTUNE))tool.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE),fortune);if(upgrade(FocusUpgradeType.silktouch)>0)tool.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),1);if(upgrade(FocusUpgradeType.dowsing)>0)net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,tool,tag->tag.putBoolean("ThaumcraftDowsing",true));player.setItemInHand(InteractionHand.MAIN_HAND,tool);if(!player.gameMode.destroyBlock(pos))return false;
        // Harvest hooks and protection run through the normal server path. The bore routes the resulting entities.
        for(var entity:server.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1),ItemEntity::isAlive)){var stack=entity.getItem();var output=base.worldPosition.relative(base.facing());var inventory=server.hasChunkAt(output)?server.getCapability(Capabilities.ItemHandler.BLOCK,output,base.facing().getOpposite()):null;var rest=inventory==null?stack:ItemHandlerHelper.insertItemStacked(inventory,stack,false);entity.discard();if(!rest.isEmpty()){var drop=new ItemEntity(server,base.worldPosition.getX()+.5+base.facing().getStepX()*.66,base.worldPosition.getY()+.4+base.facing().getStepY()*.66,base.worldPosition.getZ()+.5+base.facing().getStepZ()*.66,rest);drop.setDeltaMovement(base.facing().getStepX()*.075,.025+base.facing().getStepY()*.075,base.facing().getStepZ()*.075);server.addFreshEntity(drop);}}
        pick.setDamageValue(Math.min(pick.getMaxDamage()-1,Math.max(pick.getDamageValue()+1,tool.getDamageValue())));changed();return true;
    }
    private void findTarget(ServerLevel server){int extent=2+area;if(increment==0)increment=extent/360f;BlockPos start=null;for(int tries=0;tries<720;tries++){spiral=(spiral+2)%360;radius+=increment;if(Math.abs(radius)>extent)increment=-increment;var offset=new Vec3(0,radius,0).zRot(spiral*(float)Math.PI/180).yRot((float)Math.PI/2*facing().getStepX()).xRot((float)Math.PI/2*facing().getStepY());start=BlockPos.containing(worldPosition.getCenter().add(Vec3.atLowerCornerOf(facing().getNormal())).add(offset));if(!start.equals(last))break;}last=start;
        for(int depth=3;depth<67;depth++){var pos=start.relative(facing(),depth-1);if(server.isOutsideBuildHeight(pos)||!server.hasChunkAt(pos))break;var state=server.getBlockState(pos);if(state.getDestroySpeed(server,pos)<0)break;if(state.isAir()||state.getCollisionShape(server,pos).isEmpty()||!state.getFluidState().isEmpty())continue;
            var hit=server.clip(new ClipContext(worldPosition.getCenter().add(Vec3.atLowerCornerOf(facing().getNormal())),pos.getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));if(hit.getType()==HitResult.Type.BLOCK)pos=hit.getBlockPos();state=server.getBlockState(pos);if(state.getDestroySpeed(server,pos)<0)break;target=pos.immutable();expected=state;delay=(10+Math.max(10-speed,(int)(state.getDestroySpeed(server,pos)*2)-speed*2))*(boost<1?4:1);totalDelay=delay;changed();return;}
    }
    private void repair(ServerLevel server){var stack=items.getItem(1);int rank=Math.min(2,thaumcraft.equipment.EquipmentEvents.enchantmentLevel(stack,server,"repair"));if(rank<=0||!stack.isDamaged()||!(stack.getItem() instanceof thaumcraft.api.IRepairable))return;var primals=new HashMap<Aspect,Integer>();thaumcraft.data.ThaumcraftData.aspects(server.getServer(),stack).forEach((tag,amount)->reduce(primals,Aspect.getAspect(tag),amount));if(primals.isEmpty())return;var cost=new HashMap<Aspect,Integer>();primals.forEach((aspect,amount)->cost.put(aspect,(int)Math.sqrt(amount*2)*rank));boolean ready=true;for(var entry:cost.entrySet()){int needed=entry.getValue(),stored=repairVis.getOrDefault(entry.getKey(),0);stored+=VisNetHandler.drainVis(server,worldPosition,entry.getKey(),Math.max(0,needed-stored));repairVis.put(entry.getKey(),stored);ready&=stored>=needed;}if(ready&&repairTick%40==0){cost.forEach((aspect,amount)->repairVis.merge(aspect,-amount,Integer::sum));stack.setDamageValue(Math.max(0,stack.getDamageValue()-rank));changed();}}
    private static void reduce(Map<Aspect,Integer> result,Aspect aspect,int amount){if(aspect==null)return;if(aspect.isPrimal())result.merge(aspect,amount,Integer::sum);else for(var component:aspect.getComponents())reduce(result,component,amount);}
    private boolean draw(){for(var direction:Direction.values()){var pos=worldPosition.relative(direction);if(!level.hasChunkAt(pos))continue;var transport=level.getCapability(EssentiaCapabilities.TRANSPORT,pos,direction.getOpposite());if(transport!=null&&transport.canOutputTo(direction.getOpposite())&&transport.getSuctionAmount(direction.getOpposite())<getSuctionAmount(direction)&&transport.takeEssentia(thaumcraft.api.aspects.Aspects.ENTROPY,1,direction.getOpposite())==1)return true;}return false;}
    @Override public boolean isConnectable(Direction face){return base();}@Override public boolean canInputFrom(Direction face){return base();}@Override public boolean canOutputTo(Direction face){return false;}@Override public void setSuction(Aspect aspect,int amount){}@Override public Aspect getSuctionType(Direction face){return thaumcraft.api.aspects.Aspects.ENTROPY;}@Override public int getSuctionAmount(Direction face){return base()&&face!=facing()?128:0;}@Override public Aspect getEssentiaType(Direction face){return null;}@Override public int getEssentiaAmount(Direction face){return 0;}@Override public int takeEssentia(Aspect aspect,int amount,Direction face){return 0;}@Override public int addEssentia(Aspect aspect,int amount,Direction face){return 0;}@Override public int getMinimumSuction(){return 0;}@Override public boolean renderExtendedTube(){return true;}
    @Override public Component getDisplayName(){return Component.translatable("block.thaumcraft.arcane_bore");}@Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return base()?null:new thaumcraft.menu.ArcaneBoreMenu(id,inventory,this);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.put("Items",ContainerNbt.save(items,registries));tag.putFloat("Boost",boost);tag.putInt("Spiral",spiral);tag.putFloat("Radius",radius);tag.putFloat("Increment",increment);var vis=new CompoundTag();repairVis.forEach((aspect,amount)->vis.putInt(aspect.getTag(),amount));tag.put("RepairVis",vis);if(target!=null){tag.putLong("Target",target.asLong());if(expected!=null)tag.put("Expected",NbtUtils.writeBlockState(expected));tag.putInt("Delay",delay);tag.putInt("TotalDelay",totalDelay);}}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);ContainerNbt.load(items,tag.getList("Items",Tag.TAG_COMPOUND),registries);boost=Math.max(0,tag.getFloat("Boost"));spiral=tag.getInt("Spiral");radius=tag.getFloat("Radius");increment=tag.getFloat("Increment");repairVis.clear();var vis=tag.getCompound("RepairVis");for(var key:vis.getAllKeys()){var aspect=Aspect.getAspect(key);if(aspect!=null)repairVis.put(aspect,Math.max(0,vis.getInt(key)));}target=tag.contains("Target")?BlockPos.of(tag.getLong("Target")):null;delay=tag.getInt("Delay");totalDelay=Math.max(1,tag.contains("TotalDelay")?tag.getInt("TotalDelay"):delay);expected=tag.contains("Expected")?NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK),tag.getCompound("Expected")):null;}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}@Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
