// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_color_grade;

void main() {
    // our color grade texture is 16x16
    float cgtSize = 16.0;
    // and since its not 3d, we pack it into 2d texture with 16 slices as well
    float cgtLen = 16.0;
    // we need to scale our raw colors to fit into the color grade texture
    float scale = (cgtSize - 1.0) / cgtSize;

    // in our cgt, red and green and x, y and blue is z
    vec3 baseColor = texture2D(u_texture, v_texCoords).rgb;
    // we need to divide by the number of segments so its normalized properly
    float x = baseColor.x * scale/cgtLen;
    // move to correct segment based on z/b
    x += baseColor.z * scale;
    // normalize y
    float y = baseColor.y * scale;
    vec2 index = vec2(x, y);
    gl_FragColor = texture2D(u_color_grade, index);
}
