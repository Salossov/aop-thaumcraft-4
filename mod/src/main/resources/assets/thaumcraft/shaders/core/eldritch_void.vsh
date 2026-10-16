#version 150
in vec3 Position;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
out vec4 projected;
void main(){gl_Position=ProjMat*ModelViewMat*vec4(Position,1.0);projected=vec4(gl_Position.xy*0.5+gl_Position.ww*0.5,0.0,gl_Position.w);}
