package thaumcraft.blocks;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** TC4 BlockCosmeticSolid's coordinate-dependent textures, including Java int overflow. */
public final class EldritchStoneTextures {
    public static int stone(BlockPos pos,Direction face){
        return stone(pos,face.get3DDataValue());
    }
    public static int stairs(BlockPos pos,Direction face){return stone(pos,face.get3DDataValue()+100);}
    private static int stone(BlockPos pos,int side){
        String key=""+pos.getX()+pos.getY()+pos.getZ();
        return new Random(Math.abs(key.hashCode()*100)+1).nextInt(12345+side)%4;
    }
    public static int rock(BlockPos pos,Direction face){
        int a,b;
        switch(face){
            case UP,DOWN -> {a=pos.getX();b=pos.getZ();}
            case NORTH,SOUTH -> {a=pos.getX();b=pos.getY();}
            default -> {a=pos.getZ();b=pos.getY();}
        }
        return Math.abs(a%2)+Math.abs(b%2)*2;
    }
    private EldritchStoneTextures(){}
}
