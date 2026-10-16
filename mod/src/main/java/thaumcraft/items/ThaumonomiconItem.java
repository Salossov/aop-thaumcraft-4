package thaumcraft.items;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.menu.ThaumonomiconMenu;

public final class ThaumonomiconItem extends Item {
    public ThaumonomiconItem() { super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON)); }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        if(player instanceof ServerPlayer server) {
            KnowledgeService.initialize(server);KnowledgeService.sync(server);UUID token=UUID.randomUUID();
            server.openMenu(new MenuProvider() {
                public Component getDisplayName() { return Component.translatable("item.ItemThaumonomicon.name"); }
                public AbstractContainerMenu createMenu(int id,Inventory inventory,Player owner) { return new ThaumonomiconMenu(id,inventory,token); }
                public void writeClientSideData(AbstractContainerMenu menu,RegistryFriendlyByteBuf buffer) { buffer.writeUUID(token); }
            });
        }return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide);
    }
}
