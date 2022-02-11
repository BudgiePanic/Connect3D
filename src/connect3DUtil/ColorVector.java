package connect3DUtil;

import org.joml.Vector3f;

/**
 * Class with static Vector3f objects representing different colors.
 * @author Benjamin
 *
 */
public final class ColorVector {
	/**
	 * Static members only, no color vector objects.
	 */
	private ColorVector() {}
	
	/**
	 * RED
	 */
	public static final Vector3f RED = new Vector3f(1.0f, 0.0f, 0.0f);
	
	/**
	 * BLUE
	 */
	public static final Vector3f BLUE = new Vector3f(0.0f, 0.0f, 1.0f);
	
	/**
	 * GREEN
	 */
	public static final Vector3f GREEN= new Vector3f(0.0f, 1.0f, 0.0f);
	
	/**
	 * YELLOW
	 */
	public static final Vector3f YELLOW = new Vector3f(1.0f, 1.0f, 0.0f);
	
	/**
	 * ORANGE
	 */
	public static final Vector3f ORANGE = new Vector3f(1.0f, 0.3f, 0.0f);
	
	/**
	 * PURPLE
	 */
	public static final Vector3f PURPLE= new Vector3f(0.4f, 0.0f, 1.0f);
	
	/**
	 * WHITE
	 */
	public static final Vector3f WHITE = new Vector3f(1.0f, 1.0f, 1.0f);
	
}
