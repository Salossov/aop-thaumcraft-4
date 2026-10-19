package thaumcraft.viewers;
import java.io.*;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
/** Resolve API symbols without executing a client, registries or plugin callbacks. */
final class ApiLinkage {
 private record Pair(int first,int second){}
 static int verify(Path classes,ClassLoader loader,boolean jei,boolean emi)throws Exception{
  int checks=0;try(var walk=Files.walk(classes)){
   for(var path:walk.filter(p->p.toString().endsWith(".class")).toList()){
    String relative=classes.relativize(path).toString().replace('\\','/');boolean enabled=jei&&relative.startsWith("thaumcraft/viewers/jei/")||emi&&relative.startsWith("thaumcraft/viewers/emi/");if(!enabled)continue;
    Class<?> implementation=Class.forName(relative.substring(0,relative.length()-6).replace('/','.'),false,loader);
    if(!Modifier.isAbstract(implementation.getModifiers()))for(var method:implementation.getMethods())if(Modifier.isAbstract(method.getModifiers()))throw new AssertionError("Missing implementation: "+implementation.getName()+" "+method);
    try(var in=new DataInputStream(Files.newInputStream(path))){
     if(in.readInt()!=0xcafebabe)throw new AssertionError("class magic");in.readInt();int size=in.readUnsignedShort();var pool=new Object[size];int[] tags=new int[size];
     for(int i=1;i<size;i++){int tag=in.readUnsignedByte();tags[i]=tag;switch(tag){case 1->pool[i]=in.readUTF();case 7,8,16,19,20->pool[i]=in.readUnsignedShort();case 9,10,11,12,17,18->pool[i]=new Pair(in.readUnsignedShort(),in.readUnsignedShort());case 3,4->in.skipNBytes(4);case 5,6->{in.skipNBytes(8);i++;}case 15->in.skipNBytes(3);default->throw new AssertionError("constant tag "+tag);}}
     for(int i=1;i<size;i++)if(tags[i]==9||tags[i]==10||tags[i]==11){var reference=(Pair)pool[i];String owner=(String)pool[(Integer)pool[reference.first]];if(!owner.startsWith("mezz/jei/api/")&&!owner.startsWith("dev/emi/emi/api/"))continue;var nat=(Pair)pool[reference.second];String name=(String)pool[nat.first],descriptor=(String)pool[nat.second];Class<?> type=Class.forName(owner.replace('/','.'),false,loader);
      if(tags[i]==9){var field=type.getField(name);String actual=field.getType().descriptorString();if(!actual.equals(descriptor))throw new AssertionError("Field signature changed: "+owner+"."+name);}
      else if(name.equals("<init>"))type.getConstructor(MethodType.fromMethodDescriptorString(descriptor,loader).parameterArray());
      else {boolean found=false;for(var method:type.getMethods())if(method.getName().equals(name)&&MethodType.methodType(method.getReturnType(),method.getParameterTypes()).toMethodDescriptorString().equals(descriptor)){found=true;break;}if(!found)throw new AssertionError("Missing API method: "+owner+"."+name+descriptor);}
      checks++;
     }
    }
   }
  }return checks;
 }
 private ApiLinkage(){}
}
