package thaumcraft.smoke;

import java.util.Map;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.ThaumcraftContent;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber(modid = "thaumcraft_r1_test", value = Dist.CLIENT)
public final class R1ClientSmoke {
    private static boolean launched;
    private static int ticks;
    private static int readyTicks;
    private static int exitTicks;
    private static final String MODE = System.getProperty("thaumcraft.r1.client", "singleplayer");

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        try {
            if (++ticks > 6000) throw new IllegalStateException("client load timeout: " + mc.screen);
            if (ticks % 200 == 0) R1Smoke.LOG.info("R1 client progress: screen={}, level={}", mc.screen, mc.level != null);
            if (R1Smoke.failure != null) throw new IllegalStateException(R1Smoke.failure);
            if (!launched && mc.screen instanceof AccessibilityOnboardingScreen) {
                mc.options.onboardAccessibility = false;
                mc.options.save();
                mc.setScreen(new TitleScreen());
            }
            if (MODE.equals("multiplayer") && mc.screen instanceof DisconnectedScreen && ticks % 100 == 0) {
                launched = false;
                mc.setScreen(new TitleScreen());
            }
            if (!launched && mc.screen instanceof TitleScreen) {
                launched = true;
                mc.options.renderDistance().set(6);
                mc.options.pauseOnLostFocus = false;
                if (MODE.equals("multiplayer")) {
                    var data = new ServerData("R1 localhost", "127.0.0.1:25575", ServerData.Type.OTHER);
                    ConnectScreen.startConnecting(mc.screen, mc, ServerAddress.parseString(data.ip), data, false, null);
                } else if (R1Smoke.PHASE.equals("verify")) {
                    mc.createWorldOpenFlows().openWorld("tc4-r1", () -> mc.stop());
                } else {
                    var settings = new LevelSettings("TC4 R1", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
                    mc.createWorldOpenFlows().createFreshLevel("tc4-r1", settings, new WorldOptions(4235L, false, false), WorldPresets::createNormalWorldDimensions, mc.screen);
                }
            }
            if (exitTicks > 0) { if (--exitTicks == 0) mc.stop(); return; }
            if (mc.level == null || mc.player == null || !mc.level.getBlockState(R1Smoke.BLOCK).is(ThaumcraftContent.THAUMIUM_BLOCK.get())) return;
            if (++readyTicks < 120) return;
            var ingotSprite = mc.getItemRenderer().getModel(ThaumcraftContent.THAUMIUM_INGOT.toStack(), mc.level, mc.player, 0).getParticleIcon().contents().name().toString();
            var blockSprite = mc.getBlockRenderer().getBlockModel(ThaumcraftContent.THAUMIUM_BLOCK.get().defaultBlockState()).getParticleIcon().contents().name().toString();
            R1Smoke.require(ingotSprite.equals("thaumcraft:items/thaumiumingot"), "ingot baked texture");
            R1Smoke.require(blockSprite.equals("thaumcraft:blocks/thaumiumblock"), "block baked texture");
            var animated = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.parse("thaumcraft:blocks/candle"));
            long animationFrames = animated.contents().getUniqueFrames().count();
            R1Smoke.require(animationFrames > 1, "legacy texture animation loaded");
            R1Smoke.require(mc.player.getInventory().getItem(0).is(ThaumcraftContent.THAUMIUM_INGOT.get()), "client inventory sync");
            var slots = CuriosApi.getCuriosInventory(mc.player).orElseThrow().getCurios();
            R1Smoke.require(slots.size() == 3 && slots.get("ring").getSlots() == 2 && slots.get("necklace").getSlots() == 1 && slots.get("belt").getSlots() == 1, "client Curios sync");
            R1Smoke.require(ThaumcraftConfig.ENABLE_COPPER_CONTENT.get() == Boolean.parseBoolean(System.getProperty("thaumcraft.r1.copper", "true")), "client config sync");
            int soundCount;
            try (var reader = mc.getResourceManager().openAsReader(ResourceLocation.parse("thaumcraft:sounds.json"))) {
                var sounds = JsonParser.parseReader(reader).getAsJsonObject();
                soundCount = sounds.size();
                for (var id : sounds.keySet()) R1Smoke.require(mc.getSoundManager().getSoundEvent(ResourceLocation.fromNamespaceAndPath("thaumcraft", id)) != null, "loaded sound event " + id);
            }
            R1Smoke.write(MODE + "-" + R1Smoke.PHASE + "-client", Map.of("passed", true, "blockSprite", blockSprite, "ingotSprite", ingotSprite, "connected", true, "copper", ThaumcraftConfig.ENABLE_COPPER_CONTENT.get(), "loadedSoundEvents", soundCount, "animationFrames", animationFrames));
            Screenshot.grab(mc.gameDirectory, "r1-" + MODE + "-" + R1Smoke.PHASE + ".png", mc.getMainRenderTarget(), message -> R1Smoke.LOG.info("R1 screenshot: {}", message.getString()));
            exitTicks = 40;
        } catch (Throwable error) {
            R1Smoke.LOG.error("R1 client verification failed", error);
            R1Smoke.write(MODE + "-" + R1Smoke.PHASE + "-client-failure", Map.of("passed", false, "error", error.toString()));
            mc.stop();
        }
    }
}
