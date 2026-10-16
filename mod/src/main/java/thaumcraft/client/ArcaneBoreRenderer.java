package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import thaumcraft.production.ArcaneBoreBlockEntity;

public final class ArcaneBoreRenderer implements BlockEntityRenderer<ArcaneBoreBlockEntity> {
    private final ModelPart base=OriginalDeviceGeometry.bake("bore_base"),bore=OriginalDeviceGeometry.bake("bore"),emitter=OriginalDeviceGeometry.bake("bore_emitter");
    private final ModelPart jar=jarCore();
    private static final ResourceLocation VORTEX=ResourceLocation.parse("thaumcraft:textures/misc/vortex.png"),JAR=ResourceLocation.parse("thaumcraft:textures/models/jar.png");
    // Original ModelJar.Core; the bore renders only the glass, without its lid or brine.
    private static ModelPart jarCore(){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("Core",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-5,-12,-5,10,12,10),PartPose.ZERO);return LayerDefinition.create(mesh,64,32).bakeRoot();}
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/models/bore.png"),BEAM=ResourceLocation.parse("thaumcraft:textures/misc/beam.png");
    public ArcaneBoreRenderer(BlockEntityRendererProvider.Context context){}
    private void parts(ModelPart model,PoseStack pose,VertexConsumer buffer,int light,int overlay,String... names){for(var name:names)model.getChild(name).render(pose,buffer,light,overlay);}
    private void draw(boolean isBase,Direction direction,boolean hanging,boolean focus,float spin,ArcaneBoreBlockEntity.Aim aim,float time,PoseStack pose,MultiBufferSource buffers,int light,int overlay){var buffer=buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));pose.pushPose();pose.translate(.5,isBase?0:.5,.5);
        if(isBase){parts(base,pose,buffer,light,overlay,"Base1","Base2","PillarMid","Pillar1","Pillar2","Pillar3","Pillar4");pose.mulPose(Axis.YP.rotationDegrees(switch(direction){case NORTH->90;case SOUTH->270;case WEST->180;default->0;}));parts(base,pose,buffer,light,overlay,"Nozzle1","Nozzle2");}
        else{if(aim==null)aim=ArcaneBoreBlockEntity.aimAt(Vec3.atLowerCornerOf(direction.getNormal()));float yaw=aim.yaw(),tilt=aim.pitch();pose.mulPose(Axis.YP.rotationDegrees(yaw));pose.pushPose();if(hanging)pose.mulPose(Axis.ZP.rotationDegrees(180));pose.translate(0,-.5,0);parts(bore,pose,buffer,light,overlay,"Base","Side1","Side2","NozCrossbar");pose.popPose();pose.mulPose(Axis.ZP.rotationDegrees(tilt));pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(90));pose.translate(0,-.5,0);parts(bore,pose,buffer,light,overlay,"NozFront","NozMid");pose.popPose();pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(spin));pose.translate(0,.5,0);emitter.getChild("Knob").visible=focus;emitter.render(pose,buffer,light,overlay);pose.popPose();}
        if(!isBase){
            // TileArcaneBoreRenderer: three oppositely rotating planes, then jar glass.
            for(int i=0;i<3;i++){
                pose.pushPose();pose.translate(0,-.17f-i*.04f,0);pose.mulPose(Axis.XN.rotationDegrees(90));pose.mulPose(Axis.ZP.rotationDegrees((i==1?1:-1)*(time%45)*8));pose.mulPose(Axis.YP.rotationDegrees(i==2?-10:10));
                float size=(.4f-i*.1f)/2,alpha=i==0?1:.8f;var v=buffers.getBuffer(ThaumcraftRenderTypes.unlit(VORTEX,false,false));
                v.addVertex(pose.last().pose(),-size,size,0).setUv(0,1).setColor(1,1,1,alpha);v.addVertex(pose.last().pose(),size,size,0).setUv(1,1).setColor(1,1,1,alpha);v.addVertex(pose.last().pose(),size,-size,0).setUv(1,0).setColor(1,1,1,alpha);v.addVertex(pose.last().pose(),-size,-size,0).setUv(0,0).setColor(1,1,1,alpha);pose.popPose();
            }
            pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(180));pose.translate(0,.3,0);pose.scale(.6f,.6f,.6f);jar.render(pose,buffers.getBuffer(ThaumcraftRenderTypes.translucentModel(JAR)),light,overlay);pose.popPose();
        }
        pose.popPose();
    }
    @Override public void render(ArcaneBoreBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        float ticks=tile.getLevel()==null?0:tile.getLevel().getGameTime()+partial;
        var aim=tile.aim(partial);
        draw(tile.base(),tile.facing(),tile.getBlockState().getValue(thaumcraft.production.ArcaneBoreBlock.HANGING),!tile.items.getItem(0).isEmpty(),tile.spin(partial),aim,ticks,pose,buffers,light,overlay);
        if(tile.working()){
            // Same transformed +Y axis as the original emitter: the laser leaves its tip.
            var axis=aim.forward();var centre=new Vec3(.5,.5,.5);var start=centre.add(axis.scale(1.5));
            double distance=tile.target().getCenter().distanceTo(tile.getBlockPos().getCenter());var end=centre.add(axis.scale(Math.max(1.55,distance)));
            var offset=Vec3.atLowerCornerOf(tile.getBlockPos());var hit=tile.getLevel().clip(new net.minecraft.world.level.ClipContext(start.add(offset),end.add(offset),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));
            if(hit.getType()==HitResult.Type.BLOCK)end=hit.getLocation().subtract(offset);
            beam(pose,buffers,start,end,ticks,.055f,0x00ff66);beam(pose,buffers,start,end,ticks,.026f,0xff8855);
        }
    }
    private static void beam(PoseStack pose,MultiBufferSource buffers,Vec3 start,Vec3 end,float ticks,float radius,int color){var direction=end.subtract(start).normalize();var axis=direction.cross(Math.abs(direction.y)>.9?new Vec3(1,0,0):new Vec3(0,1,0)).normalize();var second=direction.cross(axis).normalize();var buffer=buffers.getBuffer(ThaumcraftRenderTypes.unlit(BEAM,true,false));for(int i=0;i<8;i++){var a=axis.scale(Math.cos(i*Math.PI/4)*radius).add(second.scale(Math.sin(i*Math.PI/4)*radius));var b=axis.scale(Math.cos((i+1)*Math.PI/4)*radius).add(second.scale(Math.sin((i+1)*Math.PI/4)*radius));var points=new Vec3[]{start.add(a),end.add(a),end.add(b),start.add(b)};for(int v=0;v<4;v++)buffer.addVertex(pose.last(),(float)points[v].x,(float)points[v].y,(float)points[v].z).setColor(color>>16&255,color>>8&255,color&255,210).setUv(v<2?0:1,(v==1||v==2?(float)start.distanceTo(end)/2:0)-ticks*.05f);}}
    @Override public AABB getRenderBoundingBox(ArcaneBoreBlockEntity tile){return tile.target()==null?new AABB(tile.getBlockPos()).inflate(1):new AABB(tile.getBlockPos()).minmax(new AABB(tile.target()));}
    public static final class ItemRenderer extends BlockEntityWithoutLevelRenderer {private final ArcaneBoreRenderer renderer=new ArcaneBoreRenderer(null);public ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}@Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){renderer.draw(stack.is(thaumcraft.registry.R3Content.BORE_BASE_ITEM.get()),Direction.UP,false,false,0,null,0,pose,buffers,light,overlay);}}
    public static final class Extension implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions{private ItemRenderer renderer;@Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new ItemRenderer();return renderer;}}
}
