package thaumcraft.verification;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.api.aspects.*;
import thaumcraft.entities.*;
import thaumcraft.production.TransportBlockEntity;
import thaumcraft.registry.*;

/** Regression scenes for the user's marked alembics and slow water discovery. */
final class GolemAlchemyFishingVerification {
    static BlockPos blocked;
    static int firstCast;
    static final BlockPos O=GolemDebugLiveScene.O;
    static ThaumicGolem prepare(ServerPlayer player,int stage){
        var l=player.serverLevel();firstCast=-1;blocked=null;
        boolean alchemy=stage==17;var material=alchemy?GolemMaterial.THAUMIUM:GolemMaterial.CLAY;
        var home=alchemy?O.south():stage==18?O.above(2):stage==19?O.west(5).north(3):O.above();
        var source=alchemy?O:home.below();
        l.setBlockAndUpdate(source,alchemy?R3Content.ALCHEMY_FURNACE.get().defaultBlockState():Blocks.CHEST.defaultBlockState());
        var body=new ItemStack(R3Equipment.GOLEM_BODIES.get(material).get());CustomData.update(DataComponents.CUSTOM_DATA,body,t->t.putInt("core",alchemy?6:11));
        var golem=R3Entities.GOLEM.get().create(l);golem.initialize(material,player.getUUID(),home,alchemy?Direction.SOUTH:Direction.UP,body);golem.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5);golem.getRandom().setSeed(100+stage);l.addFreshEntity(golem);
        if(alchemy){
            for(int i=1;i<=3;i++){var pos=O.above(i);var cube=Round3Verification.transport(l,pos,R3Content.ALEMBIC.get());if(i<3)cube.store.setAspects(new AspectList().add(i==1?Aspects.FIRE:Aspects.WATER,16));cube.sync();golem.mark(new GolemMarker(l.dimension(),pos,Direction.UP,-1),false);}
            var glass=Aspect.getAspect("vitreus");golem.essentia=glass;golem.essentiaAmount=12;
            for(int x=5;x<=6;x++)for(int z=-1;z<=1;z++){var pos=O.offset(x,0,z);var jar=Round3Verification.transport(l,pos,R3Content.WARDED_JAR.get());jar.store.filter(glass);golem.mark(new GolemMarker(l.dimension(),pos,Direction.UP,-1),false);}
        }else if(stage==18)pool(player);
        else if(stage==19){var water=home.offset(-2,-1,-2);l.setBlockAndUpdate(water.below(),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(water,Blocks.WATER.defaultBlockState());}
        player.teleportTo(home.getX()-3.5,home.getY()+1,home.getZ()+8.5);player.setYRot(180);player.setXRot(16);
        return golem;
    }
    private static void pool(ServerPlayer player){for(int x=-3;x<=2;x++)for(int z=2;z<=7;z++){var pos=O.offset(x,-1,z);player.serverLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());player.serverLevel().setBlockAndUpdate(pos,Blocks.WATER.defaultBlockState());}}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);GolemDebugLiveScene.checks.add(message);}
    static void tick(ServerPlayer player,ThaumicGolem golem,int stage,int age){
        if(stage<17||stage>20)return;
        if(stage==17){
            var plan=golem.debugPlan();if(plan.target()!=null&&plan.task().equals("deliver_essentia"))check(plan.target().x>4,"alchemy delivery chooses a receiving jar, never a marked alembic");
            if(age==100){int total=0;for(int x=5;x<=6;x++)for(int z=-1;z<=1;z++)total+=((TransportBlockEntity)player.serverLevel().getBlockEntity(O.offset(x,0,z))).store.amount();check(total==12,"nine marks including three alembics: all 12 vitreus delivered to jars");check(((TransportBlockEntity)player.serverLevel().getBlockEntity(O.above(3))).store.amount()==0,"marked upper alembic received no returned essentia");}
            return;
        }
        if(stage==20&&age==25)pool(player);
        var spot=golem.fishingTarget();
        if(spot!=null&&firstCast<0){firstCast=age;check(age<=(stage==20?45:5),"fishing starts at tick "+age+" in scenario "+stage);GolemDebugLiveScene.checks.add(Map.of("stage",stage,"firstCastTick",age,"water",spot.toShortString()));}
        if(stage==20&&age==20)check(firstCast<0,"golem does not cast into dry ground");
        if(stage==18&&age==35){check(spot!=null,"pond fishing is active before cache invalidation");blocked=spot;player.serverLevel().setBlockAndUpdate(spot.above(),Blocks.STONE.defaultBlockState());}
        if(stage==18&&age==45)check(spot!=null&&!spot.equals(blocked),"covered cached water is replaced within ten ticks");
        if(age==60)check(firstCast>=0,"water discovery completed scenario "+stage);
    }
    private GolemAlchemyFishingVerification(){}
}
