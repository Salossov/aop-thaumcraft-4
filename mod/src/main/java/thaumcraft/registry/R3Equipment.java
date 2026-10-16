package thaumcraft.registry;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import thaumcraft.items.CastingWandItem;
import thaumcraft.items.R3ToolTier;
import thaumcraft.items.ThaumicTools;
import thaumcraft.items.ThaumicArmorItem;
import thaumcraft.items.ThaumicCurioItem;
public final class R3Equipment {
    public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("thaumcraft");
    public static final Map<String,DeferredItem<Item>> NATIVE_CLUSTERS=new LinkedHashMap<>();
    public static final Map<String,DeferredItem<thaumcraft.items.FoodNuggetItem>> FOOD_NUGGETS=new LinkedHashMap<>();
    static {for(String metal:List.of("iron","gold","copper","tin","silver","lead","cinnabar"))NATIVE_CLUSTERS.put(metal,ITEMS.registerSimpleItem("native_"+metal+"_cluster"));for(String food:List.of("chicken","beef","pork","fish"))FOOD_NUGGETS.put(food,ITEMS.register(food+"_nugget",thaumcraft.items.FoodNuggetItem::new));}
    public static final DeferredItem<thaumcraft.items.EldritchRelics.KnowledgeFragment> KNOWLEDGE_FRAGMENT=ITEMS.register("knowledge_fragment",thaumcraft.items.EldritchRelics.KnowledgeFragment::new);
    public static final DeferredItem<thaumcraft.items.EldritchRelics.SanityChecker> SANITY_CHECKER=ITEMS.register("sanity_checker",thaumcraft.items.EldritchRelics.SanityChecker::new);
    public static final DeferredItem<thaumcraft.items.EldritchRelics.SinisterStone> SINISTER_STONE=ITEMS.register("sinister_stone",thaumcraft.items.EldritchRelics.SinisterStone::new);
    public static final DeferredItem<thaumcraft.items.EldritchRelics.Eye> ELDRITCH_EYE=R2Content.ELDRITCH_EYE;
    public static final DeferredItem<thaumcraft.items.EldritchRelics.Pearl> PRIMAL_PEARL=ITEMS.register("primal_pearl",thaumcraft.items.EldritchRelics.Pearl::new);
    public static final DeferredItem<thaumcraft.items.EldritchRelics.CrimsonRites> CRIMSON_RITES=ITEMS.register("crimson_rites",thaumcraft.items.EldritchRelics.CrimsonRites::new);
    public static final DeferredItem<thaumcraft.items.BoneBowItem> BONE_BOW=ITEMS.register("bone_bow",thaumcraft.items.BoneBowItem::new);
    public static final DeferredItem<thaumcraft.items.BottleTaintItem> BOTTLE_TAINT=ITEMS.register("bottle_taint",thaumcraft.items.BottleTaintItem::new);
    public static final java.util.List<DeferredItem<thaumcraft.items.PrimalArrowItem>> PRIMAL_ARROWS=new java.util.ArrayList<>();
    static {var names=java.util.List.of("air","fire","water","earth","order","entropy");for(int i=0;i<6;i++){final int element=i;PRIMAL_ARROWS.add(ITEMS.register(names.get(i)+"_primal_arrow",()->new thaumcraft.items.PrimalArrowItem(element)));}}
    public static final DeferredItem<thaumcraft.items.TrunkSpawnerItem> TRAVELING_TRUNK=ITEMS.register("traveling_trunk",thaumcraft.items.TrunkSpawnerItem::new);
    private static final DeferredRegister<ArmorMaterial> MATERIALS=DeferredRegister.create(Registries.ARMOR_MATERIAL,"thaumcraft");
    public static final Map<thaumcraft.entities.GolemMaterial,DeferredItem<thaumcraft.items.GolemPlacerItem>> GOLEM_BODIES=new EnumMap<>(thaumcraft.entities.GolemMaterial.class);
    public static final Map<thaumcraft.items.GolemCoreItem.Kind,DeferredItem<thaumcraft.items.GolemCoreItem>> GOLEM_CORES=new EnumMap<>(thaumcraft.items.GolemCoreItem.Kind.class);
    public static final java.util.List<DeferredItem<thaumcraft.items.GolemUpgradeItem>> GOLEM_UPGRADES=new ArrayList<>();
    public static final java.util.List<DeferredItem<thaumcraft.items.GolemDecorationItem>> GOLEM_DECORATIONS=new ArrayList<>();
    public static final DeferredItem<thaumcraft.items.GolemBellItem> GOLEM_BELL=ITEMS.register("golemancers_bell",thaumcraft.items.GolemBellItem::new);
    static{for(var material:thaumcraft.entities.GolemMaterial.values())GOLEM_BODIES.put(material,ITEMS.register(material.name().toLowerCase(Locale.ROOT)+"_golem",()->new thaumcraft.items.GolemPlacerItem(material)));for(var kind:thaumcraft.items.GolemCoreItem.Kind.values())GOLEM_CORES.put(kind,ITEMS.register("golem_core_"+kind.name().toLowerCase(Locale.ROOT),()->new thaumcraft.items.GolemCoreItem(kind)));var names=List.of("air","earth","fire","water","order","entropy");for(int i=0;i<6;i++){final int kind=i;GOLEM_UPGRADES.add(ITEMS.register(names.get(i)+"_golem_upgrade",()->new thaumcraft.items.GolemUpgradeItem(kind)));}var decorations=List.of("H","G","B","F","R","V","P","M");var ids=List.of("tophat","glasses","bowtie","fez","dart_launcher","visor","armor","mace");for(int i=0;i<ids.size();i++){var decoration=decorations.get(i);GOLEM_DECORATIONS.add(ITEMS.register("golem_"+ids.get(i),()->new thaumcraft.items.GolemDecorationItem(decoration)));}}
    public static final Map<String,DeferredHolder<ArmorMaterial,ArmorMaterial>> ARMOR_MATERIALS=new LinkedHashMap<>();
    public static final Map<String,DeferredItem<Item>> TOOLS=new LinkedHashMap<>();
    public static final Map<String,DeferredItem<ArmorItem>> ARMOR=new LinkedHashMap<>();
    public static final Map<String,DeferredItem<ThaumicCurioItem>> CURIOS=new LinkedHashMap<>();
    public static final Map<String,DeferredItem<thaumcraft.items.ThaumicFocusItem>> FOCI=new LinkedHashMap<>();
    static {
        for(var kind:thaumcraft.items.ThaumicFocusItem.Kind.values())FOCI.put(kind.name().toLowerCase(Locale.ROOT),ITEMS.register("focus_"+kind.name().toLowerCase(Locale.ROOT),()->new thaumcraft.items.ThaumicFocusItem(kind)));
        material("thaumium",new int[]{2,6,5,2},25,false);
        material("void",new int[]{3,7,6,3},10,true);
        material("robe",new int[]{1,3,2,1},25,false);
        material("fortress",new int[]{3,7,6,3},25,false);
        material("void_robe",new int[]{4,8,7,4},10,true);
        material("traveller",new int[]{1,3,2,1},25,false);
        material("harness",new int[]{1,3,2,1},25,false);
        for(var tier:List.of(R3ToolTier.THAUMIUM,R3ToolTier.VOID,R3ToolTier.ELEMENTAL)){
            String prefix=tier.name().toLowerCase(Locale.ROOT);
            TOOLS.put(prefix+"_axe",ITEMS.register(prefix+"_axe",()->new ThaumicTools.Axe(tier)));
            TOOLS.put(prefix+"_pickaxe",ITEMS.register(prefix+"_pickaxe",()->new ThaumicTools.Pickaxe(tier)));
            TOOLS.put(prefix+"_shovel",ITEMS.register(prefix+"_shovel",()->new ThaumicTools.Shovel(tier)));
            TOOLS.put(prefix+"_hoe",ITEMS.register(prefix+"_hoe",()->new ThaumicTools.Hoe(tier)));
            TOOLS.put(prefix+"_sword",ITEMS.register(prefix+"_sword",()->new ThaumicTools.Sword(tier)));
        }
        TOOLS.put("primal_crusher",ITEMS.register("primal_crusher",ThaumicTools.Crusher::new));
        armorSet("thaumium","thaumium",ThaumicArmorItem.Kind.THAUMIUM,25,true);
        armorSet("void","void",ThaumicArmorItem.Kind.VOID,10,true);
        armorSet("thaumium_fortress","fortress",ThaumicArmorItem.Kind.FORTRESS,40,false);
        armorSet("void_robe","void_robe",ThaumicArmorItem.Kind.VOID_ROBE,18,false);
        armorSet("thaumaturge","robe",ThaumicArmorItem.Kind.ROBE,25,true);
        ARMOR.put("boots_of_the_traveller",ITEMS.register("boots_of_the_traveller",()->new ThaumicArmorItem(ARMOR_MATERIALS.get("traveller"),ArmorItem.Type.BOOTS,ThaumicArmorItem.Kind.TRAVELLER,350)));
        ARMOR.put("hover_harness",ITEMS.register("hover_harness",()->new ThaumicArmorItem(ARMOR_MATERIALS.get("harness"),ArmorItem.Type.CHESTPLATE,ThaumicArmorItem.Kind.HARNESS,400)));
        curio("runic_ring_minor",ThaumicCurioItem.Kind.RING_MINOR);
        curio("runic_ring",ThaumicCurioItem.Kind.RING);
        curio("runic_ring_charged",ThaumicCurioItem.Kind.RING_CHARGED);
        curio("runic_ring_healing",ThaumicCurioItem.Kind.RING_HEALING);
        curio("runic_amulet",ThaumicCurioItem.Kind.AMULET);
        curio("runic_amulet_emergency",ThaumicCurioItem.Kind.AMULET_EMERGENCY);
        curio("runic_belt",ThaumicCurioItem.Kind.BELT);
        curio("runic_belt_kinetic",ThaumicCurioItem.Kind.BELT_KINETIC);
        curio("hover_girdle",ThaumicCurioItem.Kind.HOVER_GIRDLE);
        curio("vis_amulet_lesser",ThaumicCurioItem.Kind.VIS_AMULET_LESSER);
        curio("vis_amulet",ThaumicCurioItem.Kind.VIS_AMULET);
    }
    public static final Map<String,DeferredItem<Item>> RODS=new LinkedHashMap<>(),CAPS=new LinkedHashMap<>();
    static {
        for(String s:List.of("greatwood","obsidian","silverwood","ice","quartz","reed","blaze","bone")) {
            RODS.put(s,ITEMS.registerSimpleItem(s+"_wand_rod"));
            RODS.put(s+"_staff",ITEMS.registerSimpleItem(s+"_staff_rod"));
        }
        RODS.put("primal_staff",ITEMS.registerSimpleItem("primal_staff_rod"));
        for(String s:List.of("gold","thaumium","void","copper","silver"))CAPS.put(s,ITEMS.registerSimpleItem(s+"_wand_cap"));
    }
    public static final Map<String,DeferredItem<Item>> INERT_CAPS=new LinkedHashMap<>();
    static {for(String s:List.of("silver","thaumium","void"))INERT_CAPS.put(s,ITEMS.registerSimpleItem("inert_"+s+"_wand_cap"));}
    public static final DeferredItem<Item> THAUMIUM_NUGGET=ITEMS.registerSimpleItem("thaumium_nugget");
    public static final DeferredItem<Item> VOID_NUGGET=ITEMS.registerSimpleItem("void_nugget");
    public static final DeferredItem<Item> COPPER_NUGGET=ITEMS.registerSimpleItem("copper_nugget");
    public static final Map<String,DeferredItem<Item>> OPTIONAL_NUGGETS=new LinkedHashMap<>();
    static {for(var metal:List.of("tin","silver","lead"))OPTIONAL_NUGGETS.put(metal,ITEMS.registerSimpleItem(metal+"_nugget"));}
    public static final DeferredItem<Item> VOID_SEED=ITEMS.registerSimpleItem("void_seed");
    public static final DeferredItem<Item> VOID_INGOT=ITEMS.registerSimpleItem("void_ingot");
    public static final DeferredItem<thaumcraft.items.FocusPouchItem> FOCUS_POUCH=ITEMS.register("focus_pouch",thaumcraft.items.FocusPouchItem::new);
    public static final DeferredItem<Item> SALIS_MUNDUS=ITEMS.registerSimpleItem("salis_mundus");
    public static final DeferredItem<Item> PRIMAL_CHARM=ITEMS.registerSimpleItem("primal_charm");
    public static final DeferredItem<Item> ENCHANTED_FABRIC=ITEMS.registerSimpleItem("enchanted_fabric");
    public static final DeferredItem<Item> MIRRORED_GLASS=ITEMS.registerSimpleItem("mirrored_glass");
    public static final DeferredItem<Item> AMULET_BLANK=ITEMS.registerSimpleItem("amulet_blank",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> RING_BLANK=ITEMS.registerSimpleItem("ring_blank",new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BELT_BLANK=ITEMS.registerSimpleItem("belt_blank",new Item.Properties().stacksTo(1));
    public static final DeferredItem<CastingWandItem> WAND=ITEMS.register("casting_wand",()->new CastingWandItem(CastingWandItem.Kind.WAND));
    public static final DeferredItem<CastingWandItem> STAFF=ITEMS.register("casting_staff",()->new CastingWandItem(CastingWandItem.Kind.STAFF));
    public static final DeferredItem<CastingWandItem> SCEPTRE=ITEMS.register("casting_sceptre",()->new CastingWandItem(CastingWandItem.Kind.SCEPTRE));
    private static void material(String id,int[] protection,int enchantability,boolean isVoid){
        ARMOR_MATERIALS.put(id,MATERIALS.register(id,()->{
            var defense=new EnumMap<ArmorItem.Type,Integer>(ArmorItem.Type.class);for(var type:ArmorItem.Type.values())defense.put(type,0);
            defense.put(ArmorItem.Type.HELMET,protection[0]);defense.put(ArmorItem.Type.CHESTPLATE,protection[1]);defense.put(ArmorItem.Type.LEGGINGS,protection[2]);defense.put(ArmorItem.Type.BOOTS,protection[3]);
            var texture=ResourceLocation.fromNamespaceAndPath("thaumcraft",id);var layers=id.equals("robe")||id.equals("void_robe")?List.of(new ArmorMaterial.Layer(texture,"",true),new ArmorMaterial.Layer(texture,"_overlay",false)):List.of(new ArmorMaterial.Layer(texture));
            return new ArmorMaterial(defense,enchantability,SoundEvents.ARMOR_EQUIP_IRON,()->Ingredient.of(isVoid?VOID_INGOT.get():id.equals("robe")?ENCHANTED_FABRIC.get():id.equals("harness")?Items.GOLD_INGOT:ThaumcraftContent.THAUMIUM_INGOT.get()),layers,0,0);
        }));
    }
    private static void curio(String id,ThaumicCurioItem.Kind kind){CURIOS.put(id,ITEMS.register(id,()->new ThaumicCurioItem(kind)));}
    private static void armorSet(String prefix,String material,ThaumicArmorItem.Kind kind,int durability,boolean boots){
        var types=kind==ThaumicArmorItem.Kind.ROBE?List.of(ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS):boots?List.of(ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS):List.of(ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS);
        for(var type:types){String suffix=switch(type){case HELMET->"helmet";case CHESTPLATE->"chestplate";case LEGGINGS->"leggings";case BOOTS->"boots";default->throw new IllegalArgumentException();};String id=prefix+"_"+suffix;
            ARMOR.put(id,ITEMS.register(id,()->new ThaumicArmorItem(ARMOR_MATERIALS.get(material),type,kind,type.getDurability(durability))));
        }
    }
    public static void register(IEventBus bus){MATERIALS.register(bus);ITEMS.register(bus);}
    public static boolean visible(Item item){for(var metal:List.of("tin","silver","lead"))if(item==OPTIONAL_NUGGETS.get(metal).get()||item==NATIVE_CLUSTERS.get(metal).get())return net.minecraft.core.registries.BuiltInRegistries.ITEM.getTag(net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.parse("c:ingots/"+metal))).map(t->t.size()>0).orElse(false);return true;}
    public static void creative(CreativeModeTab.Output output){for(var item:ITEMS.getEntries())if(!(item.get() instanceof CastingWandItem)&&visible(item.get()))output.accept(item.get());
        for(var rod:thaumcraft.api.wands.WandRod.rods.values())for(var cap:thaumcraft.api.wands.WandCap.caps.values()){
            var stack=new ItemStack(rod instanceof thaumcraft.api.wands.StaffRod?STAFF.get():WAND.get());
            stack.set(WandComponents.PARTS.get(),new thaumcraft.api.wands.WandParts(rod.getTag(),cap.getTag()));output.accept(stack);
            if(!(rod instanceof thaumcraft.api.wands.StaffRod)){var sceptre=new ItemStack(SCEPTRE.get());sceptre.set(WandComponents.PARTS.get(),new thaumcraft.api.wands.WandParts(rod.getTag(),cap.getTag()));output.accept(sceptre);}
        }
    }
    private R3Equipment(){}
}
