// no define stuff, we dont care about that for testing
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
  // TODO uniform for color, whatever else
//  gl_FragColor = vec4(0.0, 1.0, 1.0, 0.2) * texture2D(u_texture, v_texCoord0).a;
//  gl_FragColor = texture2D(u_texture, v_texCoords) * vec4(.5);
  gl_FragColor = vec4(0., 1., 0., .75);
}
