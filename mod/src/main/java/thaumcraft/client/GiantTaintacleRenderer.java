package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.model.TaintacleModel;
import thaumcraft.entities.Taintacle;

/** Original giant pre-render scale1.33, then the common model's emergence and height scale. */
public final class GiantTaintacleRenderer extends MobRenderer<Taintacle,TaintacleModel> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/models/taintacle.png");
    public GiantTaintacleRenderer(EntityRendererProvider.Context context){super(context,new TaintacleModel(TaintacleModel.create(10).bakeRoot(),10),.6f);}
    @Override public ResourceLocation getTextureLocation(Taintacle entity){return TEXTURE;}
    @Override protected void scale(Taintacle entity,PoseStack pose,float partial){pose.scale(1.33f,1.33f,1.33f);float h=entity.getBbHeight(),hc=h*10,emergence=entity.tickCount<hc?(hc-entity.tickCount)/hc*h:0;pose.translate(0,(h==3?.6f:1.2f)+emergence,0);pose.scale(h/3,h/3,h/3);}
}
