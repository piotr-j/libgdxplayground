#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_saturation;

void main()
{
    vec4 c = v_color * texture2D(u_texture, v_texCoords);
    const vec3 W = vec3(0.2125, 0.7154, 0.0721);
    vec3 intensity = vec3(dot(c.rgb, W));
    // u_saturation = 0 - grayscale
    // u_saturation = 1 - normal
    // u_saturation > 1 - cranked up saturation
    gl_FragColor = vec4(mix(intensity, c.rgb, u_saturation), c.a);
}
