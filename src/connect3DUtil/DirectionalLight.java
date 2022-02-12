package connect3DUtil;

import org.joml.Vector3f;

/**
 * Directional lights model the sun, a light source that effects all models regardless of position.
 * Is not effected by attenuation.
 * @author Benjamin
 *
 */
public class DirectionalLight {
	
	/**
	 * The direction of the light. A direction vector.
	 * For example, x: 0 y: 1 z: 0 is mid-day
	 */
	private final Vector3f direction;
	
	/**
	 * The color of this light.
	 */
	private final Vector3f color;
	
	/**
	 * The intensity of the light.
	 */
	private float intensity;
	
	/**
	 * Create a new directional light.
	 * @param direction
	 * @param color
	 * @param intensity
	 */
	public DirectionalLight(Vector3f direction, Vector3f color, float intensity) {
		this.direction = direction;
		this.color = color;
		this.intensity = intensity;
	}
	
	/**
	 * Copies color values from the parameter to this Directional light.
	 * @param color
	 */
	public void setColor(Vector3f color) {
		this.color.x = color.x;
		this.color.y = color.y;
		this.color.z = color.z;
	}
	
	/**
	 * Update this directional light's light intensity.
	 * @param intensity
	 */
	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}
	
	/**
	 * Update this directional lights direction
	 * @param direction
	 */
	public void setDirection(Vector3f direction) {
		this.direction.x = direction.x;
		this.direction.y = direction.y;
		this.direction.z = direction.z;
	}

	/**
	 * 
	 * @return
	 */
	public Vector3f getDirection() {
		return direction;
	}

	/**
	 * 
	 * @return
	 */
	public Vector3f getColor() {
		return color;
	}

	/**
	 * 
	 * @return
	 */
	public float getIntensity() {
		return intensity;
	}
	
	
}
