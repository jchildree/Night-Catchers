#version 300 es
precision highp float;

in vec2 v_TexCoord;
out vec4 fragColor;

uniform sampler2D u_CameraTexture;
uniform float u_Time;
uniform vec2 u_Resolution;
uniform vec2 u_BeamOrigin;   // normalised [0,1]
uniform vec4 u_BeamColor;

float beamGlow(vec2 uv, vec2 origin) {
    float dist = distance(uv, origin);
    float beam = 1.0 - smoothstep(0.0, 0.15, dist);
    float pulse = 0.8 + 0.2 * sin(u_Time * 8.0);
    return beam * pulse;
}

void main() {
    vec2 uv = v_TexCoord;
    vec4 camera = texture(u_CameraTexture, uv);

    float glow = beamGlow(uv, u_BeamOrigin);
    vec4 beamContrib = u_BeamColor * glow;

    fragColor = camera + beamContrib;
}
