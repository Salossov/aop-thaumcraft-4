package thaumcraft.viewers;
import java.net.*;
import java.nio.file.*;
import java.util.*;
/** Class linkage checks only. Does not launch a game or instantiate its registries. */
public final class IsolationTest {
 public static void main(String[] args)throws Exception{
  var metadata=Files.readString(Path.of(args[1]));
  if(!metadata.contains("versionRange=\"[19.8.4.110,20)\"")||!metadata.contains("versionRange=\"[1.1.13,1.2)\""))throw new AssertionError("Viewer ranges were narrowed");
  var paths=System.getProperty("java.class.path").split(java.io.File.pathSeparator);var urls=new ArrayList<URL>();for(String path:paths)urls.add(Path.of(path).toUri().toURL());int checks=0;
  var jeiRange=org.apache.maven.artifact.versioning.VersionRange.createFromVersionSpec("[19.8.4.110,20)");
  var emiRange=org.apache.maven.artifact.versioning.VersionRange.createFromVersionSpec("[1.1.13,1.2)");
  for(String version:List.of("19.8.4.110","19.19.6.233","19.64.0.453","19.64.0.455")){if(!jeiRange.containsVersion(new org.apache.maven.artifact.versioning.DefaultArtifactVersion(version)))throw new AssertionError("JEI rejected "+version);checks++;}
  for(String version:List.of("1.1.13+1.21.1+neoforge","1.1.20+1.21.1+neoforge","1.1.24+1.21.1+neoforge")){if(!emiRange.containsVersion(new org.apache.maven.artifact.versioning.DefaultArtifactVersion(version)))throw new AssertionError("EMI rejected "+version);checks++;}
  if(jeiRange.containsVersion(new org.apache.maven.artifact.versioning.DefaultArtifactVersion("20.0.0"))||jeiRange.containsVersion(new org.apache.maven.artifact.versioning.DefaultArtifactVersion("18.0.0"))||emiRange.containsVersion(new org.apache.maven.artifact.versioning.DefaultArtifactVersion("1.2.0")))throw new AssertionError("Incompatible API family accepted");checks++;

  for(boolean jei:new boolean[]{false,true})for(boolean emi:new boolean[]{false,true}){
   var selected=ViewerSelection.select(jei,emi);if(selected!=(emi?ViewerSelection.EMI:jei?ViewerSelection.JEI:ViewerSelection.NONE))throw new AssertionError("selection");checks++;
   try(var loader=new URLClassLoader(urls.toArray(URL[]::new),ClassLoader.getPlatformClassLoader()){
    protected Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException{if(!jei&&name.startsWith("mezz.jei."))throw new ClassNotFoundException("JEI deliberately absent");if(!emi&&name.startsWith("dev.emi.emi."))throw new ClassNotFoundException("EMI deliberately absent");if(name.startsWith("thaumcraft.")&&!name.startsWith("thaumcraft.api.")&&!name.startsWith("thaumcraft.viewers."))throw new ClassNotFoundException("Core internals deliberately absent");return super.loadClass(name,resolve);}
   }){
    for(String name:List.of("Viewers","ViewerSelection","Catalog","Layout","AspectValue","Updates")){Class.forName("thaumcraft.viewers."+name,false,loader).getDeclaredMethods();checks++;}
    if(jei){Class.forName("thaumcraft.viewers.jei.JeiPlugin",false,loader).getDeclaredMethods();Class.forName("thaumcraft.viewers.jei.JeiAspects",false,loader).getDeclaredMethods();checks+=2;}
    if(emi){Class.forName("thaumcraft.viewers.emi.EmiPlugin",false,loader).getDeclaredMethods();Class.forName("thaumcraft.viewers.emi.EmiAspect",false,loader).getDeclaredMethods();checks+=2;}
    if(emi){Class.forName("dev.emi.emi.runtime.EmiReloadManager",false,loader).getMethod("reload");checks++;}
    checks+=ApiLinkage.verify(Path.of(args[0]),loader,jei,emi);
   }
   System.out.println("VIEWER_LINKAGE jei="+jei+" emi="+emi+" selected="+selected);
  }System.out.println("TC4_VIEWER_ISOLATION_PASS checks="+checks);
 }
}
