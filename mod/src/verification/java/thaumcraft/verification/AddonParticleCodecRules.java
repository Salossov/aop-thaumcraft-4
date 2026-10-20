package thaumcraft.verification;

import java.util.Random;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.client.ParticleApi;
import thaumcraft.network.ParticleEffectPayload;
import thaumcraft.network.SmokeSpiralPayload;

/** Actual wire codec only: no Minecraft bootstrap, world, server or graphical client. */
public final class AddonParticleCodecRules {
    public static void main(String[] args) {
        var random=new Random(164);int checks=0;
        for(int i=0;i<10000;i++){
            var effect=new ParticleApi.Effect(ParticleApi.Kind.values()[i%ParticleApi.Kind.values().length],
                    random.nextDouble()*60_000_000-30_000_000,random.nextDouble()*1024-64,
                    random.nextDouble()*60_000_000-30_000_000,random.nextFloat()*4,i%9-1,
                    random.nextFloat(),random.nextFloat(),random.nextFloat(),i%2==0,i%3==0,random.nextFloat()*2-1);
            var original=new ParticleEffectPayload(ResourceLocation.parse(i%2==0?"minecraft:overworld":"probe:other_world"),effect);
            var buffer=new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),RegistryAccess.EMPTY);
            try{
                ParticleEffectPayload.CODEC.encode(buffer,original);
                var decoded=ParticleEffectPayload.CODEC.decode(buffer);
                if(!original.equals(decoded))throw new AssertionError("Particle field/level round trip "+i);
                if(buffer.readableBytes()!=0)throw new AssertionError("Unconsumed wire fields "+i);
                checks+=2;
            }finally{buffer.release();}
        }
        for(int index=0;index<10000;index++){
            var effect=new ParticleApi.SmokeSpiral(random.nextDouble()*60_000_000-30_000_000,random.nextDouble()*1024-64,
                    random.nextDouble()*60_000_000-30_000_000,random.nextFloat()*8,random.nextInt(),random.nextInt(),random.nextInt());
            var original=new SmokeSpiralPayload(ResourceLocation.parse(index%2==0?"minecraft:overworld":"probe:other_world"),effect);
            var buffer=new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),RegistryAccess.EMPTY);
            try{
                SmokeSpiralPayload.CODEC.encode(buffer,original);var decoded=SmokeSpiralPayload.CODEC.decode(buffer);
                if(!original.equals(decoded))throw new AssertionError("Smoke field/level round trip "+index);
                if(buffer.readableBytes()!=0)throw new AssertionError("Unconsumed smoke wire fields "+index);
                checks+=2;
            }finally{buffer.release();}
        }
        System.out.println("TC_ADDON_PARTICLE_CODEC_PASS checks="+checks+" game_started=false bootstrap_replaced=false");
    }
    private AddonParticleCodecRules(){}
}
