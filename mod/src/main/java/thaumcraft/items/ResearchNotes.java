package thaumcraft.items;

import java.util.LinkedHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.api.research.ResearchNote;

/** Copy-on-write vanilla CustomData component; no mutable tag is retained on a stack. */
public final class ResearchNotes {
    public static ResearchNote read(ItemStack stack) {
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if(!root.contains("thaumcraftNote",Tag.TAG_COMPOUND)) return null;var data=root.getCompound("thaumcraftNote");
        var grid=new LinkedHashMap<ResearchNote.Hex,ResearchNote.Entry>();
        for(var value:data.getList("grid",Tag.TAG_COMPOUND)) {
            var cell=(CompoundTag)value;grid.put(new ResearchNote.Hex(cell.getInt("q"),cell.getInt("r")),new ResearchNote.Entry(cell.getString("aspect"),cell.getInt("type")));
        }
        return new ResearchNote(data.getString("key"),grid,data.getBoolean("complete"),data.getInt("copies"));
    }
    public static void write(ItemStack stack,ResearchNote note) {
        var data=new CompoundTag();data.putString("key",note.key);data.putBoolean("complete",note.complete);data.putInt("copies",note.copies);
        var grid=new ListTag();note.grid.forEach((hex,entry)->{var cell=new CompoundTag();cell.putInt("q",hex.q());cell.putInt("r",hex.r());cell.putString("aspect",entry.aspect());cell.putInt("type",entry.type());grid.add(cell);});data.put("grid",grid);
        CustomData.update(DataComponents.CUSTOM_DATA,stack,root->root.put("thaumcraftNote",data));
        stack.set(DataComponents.RARITY,note.complete?net.minecraft.world.item.Rarity.EPIC:net.minecraft.world.item.Rarity.RARE);
    }
    private ResearchNotes() {}
}
