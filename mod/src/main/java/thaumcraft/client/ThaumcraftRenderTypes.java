package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import java.util.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** RenderType owns and restores blend/depth state, including under Sodium. */
public final class ThaumcraftRenderTypes extends RenderType {
    // Vanilla NO_DEPTH_TEST is a no-op: it leaves terrain depth testing enabled.
    private static final DepthTestStateShard OVERLAY_DEPTH=new DepthTestStateShard("thaumcraft_overlay_depth",519){
        private boolean enabled;
        @Override public void setupRenderState(){enabled=org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();}
        @Override public void clearRenderState(){if(enabled)com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();else com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();}
    };
    // Preserve TC4's balanced inventory glass lighting independently of the modern GUI light rig.
    private static final RenderType RESERVOIR_INVENTORY_GLASS=create("thaumcraft_reservoir_inventory_glass",DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS,256,false,true,CompositeState.builder().setShaderState(new ShaderStateShard(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader)).setTextureState(new TextureStateShard(ResourceLocation.parse("thaumcraft:textures/blocks/essentiareservoir.png"),false,false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(CULL).setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false));
    static RenderType reservoirInventoryGlass(){return RESERVOIR_INVENTORY_GLASS;}
    private static final TransparencyStateShard TC4_ADDITIVE=new TransparencyStateShard("thaumcraft_src_alpha_additive",()->{com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);},()->{com.mojang.blaze3d.systems.RenderSystem.disableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();});
    private static final RenderType INFUSION_HALO=create("thaumcraft_infusion_halo",DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.TRIANGLES,1024,false,true,CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TC4_ADDITIVE).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    public static RenderType infusionHalo(){return INFUSION_HALO;}
    private static final Map<ResourceLocation,RenderType> INVISIBLE_CRAB=new HashMap<>();
    public static RenderType invisibleCrabOverlay(ResourceLocation texture){return INVISIBLE_CRAB.computeIfAbsent(texture,key->create("thaumcraft_invisible_crab_overlay",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,256,false,true,CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState(new TextureStateShard(key,false,false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false)));}
    private static final Map<ResourceLocation,RenderType> GUI_ICONS=new HashMap<>();
    public static RenderType guiIcon(ResourceLocation texture){return GUI_ICONS.computeIfAbsent(texture,key->create("thaumcraft_gui_icon",DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS,256,false,false,CompositeState.builder().setShaderState(new ShaderStateShard(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader)).setTextureState(new TextureStateShard(key,false,false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).createCompositeState(false)));}
    private static final Map<ResourceLocation,RenderType> PROJECTILES=new HashMap<>();
    public static RenderType projectile(ResourceLocation texture){return PROJECTILES.computeIfAbsent(texture,key->create("thaumcraft_additive_projectile",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,256,false,true,CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState(new TextureStateShard(key,false,false)).setTransparencyState(TC4_ADDITIVE).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false)));}
    private static final Map<ResourceLocation,RenderType> GLOW=new HashMap<>();
    private static final Map<ResourceLocation,RenderType> TRANSLUCENT_MODELS=new HashMap<>();
    /** Original glass draws colour without sealing the depth buffer in front of its contents. */
    static RenderType translucentModel(ResourceLocation texture){return TRANSLUCENT_MODELS.computeIfAbsent(texture,key->create("thaumcraft_translucent_model",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,256,false,true,CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState(new TextureStateShard(key,false,false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false)));}
    private record NodeKey(ResourceLocation texture,boolean additive,boolean throughWalls){}
    private static final Map<NodeKey,RenderType> NODES=new HashMap<>();
    private static final Map<NodeKey,RenderType> UNLIT=new HashMap<>();
    private static final RenderType GOLEM_RANGE=create("thaumcraft_golem_range",DefaultVertexFormat.POSITION_COLOR_NORMAL,VertexFormat.Mode.LINES,1024,false,false,CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(OVERLAY_DEPTH).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).setLineState(new LineStateShard(java.util.OptionalDouble.of(2))).createCompositeState(false));
    public static RenderType golemRange(){return GOLEM_RANGE;}
    /** TC4 overlays bypass directional entity lighting; rotating the camera must not darken them. */
    public static RenderType unlit(ResourceLocation texture,boolean additive,boolean throughWalls){return UNLIT.computeIfAbsent(new NodeKey(texture,additive,throughWalls),key->create("thaumcraft_unlit",DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS,256,false,true,CompositeState.builder().setShaderState(new ShaderStateShard(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader)).setTextureState(new TextureStateShard(key.texture,false,false)).setTransparencyState(key.additive?TC4_ADDITIVE:TRANSLUCENT_TRANSPARENCY).setDepthTestState(key.throughWalls?OVERLAY_DEPTH:LEQUAL_DEPTH_TEST).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).createCompositeState(false)));}
    private ThaumcraftRenderTypes(String name,VertexFormat format,VertexFormat.Mode mode,int size,boolean crumble,boolean sorted,Runnable setup,Runnable clear){super(name,format,mode,size,crumble,sorted,setup,clear);}
    public static RenderType glow(ResourceLocation texture){return GLOW.computeIfAbsent(texture,key->create("thaumcraft_glow",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,256,false,true,CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER).setTextureState(new TextureStateShard(key,false,false)).setTransparencyState(TC4_ADDITIVE).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false)));}
    public static RenderType node(ResourceLocation texture,boolean additive,boolean throughWalls){return NODES.computeIfAbsent(new NodeKey(texture,additive,throughWalls),key->create("thaumcraft_node",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,256,false,true,CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER).setTextureState(new TextureStateShard(key.texture,false,false)).setTransparencyState(key.additive?TC4_ADDITIVE:TRANSLUCENT_TRANSPARENCY).setDepthTestState(key.throughWalls?OVERLAY_DEPTH:LEQUAL_DEPTH_TEST).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false)));}
}
