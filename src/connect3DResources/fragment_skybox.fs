#version 330
//FRAGMENT SHADER

in vec2 outTextureCoord;

out vec4 color;

uniform sampler2D texture_sampler;
uniform vec3 ambientLight;

void main()
{
	color = vec4(ambientLight, 1.0) * texture(texture_sampler, outTextureCoord);
}