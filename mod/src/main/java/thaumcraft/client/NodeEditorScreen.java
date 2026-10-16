package thaumcraft.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.network.NodeCreatePayload;

/** A small command editor; creation and permission checks remain on the server. */
public final class NodeEditorScreen extends Screen {
    private final List<Aspect> aspects=new ArrayList<>(Aspect.all().values());
    private final Map<String,Integer> amounts=new LinkedHashMap<>();
    private final int[] position;
    private int page,type,modifier,left,top,dropdown=-1,dropdownHover;
    private final EditBox[] coordinates=new EditBox[3];
    private Button create;
    public NodeEditorScreen(BlockPos pos){super(Component.translatable("thaumcraft.node.editor"));position=new int[]{pos.getX(),pos.getY(),pos.getZ()};}
    public static void open(BlockPos pos){Minecraft.getInstance().setScreen(new NodeEditorScreen(pos));}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        clearWidgets();left=(width-300)/2;top=(height-252)/2;
        for(int axis=0;axis<3;axis++){final int index=axis;var box=new EditBox(font,left+30+axis*88,top+52,62,16,Component.literal(new String[]{"X","Y","Z"}[axis]));box.setMaxLength(9);box.setValue(Integer.toString(position[axis]));box.setFilter(v->v.matches("-?\\d{0,8}"));box.setResponder(v->{try{position[index]=Integer.parseInt(v);}catch(NumberFormatException ignored){}});coordinates[axis]=box;addRenderableWidget(box);}
        var target=addRenderableWidget(new NodeEditorButton(left+10,top+74,135,18,Component.translatable("thaumcraft.node.look_position"),b->{var hit=minecraft.player.pick(64,1,false);if(hit instanceof BlockHitResult block&&hit.getType()==HitResult.Type.BLOCK)setPosition(block.getBlockPos());else minecraft.player.displayClientMessage(Component.translatable("thaumcraft.node.no_target"),true);}));
        target.setTooltip(Tooltip.create(Component.translatable("thaumcraft.node.look_position.tip")));
        var feet=addRenderableWidget(new NodeEditorButton(left+155,top+74,135,18,Component.translatable("thaumcraft.node.feet_position"),b->setPosition(BlockPos.containing(minecraft.player.getX(),minecraft.player.getY()-.01,minecraft.player.getZ()))));
        feet.setTooltip(Tooltip.create(Component.translatable("thaumcraft.node.feet_position.tip")));
        addRenderableWidget(new NodeEditorButton(left+10,top+97,135,20,Component.translatable("thaumcraft.node.type."+NodeState.Type.values()[type].name().toLowerCase(Locale.ROOT)),b->{dropdown=dropdown==0?-1:0;dropdownHover=type;}));
        addRenderableWidget(new NodeEditorButton(left+155,top+97,135,20,Component.translatable("thaumcraft.node.mod."+NodeState.Modifier.values()[modifier].name().toLowerCase(Locale.ROOT)),b->{dropdown=dropdown==1?-1:1;dropdownHover=modifier;}));
        for(int row=0;row<6&&page*6+row<aspects.size();row++){
            var aspect=aspects.get(page*6+row);int x=left+12+row%2*144,y=top+124+row/2*27;var box=new EditBox(font,x+89,y+3,40,16,Component.literal(aspect.getName()));box.setMaxLength(8);int value=amounts.getOrDefault(aspect.getTag(),0);box.setValue(value==0?"":Integer.toString(value));box.setHint(Component.literal("0"));box.setFilter(v->v.matches("\\d{0,8}")&&(v.isEmpty()||Long.parseLong(v)<=32767));box.setResponder(v->{int n=v.isEmpty()?0:Integer.parseInt(v);amounts.put(aspect.getTag(),n);String normal=n==0?"":Integer.toString(n);if(!v.equals(normal))box.setValue(normal);});addRenderableWidget(box);
        }
        addRenderableWidget(new NodeEditorButton(left+10,top+212,30,20,Component.literal("<"),b->{page=Math.floorMod(page-1,(aspects.size()+5)/6);init();}));
        addRenderableWidget(new NodeEditorButton(left+260,top+212,30,20,Component.literal(">"),b->{page=(page+1)%((aspects.size()+5)/6);init();}));
        create=addRenderableWidget(new NodeEditorButton(left+83,top+212,134,20,Component.translatable("thaumcraft.node.create"),b->{var selected=selectedPosition();if(selected==null)return;var values=new LinkedHashMap<String,Integer>();amounts.forEach((tag,n)->{if(n>0)values.put(tag,n);});if(values.isEmpty())return;PacketDistributor.sendToServer(new NodeCreatePayload(selected,type,modifier,values));onClose();}));
    }
    private int dropdownCount(){return dropdown==0?NodeState.Type.values().length:NodeState.Modifier.values().length;}
    private Component dropdownLabel(int index){return Component.translatable(dropdown==0?"thaumcraft.node.type."+NodeState.Type.values()[index].name().toLowerCase(Locale.ROOT):"thaumcraft.node.mod."+NodeState.Modifier.values()[index].name().toLowerCase(Locale.ROOT));}
    private void selectDropdown(int index){if(dropdown==0)type=index;else modifier=index;dropdown=-1;init();}
    @Override public boolean mouseClicked(double x,double y,int button){if(dropdown>=0){int xx=left+(dropdown==0?10:155),yy=top+117,index=(int)((y-yy)/18);if(button==0&&x>=xx&&x<xx+135&&y>=yy&&index<dropdownCount())selectDropdown(index);else dropdown=-1;return true;}return super.mouseClicked(x,y,button);}
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(dropdown>=0){if(key==256)dropdown=-1;else if(key==264||key==265)dropdownHover=Math.floorMod(dropdownHover+(key==264?1:-1),dropdownCount());else if(key==257||key==335)selectDropdown(dropdownHover);return true;}return super.keyPressed(key,scan,modifiers);}
    private void setPosition(BlockPos pos){for(int i=0;i<3;i++)coordinates[i].setValue(Integer.toString(i==0?pos.getX():i==1?pos.getY():pos.getZ()));}
    private BlockPos selectedPosition(){
        try{return new BlockPos(Integer.parseInt(coordinates[0].getValue()),Integer.parseInt(coordinates[1].getValue()),Integer.parseInt(coordinates[2].getValue()));}
        catch(NumberFormatException invalid){return null;}
    }

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        super.renderBackground(g,mouseX,mouseY,partial);g.fill(0,0,width,height,0x88000000);g.fill(left-3,top-3,left+303,top+255,0xff352619);g.fill(left,top,left+300,top+252,0xffb39868);g.fill(left+5,top+5,left+295,top+247,0xffe4d2a7);g.fill(left+8,top+119,left+292,top+207,0x553b2918);g.drawCenteredString(font,title,left+150,top+9,0xfff9e8b7);
        g.drawString(font,Component.translatable("thaumcraft.node.position_title").withStyle(net.minecraft.ChatFormatting.BOLD),left+12,top+25,0xff5a3d23,false);
        g.pose().pushPose();g.pose().translate(left+12,top+38,0);g.pose().scale(.85f,.85f,1);g.drawString(font,Component.translatable("thaumcraft.node.position_hint"),0,0,0xff5a4a35,false);g.pose().popPose();
        g.fill(left+10,top+94,left+290,top+95,0x668a6637);
        for(int axis=0;axis<3;axis++)g.drawString(font,new String[]{"X","Y","Z"}[axis],left+17+axis*88,top+56,0xff3b2618,false);
        for(int row=0;row<6&&page*6+row<aspects.size();row++){var aspect=aspects.get(page*6+row);int x=left+12+row%2*144,y=top+124+row/2*27;ResearchGraphics.aspect(g,aspect,x,y+2,1);g.drawString(font,font.plainSubstrByWidth(ResearchGraphics.aspectName(aspect).getString(),67),x+20,y+7,0xff3b2618,false);}
        create.active=selectedPosition()!=null&&amounts.values().stream().anyMatch(n->n>0);g.drawCenteredString(font,(page+1)+" / "+((aspects.size()+5)/6),left+150,top+236,0xff3b2618);for(var widget:renderables)widget.render(g,mouseX,mouseY,partial);
        g.drawString(font,"v",left+132,top+103,0xffe4d2a7,false);g.drawString(font,"v",left+277,top+103,0xffe4d2a7,false);
        var preview=new LinkedHashMap<String,Integer>();amounts.forEach((tag,n)->{if(n>0)preview.put(tag,Math.min(100,n));});if(preview.isEmpty())preview.put("aer",25);
        g.flush();g.enableScissor(left+250,top+6,left+291,top+39);g.pose().pushPose();g.pose().translate(left+271,top+23,200);g.pose().scale(19,-19,19);g.pose().translate(-.5,-.5,-.5);AuraNodeRenderer.renderAura(minecraft.player.blockPosition(),preview,NodeState.Type.values()[type],NodeState.Modifier.values()[modifier],true,g.pose(),g.bufferSource());g.flush();g.pose().popPose();g.disableScissor();
        if(dropdown>=0){int x=left+(dropdown==0?10:155),y=top+117,count=dropdownCount();g.pose().pushPose();g.pose().translate(0,0,400);g.fill(x-1,y-1,x+136,y+count*18+1,0xffaf8548);g.fill(x,y,x+135,y+count*18,0xff382913);for(int i=0;i<count;i++){if(mouseX>=x&&mouseX<x+135&&mouseY>=y+i*18&&mouseY<y+(i+1)*18)dropdownHover=i;boolean selected=i==(dropdown==0?type:modifier);if(i==dropdownHover)g.fill(x+1,y+i*18,x+134,y+(i+1)*18,0xff68502a);g.drawString(font,(selected?"> ":"  ")+dropdownLabel(i).getString(),x+4,y+i*18+5,selected?0xffffe3a3:0xffe4d2a7,false);}g.pose().popPose();}

    }
}
