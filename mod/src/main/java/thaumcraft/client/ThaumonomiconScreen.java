package thaumcraft.client;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.research.ResearchDefinition;
import thaumcraft.crafting.ArcaneRecipe;
import thaumcraft.menu.ThaumonomiconMenu;
import thaumcraft.network.*;

/** TC4 category tree, parent links, parchment pages and server-authorized notes. */
public final class ThaumonomiconScreen extends FittedContainerScreen<ThaumonomiconMenu> {
    private int tabOffset;
    private List<String> categories(){var known=ClientKnowledge.snapshot().completedResearch();return thaumcraft.api.research.ResearchApi.books().categories().values().stream().filter(c->c.requiredResearch().isEmpty()||known.contains(c.requiredResearch())).sorted(Comparator.comparingInt(thaumcraft.api.book.BookCategory::order).thenComparing(thaumcraft.api.book.BookCategory::id)).map(thaumcraft.api.book.BookCategory::id).toList();}
    private List<String> tabs(){var all=categories();int count=Math.max(1,imageHeight/24);tabOffset=Math.clamp(tabOffset,0,Math.max(0,all.size()-count));return all.subList(tabOffset,Math.min(all.size(),tabOffset+count));}
    private record AddonHover(int x,int y,int width,int height,List<Component> tooltip,Runnable click){}
    private record AddonCanvas(thaumcraft.api.book.BookPage page,int x,int y){}
    private final List<AddonHover> addonHovers=new ArrayList<>();
    private final List<AddonCanvas> addonCanvases=new ArrayList<>();
    private String navigationHighlight="";private long navigationUntil;private boolean navigationNotice;
    private void openResearch(String key){
        var target=thaumcraft.api.research.ResearchNavigation.resolve(key,ClientKnowledge.definitions(),thaumcraft.api.research.ResearchApi.books(),ClientKnowledge.snapshot());
        var d=ClientKnowledge.definitions().get(target.focus());spread=0;
        if(target.reason()==thaumcraft.api.research.ResearchNavigation.Reason.KNOWN){category=d.category();selected=d;return;}
        selected=null;if(d!=null){category=d.category();panX=-d.column()*24;panY=-d.row()*24;navigationHighlight=d.key();navigationUntil=System.currentTimeMillis()+12000;var all=categories();int index=all.indexOf(category);if(index>=0)tabOffset=Math.max(0,index-Math.max(1,imageHeight/24)+1);}
        if(target.reason()==thaumcraft.api.research.ResearchNavigation.Reason.PREREQUISITES||target.reason()==thaumcraft.api.research.ResearchNavigation.Reason.UNDISCOVERED){String message=target.reason()==thaumcraft.api.research.ResearchNavigation.Reason.PREREQUISITES?"thaumcraft.book.navigation.prerequisites":"thaumcraft.book.navigation.undiscovered";navigationNotice=true;popupMessage=I18n.get(message);popupUntil=System.nanoTime()/1000000+8000;if(minecraft.player!=null)minecraft.player.displayClientMessage(Component.translatable(message).withStyle(net.minecraft.ChatFormatting.GOLD),false);}
    }

    private thaumcraft.api.client.BookRenderContext addonContext(GuiGraphics g,int originX,int originY,thaumcraft.api.book.BookPage page){
        addonCanvases.add(new AddonCanvas(page,originX,originY));var renderer=thaumcraft.api.client.BookRenderers.get(page.type());if(renderer!=null)renderer.mouseMoved(page,pointerX-originX,pointerY-originY);
        return new thaumcraft.api.client.BookRenderContext(){
            public GuiGraphics graphics(){return g;}public net.minecraft.client.gui.Font font(){return ThaumonomiconScreen.this.font;}public int x(){return originX;}public int y(){return originY;}
            public void item(ItemStack stack,int x,int y){ThaumonomiconScreen.this.item(g,stack,originX+x,originY+y,true);}
            public void aspect(thaumcraft.api.aspects.Aspect aspect,int amount,int x,int y){if(aspect!=null)tag(g,aspect,amount,originX+x,originY+y,1);}
            public void recipe(String id,int x,int y){renderRecipe(g,id,originX+x,originY+y);}
            public void link(Component text,String research,int x,int y){g.drawString(font(),text,originX+x,originY+y,0xff563e89,false);addonHovers.add(new AddonHover(originX+x,originY+y,Math.min(139,font().width(text)),10,List.of(text),()->openResearch(research)));}
            public void tooltip(int x,int y,int w,int h,List<Component> lines){addonHovers.add(new AddonHover(originX+x,originY+y,w,h,List.copyOf(lines),null));}
        };
    }

    private int pointerX=-1,pointerY=-1;
    private String category="BASICS";private ResearchDefinition selected;private double panX,panY;private long sequence;private int spread,recipeCycle;private long nextRecipeCycle;
    private String popupMessage="";private long popupUntil;
    private int researchTooltipTop=-1;
    private record Page(List<BookPageLayout.Element> elements,String recipe,List<thaumcraft.api.aspects.Aspect> aspects) {}
    private record Hover(int x,int y,int width,int height,ItemStack item,thaumcraft.api.aspects.Aspect aspect) {}
    private final List<Hover> pageHovers=new ArrayList<>();
    private List<Page> cachedPages=List.of();private ResearchDefinition cachedSelection;private long cachedRevision=-1;private thaumcraft.config.ThaumcraftClientConfig.BookFontStyle cachedFont;
    public ThaumonomiconScreen(ThaumonomiconMenu menu,Inventory inventory,Component title) {super(menu,inventory,title);imageWidth=256;imageHeight=230;}
    public static void noteCreated(ResearchNoteCreatedPayload packet){var mc=net.minecraft.client.Minecraft.getInstance();if(mc.screen instanceof ThaumonomiconScreen screen&&screen.menu.containerId==packet.containerId()){
        screen.navigationNotice=false;screen.popupMessage=I18n.get("tc.research.popup",I18n.get("tc.research_name."+packet.key()));screen.popupUntil=System.nanoTime()/1000000+3000;screen.play(thaumcraft.registry.R2Sounds.LEARN.get(),.75f);
    }}
    private boolean hasNote(String key){if(minecraft.player==null)return false;for(var stack:minecraft.player.getInventory().items){var note=thaumcraft.items.ResearchNotes.read(stack);if(note!=null&&note.key.equals(key))return true;}return false;}
    private boolean hasScribingSupplies(){if(minecraft.player==null)return false;boolean paper=false,ink=false;for(var stack:minecraft.player.getInventory().items){paper|=stack.is(net.minecraft.world.item.Items.PAPER);ink|=thaumcraft.menu.ResearchMenu.usableInk(stack);}return paper&&ink;}
    @Override protected int fittingWidth() {return 334;}
    @Override protected int fittingHeight() {return 236;}
    private boolean opened;private double treeZoom=1;
    private int treeCenterX(){return leftPos+imageWidth/2;}
    private int treeCenterY(){return topPos+imageHeight/2-7;}
    private boolean inTree(double x,double y){return ResearchGraphics.inside(x,y,leftPos+16,topPos+16,imageWidth-32,imageHeight-32);}
    private void zoom(double value,double mouseX,double mouseY){double next=Math.clamp(value,.5,2);panX+=(mouseX-treeCenterX())/next-(mouseX-treeCenterX())/treeZoom;panY+=(mouseY-treeCenterY())/next-(mouseY-treeCenterY())/treeZoom;treeZoom=next;thaumcraft.config.ThaumcraftClientConfig.BOOK_ZOOM.set(next);}
    private void frame(GuiGraphics g){var texture=ResearchGraphics.texture("gui/gui_research");int[] sx={0,16,240},sy={0,16,214},sw={16,224,16},sh={16,198,16},dx={leftPos,leftPos+16,leftPos+imageWidth-16},dy={topPos,topPos+16,topPos+imageHeight-16},dw={16,imageWidth-32,16},dh={16,imageHeight-32,16};for(int row=0;row<3;row++)for(int col=0;col<3;col++)g.blit(texture,dx[col],dy[row],dw[col],dh[row],sx[col],sy[row],sw[col],sh[row],256,256);}

    @Override protected void init() {
        imageWidth=256;imageHeight=230;super.init();var mode=thaumcraft.config.ThaumcraftClientConfig.BOOK_WINDOW_MODE.get();
        if(mode!=thaumcraft.config.ThaumcraftClientConfig.BookWindowMode.ORIGINAL){imageWidth=Math.max(128,width-64);imageHeight=Math.max(144,height-36);if(mode==thaumcraft.config.ThaumcraftClientConfig.BookWindowMode.SQUARE)imageWidth=imageHeight=Math.min(imageWidth,imageHeight);leftPos=(width-imageWidth+24)/2;topPos=(height-imageHeight)/2;}
        treeZoom=thaumcraft.config.ThaumcraftClientConfig.BOOK_ZOOM.get();if(!opened)centreCategory();if(!menu.openingResearch.isEmpty()){openResearch(menu.openingResearch);menu.openingResearch="";}
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("\u2699"),button->minecraft.setScreen(new net.neoforged.neoforge.client.gui.ConfigurationScreen(net.neoforged.fml.ModList.get().getModContainerById("thaumcraft").orElseThrow(),this))).bounds(width-26,6,20,20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("thaumcraft.book.settings"))).build());
        if(!opened){opened=true;play(thaumcraft.registry.R2Sounds.PAGE.get(),.66f);}
    }
    private List<ResearchDefinition> visible(){var k=ClientKnowledge.snapshot();return ClientKnowledge.definitions().values().stream().filter(d->!d.virtual()&&d.category().equals(category)&&(k.completedResearch().contains(d.key())||d.available(k))).toList();}
    private void centreCategory() {var nodes=visible();panX=-nodes.stream().mapToInt(ResearchDefinition::column).average().orElse(0)*24;panY=-nodes.stream().mapToInt(ResearchDefinition::row).average().orElse(0)*24;}
    private int nodeX(ResearchDefinition d) {return treeCenterX()+(int)Math.round(panX)+d.column()*24;}
    private int nodeY(ResearchDefinition d) {return treeCenterY()+(int)Math.round(panY)+d.row()*24;}
    private ResearchDefinition hovered(double x,double y) {if(!inTree(x,y))return null;double tx=treeCenterX()+(x-treeCenterX())/treeZoom,ty=treeCenterY()+(y-treeCenterY())/treeZoom;return visible().stream().filter(d->ResearchGraphics.inside(tx,ty,nodeX(d)-11,nodeY(d)-11,22,22)).findFirst().orElse(null);}
    private List<Page> pages() {
        var result=new ArrayList<Page>();if(selected==null)return result;
        var knowledge=ClientKnowledge.snapshot();var style=thaumcraft.config.ThaumcraftClientConfig.BOOK_FONT_STYLE.get();
        if(cachedSelection==selected&&cachedRevision==knowledge.revision()&&cachedFont==style)return cachedPages;
        cachedSelection=selected;cachedRevision=knowledge.revision();cachedFont=style;
        for(var key:selected.pageOrder()){
        if(selected.pageRequirements().containsKey(key)&&!thaumcraft.network.ClientKnowledge.snapshot().completedResearch().contains(selected.pageRequirements().get(key)))continue;
        if(thaumcraft.api.research.ResearchApi.books().pages().containsKey(key)){var page=thaumcraft.api.research.ResearchApi.books().pages().get(key);for(var layout:AddonBookLayouts.layout(page,font,result.isEmpty()?174:199,this::addonContext))result.add(new Page(List.of(layout),"",List.of()));}
        else if(selected.textPages().contains(key))for(var text:I18n.get(key).split("(?i)<PAGE>")) {
            var elements=new ArrayList<BookPageLayout.Element>();int used=0;
            for(var element:BookPageLayout.parse(font,text)){int capacity=result.isEmpty()?174:199;if(used+element.height()>capacity&&!elements.isEmpty()){result.add(new Page(List.copyOf(elements),"",List.of()));elements.clear();used=0;}elements.add(element);used+=element.height();}
            if(!elements.isEmpty())result.add(new Page(List.copyOf(elements),"",List.of()));
        }
        else if(!selected.recipeVariants(key).isEmpty())result.add(new Page(List.of(),key,List.of()));
        }
        if(displayResearch(selected).key().equals("ASPECTS")) {
            var aspects=knowledge.aspectPools().keySet().stream().map(thaumcraft.api.aspects.Aspect::getAspect).filter(Objects::nonNull).sorted(Comparator.comparing(thaumcraft.api.aspects.Aspect::getTag)).toList();
            for(int i=0;i<aspects.size();i+=4)result.add(new Page(List.of(),"",aspects.subList(i,Math.min(i+4,aspects.size()))));
        }
        if(result.isEmpty())result.add(new Page(List.of(),"",List.of()));cachedPages=List.copyOf(result);return cachedPages;
    }
    @Override protected void renderLabels(GuiGraphics g,int x,int y) {}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        if(selected!=null) {renderBook(g);return;}
        g.blit(ResourceLocation.parse(thaumcraft.api.research.ResearchApi.books().categories().getOrDefault(category,thaumcraft.api.book.BookCategory.originals().get("BASICS")).background()),leftPos+8,topPos+8,imageWidth-16,imageHeight-20,0f,0f,240,210,512,512);
        fittedScissor(g,leftPos+16,topPos+16,leftPos+imageWidth-16,topPos+imageHeight-16);g.pose().pushPose();g.pose().translate(treeCenterX(),treeCenterY(),0);g.pose().scale((float)treeZoom,(float)treeZoom,1);g.pose().translate(-treeCenterX(),-treeCenterY(),0);var nodes=visible();var k=ClientKnowledge.snapshot();
        var visibleKeys=nodes.stream().map(ResearchDefinition::key).collect(java.util.stream.Collectors.toSet());
        for(var d:nodes){
            for(var parent:d.parents()){var p=ClientKnowledge.definitions().get(parent);if(p!=null&&visibleKeys.contains(parent))ResearchGraphics.researchLink(g,nodeX(d),nodeY(d),nodeX(p),nodeY(p),k.completedResearch().contains(d.key()),k.completedResearch().contains(parent),(minecraft.player==null?0:minecraft.player.tickCount)+partial);}
            for(var sibling:d.siblings()){var p=ClientKnowledge.definitions().get(sibling);if(p!=null&&visibleKeys.contains(sibling)&&!p.parents().contains(d.key()))ResearchGraphics.researchLink(g,nodeX(d),nodeY(d),nodeX(p),nodeY(p),k.completedResearch().contains(d.key()),k.completedResearch().contains(sibling),(minecraft.player==null?0:minecraft.player.tickCount)+partial);}
        }
        for(var d:nodes) {int x=nodeX(d),y=nodeY(d);boolean done=k.completedResearch().contains(d.key());boolean unlockable=d.available(k);
            // Original only tints icons to ~30% when locked â€” never invisible â€” and pulses
            // brighter when they can be unlocked. Forbidden glow is only drawn behind
            // entries the player can actually see or has done.
            float brightness=done?1:unlockable?0.75f+0.25f*(float)Math.sin((System.currentTimeMillis()%600)/600d*Math.PI*2):0.3f;
            if(d.warp()>0&&(done||unlockable||!d.hidden()))ResearchGraphics.forbidden(g,x,y,minecraft.player==null?0:minecraft.player.tickCount);
            g.setColor(brightness,brightness,brightness,1);com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.blit(ResearchGraphics.texture("gui/gui_research"),x-13,y-13,d.round()?54:d.hidden()?86:d.secondary()?110:0,230,26,26,256,256);
            if(d.special())g.blit(ResearchGraphics.texture("gui/gui_research"),x-13,y-13,26,230,26,26,256,256);
            renderResearchIcon(g,d,x-8,y-8,brightness);g.setColor(1,1,1,1);}

        if(System.currentTimeMillis()<navigationUntil)for(var d:nodes)if(d.key().equals(navigationHighlight)){int x=nodeX(d),y=nodeY(d);g.renderOutline(x-17,y-17,34,34,0xffffdd55);g.renderOutline(x-16,y-16,32,32,0xffb57b21);}

        for(var d:nodes)if(ResearchHighlights.unread(d.key()))ResearchGraphics.discoveryFlame(g,nodeX(d)-9,nodeY(d)-9,1);

        g.flush();g.pose().popPose();g.disableScissor();com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        for(int i=0;i<tabs().size();i++) {var cat=tabs().get(i);if(cat.equals("ELDRITCH")&&!k.completedResearch().contains("ELDRITCHMINOR"))continue;
            int x=leftPos-24,y=topPos+i*24;boolean active=category.equals(cat);com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.blit(ResearchGraphics.texture("gui/gui_research"),x,y,active?152:176,232,24,24,256,256);ResearchGraphics.icon(g,ResourceLocation.parse(thaumcraft.api.research.ResearchApi.books().categories().get(cat).icon()),leftPos-19+(active?0:8),y+4,16,16,1);com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();if(ResearchHighlights.unread(cat))ResearchGraphics.discoveryFlame(g,x+1+(active?0:8),y,1);}
        // Inactive tabs slide under the wooden book frame; no black mask clips their icons.
        frame(g);
    }
    private void renderNoteMessage(GuiGraphics g){
        if(selected!=null||System.nanoTime()/1000000>=popupUntil||popupMessage.isEmpty())return;
        int textWidth=navigationNotice?Math.max(100,imageWidth-48):150;var lines=font.split(net.minecraft.network.chat.FormattedText.of(popupMessage),textWidth);int x=navigationNotice?leftPos+24:leftPos+53,y=navigationNotice?topPos+imageHeight-24-lines.size()*font.lineHeight:topPos+128-lines.size()*font.lineHeight/2;
        if(researchTooltipTop>=0)y=Math.max(4,Math.min(y,researchTooltipTop-lines.size()*font.lineHeight-6));
        g.flush();g.setColor(1,1,1,1);g.pose().pushPose();g.pose().translate(0,0,1200);
        g.fill(x-3,y-3,x+textWidth+3,y+lines.size()*font.lineHeight+3,0x80000000);
        for(var line:lines){g.drawString(font,line,x,y,0xff9085ff,true);y+=font.lineHeight;}g.flush();g.pose().popPose();
    }
    private ResearchDefinition displayResearch(ResearchDefinition definition){return thaumcraft.api.research.ResearchLinks.target(definition,ClientKnowledge.definitions());}
    private void renderResearchIcon(GuiGraphics g,ResearchDefinition d,int x,int y,float brightness){
        d=displayResearch(d);
        if(d.key().equals("BANNERS")){researchItem(g,thaumcraft.items.ThaumicBannerItem.stack(10,null),x,y);return;}if(d.key().equals("PRIMALARROW")){researchItem(g,new ItemStack(thaumcraft.registry.R3Equipment.PRIMAL_ARROWS.get((int)(System.currentTimeMillis()/1000%6)).get()),x,y);return;}
        var id=ResourceLocation.tryParse(d.icon());if(id==null)return;g.setColor(1,1,1,1);
        if(!id.getPath().startsWith("textures/")){researchItem(g,new ItemStack(BuiltInRegistries.ITEM.get(id)),x,y);return;}
        // Use the registered block item for original block-texture references.
        if(id.getPath().equals("textures/blocks/grate.png")){ResearchGraphics.grateIcon(g,x,y,brightness);return;}
        if(id.getPath().startsWith("textures/blocks/")){
            var aliases=Map.of("arcane_stone","arcane_stone","pedestal_side","arcane_pedestal","jar_side","warded_jar","jar_side_void","void_jar","metal_alchemy_side","alchemy_furnace","tube","essentia_tube");
            String name=id.getPath().substring(16,id.getPath().length()-4),mapped=aliases.get(name);
            if(mapped!=null){researchItem(g,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("thaumcraft",mapped))),x,y);return;}
        }
        if(id.getPath().startsWith("textures/items/")){
            var spriteId=ResourceLocation.fromNamespaceAndPath(id.getNamespace(),id.getPath().substring(9,id.getPath().length()-4));
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.blit(x,y,0,16,16,minecraft.getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(spriteId));
        }else ResearchGraphics.icon(g,id,x,y,16,16,brightness);
    }
    private void researchItem(GuiGraphics g,ItemStack stack,int x,int y){g.renderItem(stack,x,y);}
    private record Source(ItemStack item,String fluid,int amount){}
    private List<Source> knownSources(thaumcraft.api.aspects.Aspect aspect){
        var data=ClientKnowledge.aspects();var scanned=ClientKnowledge.snapshot().scanned(thaumcraft.api.knowledge.PlayerKnowledge.ScanKind.OBJECT);var result=new ArrayList<Source>();
        for(var entry:data.objects().entrySet()){
            var id=ResourceLocation.tryParse(entry.getKey());if(id==null)continue;
            if(Set.of("minecraft:water","minecraft:lava").contains(entry.getKey())){int amount=entry.getValue().getOrDefault(aspect.getTag(),0);if(amount>0&&scanned.contains(data.scanGroups().getOrDefault(entry.getKey(),entry.getKey())))result.add(new Source(ItemStack.EMPTY,id.getPath(),amount));continue;}
            if(!BuiltInRegistries.ITEM.containsKey(id))continue;var stack=new ItemStack(BuiltInRegistries.ITEM.get(id));if(stack.isEmpty())continue;
            if(!scanned.contains(thaumcraft.data.StackAspectRules.scanKey(stack,data.scanGroups())))continue;
            var values=thaumcraft.data.StackAspectRules.aspects(stack,entry.getValue(),data.modifiers());int amount=values.getOrDefault(aspect.getTag(),0);if(amount>0)result.add(new Source(stack,"",amount));
        }
        result.sort(Comparator.comparingInt(Source::amount).reversed().thenComparing(s->s.fluid.isEmpty()?BuiltInRegistries.ITEM.getKey(s.item.getItem()).toString():s.fluid));return result;
    }
    private void aspectSources(GuiGraphics g,Hover hover,int mouseX,int mouseY){
        var sources=knownSources(hover.aspect);if(sources.isEmpty())return;int columns=Math.max(1,Math.min(sources.size(),Math.min(8,(width-8)/18))),rows=(sources.size()+columns-1)/columns;
        int x=Math.clamp(Math.max(mouseX+12,hover.x+hover.width+6),4,Math.max(4,width-columns*17-4)),y=Math.clamp(Math.max(mouseY+10,hover.y+27),4,Math.max(4,height-rows*17-4));
        g.pose().pushPose();g.pose().translate(0,0,500);
        for(int i=0;i<sources.size();i++){var source=sources.get(i);int xx=x+i%columns*17,yy=y+i/columns*17;if(source.fluid.isEmpty())g.renderItem(source.item,xx,yy);else {var sprite=minecraft.getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.parse("minecraft:block/"+source.fluid+"_still"));if(source.fluid.equals("water"))g.setColor(.247f,.463f,.894f,1);g.blit(xx,yy,0,16,16,sprite);g.setColor(1,1,1,1);}g.pose().pushPose();g.pose().translate(xx+16,yy+10,150);g.pose().scale(.5f,.5f,1);var count=Integer.toString(source.amount);ResearchGraphics.outlinedNumber(g,font,count,-font.width(count),0);g.pose().popPose();}g.flush();g.pose().popPose();
    }
    private void constructionBlock(GuiGraphics g,ItemStack stack,int x,int y){item(g,stack,x,y,false);}
    private void renderBook(GuiGraphics g) {
        if(nextRecipeCycle<System.currentTimeMillis()){recipeCycle++;nextRecipeCycle=System.currentTimeMillis()+1000;}
        pageHovers.clear();addonHovers.clear();addonCanvases.clear();if(selected!=null)selected=ClientKnowledge.definitions().get(selected.key());if(selected==null)return;if(!categories().contains(category)&&!categories().isEmpty())category=categories().getFirst();int sw=(width-256)/2,sh=(height-181)/2;
        g.pose().pushPose();g.pose().translate((width-256*1.3f)/2,(height-181*1.3f)/2,0);g.pose().scale(0.65f,0.65f,1);g.blit(ResearchGraphics.texture("gui/gui_researchbook"),0,0,0,0,512,362,512,512);g.pose().popPose();
        var pages=pages();spread=Math.clamp(spread,0,(pages.size()-1)/2);
        for(int side=0;side<2;side++) {int index=spread*2+side;if(index>=pages.size())continue;var page=pages.get(index);int x=sw-15+side*152,y=sh-10;
            if(index==0){bookSprite(g,sw+4,sh-13,24,184,96,4);bookSprite(g,sw+4,sh+4,24,184,96,4);var title=Component.translatable("tc.research_name."+displayResearch(selected).key());float scale=Math.min(1,130f/Math.max(1,font.width(title)));g.pose().pushPose();g.pose().translate(sw+52-font.width(title)*scale/2,sh-6,0);g.pose().scale(scale,scale,1);g.drawString(font,title,0,0,ResearchGraphics.INK,false);g.pose().popPose();y+=25;}
            for(var element:page.elements){element.draw(g,font,x,y);y+=element.height();}
            if(!page.recipe.isEmpty())renderRecipe(g,page.recipe,sw-4+side*152,sh-8);
            if(!page.aspects.isEmpty())renderAspects(g,page.aspects,sw-8+side*152,sh-8);}
        if(spread>0)bookSprite(g,sw-16,sh+190,0,184,12,8);if(spread*2+2<pages.size())bookSprite(g,sw+262,sh+190,12,184,12,8);
    }
    static void bookSprite(GuiGraphics g,int x,int y,int u,int v,int w,int h){com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(0.5f,0.5f,1);g.blit(ResearchGraphics.texture("gui/gui_researchbook"),0,0,u*2,v*2,w*2,h*2,512,512);g.pose().popPose();}
    private void heading(GuiGraphics g,Component text,int centre,int y) {
        float scale=Math.min(1,125f/Math.max(1,font.width(text)));g.pose().pushPose();g.pose().translate(centre-font.width(text)*scale/2,y,0);g.pose().scale(scale,scale,1);g.drawString(font,text,0,0,0xff505050,false);g.pose().popPose();
    }
    private void overlay(GuiGraphics g,int x,int y,int u,int v,int w,int h,float scale,float alpha) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.setColor(1,1,1,alpha);g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(scale,scale,1);g.blit(ResearchGraphics.texture("gui/gui_researchbook_overlay"),0,0,u,v,w,h,256,256);g.pose().popPose();g.setColor(1,1,1,1);
    }
    private void item(GuiGraphics g,ItemStack stack,int x,int y,boolean count) {
        if(stack.isEmpty())return;g.renderItem(stack,x,y);if(count)g.renderItemDecorations(font,stack,x,y);pageHovers.add(new Hover(x,y,16,16,stack,null));
    }
    private void tag(GuiGraphics g,thaumcraft.api.aspects.Aspect aspect,int amount,int x,int y,float scale) {
        g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(scale,scale,1);
        if(displayResearch(selected).key().equals("ASPECTS")&&scale==2&&ResearchGraphics.inside(pointerX,pointerY,x,y,32,32))ResearchGraphics.aspectHighlight(g,-3,-3);
        ResearchGraphics.aspect(g,aspect,0,0,1);
        if(amount>0)ResearchGraphics.aspectCount(g,font,Integer.toString(amount),0,0);g.pose().popPose();pageHovers.add(new Hover(x,y,(int)(16*scale),(int)(16*scale),ItemStack.EMPTY,aspect));
    }
    private void renderAspects(GuiGraphics g,List<thaumcraft.api.aspects.Aspect> aspects,int x,int y) {
        var knowledge=ClientKnowledge.snapshot();for(int i=0;i<aspects.size();i++) {
            var aspect=aspects.get(i);int row=y+i*50;tag(g,aspect,knowledge.pool(aspect),x,row,2);
            var name=BookPageLayout.formatted(ResearchGraphics.aspectName(aspect).getString());g.drawString(font,name,x+16-font.width(name)/2,row+33,0xff505050,false);
            if(aspect.isPrimal())g.drawString(font,BookPageLayout.formatted(I18n.get("tc.aspect.primal")),x+48,row+12,0xff444444,false);
            else {g.drawString(font,"=",x+39,row+12,0xff999999,false);g.drawString(font,"+",x+83,row+12,0xff999999,false);
                for(int j=0;j<2;j++){var component=aspect.getComponents().get(j);int cx=x+54+j*42;tag(g,component,0,cx,row+4,1.5f);var label=BookPageLayout.formatted("Â§o"+ResearchGraphics.aspectName(component).getString());float labelScale=Math.min(1,36f/Math.max(1,font.width(label)));g.pose().pushPose();g.pose().translate(cx+12-font.width(label)*labelScale/2,row+30,0);g.pose().scale(labelScale,labelScale,1);g.drawString(font,label,0,0,0xff505050,false);g.pose().popPose();}
            }
        }
    }
    private void renderRecipe(GuiGraphics g,String key,int x,int y) {
        var variants=selected.recipeVariants(key);if(variants.isEmpty())variants=List.of(key);int index=0;if(variants.size()>1){if(recipeCycle>=variants.size())recipeCycle=0;index=recipeCycle;}key=thaumcraft.data.ResearchRecipePages.resolve(variants.get(index));
        if(key.startsWith("@")){renderTransformation(g,key.substring(1),x,y);return;}
        if(minecraft.level==null)return;var holder=minecraft.level.getRecipeManager().byKey(ResourceLocation.parse(key.contains(":")?key:"thaumcraft:"+key));if(holder.isEmpty())return;
        var recipe=holder.get().value();
        if(recipe instanceof thaumcraft.api.crafting.InfusionRecipe infusion){renderInfusion(g,infusion,x,y);return;}
        if(recipe instanceof thaumcraft.api.crafting.CrucibleRecipe crucible){
            heading(g,Component.translatable("recipe.type.crucible"),x+56,y);
            overlay(g,x,y+28,0,3,56,17,2,1);overlay(g,x,y+92,0,20,56,48,2,1);overlay(g,x+42,y+76,100,84,11,13,2,1);
            item(g,crucible.getRecipeOutput(),x+48,y+36,true);var options=crucible.catalyst().getItems();if(options.length>0)item(g,options[Math.floorMod((int)(System.currentTimeMillis()/1000),options.length)],x+26,y+72,false);
            var tags=crucible.aspects().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();int rows=(tags.size()-1)/3,shift=(3-tags.size()%3)*10;
            for(int i=0;i<tags.size();i++){var tag=tags.get(i);int offset=i/3>=rows&&(rows>1||tags.size()<3)?shift:0;tag(g,thaumcraft.api.aspects.Aspect.getAspect(tag.getKey()),tag.getValue(),x+28+i%3*20+offset,y+128-10*rows+i/3*20,1);}return;
        }
        var ingredients=recipe.getIngredients();boolean arcane=recipe instanceof thaumcraft.api.crafting.IArcaneRecipe;
        heading(g,Component.translatable(arcane?"recipe.type.arcane":recipe instanceof ShapelessRecipe?"recipe.type.workbenchshapeless":"recipe.type.workbench"),x+56,y);
        overlay(g,x+4,y+(arcane?54:64),arcane?112:60,15,52,52,2,1);overlay(g,x+40,y+(arcane?14:24),20,3,16,16,2,1);
        item(g,recipe.getResultItem(minecraft.level.registryAccess()),x+48,y+(arcane?22:32),true);
        int columns=recipe instanceof ShapedRecipe shaped?shaped.getWidth():recipe instanceof ArcaneRecipe a?a.pattern().width():3;
        for(int i=0;i<ingredients.size()&&i<9;i++){var options=ingredients.get(i).getCustomIngredient() instanceof thaumcraft.crafting.BlueDyeIngredient?new net.minecraft.world.item.ItemStack[]{new net.minecraft.world.item.ItemStack(thaumcraft.crafting.BlueDyeIngredient.selectedItem())}:ingredients.get(i).getItems();if(options.length>0)item(g,options[Math.floorMod((int)(System.currentTimeMillis()/1000),options.length)],x+16+i%columns*32,y+(arcane?66:76)+i/columns*32,false);}
        if(recipe instanceof thaumcraft.api.crafting.IArcaneRecipe a){overlay(g,x,y+164,68,76,12,12,2,0.4f);var tags=a.vis().entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).toList();for(int i=0;i<tags.size();i++){var tag=tags.get(i);tag(g,thaumcraft.api.aspects.Aspect.getAspect(tag.getKey()),tag.getValue(),x+14+i*18+(5-tags.size())*8,y+172,1);}}
    }
    private ItemStack option(Ingredient ingredient){var options=ingredient.getItems();return options.length==0?ItemStack.EMPTY:options[Math.floorMod((int)(System.currentTimeMillis()/1000),options.length)];}
    private ItemStack infusionInput(thaumcraft.api.crafting.InfusionRecipe recipe){
        if(recipe instanceof thaumcraft.crafting.InfusionRunicRecipe){var stack=new ItemStack(thaumcraft.registry.R2Content.GOGGLES.get());for(int n=0;n<Math.floorMod((int)(System.currentTimeMillis()/2000),5);n++)thaumcraft.equipment.RunicShield.harden(stack);return stack;}
        if(recipe instanceof thaumcraft.crafting.InfusionEnchantmentRecipeData enchantment){
            var examples=List.of(net.minecraft.world.item.Items.DIAMOND_SWORD,net.minecraft.world.item.Items.DIAMOND_PICKAXE,net.minecraft.world.item.Items.BOW,net.minecraft.world.item.Items.DIAMOND_HELMET,net.minecraft.world.item.Items.DIAMOND_CHESTPLATE,net.minecraft.world.item.Items.DIAMOND_LEGGINGS,net.minecraft.world.item.Items.DIAMOND_BOOTS,net.minecraft.world.item.Items.FISHING_ROD).stream().map(ItemStack::new).filter(stack->enchantment.enchantment().value().canEnchant(stack)).toList();
            if(!examples.isEmpty())return examples.get(Math.floorMod((int)(System.currentTimeMillis()/2000),examples.size()));
        }return option(recipe.input());
    }
    private void renderInfusion(GuiGraphics g,thaumcraft.api.crafting.InfusionRecipe recipe,int x,int y){
        heading(g,Component.translatable("recipe.type.infusion"),x+56,y);overlay(g,x,y+20,0,3,56,17,2,1);overlay(g,x,y+58,200,77,60,44,2,1);var central=infusionInput(recipe);item(g,recipe.getRecipeOutput(central),x+48,y+28,true);item(g,central,x+48,y+94,false);
        var components=recipe.components(central);for(int i=0;i<components.size();i++){double angle=Math.toRadians(-90+360d*i/components.size());item(g,option(components.get(i)),x+48+(int)(Math.cos(angle)*40),y+94+(int)(Math.sin(angle)*40),false);}
        var costs=recipe.aspects(central).entrySet().stream().sorted(Comparator.comparing(e->thaumcraft.api.aspects.Aspect.getAspect(e.getKey()).getName())).toList();int rows=(costs.size()-1)/5,shift=(5-costs.size()%5)*10;for(int i=0;i<costs.size();i++){var cost=costs.get(i);int offset=i/5>=rows&&(rows>1||costs.size()<5)?shift:0;tag(g,thaumcraft.api.aspects.Aspect.getAspect(cost.getKey()),cost.getValue(),x+8+i%5*20+offset,y+164-rows*10+i/5*20,1);}
        if(recipe.experienceCost(central)>0)heading(g,Component.translatable("container.enchant.level.requirement",recipe.experienceCost(central)),x+56,y+182);
        heading(g,Component.translatable("tc.inst").append(" ").append(Component.translatable("tc.inst."+Math.min(5,recipe.instability(central)/2))),x+56,y+194);
    }
    private void renderConstruction(GuiGraphics g,String key,int x,int y){
        int dx=key.equals("thaumatorium")?1:3,dz=dx,dy=key.equals("advanced_alchemy_furnace")?2:key.equals("node_jar")?4:3;var materials=new ArrayList<ItemStack>();var emptySpaces=new java.util.HashSet<Integer>();
        if(key.equals("thaumatorium")){materials.add(new ItemStack(thaumcraft.registry.R3Content.ALCHEMICAL_CONSTRUCT_ITEM.get()));materials.add(new ItemStack(thaumcraft.registry.R3Content.ALCHEMICAL_CONSTRUCT_ITEM.get()));materials.add(new ItemStack(thaumcraft.registry.R3Content.CRUCIBLE.get().asItem()));}
        else if(key.equals("advanced_alchemy_furnace")){for(int layer=0;layer<2;layer++)for(int z=2;z>=0;z--)for(int xx=2;xx>=0;xx--){if(layer==0&&xx==1&&z==1){emptySpaces.add(materials.size());materials.add(ItemStack.EMPTY);}else materials.add(new ItemStack((layer==0?(xx!=1&&z!=1?thaumcraft.registry.R3Content.ALEMBIC.get():thaumcraft.registry.R3Content.ALCHEMICAL_CONSTRUCT.get()):(xx==1&&z==1?thaumcraft.registry.R3Content.ALCHEMY_FURNACE.get():thaumcraft.registry.R4Content.ADVANCED_CONSTRUCT.get())).asItem()));}}
        else if(key.equals("infernal_furnace")){for(int layer=0;layer<3;layer++)for(int z=2;z>=0;z--)for(int xx=2;xx>=0;xx--){int part=xx+z*3+(2-layer)*9;if(part==22){emptySpaces.add(materials.size());materials.add(ItemStack.EMPTY);}else if(part==13)materials.add(new ItemStack(net.minecraft.world.item.Items.LAVA_BUCKET));else materials.add(new ItemStack(thaumcraft.production.InfernalFurnaceBlock.original(part,net.minecraft.core.Direction.SOUTH).getBlock().asItem()));}}
        else if(key.equals("node_jar")){for(int layer=0;layer<4;layer++)for(int z=2;z>=0;z--)for(int xx=2;xx>=0;xx--){if(layer==0)materials.add(new ItemStack(net.minecraft.world.item.Items.OAK_SLAB));else if(layer==2&&xx==1&&z==1)materials.add(new ItemStack(thaumcraft.registry.R2Content.WISP_ESSENCE.get()));else materials.add(new ItemStack(net.minecraft.world.item.Items.GLASS));}}
        else for(int layer=0;layer<3;layer++)for(int z=2;z>=0;z--)for(int xx=2;xx>=0;xx--){if(layer==0&&xx!=1&&z!=1)emptySpaces.add(materials.size());ItemStack stack=ItemStack.EMPTY;if(layer==0&&xx==1&&z==1)stack=new ItemStack(thaumcraft.registry.R3Content.INFUSION_MATRIX_ITEM.get());else if(layer>0&&xx!=1&&z!=1)stack=new ItemStack((layer==1?thaumcraft.registry.R3Content.ARCANE_STONE_ITEM:thaumcraft.registry.R3Content.ARCANE_STONE_BRICKS_ITEM).get());else if(layer==2&&xx==1&&z==1)stack=new ItemStack(thaumcraft.registry.R3Content.ARCANE_PEDESTAL_ITEM.get());materials.add(stack);}
        int xoff=64-(dx*16+dz*16)/2,yoff=-dy*25;overlay(g,x-8,y+108+yoff-119+Math.max(3-dx,3-dz)*8+dx*4+dz*4+dy*50,0,72,64,44,2,.5f);
        int index=0;for(int layer=0;layer<dy;layer++)for(int z=dz-1;z>=0;z--)for(int xx=dx-1;xx>=0;xx--){g.pose().pushPose();g.pose().translate(0,0,60-layer*10);int cellX=x+xoff+xx*16+z*16,cellY=y+108+yoff-xx*8+z*8+layer*50;if(emptySpaces.contains(index)){ResearchGraphics.emptySpaceIcon(g,cellX,cellY);pageHovers.add(new Hover(cellX,cellY,16,16,ItemStack.EMPTY,null));}else constructionBlock(g,materials.get(index),cellX,cellY);index++;g.pose().popPose();}
        var costs=new LinkedHashMap<String,Integer>();if(key.equals("infernal_furnace")){costs.put("ignis",50);costs.put("terra",50);}else if(key.equals("thaumatorium")){costs.put("ignis",15);costs.put("ordo",30);costs.put("aqua",30);}else if(key.equals("advanced_alchemy_furnace")){costs.put("ignis",50);costs.put("aqua",50);costs.put("ordo",50);}else if(key.equals("node_jar"))for(String tag:List.of("ignis","terra","ordo","aer","perditio","aqua"))costs.put(tag,70);else for(String tag:List.of("ignis","terra","ordo","aer","perditio","aqua"))costs.put(tag,25);overlay(g,x,y+174,68,76,12,12,2,.4f);int n=0;for(var cost:costs.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).toList()){tag(g,thaumcraft.api.aspects.Aspect.getAspect(cost.getKey()),cost.getValue(),x+14+18*n+++(5-costs.size())*8,y+182,1);}
    }
    private void renderTransformation(GuiGraphics g,String key,int x,int y) {
        heading(g,Component.translatable("recipe.type.construct"),x+56,y);
        if(key.equals("thaumatorium")||key.equals("infusion_altar")||key.equals("advanced_alchemy_furnace")||key.equals("node_jar")||key.equals("infernal_furnace")){renderConstruction(g,key,x,y);return;}
        var wand=new ItemStack(thaumcraft.registry.R2Content.WAND.get());var table=new ItemStack(thaumcraft.registry.R2Content.TABLE_ITEM.get());
        int depth=key.equals("research_table")?2:1,offset=64-(16+depth*16)/2;
        overlay(g,x-8,y+39+Math.max(2,3-depth)*8+4+depth*4,0,72,64,44,2,0.5f);
        if(key.equals("research_table")){item(g,new ItemStack(thaumcraft.registry.R2Content.SCRIBING_TOOLS.get()),x+offset,y+58,false);constructionBlock(g,table,x+offset+16,y+116);constructionBlock(g,table,x+offset,y+108);}
        else {item(g,wand,x+offset,y+58,false);constructionBlock(g,key.equals("thaumonomicon")?new ItemStack(net.minecraft.world.item.Items.BOOKSHELF):table,x+offset,y+108);}
    }
    @Override protected void renderFitted(GuiGraphics g,int x,int y,float partial) {researchTooltipTop=-1;pointerX=x;pointerY=y;renderBackground(g,x,y,partial);super.renderFitted(g,x,y,partial);
        if(selected==null) {var d=hovered(x,y);if(d!=null)researchTooltip(g,d,x,y);else for(int i=0;i<tabs().size();i++){var cat=tabs().get(i);if(cat.equals("ELDRITCH")&&!ClientKnowledge.snapshot().completedResearch().contains("ELDRITCHMINOR"))continue;if(ResearchGraphics.inside(x,y,leftPos-24,topPos+i*24,24,24)){fittedTooltip(g,Component.translatable(thaumcraft.api.research.ResearchApi.books().categories().get(cat).name()),x,y);break;}}}
        else {for(int i=pageHovers.size()-1;i>=0;i--){var hover=pageHovers.get(i);if(ResearchGraphics.inside(x,y,hover.x,hover.y,hover.width,hover.height)){if(hover.aspect!=null){if(displayResearch(selected).key().equals("ASPECTS")&&hover.width>=32)aspectSources(g,hover,x,y);else fittedTooltip(g,ResearchGraphics.aspectTooltip(hover.aspect),x,y);}else fittedTooltip(g,hover.item.isEmpty()?List.of(Component.translatable("tile.blockHole.name").withStyle(net.minecraft.ChatFormatting.AQUA)):net.minecraft.client.gui.screens.Screen.getTooltipFromItem(minecraft,hover.item),x,y);break;}}}
        if(selected!=null)for(var hover:addonHovers)if(ResearchGraphics.inside(x,y,hover.x,hover.y,hover.width,hover.height)){fittedTooltip(g,hover.tooltip,x,y);break;}
        renderNoteMessage(g);
    }
    private void researchTooltip(GuiGraphics g,ResearchDefinition d,int mouseX,int mouseY){
        d=displayResearch(d);
        var k=ClientKnowledge.snapshot();boolean completed=k.completedResearch().contains(d.key()),unlockable=d.available(k);
        boolean secondary=!completed&&d.secondary()&&!d.aspects().isEmpty(),primary=!secondary&&!completed;
        var costs=d.aspects().entrySet().stream().filter(e->thaumcraft.api.aspects.Aspect.getAspect(e.getKey())!=null).sorted(Map.Entry.comparingByKey()).toList();
        boolean enough=costs.stream().allMatch(e->{var a=thaumcraft.api.aspects.Aspect.getAspect(e.getKey());return k.knows(a)&&k.pool(a)>=e.getValue();});
        int maxWidth=Math.min(180,Math.max(32,width-16));
        var title=font.split(Component.translatable("tc.research_name."+d.key()),maxWidth);
        var description=font.split(Component.translatable("tc.research_text."+d.key()),maxWidth*2);
        var forbidden=d.warp()>0?font.split(Component.literal(I18n.get("tc.forbidden",I18n.get("tc.forbidden.level."+Math.min(5,d.warp())))),maxWidth*2):List.<FormattedCharSequence>of();
        boolean existing=primary&&hasNote(d.key());
        var advice=primary?font.split(Component.translatable(existing?"tc.research.hasnote":hasScribingSupplies()?"tc.research.getprim":"tc.research.shortprim"),maxWidth*2):secondary?font.split(Component.translatable(enough?"tc.research.purchase":"tc.research.short"),maxWidth*2):List.<FormattedCharSequence>of();
        int contentW=0;for(var line:title)contentW=Math.max(contentW,font.width(line));
        for(var lines:List.of(description,forbidden,advice))for(var line:lines)contentW=Math.max(contentW,(font.width(line)+1)/2);
        int columns=Math.max(1,maxWidth/18),costRows=(costs.size()+columns-1)/columns;
        if(secondary)contentW=Math.max(contentW,Math.min(columns,costs.size())*18);
        int h=title.size()*font.lineHeight+3+description.size()*5+(forbidden.isEmpty()?0:4+forbidden.size()*5)+(secondary?4+costRows*18:0)+(advice.isEmpty()?0:4+advice.size()*5);
        int x=mouseX+6,y=mouseY-4;
        if(x+contentW+3>width)x=mouseX-contentW-6;x=Math.max(4,Math.min(x,width-contentW-4));y=Math.max(4,Math.min(y,height-h-4));
        researchTooltipTop=y;
        g.pose().pushPose();g.pose().translate(0,0,500);
        g.fill(x-3,y-3,x+contentW+3,y+h+3,0xf0100010);g.fill(x-3,y-3,x+contentW+3,y-2,0xff500080);g.fill(x-3,y+h+2,x+contentW+3,y+h+3,0xff280040);
        int titleColor=d.special()?(unlockable||completed?0xffffffa0:0xffd6cf8b):(unlockable||completed?0xffffffff:0xff808080),row=y;
        for(var line:title){g.drawString(font,line,x,row,titleColor,false);row+=font.lineHeight;}row+=3;
        row=tooltipSmall(g,description,x,row,0xff9085ff);
        if(!forbidden.isEmpty())row=tooltipSmall(g,forbidden,x,row+4,0xffffffff);
        if(secondary){row+=4;int i=0;
            for(var cost:costs){var aspect=thaumcraft.api.aspects.Aspect.getAspect(cost.getKey());int px=x+(i%columns)*18,py=row+(i/columns)*18;i++;
                if(k.knows(aspect)){float alpha=k.pool(aspect)>=cost.getValue()?1:.75f+.25f*(float)Math.sin((System.currentTimeMillis()%600)/600d*Math.PI*2);ResearchGraphics.aspect(g,aspect,px,py,alpha);}
                else{g.setColor(.5f,.5f,.5f,.5f);ResearchGraphics.blit(g,ResearchGraphics.texture("aspects/_unknown"),px,py,16,16,0,0,32,32,32,32);g.setColor(1,1,1,1);}
                String amount=Integer.toString(cost.getValue());g.pose().pushPose();g.pose().translate(px+16,py+10,10);g.pose().scale(.5f,.5f,1);g.drawString(font,amount,-font.width(amount),0,0xffffffff,true);g.pose().popPose();
            }row+=costRows*18;
        }
        if(!advice.isEmpty())tooltipSmall(g,advice,x,row+4,existing?0xffffa700:secondary&&enough||primary&&hasScribingSupplies()?0xff87cdeb:0xffdc143c);
        g.flush();g.pose().popPose();
    }
    private int tooltipSmall(GuiGraphics g,List<FormattedCharSequence> lines,int x,int y,int color){
        g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(.5f,.5f,1);
        int row=0;for(var line:lines){g.drawString(font,line,0,row,color,false);row+=10;}g.pose().popPose();return y+lines.size()*5;
    }
    @Override protected boolean mouseClickedFitted(double x,double y,int button) {
        if(minecraft.player==null)return super.mouseClickedFitted(x,y,button);if(children().stream().anyMatch(widget->widget instanceof net.minecraft.client.gui.components.AbstractWidget w&&w.visible&&w.isMouseOver(x,y)))return super.mouseClickedFitted(x,y,button);
        if(selected!=null){for(var hover:addonHovers)if(hover.click!=null&&ResearchGraphics.inside(x,y,hover.x,hover.y,hover.width,hover.height)){hover.click.run();return true;}for(var canvas:addonCanvases){var renderer=thaumcraft.api.client.BookRenderers.get(canvas.page.type());if(renderer!=null&&renderer.mouseClicked(canvas.page,x-canvas.x,y-canvas.y,button))return true;}}
        if(selected!=null) {int sw=(width-256)/2,sh=(height-181)/2;if(ResearchGraphics.inside(x,y,sw-18,sh+187,20,16)){spread--;play(thaumcraft.registry.R2Sounds.PAGE.get(),.66f);return true;}if(ResearchGraphics.inside(x,y,sw+260,sh+187,20,16)){spread++;play(thaumcraft.registry.R2Sounds.PAGE.get(),.66f);return true;}return super.mouseClickedFitted(x,y,button);}
        var k=ClientKnowledge.snapshot();for(int i=0;i<tabs().size();i++)if(ResearchGraphics.inside(x,y,leftPos-24,topPos+i*24,24,24)) {var cat=tabs().get(i);if(!cat.equals("ELDRITCH")||k.completedResearch().contains("ELDRITCHMINOR")){category=cat;centreCategory();play(thaumcraft.registry.R2Sounds.PAGE.get(),.66f);}return true;}
        var d=hovered(x,y);if(d!=null) {
            if(k.completedResearch().contains(d.key())) {selected=d;spread=recipeCycle=0;nextRecipeCycle=0;play(thaumcraft.registry.R2Sounds.PAGE.get(),.66f);}
            else if(d.available(k)){
                popupUntil=0;if(!d.secondary()&&(!hasScribingSupplies()||hasNote(d.key())))return true;
                PacketDistributor.sendToServer(new ResearchActionPayload(menu.containerId,menu.token(),++sequence,0,d.key(),"",0,0));
            }
            return true;}
        return super.mouseClickedFitted(x,y,button);
    }
    private void play(net.minecraft.sounds.SoundEvent sound,float volume){if(minecraft.player!=null)minecraft.player.playSound(sound,volume,1);}
    @Override public void onClose(){thaumcraft.config.ThaumcraftClientConfig.SPEC.save();ResearchHighlights.clear();super.onClose();}
    @Override protected boolean mouseDraggedFitted(double x,double y,int button,double dx,double dy) {if(selected==null&&button==0&&inTree(x,y)){panX+=dx/treeZoom;panY+=dy/treeZoom;return true;}return super.mouseDraggedFitted(x,y,button,dx,dy);}
    @Override protected boolean mouseScrolledFitted(double x,double y,double horizontal,double vertical){if(selected==null&&ResearchGraphics.inside(x,y,leftPos-24,topPos,24,imageHeight)){tabOffset-=Math.signum(vertical);return true;}if(selected==null&&inTree(x,y)){zoom(treeZoom*Math.pow(1.1,vertical),x,y);return true;}return super.mouseScrolledFitted(x,y,horizontal,vertical);}
    @Override public boolean keyPressed(int key,int scan,int modifiers) {if(key==256&&selected!=null){selected=null;return true;}return super.keyPressed(key,scan,modifiers);}
}
