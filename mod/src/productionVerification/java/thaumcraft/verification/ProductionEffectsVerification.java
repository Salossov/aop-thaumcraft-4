package thaumcraft.verification;
import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.registry.*;
import thaumcraft.production.*;
import thaumcraft.entities.*;
import thaumcraft.mixin.FurnaceCookingAccessor;
final class ProductionEffectsVerification {
 static void run(ServerLevel l,BiConsumer<Boolean,String> check){
  var p=new BlockPos(20,140,0);for(var c:BlockPos.betweenClosed(p.offset(-4,-3,-4),p.offset(7,4,4))){l.getChunkAt(c);l.setBlock(c,Blocks.AIR.defaultBlockState(),2);}l.setBlock(p,Blocks.FURNACE.defaultBlockState(),3);var furnace=(FurnaceBlockEntity)l.getBlockEntity(p);furnace.setItem(0,new ItemStack(Items.RAW_IRON));furnace.setItem(1,new ItemStack(Items.COAL));for(int t=0;t<10;t++)AbstractFurnaceBlockEntity.serverTick(l,p,l.getBlockState(p),furnace);var cooking=(FurnaceCookingAccessor)furnace;int initial=cooking.thaumcraft$cookingProgress(),fuel=furnace.saveWithoutMetadata(l.registryAccess()).getInt("BurnTime");check.accept(initial>0,"vanilla furnace actually starts smelting before bellows test");
  l.setBlock(p.west(),R3Content.BELLOWS.get().defaultBlockState().setValue(BellowsBlock.FACING,Direction.EAST),3);var bellows=(BellowsBlockEntity)l.getBlockEntity(p.west());bellows.tick();bellows.tick();check.accept(cooking.thaumcraft$cookingProgress()==initial+1&&furnace.saveWithoutMetadata(l.registryAccess()).getInt("BurnTime")==fuel,"bellows adds original one cooking tick per two ticks without extra fuel use");check.accept(R3Content.bellows(l,p)==1,"only a bellows pointing into the device counts");
  l.setBlock(p.west().above(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);for(int t=0;t<10;t++)bellows.tick();check.accept(cooking.thaumcraft$cookingProgress()==initial+1&&R3Content.bellows(l,p)==0,"powered bellows stops both furnace acceleration and thaumic assistance");l.removeBlock(p.west().above(),false);
  l.setBlock(p.west(),R3Content.BELLOWS.get().defaultBlockState().setValue(BellowsBlock.FACING,Direction.WEST),3);bellows=(BellowsBlockEntity)l.getBlockEntity(p.west());bellows.tick();bellows.tick();check.accept(cooking.thaumcraft$cookingProgress()==initial+1&&R3Content.bellows(l,p)==0,"a reversed bellows does not accelerate its neighbouring furnace");
  l.setBlock(p.above(),R3Content.BELLOWS.get().defaultBlockState().setValue(BellowsBlock.FACING,Direction.DOWN),3);check.accept(R3Content.bellows(l,p)==1,"vertical bellows connection matches the original six-side check");
  var player=FakePlayerFactory.get(l,new GameProfile(UUID.randomUUID(),"R3_Projectile_Test"));player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.setPos(p.getX()+.5,p.getY(),p.getZ()+3);player.setYRot(-90);player.setXRot(0);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(R3Content.ALUMENTUM.get(),2));R3Content.ALUMENTUM.get().use(l,player,net.minecraft.world.InteractionHand.MAIN_HAND);var shots=l.getEntitiesOfClass(AlumentumProjectile.class,new net.minecraft.world.phys.AABB(p).inflate(10));check.accept(shots.size()==1&&player.getMainHandItem().getCount()==1,"one alumentum use spawns one real projectile and consumes one item (shots="+shots.size()+", stack="+player.getMainHandItem().getCount()+", creative="+player.isCreative()+")");check.accept(Math.abs(shots.getFirst().getDeltaMovement().length()-.75)<.08&&shots.getFirst().getOwner()==player,"alumentum retains original launch speed and owner");shots.getFirst().discard();
  boolean grief=l.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);try{l.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false,l.getServer());var origin=p.offset(0,1,-3);for(int y=-2;y<=2;y++)l.setBlock(origin.offset(3,y,0),Blocks.GLASS.defaultBlockState(),3);var cow=new Cow(EntityType.COW,l);cow.setPos(origin.getX()+2,origin.getY()-1,origin.getZ()+1);l.addFreshEntity(cow);float health=cow.getHealth();var shot=new AlumentumProjectile(R3Entities.ALUMENTUM.get(),l);shot.setPos(origin.getX()+.5,origin.getY()+.5,origin.getZ()+.5);shot.shoot(1,0,0,.75f,0);l.addFreshEntity(shot);for(int t=0;t<12&&!shot.isRemoved();t++)shot.tick();check.accept(shot.isRemoved()&&cow.getHealth()<health,"alumentum collides, explodes and damages a nearby real entity");check.accept(l.getBlockState(origin.offset(3,0,0)).is(Blocks.GLASS),"mobGriefing false preserves blocks during original alumentum explosion");cow.discard();}finally{l.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(grief,l.getServer());}

  var cp=p.offset(5,0,0);l.setBlockAndUpdate(cp,R3Content.CRUCIBLE.get().defaultBlockState());var crucible=(CrucibleBlockEntity)l.getBlockEntity(cp);var contact=new Cow(EntityType.COW,l);contact.setPos(cp.getX()+.5,cp.getY()+.3,cp.getZ()+.5);l.addFreshEntity(contact);float startHealth=contact.getHealth();crucible.fillWater(1000);crucible.heat=150;for(int i=0;i<10;i++)l.getBlockState(cp).entityInside(l,cp,contact);check.accept(contact.getHealth()==startHealth,"cold filled crucible does not burn living contacts at heat 150");crucible.empty();crucible.heat=200;for(int i=0;i<10;i++)l.getBlockState(cp).entityInside(l,cp,contact);check.accept(contact.getHealth()==startHealth,"hot empty crucible does not burn living contacts");crucible.fillWater(1000);for(int i=0;i<10;i++)l.getBlockState(cp).entityInside(l,cp,contact);check.accept(contact.getHealth()==startHealth-1,"hot filled crucible deals original one fire damage after ten contacts");contact.discard();
  check.accept(R3Content.ALUMENTUM.get().getBurnTime(new ItemStack(R3Content.ALUMENTUM.get()),net.minecraft.world.item.crafting.RecipeType.SMELTING)==6400,"throwable alumentum retains original fuel duration");
  VisualSceneVerification.run(l,check);
 }
}
