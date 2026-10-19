package thaumcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumcraftClientConfig {
    public enum CreativeTabIcon implements net.neoforged.neoforge.common.TranslatableEnum {WAND,THAUMONOMICON,THAUMOMETER;
        @Override public net.minecraft.network.chat.Component getTranslatedName(){return net.minecraft.network.chat.Component.translatable("thaumcraft.configuration.creative_icon."+name().toLowerCase(java.util.Locale.ROOT));}}
    public static final ModConfigSpec.EnumValue<CreativeTabIcon> CREATIVE_TAB_ICON;
    public enum BookFontStyle implements net.neoforged.neoforge.common.TranslatableEnum {VANILLA,UNICODE;
        @Override public net.minecraft.network.chat.Component getTranslatedName(){return net.minecraft.network.chat.Component.translatable("thaumcraft.configuration.font."+name().toLowerCase(java.util.Locale.ROOT));}}
    public static final ModConfigSpec.EnumValue<BookFontStyle> BOOK_FONT_STYLE;
    public enum GuardianFogMode implements net.neoforged.neoforge.common.TranslatableEnum {OFF,VANILLA,SHADER;
        @Override public net.minecraft.network.chat.Component getTranslatedName(){return net.minecraft.network.chat.Component.translatable("thaumcraft.configuration.fog."+name().toLowerCase(java.util.Locale.ROOT));}}
    public enum BookWindowMode implements net.neoforged.neoforge.common.TranslatableEnum {ORIGINAL,SQUARE,FULL;
        @Override public net.minecraft.network.chat.Component getTranslatedName(){return net.minecraft.network.chat.Component.translatable("thaumcraft.configuration.book_window."+name().toLowerCase(java.util.Locale.ROOT));}}
    public static final ModConfigSpec.EnumValue<BookWindowMode> BOOK_WINDOW_MODE;
    public static final ModConfigSpec.DoubleValue BOOK_ZOOM;
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue GOLEM_LINK_QUALITY;
    public static final ModConfigSpec.EnumValue<GuardianFogMode> GUARDIAN_FOG_MODE;
    public static final ModConfigSpec.BooleanValue SHOW_TAGS,WAND_DIAL_BOTTOM,FIT_LARGE_SCREENS,WARP_SHADERS,COLORED_JAR_LABELS,ORIGINAL_ANCIENT_CRYSTALS,BRIGHT_WARDED_GLASS;
    static {
        var builder=new ModConfigSpec.Builder();builder.push("effects");
        WARP_SHADERS=builder.comment("Enable original blur, hunger, death gaze and sun-scorned screen effects using a separate framebuffer pass compatible with Sodium.").translation("thaumcraft.configuration.effects.warpShaders").define("warpShaders",true);
        GUARDIAN_FOG_MODE=builder.comment("Eldritch Guardian mist: OFF, native VANILLA fog, or exponential SHADER postprocessing.","SHADER uses the completed world framebuffer, without replacing Sodium chunk shaders.","If shader loading fails, native fog remains available.").translation("thaumcraft.configuration.effects.guardianFogMode").defineEnum("guardianFogMode",GuardianFogMode.VANILLA);
        GOLEM_LINK_QUALITY=builder.comment("TC4 golem marker ribbon segments per block; values below 4 hide the animated lines while retaining markers.").translation("thaumcraft.configuration.effects.golemLinkQuality").defineInRange("golemLinkQuality",16,0,64);
        builder.pop();builder.push("interface");SHOW_TAGS=builder.comment("Show scanned item aspects without Shift; Shift inverts this choice, as in TC4.").translation("thaumcraft.configuration.interface.showTags").define("showTags",false);WAND_DIAL_BOTTOM=builder.comment("Place the original wand dial at the bottom left instead of the top left.").translation("thaumcraft.configuration.interface.wandDialBottom").define("wandDialBottom",false);
        FIT_LARGE_SCREENS=builder.comment("Reduce oversized Thaumcraft menus to fit the current window, including inventory slots.","Disable to keep TC4's original GUI scale, which can crop large menus in small windows.").translation("thaumcraft.configuration.interface.fitLargeScreens").define("fitLargeScreens",true);
        BOOK_WINDOW_MODE=builder.comment("Research tree viewport: original size, largest fitting square, or full window with margins.").translation("thaumcraft.configuration.interface.bookWindowMode").defineEnum("bookWindowMode",BookWindowMode.ORIGINAL);
        BOOK_ZOOM=builder.comment("Research tree zoom; also adjustable from inside the Thaumonomicon.").translation("thaumcraft.configuration.interface.bookZoom").defineInRange("bookZoom",1d,.5d,2d);
        BOOK_FONT_STYLE=builder.comment("Research text font: VANILLA uses the normal Minecraft font; UNICODE uses the thin Unicode style of TC4.").translation("thaumcraft.configuration.interface.bookFontStyle").defineEnum("bookFontStyle",BookFontStyle.VANILLA);
        COLORED_JAR_LABELS=builder.comment("Color the printed aspect on jar labels. False retains TC4's dark monochrome print; the liquid and goggles display remain colored.").translation("thaumcraft.configuration.interface.coloredJarLabels").define("coloredJarLabels",false);
        CREATIVE_TAB_ICON=builder.comment("Thaumcraft creative tab icon: iron-capped wooden WAND (default), THAUMONOMICON or THAUMOMETER.").translation("thaumcraft.configuration.interface.creativeTabIcon").defineEnum("creativeTabIcon",CreativeTabIcon.WAND);
        builder.pop();builder.push("models");
        ORIGINAL_ANCIENT_CRYSTALS=builder.comment("Use TC4's luminous ancient crystals. Disable to retain the subdued port shading.").translation("thaumcraft.configuration.models.originalAncientCrystals").define("originalAncientCrystals",true);
        BRIGHT_WARDED_GLASS=builder.comment("Use brighter TC4-style warded glass. Disable to retain the darker port style.").translation("thaumcraft.configuration.models.brightWardedGlass").define("brightWardedGlass",true);
        builder.pop();SPEC=builder.build();
    }
    private ThaumcraftClientConfig() {}
}
