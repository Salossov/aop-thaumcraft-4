package thaumcraft.client;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchNote;
import thaumcraft.items.ResearchNotes;
import thaumcraft.menu.ResearchMenu;
import thaumcraft.network.*;

/** Original 255px table, 9px axial hex geometry, aspect palette and ink/note slots. */
public final class ResearchScreen extends FittedContainerScreen<ResearchMenu> {
    private Aspect first,second,dragged;private int page;private long sequence;
    private long combineUntil;private Map<String,Integer> previousPools;private final Map<String,Long> discoveryFlashes=new HashMap<>();
    private record Rune(ResearchNote.Hex hex,int symbol,long start,long end){}
    private final List<Rune> runes=new ArrayList<>();private long nextRune;private ResearchNote previousNote;private String runeKey="";
    public ResearchScreen(ResearchMenu menu,Inventory inventory,Component title) {super(menu,inventory,title);imageWidth=255;imageHeight=255;}
    private List<Aspect> palette() {return ClientKnowledge.snapshot().aspectPools().keySet().stream().map(Aspect::getAspect).filter(Objects::nonNull).sorted(Comparator.comparing(Aspect::getTag)).toList();}
    private void send(int action,String a,String b,int q,int r) {PacketDistributor.sendToServer(new ResearchActionPayload(menu.containerId,menu.token(),++sequence,action,a,b,q,r));}
    private int hexX(ResearchNote.Hex hex) {return leftPos+169+(int)Math.round(13.5*hex.q());}
    private int hexY(ResearchNote.Hex hex) {return topPos+83+(int)Math.round(9*Math.sqrt(3)*(hex.r()+hex.q()/2d));}
    private ResearchNote.Hex hexAt(double x,double y,ResearchNote note) {return note.grid.keySet().stream().filter(h->Math.pow(x-hexX(h),2)+Math.pow(y-hexY(h),2)<=81).min(Comparator.comparingDouble(h->Math.pow(x-hexX(h),2)+Math.pow(y-hexY(h),2))).orElse(null);}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();var texture=ResearchGraphics.texture("gui/guiresearchtable2");g.blit(texture,leftPos,topPos,0,0,255,167,256,256);g.blit(texture,leftPos+40,topPos+167,0,166,184,88,256,256);
        var knowledge=ClientKnowledge.snapshot();var aspects=palette();long now=System.currentTimeMillis();
        if(previousPools!=null)for(var entry:knowledge.aspectPools().entrySet())if(entry.getValue()>previousPools.getOrDefault(entry.getKey(),0))discoveryFlashes.put(entry.getKey(),now+1500);
        previousPools=Map.copyOf(knowledge.aspectPools());discoveryFlashes.entrySet().removeIf(e->e.getValue()<now);
        if(first!=null&&knowledge.pool(first)+menu.bonus(first)<=0)first=null;if(second!=null&&knowledge.pool(second)+menu.bonus(second)<=0)second=null;page=Math.clamp(page,0,Math.max(0,(aspects.size()-21)/5));
        for(int i=0;i<25&&page*5+i<aspects.size();i++) {var aspect=aspects.get(page*5+i);int x=leftPos+10+i/5*16,y=topPos+40+i%5*16;
            ResearchGraphics.aspect(g,aspect,x,y,knowledge.pool(aspect)+menu.bonus(aspect)>0?1:0.33f);g.pose().pushPose();g.pose().translate(x+15,y+10,100);g.pose().scale(0.5f,0.5f,1);g.drawString(font,Integer.toString(knowledge.pool(aspect)),-font.width(Integer.toString(knowledge.pool(aspect))),0,ResearchGraphics.WHITE);g.pose().popPose();if(menu.bonus(aspect)>0)ResearchGraphics.discoveryFlame(g,x,y,1);if(discoveryFlashes.containsKey(aspect.getTag()))ResearchGraphics.discoveryFlame(g,x,y,Math.min(1,(discoveryFlashes.get(aspect.getTag())-now)/400f));}
        if(page>0)g.blit(texture,leftPos+27,topPos+121,184,208,24,8,256,256);if((page+1)*5+20<aspects.size())g.blit(texture,leftPos+51,topPos+121,208,208,24,8,256,256);
        if(first!=null&&second!=null){g.blit(texture,leftPos+35,topPos+139,184,184,32,16,256,256);if(now<combineUntil)g.blit(texture,leftPos+35,topPos+139,184,168,32,16,256,256);else ResearchGraphics.discoveryFlame(g,leftPos+47,topPos+143,.7f);}
        ResearchGraphics.aspect(g,first,leftPos+13,topPos+139,1);ResearchGraphics.aspect(g,second,leftPos+71,topPos+139,1);
        var note=ResearchNotes.read(menu.table.getItem(1));if(note==null){previousNote=null;runes.clear();runeKey="";return;}
        ResearchGraphics.blit(g,ResearchGraphics.texture("misc/parchment3"),leftPos+94,topPos+8,0,0,150,150,256,256);
        if(previousNote!=null&&previousNote.key.equals(note.key)&&!previousNote.complete&&note.complete)play(thaumcraft.registry.R2Sounds.LEARN.get(),.5f);previousNote=note.copy();
        drawRunes(g,note,now);
        var connected=connected(note,knowledge.aspectPools().keySet());
        for(var entry:note.grid.entrySet()) {var hex=entry.getKey();var aspect=Aspect.getAspect(entry.getValue().aspect());if(aspect==null||!connected.contains(hex))continue;for(int direction=0;direction<3;direction++){var neighbour=hex.neighbour(direction);var n=note.grid.get(neighbour);if(n!=null&&connected.contains(neighbour)&&aspect.connects(Aspect.getAspect(n.aspect())))ResearchGraphics.line(g,hexX(hex),hexY(hex),hexX(neighbour),hexY(neighbour),ResearchGraphics.LINK);}}
        for(var entry:note.grid.entrySet()) {var hex=entry.getKey();var cell=entry.getValue();int x=hexX(hex),y=hexY(hex);
            if(!note.complete){if(cell.type()==1){float time=(minecraft.player==null?0:minecraft.player.tickCount)+partial;int red=Math.round(255*(.85f+.15f*(float)Math.sin((time+x)/10))),green=Math.round(255*(.85f+.15f*(float)Math.sin((time+x+y)/11))),blue=Math.round(255*(.85f+.15f*(float)Math.sin((time+y)/12)));ResearchGraphics.researchOrb(g,x-8,y-8,red<<16|green<<8|blue,1);}else {g.setColor(1,1,1,0.3f);ResearchGraphics.blit(g,ResearchGraphics.texture("gui/hex1"),x-8,y-8,0,0,16,16,16,16);g.setColor(1,1,1,1);}}
            var aspect=Aspect.getAspect(cell.aspect());if(aspect!=null)ResearchGraphics.aspect(g,aspect,x-8,y-8,cell.type()==1||connected.contains(hex)?1:.66f);
        }
    }
    @Override protected void renderLabels(GuiGraphics g,int x,int y) {}
    static Set<ResearchNote.Hex> connected(ResearchNote note,Set<String> known){var result=new HashSet<ResearchNote.Hex>();var pending=new ArrayDeque<ResearchNote.Hex>();note.grid.forEach((hex,cell)->{if(cell.type()==1&&known.contains(cell.aspect())){result.add(hex);pending.add(hex);}});while(!pending.isEmpty()){var hex=pending.removeFirst();var a=Aspect.getAspect(note.grid.get(hex).aspect());if(a==null)continue;for(int dir=0;dir<6;dir++){var next=hex.neighbour(dir);var cell=note.grid.get(next);if(cell!=null&&cell.type()>0&&known.contains(cell.aspect())&&a.connects(Aspect.getAspect(cell.aspect()))&&result.add(next))pending.add(next);}}return result;}
    private void drawRunes(GuiGraphics g,ResearchNote note,long now){if(!runeKey.equals(note.key)){runes.clear();runeKey=note.key;nextRune=0;}runes.removeIf(r->r.end<now);if(now>=nextRune&&minecraft.level!=null){nextRune=now+250;var random=minecraft.level.random;int x=random.nextInt(120)-60,y=random.nextInt(120)-60;double q=x/13.5,r=y/(9*Math.sqrt(3))-q/2;var hex=new ResearchNote.Hex((int)Math.round(q),(int)Math.round(r));if(!note.grid.containsKey(hex)&&runes.stream().noneMatch(v->v.hex.equals(hex)))runes.add(new Rune(hex,random.nextInt(16),now,now+15000+random.nextInt(10000)));}
        for(var rune:runes){float p=(now-rune.start)/(float)(rune.end-rune.start),alpha=(p<.25f?p*2:p>.5f?1-p:.5f)*.66f;g.pose().pushPose();g.pose().translate(hexX(rune.hex),hexY(rune.hex),0);g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-180));g.setColor(0,0,0,alpha);ResearchGraphics.blit(g,ResearchGraphics.texture("misc/script"),-5,-5,10,10,rune.symbol*16,0,16,16,256,16);g.setColor(1,1,1,1);g.pose().popPose();}}
    @Override protected void renderFitted(GuiGraphics g,int x,int y,float partial) {renderBackground(g,x,y,partial);super.renderFitted(g,x,y,partial);renderTooltip(g,x,y);
        var aspects=palette();for(int i=0;i<25&&page*5+i<aspects.size();i++)if(ResearchGraphics.inside(x,y,leftPos+10+i/5*16,topPos+40+i%5*16,16,16)){var aspect=aspects.get(page*5+i);fittedTooltip(g,ResearchGraphics.aspectTooltip(aspect),x,y);if(ClientKnowledge.snapshot().completedResearch().contains("RESEARCHER1")&&!aspect.isPrimal()){g.pose().pushPose();g.pose().translate(0,0,450);for(int j=0;j<2;j++){int xx=Math.min(x+8+j*18,width-44+j*18),yy=Math.min(y+12,height-22);ResearchGraphics.aspectHighlight(g,xx-2,yy-2);ResearchGraphics.aspect(g,aspect.getComponents().get(j),xx,yy,1);}g.flush();g.pose().popPose();}}
        if(dragged!=null){g.pose().pushPose();g.pose().translate(0,0,400);ResearchGraphics.researchOrb(g,x-8,y-8,dragged.getColor(),1);g.flush();g.pose().popPose();}
    }
    private void combine(Aspect a,Aspect b){if(System.currentTimeMillis()<combineUntil)return;combineUntil=System.currentTimeMillis()+200;send(1,a.getTag(),b.getTag(),0,0);if(minecraft.player!=null)minecraft.player.playSound(thaumcraft.registry.R2Sounds.ASPECT_COMBINE.get(),.3f,1);}
    private void play(net.minecraft.sounds.SoundEvent sound,float volume){if(minecraft.player!=null)minecraft.player.playSound(sound,volume,1);}
    @Override protected boolean mouseClickedFitted(double x,double y,int button) {
        if(ResearchGraphics.inside(x,y,leftPos+35,topPos+139,32,16)&&first!=null&&second!=null) {combine(first,second);return true;}
        if(ResearchGraphics.inside(x,y,leftPos+27,topPos+121,24,8)) {page=Math.max(0,page-1);play(thaumcraft.registry.R2Sounds.KEY.get(),.3f);return true;}
        if(ResearchGraphics.inside(x,y,leftPos+51,topPos+121,24,8)) {page++;play(thaumcraft.registry.R2Sounds.KEY.get(),.3f);return true;}
        var aspects=palette();for(int i=0;i<25&&page*5+i<aspects.size();i++)if(ResearchGraphics.inside(x,y,leftPos+10+i/5*16,topPos+40+i%5*16,16,16)) {
            var aspect=aspects.get(page*5+i);var knowledge=ClientKnowledge.snapshot();
            if(net.minecraft.client.gui.screens.Screen.hasShiftDown()&&knowledge.completedResearch().contains("RESEARCHER2")&&!aspect.isPrimal()){var components=aspect.getComponents();int a=knowledge.pool(components.get(0))+menu.bonus(components.get(0)),b=knowledge.pool(components.get(1))+menu.bonus(components.get(1));if(a>0&&b>0&&(components.get(0)!=components.get(1)||a>1)&&System.currentTimeMillis()>=combineUntil){combineUntil=System.currentTimeMillis()+200;send(3,aspect.getTag(),"",0,0);play(thaumcraft.registry.R2Sounds.ASPECT_COMBINE.get(),.3f);}return true;}
            if(knowledge.pool(aspect)+menu.bonus(aspect)>0){play(thaumcraft.registry.R2Sounds.ASPECT_CLAIM.get(),.2f);if(button==1)second=aspect;else {dragged=aspect;if(first==null)first=aspect;else second=aspect;}}return true;
        }
        if(ResearchGraphics.inside(x,y,leftPos+13,topPos+139,16,16)) {first=null;play(thaumcraft.registry.R2Sounds.ERASE.get(),.2f);return true;}if(ResearchGraphics.inside(x,y,leftPos+71,topPos+139,16,16)) {second=null;play(thaumcraft.registry.R2Sounds.ERASE.get(),.2f);return true;}
        var note=ResearchNotes.read(menu.table.getItem(1));if(note!=null&&!note.complete) {var hex=hexAt(x,y,note);if(button==1&&hex!=null&&note.grid.get(hex).type()==2) {send(2,"","",hex.q(),hex.r());play(thaumcraft.registry.R2Sounds.ERASE.get(),.2f);return true;}}
        return super.mouseClickedFitted(x,y,button);
    }
    @Override protected boolean mouseReleasedFitted(double x,double y,int button) {if(dragged!=null) {var note=ResearchNotes.read(menu.table.getItem(1));if(note!=null&&!note.complete&&thaumcraft.menu.ResearchMenu.usableInk(menu.table.getItem(0))) {var hex=hexAt(x,y,note);if(hex!=null&&note.grid.get(hex).type()!=1&&!note.grid.get(hex).aspect().equals(dragged.getTag())){send(2,dragged.getTag(),"",hex.q(),hex.r());play(thaumcraft.registry.R2Sounds.WRITE.get(),.2f);}}dragged=null;return true;}return super.mouseReleasedFitted(x,y,button);}
    @Override protected boolean mouseScrolledFitted(double x,double y,double horizontal,double vertical) {if(ResearchGraphics.inside(x,y,leftPos+8,topPos+36,85,95)){page=Math.max(0,page+(vertical<0?1:-1));return true;}return super.mouseScrolledFitted(x,y,horizontal,vertical);}
}
