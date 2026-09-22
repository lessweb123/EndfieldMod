uniform vec2 u_campos;
uniform vec2 u_resolution;

varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main() {
	vec2 c = v_texCoords;
	vec2 v = vec2(1.0 / u_resolution.x, 1.0 / u_resolution.y);
	vec2 coords = vec2(c.x / v.x + u_campos.x, c.y / v.y + u_campos.y);

	vec2 baseCoords = vec2((mod(coords.x, 32.0)/ 8.0) + 0.5, (-mod(coords.y, 32.0)/ 8.0) + 0.5);
	gl_FragColor = texture2D(u_texture, baseCoords);
}