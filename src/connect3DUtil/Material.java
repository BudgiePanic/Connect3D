package connect3DUtil;

import java.util.Optional;

import org.joml.Vector4f;

/**
 * POJO for material struct in fragment shader.
 * @author Benjamin
 *
 */
public final class Material {
	
	/**
	 * White is the default color.
	 */
	private static final Vector4f defaultColor = new Vector4f(1.0f,1.0f,1.0f,1.0f);
	
	/**
	 * The ambient color of this material.
	 */
	private Vector4f ambient;
	/**
	 * The diffuse color of this material.
	 */
	private Vector4f diffuse;
	/**
	 * The specular color of this material.
	 */
	private Vector4f specular;
	/**
	 * The reflectance value of this texture
	 */
	private float reflectance;
	/**
	 * Some materials have a texture.
	 */
	public final Optional<Texture> texture;
	
	/**
	 * The default material.
	 * Configures the material with default values.
	 */
	public Material() {
		this.ambient = defaultColor;
		this.diffuse = defaultColor;
		this.specular = defaultColor;
		texture = Optional.empty();
		this.reflectance = 0.0f;
	}
	
	/**
	 * A material with a texture and a default reflectance value.
	 * @param texture
	 *  The texture this material will use.
	 */
	public Material(Texture texture) {
		this(texture, 0.0f);
	}
	
	/**
	 * A material with a texture and a reflectance quantity.
	 * @param texture
	 *  The texture this material will use.
	 * @param reflectance
	 *  The reflectance of the material.
	 */
	public Material(Texture texture, float reflectance) {
		this(defaultColor, defaultColor, defaultColor, Optional.of(texture), reflectance);
	}
	
	/**
	 * A material with a color and no texture.
	 * @param color
	 *  The color of the material.
	 * @param reflectance
	 *  The reflectance of the material.
	 */
	public Material(Vector4f color, float reflectance) {
		this(color, color, color, Optional.empty(), reflectance);
	}
	
	/**
	 * Constructor to individually set every value.
	 * @param ambientColor
	 * @param diffuseColor
	 * @param specularColor
	 * @param texture
	 * @param reflectance
	 */
	public Material(Vector4f ambientColor, Vector4f diffuseColor, Vector4f specularColor, Optional<Texture> texture, float reflectance){
		assert texture != null;
		this.ambient = ambientColor; this.diffuse = diffuseColor; this.specular = specularColor;
		this.reflectance = reflectance;
		this.texture = texture;
	}
	
	public Vector4f getAmbient() {
		return ambient;
	}
	public void setAmbient(Vector4f ambient) {
		this.ambient = ambient;
	}
	public Vector4f getDiffuse() {
		return diffuse;
	}
	public void setDiffuse(Vector4f diffuse) {
		this.diffuse = diffuse;
	}
	public Vector4f getSpecular() {
		return specular;
	}
	public void setSpecular(Vector4f specular) {
		this.specular = specular;
	}
	public float getReflectance() {
		return reflectance;
	}
	public void setReflectance(float reflectance) {
		this.reflectance = reflectance;
	}
	/**
	 * Update the color of this material to a new color.
	 * @param color
	 *  The new color.
	 */
	public void updateColor(Vector4f color) {
		setAmbient(color);
		setDiffuse(color);
		setSpecular(color);
	}
	/**
	 * Deletes the texture of this material if it is present.
	 * TODO if the same texture is aliased over multiple materials, then this can cause issues...
	 */
	public void delete() {
		this.texture.ifPresent((Texture t)->t.delete());
	}
}
