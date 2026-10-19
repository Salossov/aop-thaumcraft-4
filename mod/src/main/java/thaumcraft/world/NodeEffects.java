package thaumcraft.world;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import thaumcraft.api.aspects.*;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.blocks.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.entities.*;

/** TC4 natural node behaviours, including the two stabilizer lock modes. */
public final class NodeEffects {
    public static boolean tick(AuraNodeBlockEntity node,ServerLevel level,Random random) {
        var state=node.state;var pos=node.getBlockPos();boolean changed=false;
        if(state.type==NodeState.Type.HUNGRY&&ThaumcraftConfig.HARD_MODE_NODES.get()) changed|=hungryEntities(node,level,random);
        changed|=exchange(node,level,random);
        if(state.ticks%100==0){
            int lock=node.stabilizationLock();
            if(state.type==NodeState.Type.UNSTABLE&&random.nextBoolean()){
                if(lock==0){var primals=state.primalsWithVis();if(!primals.isEmpty()){var aspect=primals.get(random.nextInt(primals.size()));state.take(aspect,1);level.addFreshEntity(new AspectOrb(level,pos.getCenter(),aspect,1));changed=true;}}
                else if(random.nextInt(10000/lock)==42){state.type=NodeState.Type.NORMAL;changed=true;}
            }
            if(state.modifier==NodeState.Modifier.FADING&&lock>0&&random.nextInt(12500/lock)==69){state.modifier=NodeState.Modifier.PALE;changed=true;}
        }
        int dimensionBlacklist=PopulationPolicy.blacklist(ThaumcraftConfig.DIMENSION_BLACKLIST.get(),level.dimension().location());
        boolean overworld=level.dimension()!=Level.NETHER&&level.dimension()!=Level.END&&dimensionBlacklist!=0&&dimensionBlacklist!=2;
        if(state.ticks%50==0) {
            if(state.type==NodeState.Type.DARK&&overworld&&biomeAllowed(level,pos)) {
                WorldBiomes.change(level,pos.getX()+random.nextInt(12)-random.nextInt(12),pos.getZ()+random.nextInt(12)-random.nextInt(12),WorldBiomes.EERIE);
                if(ThaumcraftConfig.HARD_MODE_NODES.get()&&random.nextBoolean()&&level.getNearestPlayer(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,24,false)!=null
                    &&level.getEntitiesOfClass(BrainyZombie.class,new AABB(pos).inflate(10,6,10),e->e.getType()==R2Entities.GIANT_BRAINY.get()).size()<=3) {
                    var entity=R2Entities.GIANT_BRAINY.get().create(level);
                    entity.moveTo(pos.getX()+(random.nextDouble()-random.nextDouble())*5,pos.getY()+random.nextInt(3)-1,pos.getZ()+(random.nextDouble()-random.nextDouble())*5,random.nextFloat()*360,0);
                    if(entity.checkSpawnRules(level,MobSpawnType.SPAWNER)&&entity.checkSpawnObstruction(level)) { level.addFreshEntity(entity);level.levelEvent(2004,pos,0); }
                }
            }
            if(state.type==NodeState.Type.PURE&&overworld) {
                int x=pos.getX()+random.nextInt(8)-random.nextInt(8),z=pos.getZ()+random.nextInt(8)-random.nextInt(8);
                if(biomeAllowed(level,new BlockPos(x,pos.getY(),z))&&(WorldBiomes.is(level,new BlockPos(x,pos.getY(),z),WorldBiomes.TAINT)||node.getBlockState().is(thaumcraft.registry.R3Content.SILVERWOOD_NODE_LOG.get()))) WorldBiomes.change(level,x,z,WorldBiomes.MAGICAL);
            }
            if(state.type==NodeState.Type.TAINTED) {
                WorldBiomes.change(level,pos.getX()+random.nextInt(8)-random.nextInt(8),pos.getZ()+random.nextInt(8)-random.nextInt(8),WorldBiomes.TAINT);
                if(ThaumcraftConfig.HARD_MODE_NODES.get()&&random.nextBoolean()) TaintFibresBlock.spread(level,pos.offset(random.nextInt(5)-random.nextInt(5),random.nextInt(5)-random.nextInt(5),random.nextInt(5)-random.nextInt(5)));
            }
            if(state.type==NodeState.Type.HUNGRY) hungryBlock(node,level,random);
        }
        if(state.type!=NodeState.Type.PURE&&state.type!=NodeState.Type.TAINTED&&state.ticks%100==0&&WorldBiomes.is(level,pos,WorldBiomes.TAINT)&&random.nextInt(500)==0) { state.type=NodeState.Type.TAINTED;changed=true; }
        return changed;
    }
    private static boolean hungryEntities(AuraNodeBlockEntity node,ServerLevel level,Random random) {
        var center=node.getBlockPos().getCenter();boolean changed=false;
        for(var entity:level.getEntities((Entity)null,new AABB(node.getBlockPos()).inflate(15))) {
            if(entity instanceof Player player&&player.getAbilities().invulnerable) continue;
            if(entity.isAlive()&&!entity.isInvulnerable()&&entity.position().distanceToSqr(center)<2) {
                entity.hurt(level.damageSources().fellOutOfWorld(),1);
                if(!entity.isAlive()) {
                    var values=entity instanceof ItemEntity dropped?ThaumcraftData.aspects(level.getServer(),dropped.getItem()):ThaumcraftData.aspects(level.getServer(),entity);
                    var primals=reduce(values);if(!primals.isEmpty()) {
                        var keys=new ArrayList<>(primals.keySet());var aspect=keys.get(random.nextInt(keys.size()));int base=node.state.base.getOrDefault(aspect.getTag(),0);
                        if(node.state.amount(aspect)<base) node.state.add(aspect,1);
                        else if(random.nextInt(1+base*2)<primals.get(aspect)) node.state.base.merge(aspect.getTag(),1,Integer::sum);changed=true;
                    }
                }
            }
            var delta=center.subtract(entity.position()).scale(1.0/15);double distance=delta.length(),strength=1-distance;
            if(strength>0&&distance>1e-8) {strength*=strength;entity.setDeltaMovement(entity.getDeltaMovement().add(delta.multiply(0.15,0.25,0.15).scale(strength/distance)));entity.hurtMarked=true;}
        }return changed;
    }
    private static void hungryBlock(AuraNodeBlockEntity node,ServerLevel level,Random random) {
        var pos=node.getBlockPos();int x=pos.getX()+random.nextInt(16)-random.nextInt(16),z=pos.getZ()+random.nextInt(16)-random.nextInt(16);
        if(!level.hasChunkAt(new BlockPos(x,pos.getY(),z)))return;
        int y=Math.min(pos.getY()+random.nextInt(16)-random.nextInt(16),level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,x,z));
        var end=new Vec3(x+0.5,y+0.5,z+0.5);var delta=end.subtract(pos.getCenter());if(delta.lengthSqr()<1e-8) return;
        // Both ends can be loaded while a diagonal crosses an unloaded chunk.
        int steps=(int)Math.ceil(delta.length()*2);for(int i=1;i<steps;i++)if(!level.hasChunkAt(BlockPos.containing(pos.getCenter().add(delta.scale((double)i/steps)))))return;
        // Start outside the source node's own outline, matching rayTraceIgnoringSource.
        var hit=level.clip(new ClipContext(pos.getCenter().add(delta.normalize().scale(0.5)),end,ClipContext.Block.COLLIDER,ClipContext.Fluid.ANY,net.minecraft.world.phys.shapes.CollisionContext.empty()));
        if(hit.getType()!=HitResult.Type.BLOCK||hit.getBlockPos().distSqr(pos)>=256||hit.getBlockPos().equals(pos)) return;
        var state=level.getBlockState(hit.getBlockPos());float hardness=state.getDestroySpeed(level,hit.getBlockPos());
        if(!state.isAir()&&hardness>=0&&hardness<5) level.destroyBlock(hit.getBlockPos(),true);
    }
    private static boolean exchange(AuraNodeBlockEntity node,ServerLevel level,Random random) {
        var state=node.state;if(state.modifier==NodeState.Modifier.FADING||node.stabilizationLock()==1||!node.getBlockState().is(thaumcraft.registry.R2Content.AURA_NODE.get())) return false;
        boolean shiny=state.type==NodeState.Type.HUNGRY||state.modifier==NodeState.Modifier.BRIGHT;
        int interval=state.modifier==NodeState.Modifier.NONE?2:shiny?1:state.modifier==NodeState.Modifier.PALE?3:2;
        if(state.ticks%interval!=0) return false;var target=node.getBlockPos().offset(random.nextInt(5)-random.nextInt(5),random.nextInt(5)-random.nextInt(5),random.nextInt(5)-random.nextInt(5));
        if(state.modifier==NodeState.Modifier.PALE&&random.nextBoolean()) return false;
        if(target.equals(node.getBlockPos())||!level.hasChunkAt(target)||!(level.getBlockEntity(target) instanceof AuraNodeBlockEntity other)) return false;
        if(other.stabilizationLock()>0||!other.getBlockState().is(thaumcraft.registry.R2Content.AURA_NODE.get()))return false;
        int ours=total(state),theirs=total(other.state);if(theirs>=ours||other.state.current.isEmpty()) return false;
        var tags=new ArrayList<>(other.state.current.keySet());var aspect=Aspect.getAspect(tags.get(random.nextInt(tags.size())));if(aspect==null||!other.state.take(aspect,1)) return false;
        int base=state.base.getOrDefault(aspect.getTag(),0);
        if(state.amount(aspect)<base) state.add(aspect,1);
        else if(random.nextInt(1+(int)(base/(shiny?1.5:1)))==0) {
            state.base.merge(aspect.getTag(),1,Integer::sum);
            if(state.modifier==NodeState.Modifier.PALE&&random.nextInt(100)==0) state.modifier=NodeState.Modifier.NONE;
            if(random.nextInt(3)==0) other.state.base.merge(aspect.getTag(),-1,Integer::sum);
        }
        other.state.wait=other.state.rechargeInterval()/2;other.changedAndSync();var at=node.getBlockPos();net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(level,null,at.getX()+.5,at.getY()+.5,at.getZ()+.5,32,new thaumcraft.network.NodeZapPayload(other.getBlockPos().getCenter(),at.getCenter()));return true;
    }
    private static int total(NodeState state) { return (state.current.values().stream().mapToInt(Integer::intValue).sum()+state.base.values().stream().mapToInt(Integer::intValue).sum())/2; }
    private static boolean biomeAllowed(ServerLevel level,BlockPos pos){var key=WorldBiomes.effective(level,pos).unwrapKey();int value=key.map(k->PopulationPolicy.blacklist(ThaumcraftConfig.BIOME_BLACKLIST.get(),k.location())).orElse(-1);return value!=0&&value!=2;}
    public static Map<Aspect,Integer> reduce(Map<String,Integer> values) { var result=new LinkedHashMap<Aspect,Integer>();values.forEach((tag,amount)->reduce(Aspect.getAspect(tag),amount,result));return result; }
    private static void reduce(Aspect aspect,int amount,Map<Aspect,Integer> result) { if(aspect==null) return;if(aspect.isPrimal()) result.merge(aspect,amount,Integer::sum);else for(var child:aspect.getComponents()) reduce(child,amount,result); }
    private NodeEffects() {}
}
