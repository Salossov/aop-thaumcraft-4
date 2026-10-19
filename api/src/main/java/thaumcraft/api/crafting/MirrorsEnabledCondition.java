package thaumcraft.api.crafting;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import thaumcraft.api.world.ContentRules;

/** JSON recipe condition: {"type":"thaumcraft:mirrors_enabled"}. */
public final class MirrorsEnabledCondition implements ICondition {
    public static final MirrorsEnabledCondition INSTANCE = new MirrorsEnabledCondition();
    public static final MapCodec<MirrorsEnabledCondition> CODEC = MapCodec.unit(INSTANCE);
    @Override public boolean test(IContext context) { return ContentRules.mirrorsEnabled(); }
    @Override public MapCodec<? extends ICondition> codec() { return CODEC; }
    private MirrorsEnabledCondition() { }
}
