#version 300 es
precision highp float;

in vec2 v_TexCoord;
out vec4 fragColor;

uniform sampler2D u_CameraTexture;
uniform float u_Time;
uniform vec2 u_Resolution;
uniform float u_SweepAngle;   // degrees 0-360

const vec2 RADAR_CENTER = vec2(0.85, 0.85);   // bottom-right HUD position
const float RADAR_RADIUS = 0.10;
const vec3 RADAR_GREEN = vec3(0.0, 0.96, 0.0);

float sweepMask(vec2 uv) {
    vec2 d = uv - RADAR_CENTER;
    d.y *= -1.0;
    float angle = degrees(atan(d.y, d.x));
    if (angle < 0.0) angle += 360.0;
    float sweep = u_SweepAngle;
    float diff = mod(sweep - angle + 360.0, 360.0);
    return exp(-diff * 0.04);
}

void main() {
    vec2 uv = v_TexCoord;
    vec4 camera = texture(u_CameraTexture, uv);

    float dist = distance(uv, RADAR_CENTER) * (u_Resolution.x / u_Resolution.y);
    if (dist > RADAR_RADIUS) {
        fragColor = camera;
        return;
    }

    // Background disc
    vec3 radarBg = vec3(0.0, 0.05, 0.0);
    // Sweep trail
    float trail = sweepMask(uv);
    vec3 col = mix(radarBg, RADAR_GREEN, trail * 0.85);
    // Ring
    float ring = smoothstep(RADAR_RADIUS - 0.002, RADAR_RADIUS, dist)
               - smoothstep(RADAR_RADIUS, RADAR_RADIUS + 0.002, dist);
    col += RADAR_GREEN * ring;

    fragColor = vec4(col, 0.88);
}
