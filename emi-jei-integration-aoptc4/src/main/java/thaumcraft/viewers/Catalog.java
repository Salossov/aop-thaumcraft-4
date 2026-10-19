package thaumcraft.viewers;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.crafting.*;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.aspects.*;
public final class Catalog {
 private static List<RecipeDisplay> stationSource;
 private static Map<RecipeDisplay.Kind,List<ItemStack>> stations=Map.of();
 public static synchronized Map<RecipeDisplay.Kind,List<ItemStack>> workstations(){var recipes=all();if(stationSource!=recipes){stationSource=recipes;stations=Workstations.collect(recipes);}return stations;}

 public static List<RecipeDisplay> all(){return RecipeDisplays.recipes(Minecraft.getInstance().level);}
 public static boolean known(RecipeDisplay r){return ResearchApi.clientKnowledge().completedResearch().containsAll(r.research());}
 public static boolean visible(RecipeDisplay r){if(Viewers.RECIPES.get()==Viewers.Recipes.RESEARCHED_ONLY&&!known(r))return false;return r.kind()!=RecipeDisplay.Kind.DISTILLATION||Viewers.ASPECTS.get()==Viewers.Aspects.ALL||r.inputs().stream().flatMap(i->Arrays.stream(i.getItems())).anyMatch(AspectApi::scanned);}
 public static boolean arcane(AbstractContainerMenu menu){return net.minecraft.core.registries.BuiltInRegistries.MENU.getKey(menu.getType()).equals(ResourceLocation.parse("thaumcraft:arcane_workbench"));}
 public static Optional<TransferPlanner.Plan<ItemStack>> plan(AbstractContainerMenu menu,RecipeDisplay r,boolean maximum){if(!arcane(menu)||!known(r)||r.inputs().size()!=9)return Optional.empty();var inventory=new ArrayList<ItemStack>();for(int i=11;i<47;i++)inventory.add(menu.getSlot(i).getItem());var grid=new ArrayList<ItemStack>();for(int i=2;i<11;i++)grid.add(menu.getSlot(i).getItem());var tests=new ArrayList<java.util.function.Predicate<ItemStack>>();for(var i:r.inputs())tests.add(i.isEmpty()?null:i::test);return TransferPlanner.plan(inventory,grid,tests,new TransferPlanner.Stacks<ItemStack>(){
 public boolean empty(ItemStack s){return s.isEmpty();}public int count(ItemStack s){return s.getCount();}public int limit(ItemStack s){return s.getMaxStackSize();}public boolean same(ItemStack a,ItemStack b){return ItemStack.isSameItemSameComponents(a,b);}public ItemStack count(ItemStack s,int n){return n==0?ItemStack.EMPTY:s.copyWithCount(n);}public ItemStack empty(){return ItemStack.EMPTY;}
 },maximum);}
 public static void transfer(AbstractContainerMenu menu,RecipeDisplay r,boolean maximum){net.neoforged.neoforge.network.PacketDistributor.sendToServer(new ArcaneTransferRequest(menu.containerId,menu.getStateId(),r.id(),maximum));}
 public static List<Component> details(RecipeDisplay r){var lines=new ArrayList<Component>();for(String key:r.research()){lines.add(RecipeArt.researchName(key));}if(r.kind()==RecipeDisplay.Kind.INFUSION){lines.add(Component.translatable("thaumcraft_viewers.instability",r.instability()));if(r.experience()>0)lines.add(Component.translatable("thaumcraft_viewers.xp",r.experience()));}lines.addAll(r.notes());lines.add(Component.translatable("thaumcraft_viewers.open_research"));return lines;}
 public static Component title(RecipeDisplay.Kind kind){return Component.translatable("thaumcraft_viewers.category."+kind.name().toLowerCase(Locale.ROOT));}
 public static ResourceLocation id(RecipeDisplay.Kind kind){return ResourceLocation.fromNamespaceAndPath("thaumcraft_viewers",kind.name().toLowerCase(Locale.ROOT));}
 private Catalog(){}
}
