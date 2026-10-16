package thaumcraft.client;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.production.InfusionMatrixBlockEntity;
/** Original eight cubes, startup rotations, instability jitter and twenty halo rays. */
public final class InfusionMatrixRenderer implements BlockEntityRenderer<InfusionMatrixBlockEntity>{
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft","infusion_matrix"),"main");
 private static final ResourceLocation TEXTURE=ResearchGraphics.texture("models/infuser");
 private final ModelPart cube,glow;
 public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context){var model=context.bakeLayer(LAYER);cube=model.getChild("cube");glow=model.getChild("glow");}
 public static LayerDefinition layer(){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("cube",CubeListBuilder.create().texOffs(0,0).addBox(-8,-8,-8,16,16,16),PartPose.ZERO);mesh.getRoot().addOrReplaceChild("glow",CubeListBuilder.create().texOffs(0,32).addBox(-8,-8,-8,16,16,16),PartPose.ZERO);return LayerDefinition.create(mesh,64,64);}
 @Override public void render(InfusionMatrixBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  InfusionEffects.tick(tile);
  float ticks=(Minecraft.getInstance().player==null?0:Minecraft.getInstance().player.tickCount)+partial,start=tile.clientStartup;
  float instability=Math.min(6,1+tile.instability*.66f*Math.min(tile.clientCraftTicks,50)/50f);
  pose.pushPose();pose.translate(.5,.5,.5);pose.mulPose(Axis.YP.rotationDegrees(ticks%360*start));pose.mulPose(Axis.XP.rotationDegrees(35*start));pose.mulPose(Axis.ZP.rotationDegrees(45*start));
  for(int a=0;a<2;a++)for(int b=0;b<2;b++)for(int c=0;c<2;c++){
   pose.pushPose();double x=tile.active?Math.sin((ticks+a*10)/(15-instability/2))*.01*start*instability:0,y=tile.active?Math.sin((ticks+b*10)/(14-instability/2))*.01*start*instability:0,z=tile.active?Math.sin((ticks+c*10)/(13-instability/2))*.01*start*instability:0;
   pose.translate(x+(a==0?-.25:.25),y+(b==0?-.25:.25),z+(c==0?-.25:.25));if(a>0)pose.mulPose(Axis.XP.rotationDegrees(90));if(b>0)pose.mulPose(Axis.YP.rotationDegrees(90));if(c>0)pose.mulPose(Axis.ZP.rotationDegrees(90));pose.scale(.45f,.45f,.45f);
   cube.render(pose,buffers.getBuffer(RenderType.entityCutout(TEXTURE)),light,overlay);
   if(tile.active){int alpha=Math.clamp((int)((Math.sin((ticks+a*2+b*3+c*4)/4)*.1+.2)*start*255),0,255);glow.render(pose,buffers.getBuffer(ThaumcraftRenderTypes.glow(TEXTURE)),LightTexture.FULL_BRIGHT,overlay,(alpha<<24)|0xcc19ff);}pose.popPose();
  }pose.popPose();if(tile.clientCraftTicks>0)halo(tile,partial,pose,buffers);
 }
 private void halo(InfusionMatrixBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers){
  pose.pushPose();pose.translate(.5,.5,.5);var random=new Random(245);float growth=Math.min(tile.clientCraftTicks,50)/50f;
  var buffer=buffers.getBuffer(ThaumcraftRenderTypes.infusionHalo());
  for(int i=0;i<20;i++){
   pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat()*360));pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat()*360));pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat()*360));pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat()*360));pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat()*360));pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat()*360+(tile.clientCraftTicks+partial)/500f*360));
   float length=(random.nextFloat()*20+5)*growth/20,width=(random.nextFloat()*2+1)*growth/20;var p=pose.last();
   float[][] vertices={{-.866f*width,length,-.5f*width},{.866f*width,length,-.5f*width},{0,length,width}};
   for(int n=0;n<3;n++){buffer.addVertex(p,0,0,0).setColor(0xe6ffffff);var v=vertices[n];buffer.addVertex(p,v[0],v[1],v[2]).setColor(0x00cc00ff);v=vertices[(n+1)%3];buffer.addVertex(p,v[0],v[1],v[2]).setColor(0x00cc00ff);}
  }pose.popPose();
 }
 @Override public int getViewDistance(){return 128;}
}
