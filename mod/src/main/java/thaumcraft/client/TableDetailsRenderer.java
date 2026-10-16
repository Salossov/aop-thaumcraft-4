package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import thaumcraft.blocks.*;
import thaumcraft.items.*;
import thaumcraft.registry.R2Content;

/** Original TC4 decorations; table bodies remain ordinary chunk models. */
public final class TableDetailsRenderer implements BlockEntityRenderer<TableBlockEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft","research_table_details"),"main");
    public static final net.minecraft.client.resources.model.ModelResourceLocation QUILL=net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("thaumcraft","item/table_quill"));
    private final ModelPart ink,scroll,ribbon;
    public TableDetailsRenderer(BlockEntityRendererProvider.Context context){this(context.bakeLayer(LAYER));}
    TableDetailsRenderer(ModelPart model){ink=model.getChild("ink");scroll=model.getChild("scroll");ribbon=model.getChild("ribbon");}
    public static LayerDefinition layer(){var mesh=new MeshDefinition();var parts=mesh.getRoot();
        parts.addOrReplaceChild("ink",CubeListBuilder.create().texOffs(0,44).addBox(0,0,0,3,2,3),PartPose.offset(-6,-2,3));
        parts.addOrReplaceChild("scroll",CubeListBuilder.create().texOffs(0,0).addBox(-21,-0.5f,-8,8,2,2),PartPose.offsetAndRotation(-2,-2,2,0,10,0));
        parts.addOrReplaceChild("ribbon",CubeListBuilder.create().texOffs(0,4).addBox(-15.1f,-0.275f,-6.75f,1,2,2),PartPose.offsetAndRotation(-2,-2,2,0,10,0));
        return LayerDefinition.create(mesh,128,64);
    }
    @Override public void render(TableBlockEntity table,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var state=table.getBlockState();var mc=Minecraft.getInstance();
        if(state.is(R2Content.ARCANE_WORKBENCH.get())){
            var wand=table.getItem(10);if(wand.getItem() instanceof CastingWandItem){pose.pushPose();pose.translate(0.65,1.0625,0.25);pose.mulPose(Axis.XP.rotationDegrees(90));pose.mulPose(Axis.ZP.rotationDegrees(20));pose.scale(0.5f,0.5f,0.5f);mc.getItemRenderer().renderStatic(wand,ItemDisplayContext.NONE,light,overlay,pose,buffers,table.getLevel(),0);pose.popPose();}return;
        }
        if(!state.is(R2Content.RESEARCH_TABLE.get())||state.getValue(TableBlock.SLAVE))return;
        pose.pushPose();pose.translate(0.5,1,0.5);
        float turn=switch(state.getValue(TableBlock.FACING)){case NORTH->90;case SOUTH->270;case WEST->180;default->0;};pose.mulPose(Axis.YP.rotationDegrees(turn));pose.mulPose(Axis.XP.rotationDegrees(180));
        if(table.getItem(0).getItem() instanceof ScribingToolsItem){
            ink.render(pose,buffers.getBuffer(RenderType.entityCutout(ResearchGraphics.texture("models/restable"))),light,overlay);
            pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(-90));pose.mulPose(Axis.XP.rotationDegrees(180));pose.translate(-0.17,0.1,-0.15);pose.mulPose(Axis.YP.rotationDegrees(15));pose.scale(0.5f,0.5f,0.5f);
            // The legacy quad maps maxU to its left edge, opposite a generated item.
            pose.translate(1,0,0);pose.mulPose(Axis.YP.rotationDegrees(180));
            // Restore the legacy quad's 0..1 X/Y coordinates; its depth stays centered.
            pose.translate(0.5,0.5,0);
            mc.getItemRenderer().render(new ItemStack(Items.FEATHER),ItemDisplayContext.NONE,false,pose,buffers,light,overlay,mc.getModelManager().getModel(QUILL));pose.popPose();
        }
        for(int i=0;i<6;i++){
            pose.pushPose();pose.translate(0.1,-0.01-i*0.015,0.35);pose.mulPose(Axis.XP.rotationDegrees(-90));pose.mulPose(Axis.ZP.rotationDegrees(15+i%3*2));pose.scale(0.5f,0.6f,0.6f);
            var vertex=buffers.getBuffer(RenderType.entityCutoutNoCull(ResearchGraphics.texture("misc/parchment")));var p=pose.last();
            vertex.addVertex(p,0,1,0).setColor(0xffffffff).setUv(0,1).setOverlay(overlay).setLight(light).setNormal(p,0,0,-1);
            vertex.addVertex(p,1,1,0).setColor(0xffffffff).setUv(1,1).setOverlay(overlay).setLight(light).setNormal(p,0,0,-1);
            vertex.addVertex(p,1,0,0).setColor(0xffffffff).setUv(1,0).setOverlay(overlay).setLight(light).setNormal(p,0,0,-1);
            vertex.addVertex(p,0,0,0).setColor(0xffffffff).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(p,0,0,-1);pose.popPose();
        }
        var note=ResearchNotes.read(table.getItem(1));if(note!=null){var buffer=buffers.getBuffer(RenderType.entityCutout(ResearchGraphics.texture("models/restable2")));scroll.render(pose,buffer,light,overlay);pose.pushPose();pose.scale(1.2f,1.2f,1.2f);var definition=thaumcraft.network.ClientKnowledge.definitions().get(note.key);int color=definition==null?0xff999999:0xff000000|definition.aspects().keySet().stream().map(thaumcraft.api.aspects.Aspect::getAspect).filter(java.util.Objects::nonNull).mapToInt(thaumcraft.api.aspects.Aspect::getColor).findFirst().orElse(0x999999);ribbon.render(pose,buffer,light,overlay,color);pose.popPose();}
        pose.popPose();
    }
}
