#version 440

uniform sampler2D mainSampler;
uniform sampler2D mainDepthSampler;
uniform samplerBuffer data;

uniform vec2 screenSize;
uniform float time;

uniform float aspectRatio;
uniform vec3 midDirection; // normalized
uniform float fov; // is actually half FOV
uniform vec3 perpendicularX; // normalized
uniform vec3 perpendicularY; // normalized
uniform float farPlane;

in vec4 gl_FragCoord;

out vec4 color;

#define PI 3.14159265358979323846
#define NEAR_PLANE 0.05

float linearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (2.0 * NEAR_PLANE) / (farPlane + NEAR_PLANE - z * (farPlane - NEAR_PLANE));
}

float rotAngleFromNDV(float ndv, float ffov) {
    float planeToCenter = 1.0 / tan(ffov);
    return atan(ndv / abs(planeToCenter));
}

vec2 toNDC(vec2 texCoord) {
    return vec2((texCoord.x * 2.0 - 1.0), (texCoord.y * 2.0 - 1.0));
}

vec3 localRayFromNDC(vec2 ndc) {
    vec3 ray = midDirection * NEAR_PLANE;
    float fovMulX = NEAR_PLANE * tan(fov) * aspectRatio;
    float fovMulY = NEAR_PLANE * tan(fov);
    return normalize(ray + perpendicularX * ndc.x * fovMulX + perpendicularY * -ndc.y * fovMulY);
}

// get the intersection point between a ray (the orginial point of the ray is {0, 0, 0}) and a plane
vec3 orgRayPlaneIntersection(vec3 dir, vec3 planeOrg, vec3 planeNormal) {
    float dist = dot(planeOrg, planeNormal) / dot(dir, planeNormal);
    if (dist > 0.0) {
        return vec3(0.0);
    } else {
        return dir * dist;
    }
}

float distanceWarpFunc(float distToCenter) {
    return -((1.5)/(distToCenter-0.5))+3.0;

//    float f1 = distToCenter-2.2506;
//    return -2.0*f1*f1+2.1256;
}

void main() {
    vec2 texCoord = gl_FragCoord.xy / screenSize;

    vec3 ray = localRayFromNDC(toNDC(texCoord));
    vec3 pOrg = vec3(texelFetch(data, 0).r, texelFetch(data, 1).r, texelFetch(data, 2).r);
    vec3 pNormal = vec3(texelFetch(data, 3).r, texelFetch(data, 4).r, texelFetch(data, 5).r);
    float radius = texelFetch(data, 6).r;
    vec2 midTexCoord = vec2(texelFetch(data, 7).r, texelFetch(data, 8).r);

    vec3 intersection = orgRayPlaneIntersection(ray, pOrg, pNormal);
    bool intersecting = !all(equal(intersection, vec3(0.0, 0.0, 0.0)));

    float distToHoleOrg = distance(pOrg, intersection);
    float normalizedDist = distToHoleOrg / radius;

    color = texture2D(mainSampler, texCoord);

    if (intersecting) {
        if (normalizedDist < 2.0) {
//            vec2 warp = distance(texCoord, midTexCoord) / vec2(normalizedDist) * normalize(texCoord - midTexCoord) * (cos(normalizedDist * PI) + 1.0);
            vec2 warp = distance(texCoord, midTexCoord) / vec2(normalizedDist) * normalize(texCoord - midTexCoord) * distanceWarpFunc(normalizedDist);
            texCoord = midTexCoord + warp;
        }
        color = texture2D(mainSampler, texCoord);

        float depth = linearizeDepth(texture2D(mainDepthSampler, texCoord).r);
        //        color = vec4(depth);
        depth = depth * (farPlane - NEAR_PLANE);
        float distToOrg = length(intersection) - NEAR_PLANE;
        if (distToOrg < depth || true) {

            if (normalizedDist < 1.0) {
                color = vec4(normalizedDist, normalizedDist, normalizedDist, 1.0);
            }
        }
    }

    color.a = 1.0;
}
