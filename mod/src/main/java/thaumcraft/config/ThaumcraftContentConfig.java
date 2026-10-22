package thaumcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Original process-wide content switches must be available before recipe/data loading. */
public final class ThaumcraftContentConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ALLOW_MIRRORS;
    static {
        var builder = new ModConfigSpec.Builder();
        ALLOW_MIRRORS = builder.comment("Original TC4 allowMirrors: enable mirror research/crafting and dependent addon research.",
                "Loaded before recipes. Restart Minecraft/the dedicated server after changing; existing items and knowledge are retained.")
                .translation("thaumcraft.configuration.compatibility.allowMirrors").gameRestart().define("allowMirrors", true);
        SPEC = builder.build();
    }
    private ThaumcraftContentConfig() { }
}
