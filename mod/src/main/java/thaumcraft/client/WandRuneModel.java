package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import thaumcraft.api.wands.StaffRod;
import thaumcraft.items.WandMaterials;

/** Original ModelWand.drawRune passes, after the body and attached focus. */
final class WandRuneModel extends BakedModelWrapper<BakedModel> {
    private static final ResourceLocation SCRIPT=ResourceLocation.parse("thaumcraft:misc/script");
    private final boolean staff;
    private int lastTick=Integer.MIN_VALUE;
    private TextureAtlasSprite lastSprite;
    private List<BakedQuad> quads=List.of();

    private WandRuneModel(BakedModel body,boolean staff){super(body);this.staff=staff;}

    static BakedModel wrap(BakedModel body,String kind){
        if(!kind.equals("staff")&&!kind.equals("sceptre"))return body;
        boolean staff=kind.equals("staff");
        var runes=new WandLayerModel(new WandRuneModel(body,staff),(light,ticks)->200);
        return new BakedModelWrapper<BakedModel>(body){
            @Override public List<BakedModel> getRenderPasses(ItemStack stack,boolean fabulous){
                var rod=WandMaterials.rod(stack);
                if(staff&&(!(rod instanceof StaffRod staffRod)||!staffRod.hasRunes()))return body.getRenderPasses(stack,fabulous);
                var passes=new ArrayList<>(body.getRenderPasses(stack,fabulous));passes.add(runes);return passes;
            }
            @Override public BakedModel applyTransform(ItemDisplayContext context,PoseStack pose,boolean left){body.applyTransform(context,pose,left);return this;}
        };
    }

    @Override public List<RenderType> getRenderTypes(ItemStack stack,boolean fabulous){return List.of(ThaumcraftRenderTypes.wandRunes());}
    @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random,ModelData data,RenderType type){return getQuads(state,side,random);}
    @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random){
        var player=Minecraft.getInstance().player;if(side!=null||player==null)return List.of();
        int ticks=player.tickCount;
        var sprite=Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(SCRIPT);
        if(ticks==lastTick&&sprite==lastSprite)return quads;
        var result=new ArrayList<BakedQuad>(staff?56:10);
        if(staff){
            for(int face=0;face<4;face++)for(int a=0;a<14;a++)
                result.add(rune(sprite,ticks,(a+face*3)%16,90d*(face+1),.36+a*.14,-.08,true));
        }else{
            for(int turn=0;turn<10;turn++)result.add(rune(sprite,ticks,turn,36*turn+ticks,.16,-.125,false));
        }
        lastTick=ticks;lastSprite=sprite;quads=List.copyOf(result);return quads;
    }

    record Geometry(Vector3d[] points,Vector3d normal,int color){}
    static Geometry geometry(int ticks,int frame,double rotation,double x,double z,boolean staff){
        float red=Mth.sin((float)(ticks+frame*5)/5f)*.1f+.88f;
        float green=Mth.sin((float)(ticks+frame*5)/7f)*.1f+.63f;
        float alpha=Mth.sin((float)(ticks+frame*5)/10f)*.2f;
        double size=.06+(double)(alpha/40f);
        var transform=new Matrix4d().translation(0,staff?.2:0,0).rotateY(Math.toRadians(rotation)).rotateZ(Math.PI/2).translate(x,-.01f,z);
        var points=new Vector3d[]{new Vector3d(-size,size,0),new Vector3d(size,size,0),new Vector3d(size,-size,0),new Vector3d(-size,-size,0)};
        double scale=staff?1:.7,origin=staff?29.9:15;
        for(var point:points){transform.transformPosition(point);point.set(.5+point.x*scale,origin/16-point.y*scale,.5-point.z*scale);}
        var normal=new Vector3d(points[1]).sub(points[0]).cross(new Vector3d(points[2]).sub(points[0])).normalize();
        int color=((int)((alpha+.6f)*255)<<24)|((int)(.2f*255)<<16)|((int)(green*255)<<8)|(int)(red*255);
        return new Geometry(points,normal,color);
    }

    private static BakedQuad rune(TextureAtlasSprite sprite,int ticks,int frame,double rotation,double x,double z,boolean staff){
        var geometry=geometry(ticks,frame,rotation,x,z,staff);var points=geometry.points();var normal=geometry.normal();
        int packedNormal=((byte)(normal.x*127)&255)|(((byte)(normal.y*127)&255)<<8)|(((byte)(normal.z*127)&255)<<16);
        float u0=sprite.getU(frame/16f),u1=sprite.getU((frame+1)/16f),v0=sprite.getV(0),v1=sprite.getV(1);
        float[][] uv={{u1,v1},{u1,v0},{u0,v0},{u0,v1}};
        var vertices=new int[IQuadTransformer.STRIDE*4];
        for(int i=0;i<4;i++){
            int start=i*IQuadTransformer.STRIDE;
            vertices[start]=Float.floatToRawIntBits((float)points[i].x);vertices[start+1]=Float.floatToRawIntBits((float)points[i].y);vertices[start+2]=Float.floatToRawIntBits((float)points[i].z);
            vertices[start+IQuadTransformer.COLOR]=geometry.color();
            vertices[start+IQuadTransformer.UV0]=Float.floatToRawIntBits(uv[i][0]);vertices[start+IQuadTransformer.UV0+1]=Float.floatToRawIntBits(uv[i][1]);
            vertices[start+IQuadTransformer.NORMAL]=packedNormal;
        }
        return new BakedQuad(vertices,-1,Direction.getNearest(normal.x,normal.y,normal.z),sprite,false,false);
    }
}
