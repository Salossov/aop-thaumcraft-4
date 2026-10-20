#version 150
#moj_import <fog.glsl>
in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 UV2;
uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
out float vertexDistance;
out vec2 texCoord0;
out vec4 vertexColor;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexDistance = fog_distance(Position, FogShape);
    texCoord0 = UV0;
    // TC4's texture matrix scales by 1/256, then translates by 8.
    // Preserve UV2=200's fractional lightmap sample instead of integer /16.
    vertexColor = Color * texture(Sampler2, (vec2(UV2) + vec2(8.0)) / 256.0);
}
