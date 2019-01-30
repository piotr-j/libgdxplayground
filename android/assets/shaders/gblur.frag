// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float u_resolution;
uniform float u_radius;
uniform vec2 u_dir;


#define SIZE 5

// http://rastergrid.com/blog/2010/09/efficient-gaussian-blur-with-linear-sampling/#comment-65003
uniform float offset[SIZE] = float[](0.0, 1.0, 2.0, 3.0, 4.0);
uniform float weight[SIZE] = float[](0.2270270270, 0.1945945946, 0.1216216216, 0.0540540541, 0.0162162162);

// https://github.com/mattdesl/lwjgl-basics/wiki/ShaderLesson5
// too dark
//uniform float weight[5] = float[](0.16, 0.15, 0.12, 0.09, 0.05);

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