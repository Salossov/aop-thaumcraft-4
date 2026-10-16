package thaumcraft.client;
import java.util.Map;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
/** A final tooltip row, laid out with the item's vanilla text and window bounds. */
public record AspectTooltipData(Map<String,Integer> values) implements TooltipComponent {
    public AspectTooltipData {values=Map.copyOf(values);}
}
