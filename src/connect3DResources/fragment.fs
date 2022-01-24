#version 330
//FRAGMENT SHADER
in vec3 exColor;
out vec4 fragmentColor;

void main()
{
	fragmentColor = vec4(exColor, 1.0);
}