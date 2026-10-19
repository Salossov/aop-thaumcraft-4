package thaumcraft.blocks;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.api.ThaumcraftApi;

/** ConfigBlocks' original blacklist, with the old eldritch metadata family split into native IDs. */
public final class PortableHoleBlacklist {
    public static void initialize(){
        var list=ThaumcraftApi.portableHoleBlackList;
        for(var block:List.of(Blocks.RED_BED,Blocks.PISTON,Blocks.STICKY_PISTON,Blocks.PISTON_HEAD,Blocks.MOVING_PISTON,Blocks.OAK_DOOR,Blocks.IRON_DOOR))if(!list.contains(block))list.add(block);
        for(String path:List.of("eldritch_altar","eldritch_obelisk","eldritch_cap","carved_eldritch_stone","ancient_runic_stone","decorated_eldritch_stone","eldritch_door_stone","eldritch_lock","eldritch_crab_spawner","eldritch_trap","eldritch_portal","flux_goo","flux_gas","infernal_furnace")){
            var block=BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath("thaumcraft",path)).orElseThrow();
            if(!list.contains(block))list.add(block);
        }
    }
    private PortableHoleBlacklist(){}
}
