package thaumcraft.verification;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

/** Actual native locale resources and Component formatting versus owned original boss-name templates. */
final class ClientBossNameVerification {
    static void run(Minecraft mc,List<String> checks)throws Exception{
        var previous=Language.getInstance();
        try(var source=new JarFile(java.nio.file.Path.of(System.getProperty("thaumcraft.r3.root"),"Thaumcraft-1.7.10-4.2.3.5.jar").toFile())){
            for(String locale:List.of("en_us","ru_ru")){
                var nativeLanguage=ClientLanguage.loadFrom(mc.getResourceManager(),List.of("en_us",locale),false);Language.inject(nativeLanguage);
                var original=new HashMap<String,String>();
                for(String old:locale.equals("en_us")?List.of("en_US"):List.of("en_US","ru_RU"))try(var input=source.getInputStream(source.getJarEntry("assets/thaumcraft/lang/"+old+".lang"))){for(String row:new String(input.readAllBytes(),StandardCharsets.UTF_8).split("\\R")){int split=row.indexOf('=');if(split>0)original.put(row.substring(0,split),row.substring(split+1));}}
                for(String[] entry:List.of(new String[]{"cultist_leader","CultistLeader","Alberic","mighty"},new String[]{"eldritch_warden","EldritchWarden","Aphoom-Zhah","warded"},new String[]{"eldritch_golem","EldritchGolem","","bold"})){
                    String key="entity.thaumcraft."+entry[0]+".champion";
                    if(!nativeLanguage.has(key))throw new AssertionError("Missing boss-name resource "+locale+"/"+key);
                    String modifier=original.get("champion.mod."+entry[3]);String template=original.get("entity.Thaumcraft."+entry[1]+".name").replace("%d","%s");
                    Object[] args=entry[0].equals("eldritch_golem")?new Object[]{Component.translatable("champion.mod."+entry[3])}:new Object[]{entry[2],Component.translatable("champion.mod."+entry[3])};
                    String expected=entry[0].equals("eldritch_golem")?String.format(Locale.ROOT,template,modifier):String.format(Locale.ROOT,template,entry[2],modifier);
                    String actual=Component.translatable(key,args).getString();
                    if(!actual.equals(expected))throw new AssertionError(locale+" boss name: "+actual+" != original template "+expected);
                    checks.add("native "+locale+" Component formats original "+entry[1]+" title/modifier");
                }
            }
        }finally{Language.inject(previous);}
    }
    private ClientBossNameVerification(){}
}
