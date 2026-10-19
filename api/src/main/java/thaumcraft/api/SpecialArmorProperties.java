/**
 * Normalization adapted from Minecraft Forge 1.7.10 ISpecialArmor.ArmorProperties.
 * Original software: Minecraft Forge Public License v1.0.
 * https://github.com/MinecraftForge/MinecraftForge/blob/1.7.10/src/main/java/net/minecraftforge/common/ISpecialArmor.java
 */
package thaumcraft.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;

/** AbsorbMax retains Forge's scaled units: 25 per point of incoming damage. */
public final class SpecialArmorProperties implements Comparable<SpecialArmorProperties> {
    public int priority;
    public int absorbMax;
    public double absorbRatio;
    public int slot;
    private static final boolean DEBUG=false;
    public SpecialArmorProperties(int priority,double ratio,int maximum) {
        this.priority=priority;absorbRatio=ratio;absorbMax=maximum;
    }
    public record Absorbed(int slot,double amount,int durabilityDamage) {}
    public record Result(float remaining,List<Absorbed> absorbed,boolean recoveredFromLoop) {}
    private record Cursor(int index,int start,int pStart,int priority,boolean pChange,boolean pFinished,
                          double total,double damage,List<Double> ratios) {}

    private static SpecialArmorProperties[] copies(List<SpecialArmorProperties> properties) {
        var props=new SpecialArmorProperties[properties.size()];
        for(int i=0;i<props.length;i++) {
            var original=properties.get(i);
            props[i]=new SpecialArmorProperties(original.priority,original.absorbRatio,original.absorbMax);
            props[i].slot=original.slot;
        }
        return props;
    }

    /** Copies properties before normalization; callbacks follow the original sorted order. */
    public static Result apply(double damage,List<SpecialArmorProperties> properties) {
        damage*=25;
        var props=copies(properties);
        var absorbed=new ArrayList<Absorbed>();
        boolean recovered=false;
        if(props.length>0) {
            if(!standardize(props,damage)) {
                // Approved repair: the source can revisit an identical cursor forever.
                props=copies(properties);boundedDistribution(props,damage);recovered=true;
            }
            int level=props[0].priority;double ratio=0;
            for(var prop:props) {
                if(level!=prop.priority) {damage-=damage*ratio;ratio=0;level=prop.priority;}
                ratio+=prop.absorbRatio;
                double amount=damage*prop.absorbRatio;
                if(amount>0)absorbed.add(new Absorbed(prop.slot,amount/25D,(int)(amount/25D<1?1:amount/25D)));
            }
            damage-=damage*ratio;
        }
        return new Result((float)(damage/25.0F),List.copyOf(absorbed),recovered);
    }

    /** Weighted, capped distribution only for a source normalization that fails to progress. */
    private static void boundedDistribution(SpecialArmorProperties[] armor,double damage) {
        Arrays.sort(armor);
        for(int start=0;start<armor.length;) {
            int end=start+1;while(end<armor.length&&armor[end].priority==armor[start].priority)end++;
            double weight=0;for(int i=start;i<end;i++)weight+=Math.max(0,armor[i].absorbRatio);
            double target=damage*Math.min(1,weight),remaining=target;
            double[] allocations=new double[end-start];boolean[] fixed=new boolean[end-start];
            // Every pass fixes at least one capped piece, or allocates the entire remainder.
            for(int pass=0;pass<=end-start&&weight>0&&remaining>0;pass++) {
                boolean capped=false;
                for(int i=start;i<end;i++)if(!fixed[i-start]) {
                    var piece=armor[i];double share=remaining*Math.max(0,piece.absorbRatio)/weight;
                    if(share>Math.max(0,piece.absorbMax)) {
                        allocations[i-start]=Math.max(0,piece.absorbMax);fixed[i-start]=true;
                        remaining-=allocations[i-start];weight-=Math.max(0,piece.absorbRatio);capped=true;
                        // Recompute shares after removing this weight and capacity.
                        break;
                    }
                }
                if(!capped) {
                    for(int i=start;i<end;i++)if(!fixed[i-start])allocations[i-start]=remaining*Math.max(0,armor[i].absorbRatio)/weight;
                    break;
                }
            }
            double used=0;
            for(int i=start;i<end;i++) {used+=allocations[i-start];armor[i].absorbRatio=damage>0?allocations[i-start]/damage:0;}
            damage=Math.max(0,damage-used);start=end;
        }
    }

    private static boolean standardize(SpecialArmorProperties[] armor, double damage)
        {
            Arrays.sort(armor);
            int     start     = 0;
            double  total     = 0;
            int     priority  = armor[0].priority;
            int     pStart    = 0;
            boolean pChange   = false;
            boolean pFinished = false;

            if (DEBUG)
            {
                for (SpecialArmorProperties prop : armor)
                {
                    System.out.println(prop);
                }
                System.out.println("========================");
            }
            int iterations=0;
            HashSet<Cursor> visited=null;
            for (int x = 0; x < armor.length; x++)
            {
                if(++iterations>32) {
                    if(visited==null)visited=new HashSet<>();
                    var state=new Cursor(x,start,pStart,priority,pChange,pFinished,total,damage,
                            Arrays.stream(armor).map(prop->prop.absorbRatio).toList());
                    if(!visited.add(state)||iterations>Math.max(256,armor.length*64))return false;
                }
                total += armor[x].absorbRatio;
                if (x == armor.length - 1 || armor[x].priority != priority)
                {
                    if (armor[x].priority != priority)
                    {
                        total -= armor[x].absorbRatio;
                        x--;
                        pChange = true;
                    }
                    if (total > 1)
                    {
                        for (int y = start; y <= x; y++)
                        {
                            double newRatio = armor[y].absorbRatio / total;
                            if (newRatio * damage > armor[y].absorbMax)
                            {
                                armor[y].absorbRatio = (double)armor[y].absorbMax / damage;
                                total = 0;
                                for (int z = pStart; z <= y; z++)
                                {
                                    total += armor[z].absorbRatio;
                                }
                                start = y + 1;
                                x = y;
                                break;
                            }
                            else
                            {
                                armor[y].absorbRatio = newRatio;
                                pFinished = true;
                            }
                        }
                        if (pChange && pFinished)
                        {
                            damage -= (damage * total);
                            total = 0;
                            start = x + 1;
                            priority = armor[start].priority;
                            pStart = start;
                            pChange = false;
                            pFinished = false;
                            if (damage <= 0)
                            {
                                for (int y = x + 1; y < armor.length; y++)
                                {
                                    armor[y].absorbRatio = 0;
                                }
                                break;
                            }
                        }
                    }
                    else
                    {
                        for (int y = start; y <= x; y++)
                        {
                            total -= armor[y].absorbRatio;
                            if (damage * armor[y].absorbRatio > armor[y].absorbMax)
                            {
                                armor[y].absorbRatio = (double)armor[y].absorbMax / damage;
                            }
                            total += armor[y].absorbRatio;
                        }
                        damage -= (damage * total);
                        total = 0;
                        if (x != armor.length - 1)
                        {
                            start = x + 1;
                            priority = armor[start].priority;
                            pStart = start;
                            pChange = false;
                            if (damage <= 0)
                            {
                                for (int y = x + 1; y < armor.length; y++)
                                {
                                    armor[y].absorbRatio = 0;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            if (DEBUG)
            {
                for (SpecialArmorProperties prop : armor)
                {
                    System.out.println(prop);
                }
            }
            return true;
        }

    @Override
    public int compareTo(SpecialArmorProperties o)
        {
            if (o.priority != priority)
            {
                return o.priority - priority;
            }
            double left =  (  absorbRatio == 0 ? 0 :   absorbMax * 100.0D /   absorbRatio);
            double right = (o.absorbRatio == 0 ? 0 : o.absorbMax * 100.0D / o.absorbRatio);
            return (int)(left - right);
        }
}
