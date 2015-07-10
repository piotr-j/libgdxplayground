#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
 
varying vec2 vTexCoord0;
varying vec2 vTexCoord1;
varying vec2 vTexCoord2;
varying float vInvDist;
 
uniform vec3 u_color;
uniform sampler2D u_texture;

float VSM(float depth)
{
	vec2 vsmMoments =texture2D(u_texture, vTexCoord0).rg;

	if( depth < vsmMoments.x )
		return 1.0;
	else
	{
	float mean = vsmMoments.x;
	float variance = vsmMoments.y - (mean * mean);
	variance = max( 0.001, variance );	// Reduce precision issues by clamping variance to min value x
						
	float dz = depth - mean;
	float p = variance / (variance + (dz * dz));
						
	// Reduce light bleeding by cutting off lower end of range
	// This also makes shadows harder and darker
	return smoothstep( 0.5, 1.0, p*p);
	}
}

void main(void) {

	//float shadow = VSM(1.0-vInvDist);//need depth^2 					
						
	float shadow =dot(vec3(0.3,0.4,0.3), vec3(
	step(texture2D(u_texture, vTexCoord0).r, vInvDist),
	step(texture2D(u_texture, vTexCoord1).r, vInvDist),
	step(texture2D(u_texture, vTexCoord2).r, vInvDist)
	));      
	
	gl_FragColor = vec4(u_color, shadow * vInvDist);
	
	//vertex fetch version. 
	//gl_FragColor = vec4(u_color, vInvDist);
}