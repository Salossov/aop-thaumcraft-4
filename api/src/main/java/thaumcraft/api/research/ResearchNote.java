package thaumcraft.api.research;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import thaumcraft.api.aspects.Aspect;

/** TC4 axial grid, immutable anchors and component-to-parent connection rules. */
public final class ResearchNote {
    public record Hex(int q,int r) {
        private static final int[][] OFFSETS={{1,0},{1,-1},{0,-1},{-1,0},{-1,1},{0,1}};
        public Hex neighbour(int direction) { return new Hex(q+OFFSETS[direction][0],r+OFFSETS[direction][1]); }
        @Override public String toString() { return q+":"+r; }
    }
    public record Entry(String aspect,int type) {}
    public final String key;
    public final Map<Hex,Entry> grid;
    public boolean complete;
    public int copies;
    public ResearchNote(String key,Map<Hex,Entry> grid,boolean complete,int copies) {
        this.key=key;this.grid=new LinkedHashMap<>(grid);this.complete=complete;this.copies=copies;
    }
    public static ResearchNote create(String key,int complexity,List<Aspect> anchors,Random random) {
        int radius=1+Math.min(3,complexity);
        var grid=new LinkedHashMap<Hex,Entry>();
        for(int q=-radius;q<=radius;q++) for(int r=-radius;r<=radius;r++)
            if(Math.abs(q+r)<=radius) grid.put(new Hex(q,r),new Entry("",0));
        var ring=new ArrayList<Hex>(); var hex=new Hex(-radius,radius);
        for(int dir=0;dir<6;dir++) for(int n=0;n<radius;n++) { ring.add(hex);hex=hex.neighbour(dir); }
        // Original HexUtils.distributeRingRandomly: consume the same random call that picks
        // a start offset, but place anchors evenly from index 0 — matching its exact layout.
        random.nextInt(ring.size());
        float spacing=(float)ring.size()/anchors.size(),pos=0;
        for(var aspect:anchors) { grid.put(ring.get(Math.round(pos)),new Entry(aspect.getTag(),1));pos+=spacing; }
        int blanks=complexity>1?complexity*2:0;
        while(blanks>0) {
            var cells=new ArrayList<>(grid.keySet()); var candidate=cells.get(random.nextInt(cells.size()));
            if(grid.get(candidate).type()!=0) continue;
            boolean safe=true;
            for(int dir=0;dir<6;dir++) {
                var near=candidate.neighbour(dir);var entry=grid.get(near);
                if(entry==null||entry.type()!=1) continue;
                int neighbours=0;for(int d=0;d<6;d++) if(grid.containsKey(near.neighbour(d))) neighbours++;
                if(neighbours<2) safe=false;
            }
            if(safe) { grid.remove(candidate);blanks--; }
        }
        return new ResearchNote(key,grid,false,0);
    }
    public boolean checkComplete(Set<String> known) {
        var anchors=grid.entrySet().stream().filter(x->x.getValue().type()==1).map(Map.Entry::getKey).toList();
        if(anchors.isEmpty()) return false;
        var visited=new HashSet<Hex>(); var queue=new ArrayDeque<Hex>();queue.add(anchors.getFirst());visited.add(anchors.getFirst());
        while(!queue.isEmpty()) {
            var hex=queue.remove(); var aspect=Aspect.getAspect(grid.get(hex).aspect());
            if(aspect==null||!known.contains(aspect.getTag())) continue;
            for(int dir=0;dir<6;dir++) {
                var neighbour=hex.neighbour(dir);var entry=grid.get(neighbour);
                if(entry==null||entry.type()<1||visited.contains(neighbour)) continue;
                var other=Aspect.getAspect(entry.aspect());
                if(other!=null&&known.contains(other.getTag())&&aspect.connects(other)) { visited.add(neighbour);queue.add(neighbour); }
            }
        }
        if(!visited.containsAll(anchors)) return false;
        grid.entrySet().removeIf(x->x.getValue().type()!=1&&!visited.contains(x.getKey()));complete=true;return true;
    }
    public ResearchNote copy() { return new ResearchNote(key,grid,complete,copies); }
}
