package thaumcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import thaumcraft.blocks.*;
import thaumcraft.registry.R5Content;

/** Original ascending scan and random horizontal relocation, never a surface shortcut. */
public final class LegacyManaPods {
    public static boolean generate(WorldGenLevel level,RandomSource random,int x,int y,int z){
        int originX=x,originZ=z;
        while(y<Math.min(128,LegacyNaturalPopulation.surface(level,x,z))){
            var pos=new BlockPos(x,y,z);
            if(level.isEmptyBlock(pos)&&level.isEmptyBlock(pos.below())){
                var state=R5Content.MANA_POD.get().defaultBlockState();
                if(state.canSurvive(level,pos)){level.setBlock(pos,state.setValue(ManaPodBlock.AGE,2+random.nextInt(5)),2);if(level.getBlockEntity(pos) instanceof ManaPodBlockEntity tile)tile.checkGrowth();break;}
            }else{x=originX+random.nextInt(4)-random.nextInt(4);z=originZ+random.nextInt(4)-random.nextInt(4);}
            y++;
        }
        return true;
    }
    private LegacyManaPods(){}
}
