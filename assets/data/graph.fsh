uniform sampler2D u_sampler2D;
uniform vec2 u_size;
uniform float u_value;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

bool colorCheck(vec3 c) {
    if (c.g > 0.78431) return true;
    float luma = (c.g + c.g + c.g + c.b + c.r + c.r)/6.;
    if (luma > 0.549019) return true;
    return false;
}

void main() {
    vec3 c = vec3(gl_FragCoord.x / u_size.x, gl_FragCoord.y / u_size.y, u_value);
    vec3 color = hsv2rgb(c);
    if (colorCheck(color)) color = vec3(0, 0, 0);
    gl_FragColor = vec4(color, 1);
}