package thaumcraft.api.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.research.ResearchDefinition;

/** Client-only book descriptions, without changing research definitions or saved data. */
public final class ResearchTextFormatters {
    private static final Map<String, Function<ResearchDefinition, Component>> VALUES = new HashMap<>();

    /** Register once during client setup. Only research in this namespace is affected. */
    public static synchronized void register(String namespace, Function<ResearchDefinition, Component> formatter) {
        ResourceLocation.fromNamespaceAndPath(namespace, "research");
        Objects.requireNonNull(formatter);
        if (VALUES.putIfAbsent(namespace, formatter) != null)
            throw new IllegalArgumentException("Duplicate research text formatter " + namespace);
    }

    /** Called when drawing the book so configuration and language changes take effect immediately. */
    public static Component description(ResearchDefinition research) {
        int separator = research.key().indexOf(':');
        var formatter = separator < 0 ? null : VALUES.get(research.key().substring(0, separator));
        return formatter == null ? Component.translatable("tc.research_text." + research.key())
                : Objects.requireNonNull(formatter.apply(research));
    }

    private ResearchTextFormatters() {}
}
