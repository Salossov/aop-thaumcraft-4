package thaumcraft.api.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Original material identities and tool requirement; extend through data-pack block tags. */
public final class BlockMaterials {
    public static final TagKey<Block> AIR=tag("air");
    public static final TagKey<Block> GRASS=tag("grass");
    public static final TagKey<Block> GROUND=tag("ground");
    public static final TagKey<Block> WOOD=tag("wood");
    public static final TagKey<Block> ROCK=tag("rock");
    public static final TagKey<Block> IRON=tag("iron");
    public static final TagKey<Block> ANVIL=tag("anvil");
    public static final TagKey<Block> WATER=tag("water");
    public static final TagKey<Block> LAVA=tag("lava");
    public static final TagKey<Block> LEAVES=tag("leaves");
    public static final TagKey<Block> PLANTS=tag("plants");
    public static final TagKey<Block> VINE=tag("vine");
    public static final TagKey<Block> SPONGE=tag("sponge");
    public static final TagKey<Block> CLOTH=tag("cloth");
    public static final TagKey<Block> FIRE=tag("fire");
    public static final TagKey<Block> SAND=tag("sand");
    public static final TagKey<Block> CIRCUITS=tag("circuits");
    public static final TagKey<Block> CARPET=tag("carpet");
    public static final TagKey<Block> GLASS=tag("glass");
    public static final TagKey<Block> REDSTONE_LIGHT=tag("redstone_light");
    public static final TagKey<Block> TNT=tag("tnt");
    public static final TagKey<Block> CORAL=tag("coral");
    public static final TagKey<Block> ICE=tag("ice");
    public static final TagKey<Block> PACKED_ICE=tag("packed_ice");
    public static final TagKey<Block> SNOW=tag("snow");
    public static final TagKey<Block> CRAFTED_SNOW=tag("crafted_snow");
    public static final TagKey<Block> CACTUS=tag("cactus");
    public static final TagKey<Block> CLAY=tag("clay");
    public static final TagKey<Block> GOURD=tag("gourd");
    public static final TagKey<Block> DRAGON_EGG=tag("dragon_egg");
    public static final TagKey<Block> PORTAL=tag("portal");
    public static final TagKey<Block> CAKE=tag("cake");
    public static final TagKey<Block> WEB=tag("web");
    public static final TagKey<Block> PISTON=tag("piston");
    public static final TagKey<Block> TOOL_NOT_REQUIRED=tag("tool_not_required");
    private static TagKey<Block> tag(String name){return TagKey.create(Registries.BLOCK,ResourceLocation.fromNamespaceAndPath("thaumcraft","materials/"+name));}
    private BlockMaterials(){}
}
