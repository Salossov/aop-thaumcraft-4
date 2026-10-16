package thaumcraft.client;

import java.util.*;
import net.minecraft.core.Direction;
import thaumcraft.production.InfusionMatrixBlockEntity;

/** Keeps each source alive independently, as in TC4's SourceFX map. */
final class InfusionEffects {
    private static final class Source {net.minecraft.core.BlockPos pos;int color,kind,ticks;net.minecraft.world.item.ItemStack item;}
    private static final class State {long tick=-1;int serial;final Map<net.minecraft.core.BlockPos,Source> sources=new HashMap<>();}
    private static final WeakHashMap<InfusionMatrixBlockEntity,State> states=new WeakHashMap<>();
    static void tick(InfusionMatrixBlockEntity tile){
        var state=states.computeIfAbsent(tile,k->new State());var level=tile.getLevel();long tick=level.getGameTime();if(state.tick==tick)return;state.tick=tick;
        if(tile.effectSerial()!=state.serial){state.serial=tile.effectSerial();if(tile.effectSource!=null){var source=new Source();source.pos=tile.effectSource;source.color=tile.effectColor;source.kind=tile.effectKind;source.item=tile.effectItem.copy();source.ticks=source.kind==1?60:thaumcraft.effects.EssentiaFlow.SOURCE_TICKS;state.sources.put(source.pos,source);}}
        if(tile.crafting){var r=level.random;var centre=tile.getBlockPos().below(2).getCenter();var face=Direction.Plane.HORIZONTAL.getRandomDirection(r);var at=centre.add(face.getStepX()*.51,r.nextDouble()*.6-.3,face.getStepZ()*.51);LegacyParticles.rune(at,face,0xa020dc,75,-.03f);
            if(tile.instability>0&&r.nextInt(200)<=tile.instability){var origin=tile.getBlockPos().getCenter();InfusionLightning.bolt(origin,origin.add((r.nextFloat()-r.nextFloat())*2,(r.nextFloat()-r.nextFloat())*2,(r.nextFloat()-r.nextFloat())*2));}}
        var target=tile.getBlockPos().getCenter().add(0,-1,0);for(var source:state.sources.values()){
            if(source.ticks--<=0)continue;var from=source.pos.getCenter();if(source.kind==0)for(int n=0;n<4;n++)LegacyParticles.essentia(from,target,(float)tick+n*.25f,source.color);
            else if(source.kind==1&&!source.item.isEmpty()){var r=level.random;if(r.nextInt(3)==0)LegacyParticles.infusionSparkle(from.add(r.nextDouble()-.5,.5+r.nextDouble(),r.nextDouble()-.5),target);else for(int n=0;n<2;n++)LegacyParticles.ingredient(from.add((r.nextDouble()-.5)*.2,.73+r.nextDouble()*.2,(r.nextDouble()-.5)*.2),target,source.item);}
        }state.sources.values().removeIf(s->s.ticks<=0);
    }
    private InfusionEffects(){}
}
