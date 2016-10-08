#ifdef GL_ES
precision mediump float;
#endif
varying vec4 v_col;
uniform vec2 u_centre;
uniform vec2 u_resolution;
void main() {
   //gl_FragColor = v_col;
   vec2 nFC = gl_FragCoord.xy/u_resolution;
   vec2 p = 2.0 * (u_centre - nFC);
   vec2 norm = normalize(p * vec2(1, -1));
   gl_FragColor = vec4((norm + 1.0) / 2.0, 0, 1);
}
