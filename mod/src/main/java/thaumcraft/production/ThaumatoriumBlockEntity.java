package thaumcraft.production;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.*;
import thaumcraft.api.aspects.*;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.crafting.R3Recipes;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.registry.R3Content;
import thaumcraft.menu.ThaumatoriumMenu;
/** Original five-tick demand-driven automation; output is checked before any resource draw. */
public final class ThaumatoriumBlockEntity extends BlockEntity implements WorldlyContainer,MenuProvider,IAspectContainer,IEssentiaTransport {
 public final EssentiaStore store=new EssentiaStore(Integer.MAX_VALUE,true,false);
 public final LinkedHashMap<ResourceLocation,UUID> selections=new LinkedHashMap<>();
 private NonNullList<ItemStack> input=NonNullList.withSize(1,ItemStack.EMPTY);
 private CrucibleRecipe currentRecipe;
 private ResourceLocation currentId;
 private Aspect demand;
 private int ticks;
 public boolean heated;
 public int maxRecipes=1;
 public ThaumatoriumBlockEntity(BlockPos pos,BlockState state){super(R3Content.THAUMATORIUM_ENTITY.get(),pos,state);}
 public Direction facing(){return getBlockState().getValue(ThaumatoriumBlock.FACING);}
 public List<RecipeHolder<CrucibleRecipe>> recipes(){return level.getRecipeManager().getAllRecipesFor(R3Recipes.CRUCIBLE_TYPE.get()).stream().sorted(Comparator.comparing(h->h.id().toString())).toList();}
 public boolean toggle(ServerPlayer player,ResourceLocation id){
  if(!stillValid(player))return false;if(selections.remove(id)!=null){currentRecipe=null;currentId=null;demand=null;sync();return true;}
  var recipe=recipes().stream().filter(h->h.id().equals(id)).findFirst().orElse(null);
  upgrades();if(recipe==null||selections.size()>=maxRecipes||!recipe.value().catalyst().test(getItem(0))||!KnowledgeService.get(player).completedResearch().contains(recipe.value().research()))return false;
  selections.put(id,player.getUUID());currentRecipe=null;demand=null;sync();return true;
 }
 public void serverTick(){
  if(ticks%40==0){heated=R3Content.heatSource(level,worldPosition.below(2));upgrades();}ticks++;
  if(!heated||powered()||!ThaumatoriumBlock.valid(level,worldPosition)||selections.isEmpty()||getItem(0).isEmpty()){if(demand!=null){demand=null;sync();}return;}if(ticks%5!=0)return;
  var all=recipes();currentRecipe=null;currentId=null;for(var id:selections.keySet()){var holder=all.stream().filter(h->h.id().equals(id)&&h.value().catalyst().test(getItem(0))).findFirst().orElse(null);if(holder!=null){currentRecipe=holder.value();currentId=id;break;}}
  if(currentRecipe==null){demand=null;return;}
  var front=worldPosition.relative(facing());if(!level.hasChunkAt(front)){demand=null;return;}
  var destination=level.getCapability(Capabilities.ItemHandler.BLOCK,front,facing().getOpposite());
  if(destination!=null&&!ItemHandlerHelper.insertItemStacked(destination,currentRecipe.getRecipeOutput(),true).isEmpty()){if(demand!=null){demand=null;sync();}return;}
  Aspect before=demand;demand=null;for(var tag:currentRecipe.aspects().keySet().stream().sorted(Comparator.comparing(t->Aspect.getAspect(t).getName())).toList()){var a=Aspect.getAspect(tag);if(containerContains(a)<currentRecipe.aspects().get(tag)){demand=a;break;}}
  if(demand==null){complete(destination);}else{if(before!=demand)sync();fill();}
 }
 private void complete(IItemHandler destination){
  if(!currentRecipe.matches(this,getItem(0)))return;var output=currentRecipe.getRecipeOutput();var owner=level.getServer().getPlayerList().getPlayer(selections.get(currentId));removeItem(0,1);store.clear();
  if(owner!=null)output.onCraftedBy(level,owner,output.getCount());
  var remainder=destination==null?output:ItemHandlerHelper.insertItemStacked(destination,output,false);if(!remainder.isEmpty()){var d=facing();var entity=new ItemEntity(level,worldPosition.getX()+.5+d.getStepX()*.66,worldPosition.getY()+.33-d.getStepY(),worldPosition.getZ()+.5+d.getStepZ()*.66,remainder);entity.setDeltaMovement(d.getStepX()*.075,.025,d.getStepZ()*.075);level.addFreshEntity(entity);}
  level.playSound(null,worldPosition,SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.25f,2.6f+(level.random.nextFloat()-level.random.nextFloat())*.8f);currentRecipe=null;currentId=null;demand=null;sync();
 }
 private void fill(){for(int y=0;y<=1;y++)for(var face:Direction.values()){
  if(face==facing()||face==Direction.DOWN||y==0&&face==Direction.UP)continue;var p=worldPosition.above(y).relative(face);if(!level.hasChunkAt(p))continue;var source=level.getCapability(EssentiaCapabilities.TRANSPORT,p,face.getOpposite());if(source==null||!source.isConnectable(face.getOpposite())||!source.canOutputTo(face.getOpposite())||source.getEssentiaAmount(face.getOpposite())<=0||source.getSuctionAmount(face.getOpposite())>=128||source.getMinimumSuction()>128)continue;
  int taken=source.takeEssentia(demand,1,face.getOpposite());if(taken>0){int remainder=addToContainer(demand,taken);if(remainder>0)source.addEssentia(demand,remainder,face.getOpposite());return;}
 }}
 private boolean powered(){return level.hasNeighborSignal(worldPosition)||level.hasNeighborSignal(worldPosition.below())||level.hasNeighborSignal(worldPosition.above());}
 private void upgrades(){int capacity=1;for(int y=0;y<=1;y++)for(var face:Direction.values()){if(face==Direction.DOWN||face==facing())continue;var p=worldPosition.above(y).relative(face);if(!level.hasChunkAt(p))continue;var s=level.getBlockState(p);if(s.is(R3Content.BRAINBOX.get())&&s.getValue(BrainboxBlock.FACING)==face.getOpposite())capacity+=2;}if(capacity!=maxRecipes){maxRecipes=capacity;while(selections.size()>maxRecipes)selections.remove(new ArrayList<>(selections.keySet()).getLast());currentRecipe=null;demand=null;sync();}}
 public void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 @Override public boolean isConnectable(Direction face){return face!=facing();}
 @Override public boolean canInputFrom(Direction face){return isConnectable(face);}
 @Override public boolean canOutputTo(Direction face){return false;}
 @Override public void setSuction(Aspect aspect,int amount){}
 @Override public Aspect getSuctionType(Direction face){return demand;}
 @Override public int getSuctionAmount(Direction face){return demand==null?0:128;}
 @Override public Aspect getEssentiaType(Direction face){return null;}
 @Override public int getEssentiaAmount(Direction face){return 0;}
 @Override public int takeEssentia(Aspect aspect,int amount,Direction face){return 0;}
 @Override public int addEssentia(Aspect aspect,int amount,Direction face){return canInputFrom(face)?amount-addToContainer(aspect,amount):0;}
 @Override public int getMinimumSuction(){return 0;}
 @Override public boolean renderExtendedTube(){return false;}
 @Override public AspectList getAspects(){return store.getAspects();}
 @Override public void setAspects(AspectList aspects){store.setAspects(aspects);sync();}
 @Override public boolean doesContainerAccept(Aspect a){return a!=null&&currentRecipe!=null&&containerContains(a)<currentRecipe.aspects().getOrDefault(a.getTag(),0);}
 @Override public int addToContainer(Aspect a,int n){if(a==null||n<=0||currentRecipe==null)return n;int add=Math.min(n,Math.max(0,currentRecipe.aspects().getOrDefault(a.getTag(),0)-containerContains(a)));if(add>0){store.addToContainer(a,add);sync();}return n-add;}
 @Override public boolean takeFromContainer(Aspect a,int n){boolean taken=store.takeFromContainer(a,n);if(taken)sync();return taken;}
 @Override public boolean doesContainerContainAmount(Aspect a,int n){return store.doesContainerContainAmount(a,n);}
 @Override public int containerContains(Aspect a){return store.containerContains(a);}
 @Override public int getContainerSize(){return 1;}
 @Override public boolean isEmpty(){return input.getFirst().isEmpty();}
 @Override public ItemStack getItem(int slot){return input.get(slot);}
 @Override public ItemStack removeItem(int slot,int n){var s=ContainerHelper.removeItem(input,slot,n);if(!s.isEmpty())sync();return s;}
 @Override public ItemStack removeItemNoUpdate(int slot){return ContainerHelper.takeItem(input,slot);}
 @Override public void setItem(int slot,ItemStack stack){input.set(slot,stack);stack.limitSize(getMaxStackSize(stack));currentRecipe=null;demand=null;sync();}
 @Override public void clearContent(){input.clear();sync();}
 @Override public int[] getSlotsForFace(Direction face){return new int[]{0};}
 @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction face){return true;}
 @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction face){return true;}
 @Override public boolean stillValid(Player player){return level!=null&&level.getBlockEntity(worldPosition)==this&&player.distanceToSqr(worldPosition.getCenter())<=64&&ThaumatoriumBlock.valid(level,worldPosition);}
 @Override public Component getDisplayName(){return Component.translatable("block.thaumcraft.thaumatorium");}
 @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new ThaumatoriumMenu(id,inventory,this);}
 @Override public void writeClientSideData(AbstractContainerMenu menu,net.minecraft.network.RegistryFriendlyByteBuf buffer){buffer.writeBlockPos(worldPosition);buffer.writeUUID(((ThaumatoriumMenu)menu).token());}
 @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);ContainerHelper.saveAllItems(t,input,r);var list=new ListTag();selections.forEach((id,owner)->{var e=new CompoundTag();e.putString("Recipe",id.toString());e.putUUID("Player",owner);list.add(e);});t.put("Selections",list);var contents=new CompoundTag();store.getAspects().asMap().forEach((a,n)->contents.putInt(a.getTag(),n));t.put("Essentia",contents);t.putInt("Capacity",maxRecipes);t.putBoolean("Heated",heated);}
 @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);input=NonNullList.withSize(1,ItemStack.EMPTY);ContainerHelper.loadAllItems(t,input,r);selections.clear();var list=t.getList("Selections",10);if(list.size()>32)throw new IllegalArgumentException("Invalid saved automatic recipes");for(var entry:list){var e=(CompoundTag)entry;if(e.hasUUID("Player"))selections.put(ResourceLocation.parse(e.getString("Recipe")),e.getUUID("Player"));}store.clear();var contents=t.getCompound("Essentia");for(var key:contents.getAllKeys()){int n=contents.getInt(key);var a=Aspect.getAspect(key);if(a==null||n<0||n>32767)throw new IllegalArgumentException("Invalid automatic essentia");store.addToContainer(a,n);}maxRecipes=Math.clamp(t.getInt("Capacity"),1,32);heated=t.getBoolean("Heated");currentRecipe=null;currentId=null;demand=null;ticks=0;}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
