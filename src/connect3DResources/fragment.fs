#version 330
//FRAGMENT SHADER
in vec2 exTextureCoordinate;
out vec4 fragmentColor;

uniform sampler2D texture_sampler; //sampler2D holds the texture unit on the GPU that should be queried for texture color data.

void main()
{
	fragmentColor = texture(texture_sampler, exTextureCoordinate);
}