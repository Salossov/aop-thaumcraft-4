package thaumcraft.client;

import java.util.*;
/** Unread discoveries, acknowledged when the research book is closed. */
public final class ResearchHighlights {
    private static final Set<String> UNREAD=new HashSet<>();
    public static void aspect(){UNREAD.add("ASPECTS");UNREAD.add("BASICS");}
    public static void research(String key){UNREAD.add(key);var definition=thaumcraft.network.ClientKnowledge.definitions().get(key);if(definition!=null)UNREAD.add(definition.category());}
    public static boolean unread(String key){return UNREAD.contains(key);}
    public static void clear(){UNREAD.clear();}
    private ResearchHighlights(){}
}
