package thaumcraft.verification;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import thaumcraft.items.*;
import thaumcraft.registry.R3Equipment;

final class FocusReviewClient {
    static final String[] FOCI=FocusReviewScene.FOCI;
    static int frostEvents;static boolean hook;
    static void tick(int scene,int age)throws Exception{
        var mc=Minecraft.getInstance();if(scene<20)return;
        mc.options.keyUse.setDown((scene==26||scene==38||scene==39)&&age>=15&&age<110);
        if(!hook){hook=true;var original=thaumcraft.entities.FocusProjectile.clientCracks;thaumcraft.entities.FocusProjectile.clientCracks=e->{frostEvents+=e.velocities().size();original.accept(e);};}
        if((scene<=25||scene>=30&&scene<=35||scene==60)&&age==15)mc.setScreen(new Gallery(scene==60?6:scene<=25?scene-20:scene-30,scene>=30&&scene<=35));
        if(scene==26&&age==50){var field=net.minecraft.client.renderer.LevelRenderer.class.getDeclaredField("destroyingBlocks");field.setAccessible(true);var progress=(java.util.Map<?,?>)field.get(mc.levelRenderer);PhotoLiveClient.check(progress.containsKey(mc.player.getId()),"native local player receives excavation cracking overlay");}
        if(scene==28&&age==100)PhotoLiveClient.check(frostEvents>0,"authoritative frost impact reaches client and creates shard debris: "+frostEvents);
    }
    private static class Gallery extends Screen {
        final int side;final boolean staff;
        Gallery(int side,boolean staff){super(Component.literal("TC4 focus faces"));this.side=side;this.staff=staff;}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void render(GuiGraphics g,int mx,int my,float partial){
            g.fill(0,0,width,height,0xff383c43);g.drawString(font,"Port: "+(staff?"staff":"wand")+" / all ten foci / view "+side,8,6,0xffffffff,false);
            for(int i=0;i<FOCI.length;i++){
                var wand=FocusReviewScene.wand(FOCI[i]);if(staff){var old=wand;wand=new ItemStack(R3Equipment.STAFF.get());wand.set(thaumcraft.registry.WandComponents.PARTS.get(),new thaumcraft.api.wands.WandParts("greatwood_staff","gold"));WandFocus.set(wand,WandFocus.get(old));}
                int x=48+(i%5)*96,y=(staff?135:100)+(i/5)*150;g.drawCenteredString(font,FOCI[i],x,y+(staff?75:95),0xffffffff);
                var model=minecraft.getItemRenderer().getModel(wand,minecraft.level,minecraft.player,0);
                if(PhotoLiveClient.wait==16){var quads=model.getQuads(null,null,net.minecraft.util.RandomSource.create());PhotoLiveClient.check(quads.size()>=24,"native baked wand focus has complete head: "+FOCI[i]);var passes=model.getRenderPasses(wand,false);PhotoLiveClient.check(passes.size()==4,"native focus has separate depth, translucent shell and ornament passes: "+FOCI[i]);var shell=passes.get(2).getQuads(null,null,net.minecraft.util.RandomSource.create());PhotoLiveClient.check(shell.size()==6,"focus shell has all six actual baked faces: "+FOCI[i]);float minX=Float.POSITIVE_INFINITY,maxX=Float.NEGATIVE_INFINITY,minZ=minX,maxZ=maxX;for(var q:shell){int[] data=q.getVertices();int stride=data.length/4;for(int vertex=0;vertex<4;vertex++){float px=Float.intBitsToFloat(data[vertex*stride]),pz=Float.intBitsToFloat(data[vertex*stride+2]);minX=Math.min(minX,px);maxX=Math.max(maxX,px);minZ=Math.min(minZ,pz);maxZ=Math.max(maxZ,pz);}}PhotoLiveClient.check(Math.abs(minX+maxX-1)<.00001&&Math.abs(minZ+maxZ-1)<.00001,"focus shell is centred exactly on rod and cap in X/Z: "+FOCI[i]);}
                g.pose().pushPose();g.pose().translate(x,y,180);g.pose().scale(staff?45:55,staff?-45:-55,staff?45:55);g.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(side==4?90:side==5?-90:side==6?25:0));g.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(side<4?side*90:side==6?45:0));g.pose().translate(0,-.6,0);
                minecraft.getItemRenderer().render(wand,ItemDisplayContext.NONE,false,g.pose(),g.bufferSource(),0xf000f0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,model);g.flush();g.pose().popPose();
            }
        }
    }
    private FocusReviewClient(){}
}
