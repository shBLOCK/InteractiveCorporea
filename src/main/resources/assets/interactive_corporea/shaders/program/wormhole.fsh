#version 110

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;

varying vec2 texCoord;

#define PI 3.14159265358979323846

vec4 LinearizeDepth(vec4 depth) {
    vec4 near = vec4(0.1);
    vec4 far = vec4(100.0);
    vec4 z = depth * 2.0 - 1.0; // Back to NDC
    return (2.0 * near) / (far + near - z * (far - near));
}

void main() {
    vec2 uv = texCoord;
    vec2 hole = vec2(0.75, 0.75);
//    vec2 warp = normalize(uv - hole) * pow(distance(uv, hole), -2.0) * .001;
    float radius = 0.075;
    float x = distance(uv, hole) / radius;
    if (x < 2.0) {
        vec2 warp = normalize(uv - hole) * (cos(min(x * PI, PI * 2.0)) + 1.0) * radius;
//        vec2 warp = normalize(uv - hole) * max((x - 1.0) * 2.0, 0.0) * radius;
        uv = hole + warp;
    }

    vec4 color = texture2D(DiffuseSampler, uv) * sign(x - 1.0);
    gl_FragColor = LinearizeDepth(texture2D(DiffuseDepthSampler, texCoord)) + color * 0.00000000001;
//    gl_FragColor.a = 1.0;
}
