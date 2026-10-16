package thaumcraft.api.wands;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
public final class StaffRod extends WandRod {
    public StaffRod(String tag,int capacity,Supplier<ItemStack> item,int craftCost,java.util.function.BiConsumer<ItemStack,Player> update){
        super(tag+"_staff",capacity,item,craftCost,update);
    }
}
