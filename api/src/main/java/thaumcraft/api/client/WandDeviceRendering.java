package thaumcraft.api.client;

import java.util.Objects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Client-only source ENTITY model space for device-held wands and sceptres.
 * The caller supplies its device animation matrix; the core handles its baked
 * model origin/scale and ordinary focus/layer/light passes. False means the
 * installed renderer does not support this stack. No item/world data is changed.
 */
public final class WandDeviceRendering {
    @FunctionalInterface public interface Renderer {
        boolean render(ItemStack stack,PoseStack pose,MultiBufferSource buffers,int light,int overlay,Level level);
    }
    private static Renderer renderer=(stack,pose,buffers,light,overlay,level)->false;
    public static void install(Renderer implementation){renderer=Objects.requireNonNull(implementation);}
    public static boolean renderEntity(ItemStack stack,PoseStack pose,MultiBufferSource buffers,int light,int overlay,Level level){return renderer.render(stack,pose,buffers,light,overlay,level);}
    private WandDeviceRendering(){}
}
