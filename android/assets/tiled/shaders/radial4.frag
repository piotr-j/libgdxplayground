// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float distortion;// = 0.3
//uniform float zoom;// = 1
const float PI = 3.1415926535;

vec2 distort(vec2 p) {
    float theta  = atan(p.y, p.x);
    float radius = length(p);
    radius = pow(radius, distortion);
    p.x = radius * cos(theta);
    p.y = radius * sin(theta);
    return 0.5 * (p + 1.0);
}

void main() {
    // barrel thing
//    distortion = .4;

    vec2 xy = 2.0 * v_texCoords.xy - 1.0;
    vec2 uv;
    float d = length(xy);
    if (d < 1.0) {
        uv = distort(xy);
    } else {
        uv = v_texCoords.xy;
    }
    vec4 c = texture2D(u_texture, uv);
    gl_FragColor = c;
}
