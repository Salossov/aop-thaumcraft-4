package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import thaumcraft.client.CreativeWandGroups;
import thaumcraft.registry.*;

final class ThirdRoundClientChecks {
    static void run(Minecraft mc,BiConsumer<Boolean,String> check){
        var source=new ArrayList<ItemStack>();source.add(new ItemStack(Items.DIAMOND));for(var item:List.of(R2Content.WAND.get(),R3Equipment.STAFF.get(),R3Equipment.SCEPTRE.get()))for(String rod:List.of("wood","greatwood"))for(String cap:List.of("iron","gold")){var stack=new ItemStack(item);stack.set(thaumcraft.registry.WandComponents.PARTS.get(),new thaumcraft.api.wands.WandParts(rod,cap));source.add(stack);}source.add(new ItemStack(Items.STICK));
        var groups=new CreativeWandGroups();int[] expected={6,5,4,4,14};for(int mode=0;mode<5;mode++){var result=groups.project(source);check.accept(result.size()==expected[mode]&&result.getFirst().is(Items.DIAMOND)&&result.getLast().is(Items.STICK),"creative grouping projects variants without disturbing surrounding items: "+mode);if(mode<4){var representative=result.get(1);int count=groups.count(representative);check.accept(groups.toggle(representative)&&groups.project(source).size()==expected[mode]+count-1,"RMB expansion includes each real member once: "+mode);check.accept(groups.toggle(representative)&&groups.project(source).size()==expected[mode],"second RMB collapses variants: "+mode);}groups.next();}
        check.accept(Arrays.stream(net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.class.getDeclaredMethods()).anyMatch(method->method.getName().contains("tcExpand")),"native creative screen contains group RMB hook");
        for(String id:List.of("thaumatorium_brainbox","wand_recharge_pedestal","item_grate")){var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:"+id));var model=mc.getItemRenderer().getModel(new ItemStack(item),null,null,0);var right=model.getTransforms().getTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);var left=model.getTransforms().getTransform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND);check.accept(model.isGui3d()&&right.scale.x>0&&right.scale.x<=.4f&&left.scale.equals(right.scale)&&right.rotation.x!=0,"reported block uses bounded 3D transforms in both hands: "+id);}
        check.accept(mc.getSoundManager().getSoundEvent(net.minecraft.resources.ResourceLocation.parse("thaumcraft:wind"))!=null,"Zephyr wind sound resolves in the native sound engine");
        check.accept(net.minecraft.client.resources.language.I18n.exists("thaumcraft.notifications.remaining"),"notification backlog counter has localized text");
    }
}
