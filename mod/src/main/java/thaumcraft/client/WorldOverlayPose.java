package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/** Account for stages that apply the view globally and stages that pass it only in the event. */
final class WorldOverlayPose {
    static PoseStack create(RenderLevelStageEvent event){var pose=new PoseStack();pose.mulPose(new Matrix4f(RenderSystem.getModelViewMatrix()).invert().mul(event.getModelViewMatrix()));return pose;}
    private WorldOverlayPose(){}
}
