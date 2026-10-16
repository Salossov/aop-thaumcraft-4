package thaumcraft.client;

import java.util.*;
import net.minecraft.world.phys.Vec3;

/** TC4's seven fractal subdivisions and branch links, with source float vector arithmetic. */
final class LegacyLightningGraph {
    List<Segment> segments=new ArrayList<>();
    final float length;
    final int maximumAge,increment;
    int age,mainSegments=1,splits;
    private final Random random;
    private final Map<Integer,Integer> parents=new HashMap<>();
    LegacyLightningGraph(Vec3 from,Vec3 to,long seed,int duration,float multiplier,int speed){
        random=new Random(seed);var start=new V(from);var end=new V(to);length=end.copy().sub(start).length();
        random.nextInt(3); // The delegating base constructor draws its default duration first.
        maximumAge=duration+random.nextInt(duration)-duration/2;increment=speed;age=-(int)(length*3);
        segments.add(new Segment(new Point(start,new V(0,0,0)),new Point(end,new V(0,0,0)),1,0,0));
        float[] divisors={8,12,17,23,30,34,40},chances={.7f,.5f,.5f,.5f,0,0,0},angles={45,50,55,60,0,0,0};
        for(int i=0;i<7;i++)fractal(length*multiplier/divisors[i],chances[i],i<4?.1f:0,angles[i]);
        finish();
    }
    void tick(){age=Math.min(maximumAge,age+increment);}
    private void fractal(float amount,float chance,float branchLength,float angle){
        var previousSegments=segments;segments=new ArrayList<>();
        for(var segment:previousSegments){
            Segment previous=segment.previous;var half=segment.diff.copy().scale(.5f);
            var offset=V.perpendicular(segment.diff).rotate(random.nextFloat()*360,segment.diff).scale((random.nextFloat()-.5f)*amount);
            Point[] points={segment.start,new Point(segment.start.point.copy().add(half),offset),segment.end};
            for(int i=0;i<2;i++){
                var next=new Segment(points[i],points[i+1],segment.light,segment.number*2+i,segment.split);next.previous=previous;if(previous!=null)previous.next=next;
                if(i!=0&&random.nextFloat()<chance){
                    var rotation=V.xCross(next.diff).rotate(random.nextFloat()*360,next.diff);
                    var diff=next.diff.copy().rotate((random.nextFloat()*.66f+.33f)*angle,rotation).scale(branchLength);
                    parents.put(++splits,next.split);var branch=new Segment(points[i],new Point(points[i+1].base,points[i+1].offset.copy().add(diff)),segment.light/2,next.number,splits);
                    branch.previous=previous;segments.add(branch);
                }
                previous=next;segments.add(next);
            }
            if(segment.next!=null)segment.next.previous=previous;
        }
        mainSegments*=2;
    }
    private void finish(){
        segments.sort(Comparator.comparingInt((Segment s)->s.split).thenComparingInt(s->s.number));
        Map<Integer,Integer> active=new HashMap<>();int lastSplit=0,lastNumber=0;
        for(var s:segments){if(s.split>lastSplit){active.put(lastSplit,lastNumber);lastSplit=s.split;lastNumber=active.get(parents.get(s.split));}lastNumber=s.number;}
        active.put(lastSplit,lastNumber);lastSplit=0;lastNumber=active.get(0);
        var iterator=segments.iterator();while(iterator.hasNext()){
            var s=iterator.next();if(lastSplit!=s.split){lastSplit=s.split;lastNumber=active.get(s.split);}if(s.number>lastNumber)iterator.remove();s.ends();
        }
        segments.sort((a,b)->Float.compare(b.light,a.light));
    }
    static final class Segment {
        final Point start,end;final V diff;final float light;final int number,split;
        Segment previous,next;V previousDiff,nextDiff;float previousSine,nextSine;
        Segment(Point start,Point end,float light,int number,int split){this.start=start;this.end=end;this.light=light;this.number=number;this.split=split;diff=end.point.copy().sub(start.point);}
        void ends(){
            if(previous==null){previousDiff=diff.copy().normalize();previousSine=1;}
            else{var prior=previous.diff.copy().normalize();var current=diff.copy().normalize();previousDiff=current.add(prior).normalize();previousSine=(float)Math.sin(V.angle(current,prior.scale(-1))/2);}
            if(next==null){nextDiff=diff.copy().normalize();nextSine=1;}
            else{var following=next.diff.copy().normalize();var current=diff.copy().normalize();nextDiff=current.add(following).normalize();nextSine=(float)Math.sin(V.angle(current,following.scale(-1))/2);}
        }
    }
    static final class Point {final V point,base,offset;Point(V base,V offset){this.base=base;this.offset=offset;point=base.copy().add(offset);}}
    static final class V {
        float x,y,z;
        V(double x,double y,double z){this.x=(float)x;this.y=(float)y;this.z=(float)z;}
        V(Vec3 p){this(p.x,p.y,p.z);}
        V copy(){return new V(x,y,z);}
        V add(V v){x+=v.x;y+=v.y;z+=v.z;return this;}
        V sub(V v){x-=v.x;y-=v.y;z-=v.z;return this;}
        V scale(float f){x*=f;y*=f;z*=f;return this;}
        float length(){return (float)Math.sqrt(x*x+y*y+z*z);}
        V normalize(){float f=length();if(f!=0){x/=f;y/=f;z/=f;}return this;}
        static V cross(V a,V b){return new V(a.y*b.z-a.z*b.y,a.z*b.x-a.x*b.z,a.x*b.y-a.y*b.x);}
        static V xCross(V v){return new V(0,v.z,-v.y);}
        static V perpendicular(V v){return v.z==0?new V(-v.y,v.x,0):xCross(v);}
        static float angle(V a,V b){return (float)Math.acos(net.minecraft.util.Mth.clamp(a.x*b.x+a.y*b.y+a.z*b.z,-1,1));}
        V rotate(float degrees,V axis){
            var n=axis.copy().normalize();float cos=(float)Math.cos(degrees*.0174532925),sin=(float)Math.sin(degrees*.0174532925),opposite=1-cos;
            float nx=x*(n.x*n.x*opposite+cos)+y*(n.y*n.x*opposite+n.z*sin)+z*(n.x*n.z*opposite-n.y*sin);
            float ny=x*(n.x*n.y*opposite-n.z*sin)+y*(n.y*n.y*opposite+cos)+z*(n.y*n.z*opposite+n.x*sin);
            float nz=x*(n.x*n.z*opposite+n.y*sin)+y*(n.y*n.z*opposite-n.x*sin)+z*(n.z*n.z*opposite+cos);
            x=nx;y=ny;z=nz;return this;
        }
        Vec3 vec(){return new Vec3(x,y,z);}
    }
}
