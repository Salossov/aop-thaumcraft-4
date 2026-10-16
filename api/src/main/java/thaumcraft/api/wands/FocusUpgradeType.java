package thaumcraft.api.wands;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.aspects.*;

/** Stable original upgrade numbers are retained in focus components. */
public record FocusUpgradeType(int id,String key,Map<Aspect,Integer> aspects) {
    private static final Map<Integer,FocusUpgradeType> REGISTRY=new LinkedHashMap<>();
    public static final FocusUpgradeType potency=type(0,"potency","telum"),frugal=type(1,"frugal","fames"),treasure=type(2,"treasure","lucrum"),enlarge=type(3,"enlarge","iter");
    public static final FocusUpgradeType alchemistsfire=type(4,"alchemistsfire","potentia","limus"),alchemistsfrost=type(5,"alchemistsfrost","gelum","vinculum"),architect=type(6,"architect","fabrico"),extend=type(7,"extend","permutatio"),silktouch=type(8,"silktouch","lucrum");
    public static final FocusUpgradeType fireball=type(9,"fireball","tenebrae"),firebeam=type(10,"firebeam","potentia","aer"),scattershot=type(11,"scattershot","gelum","telum"),iceboulder=type(12,"iceboulder","gelum","vitreus");
    public static final FocusUpgradeType batbombs=type(13,"batbombs","potentia","vinculum"),devilbats=type(14,"devilbats","tutamen"),nightshade=type(15,"nightshade","victus","venenum","praecantatio"),seeker=type(16,"seeker","sensus","cognitio"),chainlightning=type(17,"chainlightning","tempestas"),earthshock=type(18,"earthshock","tempestas"),vampirebats=type(19,"vampirebats","fames","victus"),dowsing=type(20,"dowsing","perfodio");
    public FocusUpgradeType {if(id<0||id>32767||key.isBlank())throw new IllegalArgumentException("Invalid focus upgrade");aspects=Map.copyOf(aspects);}
    public static synchronized FocusUpgradeType register(int id,String key,Map<Aspect,Integer> aspects){if(REGISTRY.containsKey(id))throw new IllegalArgumentException("Duplicate focus upgrade "+id);var upgrade=new FocusUpgradeType(id,key,aspects);REGISTRY.put(id,upgrade);return upgrade;}
    private static FocusUpgradeType type(int id,String key,String... tags){var aspects=new LinkedHashMap<Aspect,Integer>();for(var tag:tags)aspects.put(Objects.requireNonNull(Aspect.getAspect(tag)),1);return register(id,key,aspects);}
    public static FocusUpgradeType get(int id){return REGISTRY.get(id);}
    public static Collection<FocusUpgradeType> all(){return Collections.unmodifiableCollection(REGISTRY.values());}
    public ResourceLocation icon(){return ResourceLocation.parse("thaumcraft:textures/foci/"+key+".png");}
    public String name(){return "focus.upgrade."+key+".name";}
    public String description(){return "focus.upgrade."+key+".text";}
}
