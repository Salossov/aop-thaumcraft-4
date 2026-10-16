package thaumcraft.verification;
import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.blocks.*;
import thaumcraft.registry.*;

final class PavingVerification {
    private static final BlockPos POS=new BlockPos(1456,120,0);
    static void run(ServerLevel l,BiConsumer<Boolean,String> check)throws Exception{
        l.getChunkAt(POS);for(int y=-1;y<=4;y++)for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)l.setBlockAndUpdate(POS.offset(x,y,z),y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        var player=FakePlayerFactory.get(l,new GameProfile(UUID.fromString("01234567-1111-2222-3333-012345678904"),"PavingQA"));player.removeAllEffects();var travel=R4Content.PAVING_TRAVEL.get();l.setBlockAndUpdate(POS,travel.defaultBlockState());travel.stepOn(l,POS,l.getBlockState(POS),player);
        check.accept(player.hasEffect(MobEffects.MOVEMENT_SPEED)&&player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier()==1&&player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration()==40&&player.hasEffect(MobEffects.JUMP)&&player.getEffect(MobEffects.JUMP).getAmplifier()==0,"travel stone applies Speed II and Jump I for the original forty ticks");
        check.accept(l.getBlockState(POS).getLightEmission(l,POS)==9,"travel stone retains original light level nine");
        check.accept(!l.getBlockState(POS).hasBlockEntity()&&l.getBlockEntity(POS)==null,"travel stone has no tile or native EntityBlock flag, original metadata2");
        check.accept(travel.defaultBlockState().getDestroySpeed(l,POS)==2&&travel.getExplosionResistance()==6,"original hardness2 and explosion resistance6 after old setResistance(10) conversion");
        l.setBlockAndUpdate(POS,R4Content.PAVING_WARDING.get().defaultBlockState());var tile=(WardingStoneBlockEntity)l.getBlockEntity(POS);for(int i=0;i<201;i++)tile.tick();
        check.accept(l.getBlockState(POS.above()).is(R4Content.WARDING_BARRIER.get())&&l.getBlockState(POS.above(2)).is(R4Content.WARDING_BARRIER.get()),"warding stone builds both original invisible protection layers");
        var sheep=EntityType.SHEEP.create(l);sheep.moveTo(POS.getX()+.5,POS.getY()+1.2,.5,0,0);var item=new ItemEntity(l,POS.getX()+.5,POS.getY()+1.2,.5,new ItemStack(Items.COAL));var barrier=l.getBlockState(POS.above());
        check.accept(!barrier.getCollisionShape(l,POS.above(),CollisionContext.of(sheep)).isEmpty()&&barrier.getCollisionShape(l,POS.above(),CollisionContext.of(player)).isEmpty()&&barrier.getCollisionShape(l,POS.above(),CollisionContext.of(item)).isEmpty(),"unpowered protection blocks living creatures while passing players and loose items");
        check.accept(barrier.getBlock().getBlockPathType(barrier,l,POS.above(),sheep)==PathType.BLOCKED,"native creature pathfinding sees unpowered protection as blocked");
        l.addFreshEntity(sheep);sheep.setOnGround(false);sheep.setDeltaMovement(Vec3.ZERO);for(int i=0;i<5;i++)tile.tick();
        check.accept(Math.abs(sheep.getDeltaMovement().y+.1)<.00001&&Math.abs(sheep.getDeltaMovement().z+.2)<.00001,"unpowered ward repels airborne nonplayers opposite their yaw every five ticks");
        sheep.setOnGround(true);sheep.setDeltaMovement(Vec3.ZERO);for(int i=0;i<5;i++)tile.tick();check.accept(sheep.getDeltaMovement().equals(Vec3.ZERO),"grounded creatures receive no repulsion impulse");
        l.setBlockAndUpdate(POS.east(),Blocks.REDSTONE_BLOCK.defaultBlockState());
        check.accept(barrier.getCollisionShape(l,POS.above(),CollisionContext.of(sheep)).isEmpty()&&barrier.getBlock().getBlockPathType(barrier,l,POS.above(),sheep)==PathType.OPEN,"redstone disables ward collision and allows native paths");
        sheep.setOnGround(false);for(int i=0;i<5;i++)tile.tick();check.accept(sheep.getDeltaMovement().equals(Vec3.ZERO),"powered ward stops airborne repulsion");sheep.discard();
        for(var id:List.of("paving_stone_travel","paving_stone_warding")){var recipe=(thaumcraft.crafting.ArcaneRecipe)l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:"+id)).orElseThrow().value();var stacks=recipe.getIngredients().stream().map(g->g==Ingredient.EMPTY?ItemStack.EMPTY:g.getItems()[0].copy()).toList();var grid=CraftingInput.of(recipe.pattern().width(),recipe.pattern().height(),stacks);check.accept(recipe.matches(grid,l)&&recipe.assemble(grid,l.registryAccess()).getCount()==4&&recipe.vis().values().stream().mapToInt(Integer::intValue).sum()==20,"loaded original arcane pattern, four stone yield and 20 vis: "+id);}
        var orphan=POS.offset(0,0,4);l.getChunkAt(orphan);l.setBlockAndUpdate(orphan,R4Content.WARDING_BARRIER.get().defaultBlockState());l.getBlockState(orphan).tick(l,orphan,l.random);
        check.accept(l.getBlockState(orphan).isAir(),"orphaned protection self-removes on the hundred-tick maintenance check");
        originalEffects(l,check);
        originalSparkles(check);
        var beacon=POS.offset(8,1,0);l.getChunkAt(beacon);l.setBlockAndUpdate(beacon,Blocks.BEACON.defaultBlockState());
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)l.setBlockAndUpdate(beacon.offset(x,-1,z),((x+z)%2==0?R4Content.PAVING_TRAVEL:R4Content.PAVING_WARDING).get().defaultBlockState());
        var update=net.minecraft.world.level.block.entity.BeaconBlockEntity.class.getDeclaredMethod("updateBase",net.minecraft.world.level.Level.class,int.class,int.class,int.class);update.setAccessible(true);
        check.accept(l.getBlockEntity(beacon) instanceof net.minecraft.world.level.block.entity.BeaconBlockEntity&&(int)update.invoke(null,l,beacon.getX(),beacon.getY(),beacon.getZ())==1,"real vanilla beacon base scanner accepts a mixed nine-paving foundation");
        l.setBlockAndUpdate(beacon.offset(1,-1,1),Blocks.STONE.defaultBlockState());check.accept((int)update.invoke(null,l,beacon.getX(),beacon.getY(),beacon.getZ())==0,"real beacon scanner rejects a broken paving foundation");
    }
    private static void originalEffects(ServerLevel l,BiConsumer<Boolean,String> check)throws Exception{
        var root=java.nio.file.Path.of(System.getProperty("thaumcraft.r3.root"));
        var reference=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(root.resolve("analysis/full_port/original-paving-effects.json"))).getAsJsonObject();
        check.accept(!reference.get("travel_tile").getAsBoolean()&&reference.get("ward_tile").getAsBoolean()&&reference.get("beacon_base").getAsBoolean(),"owned unchanged original methods confirm travel/ward tile distinction and beacon base");
        var savedCount=PavingStoneBlock.particleCount;var savedRunes=PavingStoneBlock.runes;
        var effects=new ArrayList<PavingStoneBlock.Rune>();PavingStoneBlock.runes=effects::add;
        var p=new BlockPos(1456,120,8);l.getChunkAt(p);int rows=0;
        try{
            for(var value:reference.getAsJsonArray("ambient")){
                var row=value.getAsJsonObject();int scenario=row.get("scenario").getAsInt(),setting=row.get("setting").getAsInt();PavingStoneBlock.particleCount=n->setting==2?0:setting==1?n:n*2;
                var block=(scenario==6?R4Content.PAVING_TRAVEL:R4Content.PAVING_WARDING).get();l.setBlockAndUpdate(p,block.defaultBlockState());l.setBlockAndUpdate(p.east(),scenario==0?Blocks.REDSTONE_BLOCK.defaultBlockState():Blocks.AIR.defaultBlockState());
                l.setBlockAndUpdate(p.above(),scenario==1?Blocks.STONE.defaultBlockState():scenario==2?R4Content.WARDING_BARRIER.get().defaultBlockState():Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(p.above(2),scenario==3?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
                var mob=EntityType.SHEEP.create(l);mob.moveTo(p.getX()+.5,p.getY()+1.1,p.getZ()+.5,0,0);if(scenario==2||scenario==3||scenario==4)l.addFreshEntity(mob);
                effects.clear();l.random.setSeed(row.get("seed").getAsLong());block.animateTick(block.defaultBlockState(),l,p,l.random);
                long after=l.random.nextLong();var expected=row.getAsJsonArray("effects");boolean same=effects.size()==expected.size()&&after==row.get("rng_after").getAsLong();
                for(int i=0;i<effects.size()&&i<expected.size();i++){var e=effects.get(i);double[] actual={e.x(),e.y(),e.z()-8,e.red(),e.green(),e.blue(),e.duration(),e.gravity()};var original=expected.get(i).getAsJsonArray();for(int j=0;j<actual.length;j++)same&=Double.compare(actual[j],original.get(j).getAsDouble())==0;}
                check.accept(same,"native runes equal original branch/count/RGB/height/lifetime/gravity and RNG, seed"+row.get("seed")+" setting"+setting+" scenario"+scenario);mob.discard();rows++;
            }
            check.accept(rows==672,"all672 original ambient cases exercised, including clear lower/solid upper branch and minimal particles");
        }finally{PavingStoneBlock.particleCount=savedCount;PavingStoneBlock.runes=savedRunes;}
    }
    private static void originalSparkles(BiConsumer<Boolean,String> check)throws Exception{
        var root=java.nio.file.Path.of(System.getProperty("thaumcraft.r3.root"));var reference=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(root.resolve("analysis/full_port/original-paving-sparkles.json"))).getAsJsonArray();
        for(var value:reference){var row=value.getAsJsonObject();int setting=row.get("setting").getAsInt();var xyz=row.getAsJsonArray("pos");var pos=new BlockPos(xyz.get(0).getAsInt(),xyz.get(1).getAsInt(),xyz.get(2).getAsInt());var r=net.minecraft.util.RandomSource.create(row.get("seed").getAsLong());var effects=new ArrayList<PavingSparkles.Sparkle>();PavingSparkles.emit(pos,row.get("color").getAsInt(),setting==2?0:setting==1?5:10,r,effects::add);
            var expected=row.getAsJsonArray("effects");boolean same=effects.size()==expected.size()&&r.nextLong()==row.get("rng_after").getAsLong();for(int i=0;i<effects.size()&&i<expected.size();i++){var e=effects.get(i);double[] actual={e.position().x,e.position().y,e.position().z,e.upward(),e.red(),e.green(),e.blue(),e.lifetime(),e.delay(),e.scale()};var original=expected.get(i).getAsJsonArray();for(int j=0;j<actual.length;j++)same&=Double.compare(actual[j],original.get(j).getAsDouble())==0;}
            check.accept(same,"original travel sparkle parameters and RNG at "+pos+" seed"+row.get("seed")+" setting"+setting+" color"+row.get("color"));
        }check.accept(reference.size()==192,"all192 source sparkle cases including minimum particles and world-border float coordinates");
    }
    static void reload(ServerLevel l,BiConsumer<Boolean,String> check){l.getChunkAt(POS);check.accept(l.getBlockEntity(POS) instanceof WardingStoneBlockEntity&&l.getBlockState(POS.above(2)).is(R4Content.WARDING_BARRIER.get()),"warding stone and invisible protection persist after server restart");var mob=EntityType.SHEEP.create(l);var s=l.getBlockState(POS.above());check.accept(s.getCollisionShape(l,POS.above(),CollisionContext.of(mob)).isEmpty(),"saved adjacent redstone still disables ward collision after server restart");}
}
