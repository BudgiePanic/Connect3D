#version 330
//FRAGMENT SHADER
in vec2 exTextureCoordinate;
out vec4 fragmentColor;

uniform sampler2D texture_sampler;

void main()
{
	fragmentColor = texture(texture_sampler, exTextureCoordinate);
}