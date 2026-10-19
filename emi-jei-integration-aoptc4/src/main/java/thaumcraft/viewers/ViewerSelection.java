package thaumcraft.viewers;
/** Pure selection policy, also used by the dependency-isolation matrix. */
public enum ViewerSelection {
 NONE,JEI,EMI;
 public static ViewerSelection select(boolean jei,boolean emi){return emi?EMI:jei?JEI:NONE;}
}
