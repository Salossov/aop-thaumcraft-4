package thaumcraft.api.crafting;

import java.util.*;
import java.util.function.Predicate;

/** Pure two-phase planner. No caller-owned stack is mutated, including on failure. */
public final class TransferPlanner {
    public interface Stacks<S> {boolean empty(S stack);int count(S stack);int limit(S stack);boolean same(S a,S b);S count(S stack,int count);S empty();}
    public record Plan<S>(List<S> inventory,List<S> grid,int crafts){public Plan{inventory=List.copyOf(inventory);grid=List.copyOf(grid);}}
    private record Group<S>(S stack,int amount){}
    public static <S> Optional<Plan<S>> plan(List<S> inventory,List<S> grid,List<Predicate<S>> requirements,Stacks<S> stacks,boolean maximum){
        if(grid.size()!=requirements.size()||grid.size()>9)throw new IllegalArgumentException("Invalid grid");
        var groups=new ArrayList<Group<S>>();var all=new ArrayList<>(inventory);all.addAll(grid);
        for(var item:all){if(stacks.empty(item))continue;int index=-1;for(int i=0;i<groups.size();i++)if(stacks.same(groups.get(i).stack,item)){index=i;break;}if(index<0)groups.add(new Group<>(stacks.count(item,1),stacks.count(item)));else {var old=groups.get(index);groups.set(index,new Group<>(old.stack,Math.addExact(old.amount,stacks.count(item))));}}
        var slots=new ArrayList<Integer>();for(int i=0;i<requirements.size();i++)if(requirements.get(i)!=null)slots.add(i);if(slots.isEmpty())return Optional.empty();
        slots.sort(Comparator.comparingLong(slot->groups.stream().filter(g->requirements.get(slot).test(g.stack)).count()));
        for(int amount=maximum?64:1;amount>=1;amount--){var remaining=groups.stream().mapToInt(Group::amount).toArray();int[] selected=new int[grid.size()];Arrays.fill(selected,-1);if(!assign(0,slots,requirements,groups,remaining,selected,stacks,amount))continue;
            var output=new ArrayList<S>();for(int i=0;i<inventory.size();i++)output.add(stacks.empty());
            // Keep original player slot positions whenever their stack survives the transfer.
            for(int i=0;i<inventory.size();i++){var old=inventory.get(i);if(stacks.empty(old))continue;for(int g=0;g<groups.size();g++)if(remaining[g]>0&&stacks.same(groups.get(g).stack,old)){int n=Math.min(stacks.count(old),Math.min(stacks.limit(old),remaining[g]));output.set(i,stacks.count(old,n));remaining[g]-=n;break;}}
            boolean fits=true;for(int g=0;g<groups.size();g++){var stack=groups.get(g).stack;for(int i=0;i<output.size()&&remaining[g]>0;i++){var old=output.get(i);if(!stacks.empty(old)&&stacks.same(old,stack)){int n=Math.min(remaining[g],stacks.limit(old)-stacks.count(old));output.set(i,stacks.count(old,stacks.count(old)+n));remaining[g]-=n;}}for(int i=0;i<output.size()&&remaining[g]>0;i++)if(stacks.empty(output.get(i))){int n=Math.min(remaining[g],stacks.limit(stack));output.set(i,stacks.count(stack,n));remaining[g]-=n;}if(remaining[g]>0){fits=false;break;}}
            if(!fits)continue;var newGrid=new ArrayList<S>();for(int i:selected)newGrid.add(i<0?stacks.empty():stacks.count(groups.get(i).stack,amount));return Optional.of(new Plan<>(output,newGrid,amount));
        }return Optional.empty();
    }
    private static <S> boolean assign(int at,List<Integer> slots,List<Predicate<S>> tests,List<Group<S>> groups,int[] counts,int[] selected,Stacks<S> stacks,int amount){if(at==slots.size())return true;int slot=slots.get(at);for(int i=0;i<groups.size();i++)if(counts[i]>=amount&&stacks.limit(groups.get(i).stack)>=amount&&tests.get(slot).test(groups.get(i).stack)){counts[i]-=amount;selected[slot]=i;if(assign(at+1,slots,tests,groups,counts,selected,stacks,amount))return true;counts[i]+=amount;selected[slot]=-1;}return false;}
    private TransferPlanner(){}
}
