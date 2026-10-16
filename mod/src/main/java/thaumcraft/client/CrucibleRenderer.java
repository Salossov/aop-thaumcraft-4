package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import thaumcraft.production.CrucibleBlockEntity;

/** The tank is synchronized independently of the static hollow crucible model. */
public final class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
    public CrucibleRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(CrucibleBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(tile.water()<=0||tile.getLevel()==null)return;
        var sprite=Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.parse("minecraft:block/water_still"));
        int water=BiomeColors.getAverageWaterColor(tile.getLevel(),tile.getBlockPos());float contamination=tile.store.amount()/100f;if(contamination>0)contamination=.5f+contamination/2;
        int r=Math.clamp((int)((water>>16&255)*(1-contamination/3)),0,255),g=Math.clamp((int)((water>>8&255)*(1-contamination)),0,255),b=Math.clamp((int)((water&255)*(1-contamination/2)),0,255);
        float lo=.123f,hi=.877f,height=tile.fluidHeight();var out=buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));float[][] points={{lo,height,lo},{lo,height,hi},{hi,height,hi},{hi,height,lo}};
        for(int i=0;i<4;i++)out.addVertex(pose.last(),points[i][0],points[i][1],points[i][2]).setColor(r,g,b,255).setUv(sprite.getU(points[i][0]),sprite.getV(points[i][2])).setOverlay(overlay).setLight(light).setNormal(pose.last(),0,1,0);
    }
}
