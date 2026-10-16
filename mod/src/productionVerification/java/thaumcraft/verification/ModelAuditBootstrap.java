package thaumcraft.verification;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class ModelAuditBootstrap {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event){
        if(!"model-audit-live".equals(System.getProperty("thaumcraft.r3.phase")))return;
        event.enqueueWork(()->{var mc=net.minecraft.client.Minecraft.getInstance();mc.options.onboardAccessibility=false;mc.options.skipMultiplayerWarning=true;mc.options.pauseOnLostFocus=false;mc.options.save();long handle=mc.getWindow().getWindow();org.lwjgl.glfw.GLFW.glfwSetWindowAttrib(handle,org.lwjgl.glfw.GLFW.GLFW_FOCUS_ON_SHOW,org.lwjgl.glfw.GLFW.GLFW_FALSE);org.lwjgl.glfw.GLFW.glfwShowWindow(handle);org.lwjgl.glfw.GLFW.glfwRestoreWindow(handle);System.out.println("MODEL_AUDIT_BOOTSTRAP");});
    }
}
