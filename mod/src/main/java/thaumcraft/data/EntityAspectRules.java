package thaumcraft.data;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.aspects.Aspect;

/** Shared identification keeps the scanner's display consistent with server scan keys. */
public final class EntityAspectRules {
    public static String scanKey(Entity entity,Map<String,List<ThaumcraftData.EntityVariant>> variants) {
        String id=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();if(entity instanceof Player player)return "player_"+player.getGameProfile().getName();
        var rules=variants.get(id);if(rules!=null){var nbt=entity.saveWithoutId(new CompoundTag());for(var rule:rules)if(matches(nbt,rule))id+="/"+rule.field()+"="+rule.value();}
        if(entity instanceof LivingEntity living&&living.isBaby())id+="/CHILD";
        if(entity instanceof Creeper creeper){if(creeper.getSwellDir()==1)id+="/FLASHING";if(creeper.isPowered())id+="/POWERED";}return id;
    }
    public static Map<String,Integer> aspects(Entity entity,Map<String,Map<String,Integer>> definitions,Map<String,List<ThaumcraftData.EntityVariant>> variants) {
        if(entity instanceof Player player){var values=new LinkedHashMap<String,Integer>();values.put("humanus",4);String name=player.getGameProfile().getName();
            if(name.equalsIgnoreCase("azanor"))values.put("alienis",20);else if(name.equalsIgnoreCase("direwolf20"))values.put("bestia",20);else if(name.equalsIgnoreCase("pahimar"))values.put("permutatio",20);
            else{var random=new Random(("player_"+name).hashCode());var options=new ArrayList<>(Aspect.all().keySet());for(int i=0;i<3;i++)values.merge(options.get(random.nextInt(options.size())),4,Integer::sum);}return Map.copyOf(values);
        }
        String id=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();var values=definitions.getOrDefault(id,Map.of());var rules=variants.get(id);
        if(rules!=null){var nbt=entity.saveWithoutId(new CompoundTag());for(var rule:rules)if(matches(nbt,rule))values=rule.aspects();}return values;
    }
    private static boolean matches(CompoundTag tag,ThaumcraftData.EntityVariant rule){if(!tag.contains(rule.field()))return false;var value=tag.get(rule.field());return value.getAsString().equals(rule.value())||value instanceof net.minecraft.nbt.NumericTag number&&number.getAsNumber().toString().equals(rule.value());}
    private EntityAspectRules(){}
}
