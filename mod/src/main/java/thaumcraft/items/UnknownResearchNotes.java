package thaumcraft.items;

import java.util.List;
import java.util.Random;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.research.*;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.registry.*;

/** TC4 research-note metadata 42 is represented by a persistent stack component. */
public final class UnknownResearchNotes {
    public static boolean isUnknown(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBoolean("thaumcraftUnknownNote");}
    public static List<ResearchDefinition> candidates(Iterable<ResearchDefinition> definitions,PlayerKnowledge knowledge){
        var result=new java.util.ArrayList<ResearchDefinition>();
        for(var definition:definitions)if(definition.hidden()&&!definition.aspects().isEmpty()&&!knowledge.completedResearch().contains(definition.key())&&knowledge.completedResearch().containsAll(definition.parents())&&knowledge.completedResearch().containsAll(definition.hiddenParents())&&(!definition.itemTriggers().isEmpty()||!definition.entityTriggers().isEmpty()||!definition.aspectTriggers().isEmpty()))result.add(definition);
        return List.copyOf(result);
    }
    public static void open(ItemStack stack,ServerPlayer player){
        var choices=candidates(ThaumcraftData.get(player.server).research().values(),KnowledgeService.get(player));
        if(choices.isEmpty()){
            stack.shrink(1);player.drop(new ItemStack(R3Equipment.KNOWLEDGE_FRAGMENT.get(),7+player.level().random.nextInt(3)),false);
            player.level().playSound(null,player.blockPosition(),R2Sounds.ERASE.get(),SoundSource.PLAYERS,.75f,1);
        }else{
            var selected=choices.get(new Random(player.level().getGameTime()/50).nextInt(choices.size()));
            var anchors=selected.aspects().keySet().stream().map(Aspect::getAspect).toList();
            var note=ResearchNote.create(selected.key(),selected.complexity(),anchors,new Random(player.level().random.nextLong()));
            ResearchNotes.write(stack,note);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->tag.remove("thaumcraftUnknownNote"));
            player.level().playSound(null,player.blockPosition(),R2Sounds.WRITE.get(),SoundSource.PLAYERS,.75f,1);
        }
    }
    private UnknownResearchNotes(){}
}
