#version 150
uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform vec2 BlurDir;
uniform float BlurRadius;
uniform float Saturation;
uniform vec3 ColorScale;
uniform float SunScorned;
uniform float ApplyColor;
uniform float Vignette;
in vec2 texCoord;
out vec4 fragColor;

vec3 sampleColor(vec2 uv){return texture(DiffuseSampler,clamp(uv,vec2(0.0),vec2(1.0))).rgb;}
void main(){
    vec3 color=sampleColor(texCoord);
    if(BlurRadius>0.0){vec3 sum=vec3(0.0);float weights=0.0;for(int i=-10;i<=10;i++){float weight=1.0-abs(float(i))/11.0;sum+=sampleColor(texCoord+BlurDir*float(i)/InSize)*weight;weights+=weight;}color=sum/weights;}
    if(ApplyColor>0.0){float gray=dot(color,vec3(0.3,0.59,0.11));color=mix(vec3(gray),color,Saturation)*ColorScale;
        if(SunScorned>0.0){vec3 average=vec3(0.0);for(int x=1;x<=9;x++)for(int y=1;y<=9;y++)average+=sampleColor(vec2(float(x),float(y))*.1);average/=81.0;float averageLight=(min(min(average.r,average.g),average.b)+max(max(average.r,average.g),average.b))*.5;float light=(min(min(color.r,color.g),color.b)+max(max(color.r,color.g),color.b))*.5;float saturation=.5+min(.2,light)*2.5;gray=dot(color,vec3(.3,.59,.11));color=mix(vec3(gray),color,saturation);color*=.6+.4*clamp(.3/max(averageLight,.0001),.7,4.0);float brightness=(color.r+color.g+color.b)/3.0;vec3 sum=(sampleColor(texCoord+vec2(-.00014,-.00014))+sampleColor(texCoord+vec2(-.00014,.00014))+sampleColor(texCoord+vec2(0.0,-.0002))+sampleColor(texCoord+vec2(-.0002,0.0)))*(10.0/4.8);color=(color+sum*sum*(.015-brightness*.01)*5.0)*.9;}
        if(Vignette>0.0){float dist=distance(texCoord,vec2(0.5));color*=mix(1.0,smoothstep(0.9,0.25,dist),Vignette);}
    }
    fragColor=vec4(color,1.0);
}
