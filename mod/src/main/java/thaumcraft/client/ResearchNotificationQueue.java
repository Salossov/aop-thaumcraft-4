package thaumcraft.client;

import java.util.*;

/** FIFO lifetime starts at display; only the oldest row fades, like TC4's list. */
public final class ResearchNotificationQueue<T> {
    public record Row<T>(T value,long entered){}
    private final Deque<Row<T>> visible=new ArrayDeque<>();private final Deque<T> pending=new ArrayDeque<>();
    private final int maximum;private final long lifetime;private long headExpires,shiftStarted=-1;private int shifted;
    public ResearchNotificationQueue(int maximum,long lifetime){this.maximum=maximum;this.lifetime=lifetime;}
    public void add(T value,long now){pending.addLast(value);advance(now);}
    public void advance(long now){
        if(!visible.isEmpty()&&now>=headExpires){visible.removeFirst();headExpires=now+lifetime;shiftStarted=now;shifted=1;}
        while(visible.size()<maximum&&!pending.isEmpty()){boolean empty=visible.isEmpty();visible.addLast(new Row<>(pending.removeFirst(),now));if(empty)headExpires=now+lifetime+lifetime/2;}
    }
    public List<Row<T>> rows(){return List.copyOf(visible);}
    public int remaining(){return pending.size();}
    public float alpha(int index,long now){float entry=Math.clamp((now-rows().get(index).entered())/250f,0,1);return index==0?Math.min(entry,Math.clamp((headExpires-now)/(float)lifetime,.06f,1)):entry;}
    public float shift(long now){return shiftStarted<0?0:shifted*8*(float)Math.pow(1-Math.clamp((now-shiftStarted)/250f,0,1),3);}
    public boolean dismiss(int index,long now){
        if(index<0||index>=visible.size())return false;var iterator=visible.iterator();for(int i=0;i<=index;i++)iterator.next();iterator.remove();
        if(index==0){headExpires=now+lifetime;shiftStarted=now;shifted=1;}advance(now);return true;
    }
    public void clear(){visible.clear();pending.clear();headExpires=0;shiftStarted=-1;shifted=0;}
}
