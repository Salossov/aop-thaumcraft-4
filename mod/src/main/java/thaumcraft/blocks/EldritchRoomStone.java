package thaumcraft.blocks;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.*;

/** Native forms of original BlockEldritch metadata4,5,6,7. Full gameplay bounds. */
public final class EldritchRoomStone extends DropExperienceBlock {
    private final int metadata;
    public EldritchRoomStone(int metadata){super(UniformInt.of(metadata==5?1:0,metadata==5?4:0),Properties.of().strength(metadata==7?-1:metadata==6?4:2,metadata==7?Float.MAX_VALUE:metadata==6?100:30).sound(SoundType.STONE).noOcclusion().lightLevel(s->metadata==6?5:12).isValidSpawn((s,l,p,t)->false));this.metadata=metadata;}
    public int legacyMetadata(){return metadata;}
}
