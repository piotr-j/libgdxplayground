// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float distortion;// = 0.3
uniform float zoom;// = 1
uniform vec2 resolution;// = 1

//void main() {
//  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
//}

vec2 radialDistortion(vec2 coord) {
    vec2 cc = coord - 0.5;
    float dist = dot(cc, cc) * distortion;
    return (coord + cc * (1.0 + dist) * dist);
}

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);

    vec2 uv = radialDistortion(v_texCoords);
    uv = 0.5 + (uv-0.5)*(zoom);

    if(uv.s<0.0 || uv.s>1.0 || uv.t<0.0 || uv.t >1.0) {
        gl_FragColor = vec4(0.0,0.0,0.0,1.0);
        return;
    }

//  gl_FragColor = vec4(texture2D(u_texture, uv).rgb,1.0);
//    vec4 texColor = texture2D(u_texture, v_texCoords);

    //determine origin
    vec2 position = (gl_FragCoord.xy / resolution.xy) - vec2(0.5);
    position.x *= resolution.x / resolution.y;
    //determine the vector length of the center position
    float len = length(position);

    //show our length for debugging
    float r = 0.5;
    float softness = 0.05;
    if (r < len) {
        gl_FragColor = vec4(texture2D(u_texture, uv).rgb,1.0);
        return;
    }
    gl_FragColor = vec4( texColor.rgb * vec3( smoothstep(r, r-softness, len) ), 1.0 );

//  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
}
