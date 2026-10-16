package thaumcraft.client;

/** Time equivalents of TC4's per-frame recurrences, calibrated at 60 FPS. */
public final class FrameAnimation {
    private long last;
    public double seconds(){long now=System.nanoTime();double elapsed=last==0?1d/60:Math.max(0,Math.min(.25,(now-last)/1e9));last=now;return elapsed;}
    public void reset(){last=0;}
    public static float aspect(float value,double seconds,boolean active){
        double frames=Math.max(0,seconds)*60;
        if(!active)return (float)Math.max(0,value-.005*frames);
        return (float)(.265+(Math.max(.005,value)-.265)*Math.pow(.9,frames));
    }
    public static float hover(float value,float target,double seconds){return (float)(target+(value-target)*Math.pow(.75,Math.max(0,seconds)*60));}
}
