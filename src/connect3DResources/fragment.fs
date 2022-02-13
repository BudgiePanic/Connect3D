#version 330
//FRAGMENT SHADER

struct Attenuation
{
	float constant;
	float linear;
	float exponent;
};

struct PointLight
{
	vec3 color;
	vec3 viewPosition;
	float intensity;
	Attenuation att;
};

struct Material
{
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	int hasTexture;
	float reflectance;
};

struct DirectionalLight
{
	vec3 color;
	vec3 direction;
	float intensity;
};

in vec2 exTextureCoordinate;
in vec3 worldViewNormal;
in vec3 worldViewVertexPosition;

out vec4 fragmentColor;

uniform sampler2D texture_sampler; //sampler2D holds the texture unit on the GPU that should be queried for texture color data.
uniform vec3 ambientLight; //the color of the ambient light
uniform float specularPower;
uniform Material material;
uniform PointLight pointLight;
uniform DirectionalLight directionLight;

//global color variables
vec4 ambientColor;
vec4 diffuseColor;
vec4 specularColor;

//configure the global color variables.
void configureColors(Material material, vec2 textureCoordinate)
{
	if(material.hasTexture == 1)
	{
		ambientColor = texture(texture_sampler, exTextureCoordinate);
		diffuseColor = ambientColor;
		specularColor = ambientColor;
	}
	else
	{
		ambientColor = material.ambient;
		diffuseColor = material.diffuse;
		specularColor = material.specular;
	}
}

vec4 calculateLightColor(vec3 lightColor, float lightIntensity, vec3 position, vec3 toLightDirection, vec3 normal)
{
	vec4 colorDiffuse = vec4(0,0,0,0);
	vec4 colorSpecular = vec4(0,0,0,0);
	//diffuse
	float diffuseFactor = max(dot(normal, toLightDirection),0.0);
	colorDiffuse = diffuseColor * vec4(lightColor, 1.0) * lightIntensity * diffuseFactor;
	//specular
	vec3 cameraDirection = normalize(-position); //camera is at 0,0,0 in view space...
	vec3 fromLightSource = -toLightDirection;
	vec3 reflectedLight = normalize(reflect(fromLightSource, normal));
	float specularFactor = max(dot(cameraDirection, reflectedLight), 0.0);
	specularFactor = pow(specularFactor, specularPower);
	colorSpecular = specularColor * lightIntensity * specularFactor * material.reflectance * vec4(lightColor, 1.0);
	
	return (colorDiffuse + colorSpecular);
}

vec4 calculateDirectionalLight(DirectionalLight light, vec3 position, vec3 normal)
{
	return calculateLightColor(light.color, light.intensity, position, normalize(light.direction), normal);
}

vec4 calculatePointLight(PointLight light, vec3 vertexPosition, vec3 normal)
{
	vec3 lightDirection = light.viewPosition - vertexPosition;
	vec3 toLightDirection = normalize(lightDirection);
	vec4 lightColor = calculateLightColor(light.color, light.intensity, vertexPosition, toLightDirection, normal);
	
	//attenuation
	float distance = length(lightDirection);
	float attenuationInv = light.att.constant + light.att.linear * distance + light.att.exponent * distance * distance;
	
	return lightColor / attenuationInv;
}

void main()
{
	configureColors(material, exTextureCoordinate);
	
	vec4 diffuseSpecularComponent = calculatePointLight(pointLight, worldViewVertexPosition, worldViewNormal);
	diffuseSpecularComponent += calculateDirectionalLight(directionLight, worldViewVertexPosition, worldViewNormal);
	
	fragmentColor = ambientColor * vec4(ambientLight, 1) + diffuseSpecularComponent;
}