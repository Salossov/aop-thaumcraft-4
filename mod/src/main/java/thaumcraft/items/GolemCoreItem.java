package thaumcraft.items;
import net.minecraft.world.item.*;
public final class GolemCoreItem extends Item {
    public enum Kind {FILL,EMPTY,GATHER,HARVEST,GUARD,LIQUID,ALCHEMY,LUMBER,USE,BUTCHER,SORTING,FISHING,BLANK}
    public final Kind kind;
    public GolemCoreItem(Kind kind){super(new Properties().rarity(kind==Kind.BLANK?Rarity.COMMON:Rarity.UNCOMMON));this.kind=kind;}
}
