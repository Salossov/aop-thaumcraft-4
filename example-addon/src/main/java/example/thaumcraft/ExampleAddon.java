package example.thaumcraft;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.*;
import thaumcraft.api.addon.*;
import thaumcraft.api.aspects.*;
import thaumcraft.api.book.*;
import thaumcraft.api.research.ResearchDefinition;
import thaumcraft.api.wands.*;
@Mod("thaumcraft_example")
public final class ExampleAddon {
 public static final String ID="thaumcraft_example";
 public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(ID);
 public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(ID);
 public static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,ID);
 public static final DeferredItem<ExampleFocus> FOCUS=ITEMS.register("focus",ExampleFocus::new);
 public static final DeferredItem<Item> ROD=ITEMS.registerSimpleItem("rod");
 public static final DeferredItem<Item> CAP=ITEMS.registerSimpleItem("cap");
 public static final DeferredBlock<DeviceBlock> DEVICE=BLOCKS.register("device",DeviceBlock::new);
 public static final DeferredItem<BlockItem> DEVICE_ITEM=ITEMS.registerSimpleBlockItem(DEVICE);
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DeviceEntity>> DEVICE_TYPE=TILES.register("device",()->BlockEntityType.Builder.of(DeviceEntity::new,DEVICE.get()).build(null));
 public static Aspect ASPECT;
 public static FocusUpgradeType UPGRADE;
 public ExampleAddon(IEventBus bus){ITEMS.register(bus);BLOCKS.register(bus);TILES.register(bus);bus.addListener(ExampleAddon::capabilities);AddonRegistrationEvent.BUS.addListener(ExampleAddon::register);}
 private static void capabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e){e.registerBlockEntity(EssentiaCapabilities.TRANSPORT,DEVICE_TYPE.get(),(be,face)->be);e.registerBlockEntity(EssentiaCapabilities.CONTAINER,DEVICE_TYPE.get(),(be,unused)->be);}
 private static void register(AddonRegistrationEvent e){
  ASPECT=Aspect.registerCompound(ResourceLocation.fromNamespaceAndPath(ID,"resonantia"),0xdca73a,ResourceLocation.parse("thaumcraft:textures/aspects/auram.png"),"aspect.thaumcraft_example.resonantia",Aspects.AURA,Aspects.CRYSTAL);
  UPGRADE=FocusUpgradeType.register(24000,ResourceLocation.parse(ID+":pulse"),ResourceLocation.parse("thaumcraft:textures/foci/frugal.png"),Map.of(ASPECT,1));
  PageTypes.register(ResourceLocation.parse(ID+":meter"),json->{if(!json.has("label"))throw new IllegalArgumentException("Meter label is required");});
  AddonRegistry.category(new BookCategory(ID+":workshop","category.thaumcraft_example.workshop","minecraft:textures/item/diamond.png","thaumcraft:textures/gui/gui_researchback.png",100,""));
  AddonRegistry.research(new ResearchDefinition(ID+":introduction",ID+":workshop",List.of("THAUMONOMICON"),Map.of(),0,true,false,0,0,List.of(),List.of("thaumcraft_example:device"),"thaumcraft_example:device",false,false,false,false,List.of(),List.of(ID+":mixed",ID+":meter","thaumcraft_example:device")));
  var meter=new com.google.gson.JsonObject();meter.addProperty("label","page.thaumcraft_example.meter");AddonRegistry.page(new BookPage(ID+":meter",ID+":meter",List.of(),meter));
  new WandRod(ResourceLocation.parse(ID+":resonant"),ID+":introduction",75,()->new ItemStack(ROD.get()),8,(wand,player)->{});
  new WandCap(ResourceLocation.parse(ID+":resonant"),ID+":introduction",.95f,Set.of(Aspects.AIR),.9f,()->new ItemStack(CAP.get()),2);
  for(String kind:List.of("wand","sceptre"))WandVisuals.register(kind,new WandParts(ID+":resonant",ID+":resonant"),ResourceLocation.parse(ID+":item/"+kind));
  FocusBehaviors.register(FOCUS.get(),new FocusBehavior(){
   public boolean continuous(ItemStack focus){return true;}
   public boolean begin(net.minecraft.server.level.ServerPlayer p,ItemStack wand,ItemStack focus,net.minecraft.world.InteractionHand hand){return p.isAlive();}
   public boolean cast(net.minecraft.server.level.ServerPlayer p,ItemStack wand,ItemStack focus){p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,p.getX(),p.getEyeY(),p.getZ(),4,.3,.3,.3,.02);return true;}
   public void end(net.minecraft.server.level.ServerPlayer p,ItemStack wand,ItemStack focus){p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.thaumcraft_example.focus_end"),true);}
  },ResourceLocation.parse(ID+":item/focus_attached"));
 }
}
