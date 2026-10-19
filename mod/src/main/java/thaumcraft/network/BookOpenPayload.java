package thaumcraft.network;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record BookOpenPayload(String research) implements CustomPacketPayload {
 public static final Type<BookOpenPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:book_open"));
 public static final StreamCodec<RegistryFriendlyByteBuf,BookOpenPayload> CODEC=StreamCodec.of((b,p)->b.writeUtf(p.research,128),b->new BookOpenPayload(b.readUtf(128)));
 public Type<BookOpenPayload> type(){return TYPE;}
 public void open(net.minecraft.server.level.ServerPlayer p){if(!p.getInventory().contains(new net.minecraft.world.item.ItemStack(thaumcraft.registry.R2Content.BOOK.get()))||!thaumcraft.data.ThaumcraftData.get(p.server).research().containsKey(research))return;
  thaumcraft.knowledge.KnowledgeService.sync(p);var token=java.util.UUID.randomUUID();p.openMenu(new net.minecraft.world.MenuProvider(){
   public net.minecraft.network.chat.Component getDisplayName(){return net.minecraft.network.chat.Component.translatable("item.ItemThaumonomicon.name");}
   public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id,net.minecraft.world.entity.player.Inventory inv,net.minecraft.world.entity.player.Player owner){return new thaumcraft.menu.ThaumonomiconMenu(id,inv,token);}
   public void writeClientSideData(net.minecraft.world.inventory.AbstractContainerMenu menu,RegistryFriendlyByteBuf b){b.writeUUID(token);b.writeUtf(research,128);}
  });
 }
}
