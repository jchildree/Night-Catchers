#version 300 es
precision highp float;

in vec2 v_TexCoord;
out vec4 fragColor;

uniform sampler2D u_CameraTexture;
uniform float u_Time;
uniform vec2 u_Resolution;
uniform vec4 u_VinetteColor;   // slime green rgba
uniform float u_Intensity;

float vignette(vec2 uv) {
    vec2 d = uv - 0.5;
    d.x *= u_Resolution.x / u_Resolution.y;
    return smoothstep(0.45, 0.8, length(d));
}

void main() {
    vec2 uv = v_TexCoord;
    vec4 camera = texture(u_CameraTexture, uv);

    float vig = vignette(uv);
    float pulse = 0.9 + 0.1 * sin(u_Time * 3.0);
    vec4 slime = u_VinetteColor * vig * u_Intensity * pulse;

    fragColor = mix(camera, slime, vig * 0.55);
}
