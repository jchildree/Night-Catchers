#version 300 es
precision highp float;

in vec2 v_TexCoord;
out vec4 fragColor;

uniform sampler2D u_CameraTexture;
uniform float u_Time;
uniform vec2 u_Resolution;
uniform float u_SplatIntensity;

float splat(vec2 uv, vec2 centre, float radius) {
    float d = distance(uv, centre);
    return smoothstep(radius, 0.0, d);
}

void main() {
    vec2 uv = v_TexCoord;
    vec4 camera = texture(u_CameraTexture, uv);

    // Procedural splat positions — fixed seeds give consistent visual
    float s = 0.0;
    s += splat(uv, vec2(0.15, 0.10), 0.09);
    s += splat(uv, vec2(0.82, 0.08), 0.07);
    s += splat(uv, vec2(0.45, 0.92), 0.11);
    s += splat(uv, vec2(0.08, 0.55), 0.06);
    s = clamp(s, 0.0, 1.0);

    float fade = 0.6 + 0.4 * sin(u_Time * 2.0);
    vec4 slimeColor = vec4(0.46, 1.0, 0.01, 1.0);

    fragColor = mix(camera, slimeColor, s * u_SplatIntensity * fade);
}
