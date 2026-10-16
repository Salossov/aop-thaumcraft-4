#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 InverseProjection;
uniform vec3 MistColor;
uniform float MistDensity;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec4 scene = texture(DiffuseSampler, texCoord);
    float depth = texture(DepthSampler, texCoord).r;
    vec4 eye = InverseProjection * vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    // Fixed-function GL_EXP used eye-space Z. Reconstruct it from the completed
    // depth buffer, so this pass also works with Sodium's separate chunk shaders.
    float distance = abs(eye.z / max(abs(eye.w), 0.000001));
    float visibility = clamp(exp(-max(MistDensity, 0.0) * distance), 0.0, 1.0);
    fragColor = vec4(mix(MistColor, scene.rgb, visibility), scene.a);
}
