package thaumcraft.verification;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import thaumcraft.world.dim.*;

/** Independent navigation of saved connectivity followed by actual native collision queries. */
final class NativeMazeRoute {
    static List<LabyrinthData.Position> cells(LabyrinthData maze,LabyrinthData.Position start,java.util.function.Predicate<MazeCell> goal,boolean bossAllowed){
        var previous=new HashMap<LabyrinthData.Position,LabyrinthData.Position>();var queue=new ArrayDeque<LabyrinthData.Position>();queue.add(start);previous.put(start,start);
        while(!queue.isEmpty()){
            var pos=queue.remove();var cell=maze.cell(pos.x(),pos.z());if(cell==null)continue;
            if(goal.test(cell)){var result=new ArrayList<LabyrinthData.Position>();for(var at=pos;;at=previous.get(at)){result.add(at);if(at.equals(start))break;}Collections.reverse(result);return result;}
            for(int d:new int[]{1,2,4,8}){
                if((cell.pack()&d)==0)continue;
                var next=new LabyrinthData.Position(pos.x()+LegacyMazeGenerator.getDX(d),pos.z()+LegacyMazeGenerator.getDY(d));var neighbor=maze.cell(next.x(),next.z());
                if(neighbor==null||(!bossAllowed&&neighbor.feature>=2&&neighbor.feature<=5)||(neighbor.pack()&LegacyMazeGenerator.getOPP(d))==0||previous.containsKey(next))continue;
                previous.put(next,pos);queue.add(next);
            }
        }
        throw new AssertionError("No saved maze route from "+start+" to target");
    }
    private record Step(BlockPos pos,double cost,double score){}
    static List<BlockPos> walk(ServerLevel level,BlockPos start,BlockPos target,Set<LabyrinthData.Position> allowed){
        return search(level,start,target,allowed,p->p.equals(target));
    }
    static List<BlockPos> interact(ServerLevel level,BlockPos start,BlockPos target,Set<LabyrinthData.Position> allowed,net.minecraft.server.level.ServerPlayer player){
        double reach=player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE);
        return search(level,start,target,allowed,p->{
            var eye=new net.minecraft.world.phys.Vec3(p.getX()+.5,p.getY()+player.getEyeHeight(),p.getZ()+.5);
            return eye.distanceToSqr(target.getCenter())<=reach*reach&&level.clip(new net.minecraft.world.level.ClipContext(eye,target.getCenter(),net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,player)).getBlockPos().equals(target);
        });
    }
    private static List<BlockPos> search(ServerLevel level,BlockPos start,BlockPos target,Set<LabyrinthData.Position> allowed,java.util.function.Predicate<BlockPos> goal){
        return search(level,start,target,allowed,goal,false);
    }
    private static List<BlockPos> search(ServerLevel level,BlockPos start,BlockPos target,Set<LabyrinthData.Position> allowed,java.util.function.Predicate<BlockPos> goal,boolean mineCrust){
        var open=new PriorityQueue<Step>(Comparator.comparingDouble(Step::score));var cost=new HashMap<BlockPos,Double>();var previous=new HashMap<BlockPos,BlockPos>();
        cost.put(start,0d);open.add(new Step(start,0,estimate(start,target)));int visits=0;
        while(!open.isEmpty()){
            var entry=open.remove();var pos=entry.pos;if(entry.cost>cost.get(pos))continue;
            if(++visits>250000)throw new AssertionError("Native collision route exceeded search budget: "+start+" -> "+target);
            if(goal.test(pos)){var result=new ArrayList<BlockPos>();for(var at=pos;!at.equals(start);at=previous.get(at))result.add(at);Collections.reverse(result);return result;}
            for(var direction:net.minecraft.core.Direction.Plane.HORIZONTAL){
                for(int dy=1;dy>=-3;dy--){
                    var next=pos.relative(direction).offset(0,dy,0);
                    if(next.getY()<49||next.getY()>65||!allowed.contains(new LabyrinthData.Position(next.getX()>>4,next.getZ()>>4))||!standable(level,next,mineCrust))continue;
                    // A one-block ascent represents a jump. Its elevated horizontal sweep must fit.
                    if(dy>0&&!bodyClear(level,pos.above()))continue;
                    double c=entry.cost+1+Math.abs(dy)*.2+(bodyClear(level,next)?0:20);if(c>=cost.getOrDefault(next,Double.POSITIVE_INFINITY))continue;
                    cost.put(next,c);previous.put(next,pos);open.add(new Step(next,c,c+estimate(next,target)));
                }
                // TC4 passages include broken floors. Vanilla sprint jumps can cross these
                // gaps; a walking-only search would report a valid original room as blocked.
                for(int span=2;span<=4;span++)for(int dy=1;dy>=-2;dy--){
                    var next=pos.relative(direction,span).offset(0,dy,0);
                    if(!allowed.contains(new LabyrinthData.Position(next.getX()>>4,next.getZ()>>4))||!standable(level,next)||!jumpClear(level,pos,next,allowed))continue;
                    double c=entry.cost+span+.4+Math.abs(dy)*.2;if(c>=cost.getOrDefault(next,Double.POSITIVE_INFINITY))continue;
                    cost.put(next,c);previous.put(next,pos);open.add(new Step(next,c,c+estimate(next,target)));
                }
            }
        }
        if(!mineCrust)return search(level,start,target,allowed,goal,true);
        throw new AssertionError("Saved maze route has no traversable native corridor, including original mineable crust: "+start+" -> "+target+" visited="+visits);
    }
    private static boolean jumpClear(ServerLevel level,BlockPos from,BlockPos to,Set<LabyrinthData.Position> allowed){
        for(int step=1;step<24;step++){
            double t=step/24d,x=from.getX()+.5+(to.getX()-from.getX())*t,z=from.getZ()+.5+(to.getZ()-from.getZ())*t;
            double y=from.getY()+(to.getY()-from.getY())*t+4*1.25*t*(1-t);
            if(!allowed.contains(new LabyrinthData.Position(BlockPos.containing(x,y,z).getX()>>4,BlockPos.containing(x,y,z).getZ()>>4))||!level.noCollision(new AABB(x-.3,y+.01,z-.3,x+.3,y+1.79,z+.3)))return false;
        }return true;
    }
    static boolean bodyClear(ServerLevel level,BlockPos pos){return level.noCollision(new AABB(pos.getX()+.2,pos.getY()+.01,pos.getZ()+.2,pos.getX()+.8,pos.getY()+1.79,pos.getZ()+.8));}
    private static boolean standable(ServerLevel level,BlockPos pos,boolean mineCrust){
        if(standable(level,pos))return true;
        if(!mineCrust||level.getBlockState(pos.below()).getCollisionShape(level,pos.below()).isEmpty())return false;
        boolean crust=false;
        for(var p:List.of(pos,pos.above())){
            var state=level.getBlockState(p);
            if(state.is(thaumcraft.registry.R5Content.ANCIENT_STONE.get()))crust=true;
            else if(!state.getCollisionShape(level,p).isEmpty())return false;
        }
        return crust;
    }
    static boolean standable(ServerLevel level,BlockPos pos){return bodyClear(level,pos)&&!level.getBlockState(pos.below()).getCollisionShape(level,pos.below()).isEmpty();}
    static BlockPos nearest(ServerLevel level,BlockPos preferred){
        for(int radius=0;radius<=3;radius++)for(int dy=0;dy<=3;dy++)for(int y:new int[]{preferred.getY()+dy,preferred.getY()-dy})for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){
            var p=new BlockPos(preferred.getX()+x,y,preferred.getZ()+z);if(standable(level,p))return p;
        }throw new AssertionError("No walkable feet near "+preferred);
    }
    private static double estimate(BlockPos a,BlockPos b){return Math.abs(a.getX()-b.getX())+Math.abs(a.getZ()-b.getZ())+Math.abs(a.getY()-b.getY())*.2;}
    private NativeMazeRoute(){}
}
