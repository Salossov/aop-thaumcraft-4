package thaumcraft.items;

import java.util.Map;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** Fixed-width columns keep live charge updates from moving a tooltip. */
public record WandVisTooltip(Map<String,Integer> amounts,int capacity) implements TooltipComponent {
    public WandVisTooltip {amounts=Map.copyOf(amounts);}
}
