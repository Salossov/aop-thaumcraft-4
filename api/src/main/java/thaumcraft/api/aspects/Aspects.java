package thaumcraft.api.aspects;

import java.util.List;

/** Original 48 definitions from TC4 4.2.3.5 Aspect.java. */
public final class Aspects {
    public static final Aspect AIR = Aspect.register("aer", 16777086, "e", 1);
    public static final Aspect EARTH = Aspect.register("terra", 5685248, "2", 1);
    public static final Aspect FIRE = Aspect.register("ignis", 16734721, "c", 1);
    public static final Aspect WATER = Aspect.register("aqua", 3986684, "3", 1);
    public static final Aspect ORDER = Aspect.register("ordo", 14013676, "7", 1);
    public static final Aspect ENTROPY = Aspect.register("perditio", 4210752, "8", 771);
    public static final Aspect VOID = Aspect.register("vacuos", 8947848, "f", 771, AIR, ENTROPY);
    public static final Aspect LIGHT = Aspect.register("lux", 16774755, "f", 771, AIR, FIRE);
    public static final Aspect WEATHER = Aspect.register("tempestas", 16777215, "f", 771, AIR, WATER);
    public static final Aspect MOTION = Aspect.register("motus", 13487348, "f", 771, AIR, ORDER);
    public static final Aspect COLD = Aspect.register("gelum", 14811135, "f", 771, FIRE, ENTROPY);
    public static final Aspect CRYSTAL = Aspect.register("vitreus", 8454143, "f", 771, EARTH, ORDER);
    public static final Aspect LIFE = Aspect.register("victus", 14548997, "f", 771, WATER, EARTH);
    public static final Aspect POISON = Aspect.register("venenum", 9039872, "f", 771, WATER, ENTROPY);
    public static final Aspect ENERGY = Aspect.register("potentia", 12648447, "f", 771, ORDER, FIRE);
    public static final Aspect EXCHANGE = Aspect.register("permutatio", 5735255, "f", 771, ENTROPY, ORDER);
    public static final Aspect METAL = Aspect.register("metallum", 11908557, "f", 771, EARTH, CRYSTAL);
    public static final Aspect DEATH = Aspect.register("mortuus", 8943496, "f", 771, LIFE, ENTROPY);
    public static final Aspect FLIGHT = Aspect.register("volatus", 15198167, "f", 771, AIR, MOTION);
    public static final Aspect DARKNESS = Aspect.register("tenebrae", 2236962, "f", 771, VOID, LIGHT);
    public static final Aspect SOUL = Aspect.register("spiritus", 15461371, "f", 771, LIFE, DEATH);
    public static final Aspect HEAL = Aspect.register("sano", 16723764, "f", 771, LIFE, ORDER);
    public static final Aspect TRAVEL = Aspect.register("iter", 14702683, "f", 771, MOTION, EARTH);
    public static final Aspect ELDRITCH = Aspect.register("alienis", 8409216, "f", 771, VOID, DARKNESS);
    public static final Aspect MAGIC = Aspect.register("praecantatio", 9896128, "f", 771, VOID, ENERGY);
    public static final Aspect AURA = Aspect.register("auram", 16761087, "f", 771, MAGIC, AIR);
    public static final Aspect TAINT = Aspect.register("vitium", 8388736, "f", 771, MAGIC, ENTROPY);
    public static final Aspect SLIME = Aspect.register("limus", 129024, "f", 771, LIFE, WATER);
    public static final Aspect PLANT = Aspect.register("herba", 109568, "f", 771, LIFE, EARTH);
    public static final Aspect TREE = Aspect.register("arbor", 8873265, "f", 771, AIR, PLANT);
    public static final Aspect BEAST = Aspect.register("bestia", 10445833, "f", 771, MOTION, LIFE);
    public static final Aspect FLESH = Aspect.register("corpus", 15615885, "f", 771, DEATH, BEAST);
    public static final Aspect UNDEAD = Aspect.register("exanimis", 3817472, "f", 771, MOTION, DEATH);
    public static final Aspect MIND = Aspect.register("cognitio", 16761523, "f", 771, FIRE, SOUL);
    public static final Aspect SENSES = Aspect.register("sensus", 1038847, "f", 771, AIR, SOUL);
    public static final Aspect MAN = Aspect.register("humanus", 16766912, "f", 771, BEAST, MIND);
    public static final Aspect CROP = Aspect.register("messis", 14791537, "f", 771, PLANT, MAN);
    public static final Aspect MINE = Aspect.register("perfodio", 14471896, "f", 771, MAN, EARTH);
    public static final Aspect TOOL = Aspect.register("instrumentum", 4210926, "f", 771, MAN, ORDER);
    public static final Aspect HARVEST = Aspect.register("meto", 15641986, "f", 771, CROP, TOOL);
    public static final Aspect WEAPON = Aspect.register("telum", 12603472, "f", 771, TOOL, FIRE);
    public static final Aspect ARMOR = Aspect.register("tutamen", 49344, "f", 771, TOOL, EARTH);
    public static final Aspect HUNGER = Aspect.register("fames", 10093317, "f", 771, LIFE, VOID);
    public static final Aspect GREED = Aspect.register("lucrum", 15121988, "f", 771, MAN, HUNGER);
    public static final Aspect CRAFT = Aspect.register("fabrico", 8428928, "f", 771, MAN, TOOL);
    public static final Aspect CLOTH = Aspect.register("pannus", 15395522, "f", 771, TOOL, BEAST);
    public static final Aspect MECHANISM = Aspect.register("machina", 8421536, "f", 771, MOTION, TOOL);
    public static final Aspect TRAP = Aspect.register("vinculum", 10125440, "f", 771, MOTION, ENTROPY);
    public static final List<Aspect> PRIMALS = List.of(AIR, FIRE, EARTH, WATER, ORDER, ENTROPY);
    public static void initialize() {}
    private Aspects() {}
}
