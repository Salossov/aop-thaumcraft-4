package thaumcraft.client;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import thaumcraft.entities.Cultist;
import thaumcraft.network.ChampionShieldPayload;
/** Source directional hemispheres; normal monster shields use ripple frames. */
public final class ChampionShieldEffects {
    private static ParticleRenderType type(String atlas){return(tessellator,textures)->{RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture("models/"+atlas));return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);};}
    private static final ParticleRenderType RIPPLE=type("sonic_frames"),RUNES=type("hemis_frames");
    public static void receive(ChampionShieldPayload packet){var level=Minecraft.getInstance().level;if(level==null)return;var source=level.getEntity(packet.source());if(source==null)return;if(packet.target()>=0){var target=level.getEntity(packet.target());float yaw=0,pitch=90;if(target!=null){double dx=source.getX()-target.getX(),dy=source.getBoundingBox().getCenter().y-target.getBoundingBox().getCenter().y,dz=source.getZ()-target.getZ();yaw=(float)(Math.atan2(dz,dx)*180/Math.PI)-90;pitch=(float)(-Math.atan2(dy,net.minecraft.util.Mth.sqrt((float)(dx*dx+dz*dz)))*180/Math.PI);}add(source,yaw,pitch);}else if(packet.target()==-1){add(source,0,90);add(source,0,270);}else if(packet.target()==-2)add(source,0,270);else if(packet.target()==-3)add(source,0,90);}
    private static void add(Entity target,float yaw,float pitch){Minecraft.getInstance().particleEngine.add(new Shield(target,yaw,pitch));}
    private static final class Shield extends Particle {
        private final Entity target;private final float yaw,pitch;private final boolean ripple;
        Shield(Entity target,float yaw,float pitch){super(Minecraft.getInstance().level,target.getX(),target.getBoundingBox().getCenter().y,target.getZ());this.target=target;this.yaw=yaw;this.pitch=pitch;ripple=target instanceof Monster&&!(target instanceof Cultist);lifetime=8+random.nextInt(4);hasPhysics=false;}
        @Override public ParticleRenderType getRenderType(){return ripple?RIPPLE:RUNES;}
        @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime)remove();setPos(target.getX(),target.getBoundingBox().getCenter().y,target.getZ());}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){float fade=(age+partial)/lifetime;var pose=new PoseStack();var view=camera.getPosition();pose.translate(xo+(x-xo)*partial-view.x,yo+(y-yo)*partial-view.y,zo+(z-zo)*partial-view.z);pose.mulPose(Axis.YP.rotationDegrees(180-yaw));pose.mulPose(Axis.XP.rotationDegrees(-pitch));float scale=(float)(.4*target.getBbHeight());pose.scale(scale,scale,scale);float grey=ripple?.5f:1,opacity=Math.min(1,(1-fade)*3);var color=new VertexConsumer(){public VertexConsumer addVertex(float x,float y,float z){vertices.addVertex(x,y,z);return this;}public VertexConsumer setColor(int r,int g,int b,int a){vertices.setColor(grey,grey,grey,opacity);return this;}public VertexConsumer setUv(float u,float v){vertices.setUv(u,v);return this;}public VertexConsumer setUv1(int u,int v){vertices.setUv1(u,v);return this;}public VertexConsumer setUv2(int u,int v){vertices.setUv2(u,v);return this;}public VertexConsumer setNormal(float x,float y,float z){vertices.setNormal(x,y,z);return this;}};OriginalObjMesh.get("hemis").renderParticles(pose,color,Math.min(15,(int)(14*fade)+1),220);}
    }
    private ChampionShieldEffects(){}
}
