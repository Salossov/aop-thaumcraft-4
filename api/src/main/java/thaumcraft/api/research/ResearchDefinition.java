package thaumcraft.api.research;

import java.util.List;
import java.util.Map;
import thaumcraft.api.knowledge.PlayerKnowledge;

/** Data-pack definition retaining the TC4 research identity. */
public record ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,
                                 int complexity,boolean autoUnlock,boolean concealed,int column,int row,
                                 List<String> textPages,List<String> recipePages,String icon,
                                 boolean virtual,boolean round,boolean stub,boolean secondary,List<String> siblings,List<String> pageOrder,boolean hidden,int warp,
                                 List<String> itemTriggers,List<String> entityTriggers,List<String> aspectTriggers,List<String> hiddenParents,Map<String,String> pageRequirements,boolean special,Map<String,List<String>> recipeGroups,String linkedResearch) {
    /** Compatibility constructor for definitions without a cross-category research link. */
    public ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,int complexity,boolean autoUnlock,boolean concealed,int column,int row,List<String> textPages,List<String> recipePages,String icon,boolean virtual,boolean round,boolean stub,boolean secondary,List<String> siblings,List<String> pageOrder,boolean hidden,int warp,List<String> itemTriggers,List<String> entityTriggers,List<String> aspectTriggers,List<String> hiddenParents,Map<String,String> pageRequirements,boolean special,Map<String,List<String>> recipeGroups){
        this(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,virtual,round,stub,secondary,siblings,pageOrder,hidden,warp,itemTriggers,entityTriggers,aspectTriggers,hiddenParents,pageRequirements,special,recipeGroups,"");
    }
    /** Retains the existing canonical constructor for single-recipe pages. */
    public ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,int complexity,boolean autoUnlock,boolean concealed,int column,int row,List<String> textPages,List<String> recipePages,String icon,boolean virtual,boolean round,boolean stub,boolean secondary,List<String> siblings,List<String> pageOrder,boolean hidden,int warp,List<String> itemTriggers,List<String> entityTriggers,List<String> aspectTriggers,List<String> hiddenParents,Map<String,String> pageRequirements,boolean special){
        this(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,virtual,round,stub,secondary,siblings,pageOrder,hidden,warp,itemTriggers,entityTriggers,aspectTriggers,hiddenParents,pageRequirements,special,Map.of());
    }
    /** Retains the existing API constructor when the optional special frame is absent. */
    public ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,int complexity,boolean autoUnlock,boolean concealed,int column,int row,List<String> textPages,List<String> recipePages,String icon,boolean virtual,boolean round,boolean stub,boolean secondary,List<String> siblings,List<String> pageOrder,boolean hidden,int warp,List<String> itemTriggers,List<String> entityTriggers,List<String> aspectTriggers,List<String> hiddenParents,Map<String,String> pageRequirements){
        this(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,virtual,round,stub,secondary,siblings,pageOrder,hidden,warp,itemTriggers,entityTriggers,aspectTriggers,hiddenParents,pageRequirements,false);
    }
    public ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,int complexity,boolean autoUnlock,boolean concealed,int column,int row,List<String> textPages,List<String> recipePages,String icon,boolean virtual,boolean round,boolean stub,boolean secondary,List<String> siblings,List<String> pageOrder,boolean hidden,int warp,List<String> itemTriggers,List<String> entityTriggers,List<String> aspectTriggers,List<String> hiddenParents){
        this(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,virtual,round,stub,secondary,siblings,pageOrder,hidden,warp,itemTriggers,entityTriggers,aspectTriggers,hiddenParents,Map.of());
    }
    public ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,int complexity,boolean autoUnlock,boolean concealed,int column,int row,List<String> textPages,List<String> recipePages,String icon,boolean virtual,boolean round,boolean stub,boolean secondary,List<String> siblings,List<String> pageOrder,boolean hidden,int warp,List<String> itemTriggers,List<String> entityTriggers,List<String> aspectTriggers){
        this(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,virtual,round,stub,secondary,siblings,pageOrder,hidden,warp,itemTriggers,entityTriggers,aspectTriggers,List.of());
    }
    public ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,int complexity,boolean autoUnlock,boolean concealed,int column,int row,List<String> textPages,List<String> recipePages,String icon,boolean virtual,boolean round,boolean stub,boolean secondary,List<String> siblings,List<String> pageOrder,boolean hidden,int warp){
        this(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,virtual,round,stub,secondary,siblings,pageOrder,hidden,warp,List.of(),List.of(),List.of());
    }
    public ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,int complexity,boolean autoUnlock,boolean concealed,int column,int row,List<String> textPages,List<String> recipePages,String icon,boolean virtual,boolean round,boolean stub,boolean secondary,List<String> siblings,List<String> pageOrder){
        this(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,virtual,round,stub,secondary,siblings,pageOrder,false,0);
    }
    /** Compatibility constructor retaining the previous API and text-first default. */
    public ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,int complexity,boolean autoUnlock,boolean concealed,int column,int row,List<String> textPages,List<String> recipePages,String icon,boolean virtual,boolean round,boolean stub,boolean secondary,List<String> siblings) {
        this(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,virtual,round,stub,secondary,siblings,List.of());
    }
    public ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,
                              int complexity,boolean autoUnlock,boolean concealed,int column,int row,
                              List<String> textPages,List<String> recipePages,String icon,boolean virtual,boolean round,boolean stub) {
        this(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,virtual,round,stub,false,List.of());
    }
    /** Compatibility constructor for definitions without TC4 display flags. */
    public ResearchDefinition(String key,String category,List<String> parents,Map<String,Integer> aspects,
                              int complexity,boolean autoUnlock,boolean concealed,int column,int row,
                              List<String> textPages,List<String> recipePages,String icon) {
        this(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,false,false,false);
    }
    public ResearchDefinition {
        linkedResearch=linkedResearch==null?"":linkedResearch;
        if(warp<0||warp>127)throw new IllegalArgumentException("Invalid research warp");
        pageRequirements=pageRequirements==null?Map.of():Map.copyOf(pageRequirements);
        hiddenParents=hiddenParents==null?List.of():List.copyOf(hiddenParents);
        itemTriggers=itemTriggers==null?List.of():List.copyOf(itemTriggers);entityTriggers=entityTriggers==null?List.of():List.copyOf(entityTriggers);aspectTriggers=aspectTriggers==null?List.of():List.copyOf(aspectTriggers);
        parents=List.copyOf(parents);siblings=List.copyOf(siblings);aspects=java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(aspects));
        textPages=List.copyOf(textPages);recipePages=List.copyOf(recipePages);
        var groups=new java.util.LinkedHashMap<String,List<String>>();if(recipeGroups!=null)recipeGroups.forEach((name,variants)->{if(variants==null||variants.isEmpty())throw new IllegalArgumentException("Empty recipe group "+name);groups.put(name,List.copyOf(variants));});recipeGroups=java.util.Collections.unmodifiableMap(groups);
        for(var group:recipeGroups.entrySet())if(!recipePages.containsAll(group.getValue()))throw new IllegalArgumentException("Unknown recipe variant in "+group.getKey()+" for "+key);
        if(pageOrder==null||pageOrder.isEmpty()){var order=new java.util.ArrayList<String>(textPages);var grouped=new java.util.HashSet<String>();recipeGroups.values().forEach(grouped::addAll);for(var recipe:recipePages)if(!grouped.contains(recipe))order.add(recipe);order.addAll(recipeGroups.keySet());pageOrder=List.copyOf(order);}else pageOrder=List.copyOf(pageOrder);
        for(var page:pageOrder)if(!textPages.contains(page)&&!recipePages.contains(page)&&!recipeGroups.containsKey(page)&&!page.contains(":"))throw new IllegalArgumentException("Unknown research page "+page+" for "+key);
    }
    public List<String> recipeVariants(String page){return recipeGroups.getOrDefault(page,recipePages.contains(page)?List.of(page):List.of());}
    /** Copy only hidden requirements, retaining every display, trigger and recipe field. */
    public ResearchDefinition withHiddenParents(List<String> requirements) {
        return new ResearchDefinition(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,textPages,recipePages,icon,
                virtual,round,stub,secondary,siblings,pageOrder,hidden,warp,itemTriggers,entityTriggers,aspectTriggers,requirements,pageRequirements,special,recipeGroups,linkedResearch);
    }
    /** Alias identity, placement and flags stay local; presentation follows the active target. */
    ResearchDefinition withLinkedDisplay(ResearchDefinition target) {
        return new ResearchDefinition(key,category,parents,aspects,complexity,autoUnlock,concealed,column,row,target.textPages,target.recipePages,target.icon,
                virtual,round,stub,secondary,siblings,target.pageOrder,hidden,warp,itemTriggers,entityTriggers,aspectTriggers,hiddenParents,target.pageRequirements,special,target.recipeGroups,linkedResearch);
    }
    public boolean available(PlayerKnowledge knowledge) {
        return linkedResearch.isEmpty()&&!virtual&&(!hidden||knowledge.completedResearch().contains("@"+key))&&!knowledge.completedResearch().contains(key)&&knowledge.completedResearch().containsAll(parents)&&knowledge.completedResearch().containsAll(hiddenParents);
    }
}
