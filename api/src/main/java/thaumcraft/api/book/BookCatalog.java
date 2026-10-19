package thaumcraft.api.book;

import java.util.*;
import thaumcraft.api.research.ResearchDefinition;

public record BookCatalog(Map<String,BookCategory> categories,Map<String,BookPage> pages) {
    public static final BookCatalog EMPTY=new BookCatalog(BookCategory.originals(),Map.of());
    public BookCatalog {categories=Collections.unmodifiableMap(new LinkedHashMap<>(categories));pages=Map.copyOf(pages);}
    public void validate(Map<String,ResearchDefinition> research){for(var d:research.values()){if(!categories.containsKey(d.category()))throw new IllegalArgumentException("Unknown category "+d.category()+" in "+d.key());for(var page:d.pageOrder())if(page.contains(":")&&!d.recipePages().contains(page)&&!d.recipeGroups().containsKey(page)&&!pages.containsKey(page))throw new IllegalArgumentException("Unknown page "+page+" in "+d.key());}for(var category:categories.values())if(!category.requiredResearch().isEmpty()&&!research.containsKey(category.requiredResearch()))throw new IllegalArgumentException("Unknown category requirement "+category.requiredResearch());}
}
