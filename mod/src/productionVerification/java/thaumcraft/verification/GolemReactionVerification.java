package thaumcraft.verification;

import java.util.Map;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import thaumcraft.api.aspects.*;
import thaumcraft.entities.*;
import thaumcraft.registry.*;

/** Real server AI: mutate the world/configuration while a task is running. */
final class GolemReactionVerification {
    static final BlockPos O=GolemDebugLiveScene.O;
    static int changed,reacted;
    static BlockPos crop;
    static ChestBlockEntity chest(ServerPlayer p,BlockPos pos){p.serverLevel().setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState());return (ChestBlockEntity)p.serverLevel().getBlockEntity(pos);}
    static void mark(ServerPlayer p,ThaumicGolem g,BlockPos pos,boolean remove){g.mark(new GolemMarker(p.level().dimension(),pos,Direction.UP,-1),remove);}
    static ThaumicGolem prepare(ServerPlayer p,int stage){
        changed=reacted=-1;var l=p.serverLevel();chest(p,O);chest(p,O.east(6));chest(p,O.west(7));
        int core=stage==21||stage==29?6:stage==26?2:stage==27?3:stage==28?7:1;
        var home=stage==21?O.above(3).south():O.south();
        if(stage==21){l.setBlockAndUpdate(O,R3Content.ALCHEMY_FURNACE.get().defaultBlockState());for(int i=1;i<=3;i++)Round3Verification.transport(l,O.above(i),R3Content.ALEMBIC.get());Round3Verification.transport(l,O.east(6),R3Content.WARDED_JAR.get());}
        var body=new ItemStack(R3Equipment.GOLEM_BODIES.get(GolemMaterial.THAUMIUM).get());CustomData.update(DataComponents.CUSTOM_DATA,body,t->t.putInt("core",core));
        var g=R3Entities.GOLEM.get().create(l);g.initialize(GolemMaterial.THAUMIUM,p.getUUID(),home,Direction.SOUTH,body);g.moveTo(stage==22||stage==23?-6.5:.5,65,1.5);l.addFreshEntity(g);
        if(stage<=25)mark(p,g,O.east(6),false);
        if(stage==23)((ChestBlockEntity)l.getBlockEntity(O)).setItem(0,new ItemStack(Items.STONE,4));
        if(stage==24||stage==25){g.carried(new ItemStack(Items.STONE,4));if(stage==24)mark(p,g,O.west(7),false);}
        if(stage==26){g.filters.setItem(0,new ItemStack(Items.STONE));for(int side:new int[]{-1,1}){var item=new ItemEntity(l,side*6+.5,65,1.5,new ItemStack(side>0?Items.STONE:Items.DIAMOND));item.setNoPickUpDelay();l.addFreshEntity(item);}}
        if(stage==29){g.setNoAi(true);p.teleportTo(.5,67,4.5);p.setXRot(0);}
        return g;
    }
    static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);GolemDebugLiveScene.checks.add(text);}
    static void tick(ServerPlayer p,ThaumicGolem g,int stage,int age){
        if(stage<21||stage>29||stage==29)return;var l=p.serverLevel();int changeAt=stage==21?20:5;
        if(age==changeAt){changed=age;
            switch(stage){
                case 21->{var cube=(thaumcraft.production.TransportBlockEntity)l.getBlockEntity(O.above(3));cube.store.setAspects(new AspectList().add(Aspects.FIRE,6));cube.sync();}
                case 22->((ChestBlockEntity)l.getBlockEntity(O)).setItem(0,new ItemStack(Items.STONE,4));
                case 23->((ChestBlockEntity)l.getBlockEntity(O)).clearContent();
                case 24->{var c=(ChestBlockEntity)l.getBlockEntity(O.east(6));for(int i=0;i<c.getContainerSize();i++)c.setItem(i,new ItemStack(Items.STONE,64));}
                case 25->{mark(p,g,O.east(6),true);mark(p,g,O.west(7),false);}
                case 26->g.filters.setItem(0,new ItemStack(Items.DIAMOND));
                case 27->{crop=g.home().offset(4,0,4);l.setBlockAndUpdate(crop.below(),Blocks.FARMLAND.defaultBlockState());l.setBlockAndUpdate(crop,Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7));}
                case 28->{crop=g.home().offset(6,0,6);l.setBlockAndUpdate(crop,Blocks.OAK_LOG.defaultBlockState());}
            }
        }
        if(changed>=0&&age>changed&&reacted<0){var plan=g.debugPlan();boolean yes=switch(stage){
            case 21->plan.task().equals("take_essentia")&&plan.target()!=null||g.essentiaAmount>0;
            case 22->plan.task().equals("take_items")&&plan.target()!=null||!g.carried().isEmpty();
            case 23->!plan.task().equals("take_items")||plan.target()==null;
            case 24,25,26->plan.target()!=null&&plan.target().x<0;
            case 27,28->plan.task().equals(stage==27?"harvest":"chop")&&plan.target()!=null&&BlockPos.containing(plan.target()).equals(crop);
            default->false;};if(yes){reacted=age;GolemDebugLiveScene.checks.add(Map.of("stage",stage,"reactionTicks",age-changed,"task",plan.task()));}}
        int limit=stage==27?20:stage==28?30:6;
        if(age==changeAt+limit)check(reacted>=0&&reacted-changed<=limit,"world/config change handled in "+(reacted-changed)+" ticks, stage "+stage);
        if(stage==21&&age==85)check(((thaumcraft.production.TransportBlockEntity)l.getBlockEntity(O.east(6))).store.amount()==6,"new essentia delivered despite unreachable elevated home");
    }
    private GolemReactionVerification(){}
}
