package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.production.AdvancedAlchemyBlockEntity;

/** Original named OBJ pieces and liquid-gauge transforms, using the native atlas. */
public final class AdvancedAlchemyRenderer implements BlockEntityRenderer<AdvancedAlchemyBlockEntity> {
    public AdvancedAlchemyRenderer(BlockEntityRendererProvider.Context context){}
    private static ResourceLocation model(String name){return ResourceLocation.parse("thaumcraft:textures/models/"+name+".png");}
    private static TextureAtlasSprite sprite(String name){return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.parse(name));}
    @Override public void render(AdvancedAlchemyBlockEntity tile,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.XN.rotationDegrees(90));var mesh=OriginalObjMesh.get("adv_alch_furnace");mesh.render("Base",p,buffers,model(tile.heat>100?"alch_furnace_on":"alch_furnace"),light,overlay,-1,false);
        for(int a=0;a<4;a++){p.pushPose();p.mulPose(Axis.ZP.rotationDegrees(90*a));mesh.render("Tank",p,buffers,model(tile.store.amount()>0?"alch_furnace_tank_on":"alch_furnace_tank"),light,overlay,-1,false);p.popPose();}
        var metal=sprite("thaumcraft:blocks/metalbase");var goo=sprite("thaumcraft:blocks/fluxgoo");
        if(tile.store.amount()>0){p.pushPose();p.translate(.5,-.5,1.1);p.mulPose(Axis.YP.rotationDegrees(180));quad(p,buffers,goo,190,0);p.popPose();float f=1-tile.store.amount()/500f;
            for(int a=0;a<4;a++){p.pushPose();p.mulPose(Axis.ZP.rotationDegrees(90*a));p.mulPose(Axis.XN.rotationDegrees(90));p.translate(.85,-1.8,-1.4);p.scale(.3f,.6f,1);quad(p,buffers,metal,150,0);p.translate(0,0,-.01);quad(p,buffers,goo,190,f);p.popPose();
                p.pushPose();p.mulPose(Axis.ZN.rotationDegrees(90*a));p.mulPose(Axis.XP.rotationDegrees(90));p.translate(1.15,1.8,-1.4);p.scale(-.3f,-.6f,-1);quad(p,buffers,metal,150,0);p.translate(0,0,.01);quad(p,buffers,goo,190,f);p.popPose();}}
        if(tile.heat>100){p.pushPose();p.translate(0,0,1);for(int a=0;a<4;a++){p.pushPose();p.mulPose(Axis.ZP.rotationDegrees(90*a));p.mulPose(Axis.XP.rotationDegrees(135));p.mulPose(Axis.ZP.rotationDegrees(180));p.translate(-.5,0,-1);quad(p,buffers,sprite("minecraft:block/lava_still"),220,1-Math.min(1,tile.heat/500f));p.translate(0,0,.05);quad(p,buffers,metal,150,0);p.popPose();}p.popPose();}p.popPose();
    }
    private static void quad(PoseStack p,MultiBufferSource b,TextureAtlasSprite s,int light,float bottom){var v=b.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));vertex(v,p,0,1,s.getU1(),s.getV1(),light);vertex(v,p,1,1,s.getU0(),s.getV1(),light);vertex(v,p,1,bottom,s.getU0(),s.getV0(),light);vertex(v,p,0,bottom,s.getU1(),s.getV0(),light);}
    private static void vertex(VertexConsumer v,PoseStack p,float x,float y,float u,float t,int light){v.addVertex(p.last(),x,y,0).setColor(-1).setUv(u,t).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p.last(),0,0,1);}
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(AdvancedAlchemyBlockEntity tile){var p=tile.getBlockPos();return new net.minecraft.world.phys.AABB(p.getX()-1,p.getY(),p.getZ()-1,p.getX()+2,p.getY()+2,p.getZ()+2);}
    @Override public boolean shouldRenderOffScreen(AdvancedAlchemyBlockEntity tile){return true;}
    static void bubbles(net.minecraft.world.level.Level l,net.minecraft.core.BlockPos p){var r=l.random;CleansingParticles.bubble(p.getX()+r.nextFloat(),p.getY()+1,p.getZ()+r.nextFloat(),.06f+r.nextFloat()*.06f,.6f-r.nextFloat()*.2f,0,.6f+r.nextFloat()*.2f,.8f);CleansingParticles.bubble(p.getX()+r.nextInt(2)*2-.6+r.nextFloat()*.2,p.getY()+2,p.getZ()+r.nextInt(2)*2-.6+r.nextFloat()*.2,.06f+r.nextFloat()*.06f,.6f-r.nextFloat()*.2f,0,.6f+r.nextFloat()*.2f,.8f);if(r.nextInt(50)==0)l.playLocalSound(p,net.minecraft.sounds.SoundEvents.LAVA_POP,net.minecraft.sounds.SoundSource.BLOCKS,.1f+r.nextFloat()*.1f,.9f+r.nextFloat()*.15f,false);}
}
