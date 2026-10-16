package thaumcraft.entities;

public enum GolemMaterial {
    STRAW(10,0,.38,false,1,1,75,0),WOOD(20,6,.35,false,1,4,75,1),TALLOW(20,9,.33,false,2,8,75,2),CLAY(25,9,.33,true,1,8,100,2),FLESH(15,6,.35,false,2,4,40,1),STONE(30,12,.32,true,1,16,100,3),IRON(35,15,.31,true,1,32,125,4),THAUMIUM(40,15,.32,true,2,32,100,4);
    public final int health,armor,upgrades,carry,regenDelay,strength;
    public final double speed;public final boolean fireResist;
    GolemMaterial(int health,int armor,double speed,boolean fireResist,int upgrades,int carry,int regenDelay,int strength){this.health=health;this.armor=armor;this.speed=speed;this.fireResist=fireResist;this.upgrades=upgrades;this.carry=carry;this.regenDelay=regenDelay;this.strength=strength;}
    public static GolemMaterial byId(int id){return values()[Math.max(0,Math.min(values().length-1,id))];}
}
