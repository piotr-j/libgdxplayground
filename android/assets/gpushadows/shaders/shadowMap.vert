attribute vec2 a_position;
attribute vec4 a_scaleOffset;
 
varying float vTexCoord0;
varying vec4 vScaleOffset;
 
#define PI 3.14159265
#define PI15 4.712388975

void main() {
  vScaleOffset = a_scaleOffset;
  vTexCoord0 = a_position.x * PI + PI15;
  gl_Position = vec4(a_position, 0.0, 1.0);
}