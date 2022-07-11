// Copied From: Draconic-Evolution mod by brandon3055
// https://github.com/brandon3055/Draconic-Evolution/blob/master/src/main/resources/assets/draconicevolution/shaders/common.vert

#version 120

varying vec3 position;

void main() {
    position = (gl_ModelViewMatrix * gl_Vertex).xyz + gl_Normal;

    gl_FrontColor = gl_Color;
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}