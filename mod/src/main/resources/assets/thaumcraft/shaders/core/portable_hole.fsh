#version 150
#moj_import <matrix.glsl>
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float HoleTime;
in vec4 texProj0;
out vec4 fragColor;
// TC4's sixteen layers use Random(31100), with its original brightness weights.
const vec3 COLORS[16] = vec3[16](
    vec3(0.100000000, 0.100000000, 0.100000000),
    vec3(0.006317379, 0.050959780, 0.047539063),
    vec3(0.014739357, 0.054233966, 0.053507042),
    vec3(0.024944938, 0.058865726, 0.061520155),
    vec3(0.034946809, 0.063374595, 0.052332562),
    vec3(0.034537266, 0.047068044, 0.066974995),
    vec3(0.046264088, 0.061087713, 0.090752775),
    vec3(0.053619147, 0.084765824, 0.050085223),
    vec3(0.058973432, 0.072857558, 0.108439267),
    vec3(0.054968220, 0.061980724, 0.105316158),
    vec3(0.076295053, 0.079016101, 0.084903785),
    vec3(0.040836658, 0.141943811, 0.137545303),
    vec3(0.118059452, 0.085739641, 0.128817332),
    vec3(0.029550613, 0.197086161, 0.201231413),
    vec3(0.136449697, 0.260006843, 0.201377581),
    vec3(0.060716051, 0.236115468, 0.496118203));
const mat4 SCALE_TRANSLATE = mat4(
    0.5, 0.0, 0.0, 0.25,
    0.0, 0.5, 0.0, 0.25,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
);
mat4 layer_transform(float layer) {
    mat4 translate = mat4(
        1.0, 0.0, 0.0, 17.0 / layer,
        0.0, 1.0, 0.0, 0.0,
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    mat2 rotate = mat2_rotate_z(radians((layer * layer * 4321.0 + layer * 9.0) * 2.0));
    // Preserve TC4's .5/.0625 layer scale ratio in projected coordinates.
    mat2 scale = mat2(layer == 1.0 ? 8.0 : 1.0);
    return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
}
void main() {
    vec4 base = texProj0;
    base.y += HoleTime * base.w;
    vec3 color = textureProj(Sampler0, base).rgb * COLORS[0];
    for (int i = 1; i < 16; i++) {
        vec4 projected = texProj0 * layer_transform(float(i));
        projected.y += HoleTime * projected.w;
        color += textureProj(Sampler1, projected).rgb * COLORS[i];
    }
    fragColor = vec4(color, 1.0);
}
