varying vec2 v_texCoords;

uniform vec4 u_target_color;

uniform sampler2D u_texture;
uniform sampler2D u_texture_mask;

void main() {
	vec4 mask = texture2D(u_texture_mask, v_texCoords);
	vec4 color = vec4(0.0,0.0,0.0,0.0);

	if (u_target_color.rgb == mask.rgb) {
		color.rgb = texture2D(u_texture, v_texCoords).rgb;
		color.a = mask.a;
	} else {
		color = mask;
	}
	gl_FragColor = color;
}