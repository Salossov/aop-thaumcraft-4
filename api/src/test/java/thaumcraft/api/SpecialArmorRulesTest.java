package thaumcraft.api;

import java.util.List;

public final class SpecialArmorRulesTest {
    private static int checks;
    private static void check(boolean value,String message) {checks++;if(!value)throw new AssertionError(message);}
    private static SpecialArmorProperties piece(int slot,int priority,double ratio,int maximum) {
        var result=new SpecialArmorProperties(priority,ratio,maximum);result.slot=slot;return result;
    }
    private static SpecialArmorProperties piece(int slot,double ratio) {return piece(slot,0,ratio,Integer.MAX_VALUE);}
    private static void close(float actual,double expected,String message) {check(Math.abs(actual-expected)<1E-5,message+": "+actual);}
    public static void main(String[] args) {
        close(SpecialArmorProperties.apply(10,List.of()).remaining(),10,"empty set");
        var full=List.of(piece(0,3*.0425),piece(1,6*.0425),piece(2,8*.0425),piece(3,3*.0425));
        var protectedDamage=SpecialArmorProperties.apply(10,full);
        close(protectedDamage.remaining(),1.5,"original complete ichor set absorbs 85 percent");
        check(protectedDamage.absorbed().size()==4,"each absorbing piece receives its own durability callback");
        check(full.getFirst().absorbRatio==3*.0425&&full.getFirst().slot==0,"normalization never mutates the caller's properties");
        close(SpecialArmorProperties.apply(10,List.of(piece(0,.12),piece(1,.24),piece(2,.32),piece(3,.12))).remaining(),2,"ordinary armor in a legacy set uses linear defense");
        close(SpecialArmorProperties.apply(10,List.of(piece(0,.12),piece(1,.24),piece(2,.32),piece(3,.1275))).remaining(),1.925,"mixed ordinary and ichor coefficients add at equal priority");
        var priority=SpecialArmorProperties.apply(10,List.of(piece(0,0,.25,Integer.MAX_VALUE),piece(1,1,.5,Integer.MAX_VALUE)));
        close(priority.remaining(),3.75,"higher priority absorbs before the lower priority fraction");
        check(priority.absorbed().getFirst().slot()==1&&priority.absorbed().getFirst().durabilityDamage()==5,"callbacks follow actual priority order");
        var limited=SpecialArmorProperties.apply(10,List.of(piece(0,1,.5,25),piece(1,0,.5,Integer.MAX_VALUE)));
        close(limited.remaining(),4.5,"maximum 25 is one damage point, before subsequent priority");
        check(limited.absorbed().getFirst().amount()==1&&limited.absorbed().getFirst().durabilityDamage()==1,"maximum retains original scaled units");
        var saturation=SpecialArmorProperties.apply(10,List.of(piece(0,.8),piece(1,.8)));
        close(saturation.remaining(),0,"ratios above one normalize at equal priority");
        check(saturation.absorbed().stream().allMatch(a->a.amount()==5&&a.durabilityDamage()==5),"overlapping ratios distribute damage instead of double counting");
        var tiny=SpecialArmorProperties.apply(.01,List.of(piece(0,.1)));
        check(tiny.absorbed().getFirst().durabilityDamage()==1,"every positive absorption causes at least one durability point");
        check(SpecialArmorProperties.apply(10,List.of(piece(0,0))).absorbed().isEmpty(),"a zero ratio receives no callback");
        var recovered=SpecialArmorProperties.apply(20.932944020291224,List.of(piece(0,2,1.9,444),piece(1,2,1.2,175),piece(3,0,.4,402)));
        check(recovered.recoveredFromLoop(),"exact confirmed source hang selects the approved bounded recovery");
        close(recovered.remaining(),0,"bounded allocation resolves the source hang without exceeding incoming damage");
        check(recovered.absorbed().size()==2&&recovered.absorbed().stream().noneMatch(a->a.slot()==3),"saturated higher priority does not damage lower priority equipment");
        check(recovered.absorbed().stream().filter(a->a.slot()==1).findFirst().orElseThrow().amount()==7,"recovery respects the 175 scaled-unit cap");
        check(!protectedDamage.recoveredFromLoop()&&!priority.recoveredFromLoop()&&!saturation.recoveredFromLoop(),"ordinary source results never use the repair path");
        try {protectedDamage.absorbed().clear();throw new AssertionError("mutable result");}catch(UnsupportedOperationException expected){checks++;}
        System.out.println("TC_SPECIAL_ARMOR_RULES_PASS checks="+checks);
    }
    private SpecialArmorRulesTest() {}
}
