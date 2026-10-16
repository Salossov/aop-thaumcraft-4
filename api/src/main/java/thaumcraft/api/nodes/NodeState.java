package thaumcraft.api.nodes;

import java.util.*;
import thaumcraft.api.aspects.Aspect;

/** TC4 base/current vis, recharge intervals and damage from completely exhausting an aspect. */
public final class NodeState {
    public enum Type { NORMAL,DARK,UNSTABLE,HUNGRY,PURE,TAINTED }
    public enum Modifier { NONE,BRIGHT,PALE,FADING }
    public Type type=Type.NORMAL;
    public Modifier modifier=Modifier.NONE;
    public final Map<String,Integer> current=new LinkedHashMap<>(),base=new LinkedHashMap<>();
    public int ticks,wait;
    public int stabilizationLock;
    public long lastActive=System.currentTimeMillis();
    public int rechargeInterval() { return (switch(modifier) { case NONE->600;case BRIGHT->400;case PALE->900;case FADING->0; })*(stabilizationLock==1?2:stabilizationLock==2?20:1); }
    public void initialize(Map<String,Integer> values) { current.clear();base.clear();current.putAll(values);base.putAll(values); }
    public int amount(Aspect aspect) { return current.getOrDefault(aspect.getTag(),0); }
    public boolean take(Aspect aspect,int amount) { if(amount<0||amount(aspect)<amount) return false;current.put(aspect.getTag(),amount(aspect)-amount);return true; }
    public void add(Aspect aspect,int amount) { current.put(aspect.getTag(),Math.min(base.getOrDefault(aspect.getTag(),0),amount(aspect)+amount)); }
    public List<Aspect> primalsWithVis() { return current.entrySet().stream().filter(x->x.getValue()>0).map(x->Aspect.getAspect(x.getKey())).filter(a->a!=null&&a.isPrimal()).toList(); }
    public boolean recharge(Random random,long now) {
        ticks++;if(wait>0) wait--;boolean changed=false;
        if(ticks%1200==0) for(var tag:List.copyOf(current.keySet())) {
            if(current.get(tag)>0) continue;int reduced=base.getOrDefault(tag,0)-1;base.put(tag,reduced);changed=true;
            if(random.nextInt(20)==0||reduced<=0) {
                current.remove(tag);
                if(random.nextInt(5)==0) {
                    if(modifier==Modifier.BRIGHT) modifier=Modifier.NONE;
                    else if(modifier==Modifier.NONE) modifier=Modifier.PALE;
                    if(modifier==Modifier.PALE&&random.nextInt(5)==0) modifier=Modifier.FADING;
                }break;
            }
        }
        int interval=rechargeInterval();
        if(interval>0&&wait==0&&ticks%interval==0) { lastActive=now;changed|=rechargeOne(random); }
        return changed;
    }
    public boolean catchUp(Random random,long now) {
        int interval=rechargeInterval()*75;if(interval==0) return false;
        long count=Math.max(0,(now-lastActive)/interval);int total=base.values().stream().mapToInt(Integer::intValue).sum();boolean changed=false;
        for(int i=0;i<Math.min(count,total);i++) changed|=rechargeOne(random);return changed;
    }
    private boolean rechargeOne(Random random) {
        var missing=current.keySet().stream().filter(tag->current.get(tag)<base.getOrDefault(tag,0)).toList();
        if(missing.isEmpty()) return false;String tag=missing.get(random.nextInt(missing.size()));current.merge(tag,1,Integer::sum);return true;
    }
}
