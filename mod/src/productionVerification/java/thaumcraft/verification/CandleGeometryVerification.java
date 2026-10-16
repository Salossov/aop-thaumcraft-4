package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SoundType;
import thaumcraft.blocks.TallowCandleGeometry;

/** Native server math/palette/sound versus the retained original renderer controls. */
final class CandleGeometryVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));var reference=JsonParser.parseString(Files.readString(root.resolve("analysis/full_port/original-candle-geometry.json"))).getAsJsonObject();
        check.accept(reference.getAsJsonArray("world").size()==256,"256 unchanged original coordinate-seeded candle renderer controls");
        for(var value:reference.getAsJsonArray("world")){
            var row=value.getAsJsonObject();var xyz=row.getAsJsonArray("pos");var pos=new BlockPos(xyz.get(0).getAsInt(),xyz.get(1).getAsInt(),xyz.get(2).getAsInt());
            check.accept(matches(TallowCandleGeometry.world(pos),row.getAsJsonArray("boxes")),"native candle body/drips/wick equal actual original renderer at "+pos);
        }
        for(var value:reference.getAsJsonArray("inventory")){
            var row=value.getAsJsonObject();int meta=row.get("metadata").getAsInt();var candle=thaumcraft.registry.R4Content.CANDLES.get(meta).get();
            check.accept(matches(TallowCandleGeometry.inventory(),row.getAsJsonArray("boxes")),"original inventory candle has body and white wick, without world drips: "+meta);
            check.accept(candle.color==row.get("color").getAsInt(),"original metadata palette retained: "+meta);
            check.accept(candle.defaultBlockState().getSoundType()==SoundType.WOOL,"original field149775_l cloth sound maps to native wool: "+meta);
        }
    }
    static boolean matches(List<TallowCandleGeometry.Box> actual,JsonArray expected){
        if(actual.size()!=expected.size())return false;
        for(int i=0;i<actual.size();i++){
            var b=actual.get(i);var row=expected.get(i).getAsJsonArray();var values=new float[]{b.x0(),b.y0(),b.z0(),b.x1(),b.y1(),b.z1(),b.wick()?1:0};
            for(int n=0;n<7;n++)if(Math.abs(values[n]-row.get(n).getAsFloat())>1e-7)return false;
        }
        return true;
    }
    private CandleGeometryVerification(){}
}
