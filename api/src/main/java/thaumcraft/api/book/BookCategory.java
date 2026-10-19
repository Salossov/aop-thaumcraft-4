package thaumcraft.api.book;

import java.util.*;
import net.minecraft.resources.ResourceLocation;

public record BookCategory(String id,String name,String icon,String background,int order,String requiredResearch) {
    public BookCategory {Objects.requireNonNull(id);Objects.requireNonNull(name);ResourceLocation.parse(icon);ResourceLocation.parse(background);requiredResearch=requiredResearch==null?"":requiredResearch;}
    public static Map<String,BookCategory> originals(){var result=new LinkedHashMap<String,BookCategory>();String[] ids={"BASICS","THAUMATURGY","ALCHEMY","ARTIFICE","GOLEMANCY","ELDRITCH"},icons={"items/thaumonomiconcheat","misc/r_thaumaturgy","misc/r_crucible","misc/r_artifice","misc/r_golemancy","misc/r_eldritch"};for(int i=0;i<ids.length;i++)result.put(ids[i],new BookCategory(ids[i],"tc.research_category."+ids[i],"thaumcraft:textures/"+icons[i]+".png","thaumcraft:textures/gui/gui_researchback"+(i==5?"eldritch":"")+".png",i,i==5?"ELDRITCHMINOR":""));return result;}
}
