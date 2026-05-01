#version 300 es
precision highp float;

in vec2 v_TexCoord;
out vec4 fragColor;

uniform sampler2D u_CameraTexture;
uniform float u_Time;
uniform vec2 u_Resolution;
uniform float u_GreenTint;
uniform float u_ScanlineIntensity;

float scanlines(vec2 uv) {
    float line = mod(uv.y * u_Resolution.y, 2.0);
    return 1.0 - u_ScanlineIntensity * step(1.0, line);
}

float phosphorGrain(vec2 uv) {
    // Simple pseudo-random grain
    vec2 seed = uv * vec2(12.9898, 78.233) + u_Time * 0.1;
    return fract(sin(dot(seed, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 uv = v_TexCoord;
    vec4 camera = texture(u_CameraTexture, uv);

    // Desaturate
    float lum = dot(camera.rgb, vec3(0.2126, 0.7152, 0.0722));

    // Re-tint green
    vec3 nvg = vec3(0.0, lum * u_GreenTint, 0.0);

    // Scanlines + grain
    float scan = scanlines(uv);
    float grain = phosphorGrain(uv) * 0.08;

    fragColor = vec4((nvg + grain) * scan, camera.a);
}
