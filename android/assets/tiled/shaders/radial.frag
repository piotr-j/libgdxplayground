// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float distortion;// = 0.3
uniform float zoom;// = 1

//void main() {
//  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
//}

vec2 radialDistortion(vec2 coord) {
  vec2 cc = coord - 0.5;
  float dist = dot(cc, cc) * distortion;
  return (coord + cc * (1.0 + dist) * dist);
}

void main() {
  vec2 uv = radialDistortion(v_texCoords);
  uv = 0.5 + (uv-0.5)*(zoom);

  if(uv.s<0.0 || uv.s>1.0 || uv.t<0.0 || uv.t >1.0) {
    gl_FragColor = vec4(0.0,0.0,0.0,1.0);
    return;
  }

  gl_FragColor = vec4(texture2D(u_texture, uv).rgb,1.0);

//  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
}
