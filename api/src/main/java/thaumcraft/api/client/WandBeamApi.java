package thaumcraft.api.client;

import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Client-only, fresh TC4 wand beam. The renderer owns its source geometry and eight-tick fade. */
public final class WandBeamApi {
    public record Beam(Vec3 target,int texture,int color,boolean reverse,float endWidth){
        public Beam{
            Objects.requireNonNull(target);
            if(!Double.isFinite(target.x)||!Double.isFinite(target.y)||!Double.isFinite(target.z)
                    ||texture<0||texture>3||!Float.isFinite(endWidth)||endWidth<0)
                throw new IllegalArgumentException("Invalid wand beam");
        }
    }
    private static volatile BiConsumer<Player,Beam> access=(player,beam)->{};
    public static void install(BiConsumer<Player,Beam> implementation){access=Objects.requireNonNull(implementation);}
    public static void emit(Player source,Beam beam){
        Objects.requireNonNull(source);Objects.requireNonNull(beam);
        if(source.level().isClientSide())access.accept(source,beam);
    }
    private WandBeamApi(){}
}
