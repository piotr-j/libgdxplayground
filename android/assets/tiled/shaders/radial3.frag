// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float distortion;// = 0.3
uniform float zoom;// = 1
uniform vec2 resolution;// = 1
const float PI = 3.1415926535;

void main() {
//    distortion = .4;

    vec2 uv;
    vec2 xy = 2.0 * v_texCoords.xy - 1.0;
    float d = length(xy);
    if (d < (2.0 - distortion)) {
        d = length(xy * distortion);
        float z = sqrt(1.0 - d * d);
        float r = atan(d, z) / PI;
        float phi = atan(xy.y, xy.x);

        uv.x = r * cos(phi) + 0.5;
        uv.y = r * sin(phi) + 0.5;
    } else {
        uv = v_texCoords.xy;
    }
//    float zoom = 3.8;
    uv = 0.5 + (uv-0.5)*(zoom);
    vec4 c = texture2D(u_texture, uv);
    gl_FragColor = c;
}
