package thaumcraft.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import thaumcraft.api.client.ParticleApi;

/** TT's original Particle extends FXSparkle: frame 1, multiplier 1, no shrink or slowdown. */
final class ShadowSparkleParticle extends TextureSheetParticle {
    private final float size;

    ShadowSparkleParticle(ClientLevel level, ParticleApi.Effect effect) {
        super(level,effect.x(),effect.y(),effect.z());
        size=(random.nextFloat()*.5f+.5f)*2*effect.size();
        lifetime=5;
        xd=yd=zd=0;
        gravity=effect.gravity();
        alpha=1;
        hasPhysics=false;
        setSize(.01f,.01f);
        setColor(effect.red(),effect.green(),effect.blue());
    }

    @Override public ParticleRenderType getRenderType() { return LegacyParticles.TRAIL; }
    @Override protected int getLightColor(float partialTick) { return 240; }
    @Override public float getQuadSize(float partialTick) { return .1f*size; }
    @Override protected float getU0() { return ((1+age)%4)/16f; }
    @Override protected float getU1() { return getU0()+.0624375f; }
    @Override protected float getV0() { return .25f; }
    @Override protected float getV1() { return getV0()+.0624375f; }

    @Override public void tick() {
        xo=x;yo=y;zo=z;
        if(age++>=lifetime)remove();
        yd-=.04*(double)gravity;
        setPos(x+xd,y+yd,z+zd);
    }
}
