#version 330
//FRAGMENT SHADER
in vec2 exTextureCoordinate;
out vec4 fragmentColor;

uniform sampler2D texture_sampler; //sampler2D holds the texture unit on the GPU that should be queried for texture color data.
uniform vec3 color; //the color to use if the use color flag has been set.
uniform int useColor; //A flag to indicate whether we should use the texture sampler or the color value. +ve value = use the color.

void main()
{
	if( useColor == 1 )
	{
		fragmentColor = vec4(color, 1);
	} 
	else
	{
		fragmentColor = texture(texture_sampler, exTextureCoordinate);
	}
}