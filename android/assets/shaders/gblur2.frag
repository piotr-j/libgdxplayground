// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float u_resolution;
uniform float u_radius;
uniform vec2 u_dir;


#define SIZE 3

// http://rastergrid.com/blog/2010/09/efficient-gaussian-blur-with-linear-sampling/#comment-65003
// in this version we exploit gpu linear filtering to sample two adjecent texels at same itme
// texture we sample must have linear filtering set
// does not compile on macos, fun times
uniform float offset[SIZE];// = float[]( 0.0, 1.3846153846, 3.2307692308 );
uniform float weight[SIZE];// = float[]( 0.2270270270, 0.3162162162, 0.0702702703 );

void main() {
    vec2 tc = v_texCoords;
    float blur = u_radius/u_resolution;

    float hstep = u_dir.x;
    float vstep = u_dir.y;

    vec4 sum = texture2D(u_texture, vec2(tc.x, tc.y)) * weight[0];
    for (int i = 1; i < SIZE; i++) {
        sum += texture2D(u_texture,
            vec2(tc.x - offset[i] * blur * hstep, tc.y - offset[i] * blur * vstep)) * weight[i];
        sum += texture2D(u_texture,
            vec2(tc.x + offset[i] * blur * hstep, tc.y + offset[i] * blur * vstep)) * weight[i];
    }

    gl_FragColor = v_color * sum;
}