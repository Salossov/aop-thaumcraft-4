package thaumcraft.verification;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.registry.R2Content;

final class TaintVisualReviewClient {
 static int sparks,swarm;
 static void tick(int scene,int age)throws Exception{var mc=Minecraft.getInstance();
  if(scene>=130&&scene<=133&&age==60){for(boolean occupied:new boolean[]{false,true}){var state=R2Content.TAINT_SPORE_STALK.get().defaultBlockState().setValue(thaumcraft.blocks.TaintFibresBlock.OCCUPIED,occupied);var quads=mc.getBlockRenderer().getBlockModelShaper().getBlockModel(state).getQuads(state,null,net.minecraft.util.RandomSource.create());PhotoLiveClient.check(quads.size()==8&&quads.stream().allMatch(q->q.isTinted()&&q.getTintIndex()==0),"native stalk has four double-sided tinted crop planes rather than a closed cube, occupied="+occupied);}}
  if(scene==123&&age==30){for(var block:java.util.List.of(R2Content.TAINT_FIBRES,R2Content.TAINT_GROWTH,R2Content.TAINT_TENDRILS,R2Content.TAINT_SPORE_STALK,R2Content.TAINTED_SOIL)){var stack=new ItemStack(block.get());int tint=mc.getItemColors().getColor(stack,0);PhotoLiveClient.check(tint>>>24==255,"native item tint is opaque: "+stack);var model=mc.getItemRenderer().getModel(stack,mc.level,mc.player,0);PhotoLiveClient.check(!model.getQuads(null,null,net.minecraft.util.RandomSource.create()).isEmpty(),"native baked item has visible inventory geometry: "+stack);}mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));}
  if(scene==125&&age==30){sparks=swarm=0;}
  if(scene==125&&age>=31&&age<=250){var groups=(java.util.Map<?,?>)FollowupReviewClient.field(mc.particleEngine,ParticleEngine.class,"particles");for(var value:groups.values())for(var particle:(java.util.Collection<?>)value){String name=particle.getClass().getName();if(name.equals("thaumcraft.client.GolemClientEffects$Sprite")&&(Integer)FollowupReviewClient.field(particle,particle.getClass(),"mode")==0){sparks++;var type=((Particle)particle).getRenderType();String atlas=null;for(var f:type.getClass().getDeclaredFields())if(f.getType()==String.class){f.setAccessible(true);atlas=(String)f.get(type);}if(!"misc/particles2".equals(atlas))throw new AssertionError("actual champion spark binds incorrect atlas: "+atlas);}if(name.equals("thaumcraft.client.SwarmParticles$Swarm"))swarm++;}}
  if(scene==125&&age==260){PhotoLiveClient.check(sparks>30,"real Bold Warden tick emits animated sparks using original particles2 atlas: "+sparks);PhotoLiveClient.check(swarm>100,"native taint swarm entity renders its moving motes: "+swarm);}
 }
 private TaintVisualReviewClient(){}
}
