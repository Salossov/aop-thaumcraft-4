#version 150
#moj_import <light.glsl>
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;
uniform sampler2D Sampler2;
out vec4 vertexColor;
out vec2 texCoord0;
out float vertexDistance;
void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * view;
    vertexDistance = length(view.xyz);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
    // TC4's lightmap texture matrix scales by 1/256, then translates by 8.
    // Keep the raw, pulsating UV instead of rounding it to a light level.
    vertexColor *= texture(Sampler2, (vec2(UV2) + vec2(8.0)) / 256.0);
    texCoord0 = UV0;
}
