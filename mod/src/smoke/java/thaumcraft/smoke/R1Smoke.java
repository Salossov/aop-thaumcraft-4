package thaumcraft.smoke;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.ThaumcraftContent;
import top.theillusivec4.curios.api.CuriosApi;

/** Runs only with runSmokeClient/runSmokeServer, in disposable development worlds. */
@Mod("thaumcraft_r1_test")
public final class R1Smoke {
    static final Logger LOG = LogUtils.getLogger();
    static final Path EVIDENCE = Path.of(System.getProperty("thaumcraft.r1.root"), "analysis/r1/evidence");
    static final String PHASE = System.getProperty("thaumcraft.r1.phase", "seed");
    static final BlockPos BLOCK = new BlockPos(0, 101, 0);
    static final BlockPos CHEST = new BlockPos(2, 101, 0);
    static volatile boolean serverPassed;
    static volatile String failure;
    private boolean prepared;
    private boolean checked;
    private int ticks;

    public R1Smoke() {
        NeoForge.EVENT_BUS.addListener(this::started);
        NeoForge.EVENT_BUS.addListener(this::tick);
    }

    private void started(ServerStartedEvent event) {
        try {
            var server = event.getServer();
            var level = server.overworld();
            if (PHASE.equals("seed")) {
                for (int x = -4; x <= 4; x++) for (int z = -4; z <= 6; z++) {
                    level.setBlock(new BlockPos(x, 100, z), Blocks.STONE.defaultBlockState(), 3);
                    for (int y = 101; y <= 105; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
                level.setBlock(BLOCK, ThaumcraftContent.THAUMIUM_BLOCK.get().defaultBlockState(), 3);
                level.setBlock(CHEST, Blocks.CHEST.defaultBlockState(), 3);
                var chest = (ChestBlockEntity) level.getBlockEntity(CHEST);
                chest.setItem(0, new ItemStack(ThaumcraftContent.THAUMIUM_INGOT.get(), 17));
                chest.setChanged();
                server.saveEverything(false, true, true);
            }
            require(level.getBlockState(BLOCK).is(ThaumcraftContent.THAUMIUM_BLOCK.get()), "saved block ID");
            require(level.getBlockEntity(CHEST) instanceof ChestBlockEntity, "saved chest");
            var stack = ((ChestBlockEntity) level.getBlockEntity(CHEST)).getItem(0);
            require(stack.is(ThaumcraftContent.THAUMIUM_INGOT.get()) && stack.getCount() == 17, "saved ingot count");
            var parsed = ItemStack.parseOptional(level.registryAccess(), (net.minecraft.nbt.CompoundTag) stack.save(level.registryAccess()));
            require(ItemStack.isSameItemSameComponents(stack, parsed) && parsed.getCount() == 17, "item NBT round trip");
            require(BuiltInRegistries.BLOCK.getKey(ThaumcraftContent.THAUMIUM_BLOCK.get()).toString().equals("thaumcraft:thaumium_block"), "block registry ID");
            require(stack.getMaxStackSize() == 64, "TC4 stack size");
            require(level.getBlockState(BLOCK).getDestroySpeed(level, BLOCK) == 4, "TC4 hardness");
            require(ThaumcraftContent.THAUMIUM_BLOCK.get().getExplosionResistance() == 20, "TC4 resistance");
            var drops = Block.getDrops(level.getBlockState(BLOCK), level, BLOCK, null, null, new ItemStack(Items.WOODEN_PICKAXE));
            require(drops.size() == 1 && drops.getFirst().is(ThaumcraftContent.THAUMIUM_BLOCK.get().asItem()), "block loot");
            require(ThaumcraftConfig.ENABLE_COPPER_CONTENT.get() == Boolean.parseBoolean(System.getProperty("thaumcraft.r1.copper", "true")), "server copper setting");
            var slots = CuriosApi.getPlayerSlots(level);
            require(slots.size() == 3 && slots.get("necklace").getSize() == 1 && slots.get("ring").getSize() == 2 && slots.get("belt").getSize() == 1, "Curios slot definitions 1/2/1");
            prepared = true;
            write(serverKind(server) + "-" + PHASE + "-world", Map.of("passed", true, "checks", 11, "copper", ThaumcraftConfig.ENABLE_COPPER_CONTENT.get()));
        } catch (Throwable error) { fail(event.getServer(), error); }
    }

    private void tick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (++ticks > 3600 && !checked) { fail(server, new IllegalStateException("client connection timeout")); return; }
        if (server.isDedicatedServer() && Files.exists(EVIDENCE.resolve("stop-server"))) { server.halt(false); return; }
        if (!prepared || checked || server.getPlayerList().getPlayers().isEmpty()) return;
        try {
            var player = server.getPlayerList().getPlayers().getFirst();
            player.teleportTo(server.overworld(), 0.5, 101, 4.5, 180, 10);
            if (PHASE.equals("seed")) {
                player.getInventory().setItem(0, new ItemStack(ThaumcraftContent.THAUMIUM_INGOT.get(), 3));
                player.getInventory().setItem(1, new ItemStack(ThaumcraftContent.THAUMIUM_BLOCK.get().asItem()));
            }
            var inventory = CuriosApi.getCuriosInventory(player).orElseThrow();
            var slots = inventory.getCurios();
            require(slots.size() == 3 && slots.get("necklace").getSlots() == 1 && slots.get("ring").getSlots() == 2 && slots.get("belt").getSlots() == 1, "actual player Curios slots");
            require(player.getInventory().getItem(0).is(ThaumcraftContent.THAUMIUM_INGOT.get()) && player.getInventory().getItem(0).getCount() == 3, "player inventory persistence");
            server.saveEverything(false, true, true);
            checked = true;
            serverPassed = true;
            write(serverKind(server) + "-" + PHASE + "-player", Map.of("passed", true, "player", player.getGameProfile().getName(), "slots", Map.of("necklace", 1, "ring", 2, "belt", 1)));
        } catch (Throwable error) { fail(server, error); }
    }

    static String serverKind(MinecraftServer server) { return server.isDedicatedServer() ? "dedicated" : "singleplayer"; }
    static void require(boolean condition, String check) { if (!condition) throw new IllegalStateException(check); }
    static void write(String name, Map<String, ?> values) {
        try {
            Files.createDirectories(EVIDENCE);
            var report = new LinkedHashMap<String, Object>(values);
            report.put("phase", PHASE);
            report.put("time", java.time.Instant.now().toString());
            Files.writeString(EVIDENCE.resolve(name + ".json"), new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n");
            LOG.info("R1 CHECK {}: {}", name, values);
        } catch (java.io.IOException error) { throw new java.io.UncheckedIOException(error); }
    }
    private static void fail(MinecraftServer server, Throwable error) {
        failure = error.toString();
        LOG.error("R1 verification failed", error);
        write(serverKind(server) + "-" + PHASE + "-failure", Map.of("passed", false, "error", failure));
        if (server.isDedicatedServer()) server.halt(false);
    }
}
