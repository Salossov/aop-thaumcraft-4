#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float Clock;
in vec4 projected;
out vec4 fragColor;
const vec3 colors[16]=vec3[16](
vec3(0.1,0.1,0.1),
vec3(0.0063173794,0.05095978,0.047539063),
vec3(0.014739357,0.05423397,0.053507045),
vec3(0.024944939,0.05886573,0.06152016),
vec3(0.03494681,0.063374594,0.052332565),
vec3(0.034537267,0.047068045,0.066975),
vec3(0.046264093,0.06108772,0.09075278),
vec3(0.053619146,0.08476583,0.050085224),
vec3(0.05897343,0.07285756,0.10843927),
vec3(0.05496822,0.061980724,0.10531616),
vec3(0.076295055,0.07901611,0.08490379),
vec3(0.04083666,0.14194381,0.13754532),
vec3(0.11805946,0.08573964,0.12881733),
vec3(0.029550614,0.19708616,0.20123142),
vec3(0.1364497,0.26000684,0.2013776),
vec3(0.06071605,0.23611547,0.4961182));
vec2 coordinates(int layer){float scale=layer==0?0.125:layer==1?0.5:0.0625;float angle=radians(float((layer*layer*4321+layer*9)*2));mat2 rotation=mat2(cos(angle),sin(angle),-sin(angle),cos(angle));return rotation*((projected.xy/projected.w)*scale+vec2(0.5,Clock));}
void main(){vec4 base=texture(Sampler0,coordinates(0));vec3 color=base.rgb*colors[0]*base.a;for(int i=1;i<16;i++)color+=texture(Sampler1,coordinates(i)).rgb*colors[i];fragColor=vec4(color,1.0);}
