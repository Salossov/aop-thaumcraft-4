package thaumcraft.verification;

import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.registry.R3Content;

final class LabelRecipeVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){int count=0;
        for(var aspect:Aspect.all().values()){
            var phial=new ItemStack(R3Content.ESSENTIA_PHIAL.get());R3Content.setAspect(phial,aspect);
            var input=CraftingInput.of(2,1,List.of(new ItemStack(R3Content.JAR_LABEL.get()),phial));
            var holder=level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,level).orElseThrow();
            var output=holder.value().assemble(input,level.registryAccess());
            check.accept(output.is(R3Content.JAR_LABEL.get())&&R3Content.aspect(output)==aspect,"label crafting preserves the exact phial aspect: "+aspect.getTag());
            var wrong=input.items().getFirst().copy();R3Content.setAspect(wrong,aspect);
            check.accept(!holder.value().matches(CraftingInput.of(2,1,List.of(wrong,phial)),level),"label recipe refuses a previously printed label: "+aspect.getTag());
            var clearing=level.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:jarlabelnull")).orElseThrow();
            @SuppressWarnings("unchecked") var recipe=(Recipe<CraftingInput>)clearing.value();var single=CraftingInput.of(1,1,List.of(output));
            check.accept(recipe.matches(single,level)&&!recipe.assemble(single,level.registryAccess()).has(DataComponents.CUSTOM_DATA),"clearing removes the printed aspect component: "+aspect.getTag());count++;
        }
        check.accept(count==48,"all 48 original aspect labels craft and clear through the native recipe manager");
    }
}
