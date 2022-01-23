#version  330
//VERTEX SHADER
layout (location=0) in vec3 position; //world space position of the vertex. From position 0 in the input buffer, interpret buffer data as a vec3.

void main()
{
	gl_Position = vec4(position, 1.0);
}