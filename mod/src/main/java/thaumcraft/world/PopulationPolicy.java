package thaumcraft.world;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/** TC4's population gates and ordered decisions, independent of chunk scheduling. */
public final class PopulationPolicy {
    public enum Structure { MOUND, RING, HILLTOP, NONE }
    public record Gates(boolean trees,boolean ores,boolean aura,boolean structures,boolean totems){}
    public record Switches(boolean trees,boolean aura,boolean structures,boolean regenTrees,boolean regenAura,boolean regenStructures){}
    public static boolean validBlacklist(String text){
        int split=text.lastIndexOf('=');if(split<1)return false;
        try{return ResourceLocation.tryParse(text.substring(0,split))!=null&&Integer.parseInt(text.substring(split+1))>=0;}catch(NumberFormatException ignored){return false;}
    }
    public static int blacklist(List<? extends String> entries,ResourceLocation id){
        int result=-1;
        for(var entry:entries){int split=entry.lastIndexOf('=');if(split>0&&entry.substring(0,split).equals(id.toString()))result=Integer.parseInt(entry.substring(split+1));}
        return result;
    }
    public static Gates gates(int dimensionBlacklist,int biomeBlacklist,boolean overworld,boolean nether,boolean end,boolean flat,boolean newGen,Switches switches){
        if(end)return new Gates(false,false,false,false,false);
        if(nether)return new Gates(false,false,switches.aura&&(newGen||switches.regenAura),false,false);
        boolean trees=dimensionBlacklist==-1&&biomeBlacklist==-1&&switches.trees&&!flat&&(newGen||switches.regenTrees);
        boolean ores=dimensionBlacklist!=0&&dimensionBlacklist!=2&&biomeBlacklist!=0&&biomeBlacklist!=2;
        boolean aura=dimensionBlacklist!=0&&dimensionBlacklist!=2&&switches.aura&&(newGen||switches.regenAura);
        boolean structures=dimensionBlacklist==-1&&switches.structures&&overworld&&!flat&&(newGen||switches.regenStructures);
        return new Gates(trees,ores,aura,structures,structures&&newGen);
    }
    /** A selected but failed placement does not fall through to another branch. */
    public static Structure structure(RandomSource random){
        if(random.nextInt(150)==0)return Structure.MOUND;
        if(random.nextInt(66)==0)return Structure.RING;
        if(random.nextInt(40)==0)return Structure.HILLTOP;
        return Structure.NONE;
    }
    public static int mazeSize(RandomSource random){return 11+random.nextInt(6)*2;}
    public static long populationSeed(long worldSeed,int chunkX,int chunkZ){
        var random=new java.util.Random(worldSeed);
        long x=random.nextLong()/2*2+1,z=random.nextLong()/2*2+1;
        return ((long)chunkX*x+(long)chunkZ*z)^worldSeed;
    }
    private PopulationPolicy(){}
}
