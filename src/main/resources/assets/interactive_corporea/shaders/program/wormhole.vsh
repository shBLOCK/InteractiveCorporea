#version 110

attribute vec4 Position;

varying vec2 texCoord;
varying vec2 oneTexel;

void main(){
    vec2 outPos = Position.xy - 1.0;
    gl_Position = vec4(outPos, 0.2, 1.0);

    oneTexel = vec2(1.0, 1.0);
    texCoord = outPos * 0.5 + 0.5;
}
