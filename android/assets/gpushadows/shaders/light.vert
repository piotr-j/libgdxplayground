attribute vec3 a_position;

uniform mat4 u_mvp;
uniform float y;
uniform sampler2D u_texture;

varying float vInvDist;
varying vec2 vTexCoord0;
varying vec2 vTexCoord1;
varying vec2 vTexCoord2;

varying vec4 vColor;
#define PI 3.14 
void main() {
	vTexCoord0 = vec2(a_position.z, y);
	vInvDist = step(dot(a_position.xy,a_position.xy), 0.5);
	
	float a = (1.0-vInvDist) * (1.0/256.0);
	vTexCoord1 = vec2(a_position.z-a , y);
	vTexCoord2 = vec2(a_position.z+a, y);
	
  	gl_Position = u_mvp * vec4(a_position.xy, 0.0, 1.0);
  	
  	//vertex fetch version
  	//gl_Position = u_mvp * vec4(a_position.xy * texture2DLod(u_texture, uv, 0.0 ).g, 0.0, 1.0);
}