package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import thaumcraft.production.*;

/** TC4 jar fill height, aspect colour and optional paper label, on the synced tile state. */
public final class EssentiaTransportRenderer implements BlockEntityRenderer<TransportBlockEntity> {
 private static final ResourceLocation PAPER=ResourceLocation.fromNamespaceAndPath("thaumcraft","textures/models/label.png");
 private static final ResourceLocation LIQUID=ResourceLocation.fromNamespaceAndPath("thaumcraft","blocks/animatedglow");
 private final CentrifugeRenderer centrifuge=new CentrifugeRenderer();
 public EssentiaTransportRenderer(BlockEntityRendererProvider.Context context){}
 @Override public void render(TransportBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  if(tile.kind==ProductionBlock.Kind.CENTRIFUGE){centrifuge.draw(pose,buffers,light,overlay,tile.processing==null?0:(tile.getLevel().getGameTime()+partial)*5);return;}
  if(tile.kind==ProductionBlock.Kind.ALEMBIC||tile.kind==ProductionBlock.Kind.CRYSTALLIZER||tile.kind==ProductionBlock.Kind.RESERVOIR){var below=tile.getLevel().getBlockState(tile.getBlockPos().below()).getBlock();int support=below==thaumcraft.registry.R3Content.ALCHEMY_FURNACE.get()?1:below==thaumcraft.registry.R3Content.ALEMBIC.get()?2:0;TransportDeviceGeometry.draw(tile.kind,tile.getBlockState().getValue(ProductionBlock.FACING),pose,buffers,light,overlay,tile.processing==null?0:(tile.getLevel().getGameTime()+partial)*5,support,false);return;}
  if(tile.kind!=ProductionBlock.Kind.JAR&&tile.kind!=ProductionBlock.Kind.VOID_JAR)return;
  var aspect=tile.store.first();int amount=tile.store.amount();
  if(aspect!=null&&amount>0){var sprite=Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(LIQUID);float bottom=.0625f,top=bottom+amount/64f*.625f;int bright=LightTexture.pack(Math.max(12,LightTexture.block(light)),LightTexture.sky(light));var out=buffers.getBuffer(RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS));int colour=0xff000000|aspect.getColor();float a=.25f,b=.75f,u0=sprite.getU(a),u1=sprite.getU(b),v0=sprite.getV(bottom),v1=sprite.getV(top);var p=pose.last();
   quad(out,p,new float[][]{{a,bottom,a},{a,top,a},{b,top,a},{b,bottom,a}},u0,v0,u1,v1,colour,bright,0,0,-1);
   quad(out,p,new float[][]{{b,bottom,b},{b,top,b},{a,top,b},{a,bottom,b}},u0,v0,u1,v1,colour,bright,0,0,1);
   quad(out,p,new float[][]{{a,bottom,b},{a,top,b},{a,top,a},{a,bottom,a}},u0,v0,u1,v1,colour,bright,-1,0,0);
   quad(out,p,new float[][]{{b,bottom,a},{b,top,a},{b,top,b},{b,bottom,b}},u0,v0,u1,v1,colour,bright,1,0,0);
   quad(out,p,new float[][]{{a,top,a},{a,top,b},{b,top,b},{b,top,a}},u0,sprite.getV(a),u1,sprite.getV(b),colour,bright,0,1,0);
   quad(out,p,new float[][]{{a,bottom,b},{a,bottom,a},{b,bottom,a},{b,bottom,b}},u0,sprite.getV(a),u1,sprite.getV(b),colour,bright,0,-1,0);
  }
  var filter=tile.store.filter();if(filter==null)return;var face=tile.getBlockState().getValue(ProductionBlock.FACING);pose.pushPose();pose.translate(.5,.4,.5);float angle=switch(face){case SOUTH->0;case WEST->270;case EAST->90;default->180;};pose.mulPose(Axis.YP.rotationDegrees(angle));
  var paper=buffers.getBuffer(RenderType.entityTranslucent(PAPER));quad(paper,pose.last(),new float[][]{{-.25f,-.25f,.315f},{-.25f,.25f,.315f},{.25f,.25f,.315f},{.25f,-.25f,.315f}},0,1,1,0,-1,light,0,0,1);
  int print=thaumcraft.config.ThaumcraftClientConfig.COLORED_JAR_LABELS.get()?0xff000000|filter.getColor():0xcc1a1a1a;
  var icon=buffers.getBuffer(RenderType.entityTranslucent(ResourceLocation.parse(filter.texture())));quad(icon,pose.last(),new float[][]{{-.168f,-.168f,.316f},{-.168f,.168f,.316f},{.168f,.168f,.316f},{.168f,-.168f,.316f}},0,1,1,0,print,light,0,0,1);pose.popPose();
 }
 private static void quad(VertexConsumer out,PoseStack.Pose p,float[][] xyz,float u0,float v0,float u1,float v1,int colour,int light,float nx,float ny,float nz){float[][] uv={{u0,v0},{u0,v1},{u1,v1},{u1,v0}};for(int i=0;i<4;i++)out.addVertex(p,xyz[i][0],xyz[i][1],xyz[i][2]).setColor(colour).setUv(uv[i][0],uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,nx,ny,nz);}
}
