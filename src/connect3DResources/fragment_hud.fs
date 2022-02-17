#version 330
//FRAGMENT-SHADER
in vec2 outTextCoord; //recieved from the vertex shader

out vec4 fragmentColor;

uniform sampler2D texture_sampler;
uniform vec4 color;

void main()
{
	fragmentColor = color * texture(texture_sampler, outTextCoord);
}