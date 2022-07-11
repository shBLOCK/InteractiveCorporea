#version 120

//varying vec4 gl_FR

uniform int alpha; // Range: 0~16

//Inspired by: https://youtu.be/--GB9qyZJqg?t=320
void main() {
    const ivec4[4] STIP_MAT = ivec4[4](
        ivec4(1, 9, 3, 11),
        ivec4(13, 5, 15, 7),
        ivec4(4, 12, 2, 10),
        ivec4(16, 8, 14, 6)
    );

    ivec2 pCoord = ivec2(gl_FragCoord.xy);
    int threshold = STIP_MAT[pCoord.x - pCoord.x / 4 * 4][pCoord.y - pCoord.y / 4 * 4];
    if (threshold > alpha) {
        discard;
    }
    gl_FragColor = vec4(1.0);
}