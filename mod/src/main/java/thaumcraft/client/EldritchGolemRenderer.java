package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.entities.EldritchGolem;

final class EldritchGolemRenderer extends MobRenderer<EldritchGolem,EldritchGolemModel> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/models/eldritch_golem.png");
    EldritchGolemRenderer(EntityRendererProvider.Context context){super(context,new EldritchGolemModel(),1);}
    @Override public ResourceLocation getTextureLocation(EldritchGolem golem){return TEXTURE;}
    @Override protected void scale(EldritchGolem golem,PoseStack pose,float partial){pose.scale(2.15f,2.15f,2.15f);}
    @Override protected RenderType getRenderType(EldritchGolem golem,boolean body,boolean translucent,boolean glowing){return body?RenderType.entityTranslucent(TEXTURE):super.getRenderType(golem,body,translucent,glowing);}
}
