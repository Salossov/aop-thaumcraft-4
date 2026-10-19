package example.thaumcraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import thaumcraft.api.client.*;
@EventBusSubscriber(modid=ExampleAddon.ID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class ExampleClient {
 @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event){event.enqueueWork(()->{
  BookRenderers.register(net.minecraft.resources.ResourceLocation.parse(ExampleAddon.ID+":meter"),new BookPageRenderer(){
   public void render(thaumcraft.api.book.BookPage page,BookRenderContext c){var label=net.minecraft.network.chat.Component.translatable(page.data().get("label").getAsString());c.graphics().drawWordWrap(c.font(),label,c.x(),c.y(),139,0xff403020);c.graphics().fill(c.x(),c.y()+35,c.x()+120,c.y()+44,0xff403020);c.graphics().fill(c.x()+1,c.y()+36,c.x()+81,c.y()+43,0xffdca73a);c.link(net.minecraft.network.chat.Component.translatable("page.thaumcraft_example.back"),ExampleAddon.ID+":introduction",0,60);}
   public boolean mouseClicked(thaumcraft.api.book.BookPage page,double x,double y,int button){return false;}
  });
 });}
}
