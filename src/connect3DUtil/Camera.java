package connect3DUtil;

import org.joml.Vector3f;

import connect3DUtil.MathUtil.Coord3D;

/**
 * Camera class remembers the camera position and rotation values.
 * Works with JOML.
 * The objects storing the values are mutable.
 * @author Benjamin
 *
 */
public class Camera {
	
	/**
	 * The position of the camera in the world.
	 */
	public final Vector3f worldPosition;
	
	/**
	 * The Pitch, Yaw and Roll values of the camera.
	 */
	public final Vector3f cameraRotation;
	
	/**
	 * The location in the world that the camera is pointing at.
	 */
	public final Vector3f cameraPointingAt;
	
	/**
	 * A vector representing the up direction in the world.
	 */
	public final Vector3f worldUp;
	
	/**
	 * How far away the camera is from the origin.
	 */
	public float radius = 2.0f;
	
	/**
	 * The displacement of the camera in radians along the polar (Z) axis.
	 * 0 radians == pointing along the Z axis.
	 */
	private float theta = (float)(Math.PI * 0.5);
	
	/**
	 * The displacement of the camera in radians along the XY plane.
	 * 0 radians == pointing along the X axis.
	 */
	private float phi = 0.0f;
	
	/**
	 * Create a camera at {0,0,5} with rotation values {0,0,0}.
	 */
	public Camera() {
		this.worldPosition = new Vector3f(0,0,radius);
		this.cameraRotation = new Vector3f(0,0,0);
		this.cameraPointingAt = new Vector3f(0,0,0);
		this.worldUp = new Vector3f(0,1,0);
	}
	
	/**
	 * Set a new position for the camera.
	 * @param newPosition
	 *  The new position of the camera.
	 */
	public void setCameraPosition(Coord3D newPosition) {
		this.worldPosition.x = (float)newPosition.x;
		this.worldPosition.y = (float)newPosition.y;
		this.worldPosition.z = (float)newPosition.z;
	}
	
	/**
	 * Move the camera by some amount.
	 * @param dx
	 *  The amount to move in the X axis.
	 * @param dy
	 *  The amount to move in the Y axis.
	 * @param dz
	 *  The amount to move in the Z axis.
	 */
	protected void moveCamera(float dx, float dy, float dz) {
		
	}

	/**
	 * Updates the camera's position vector based on the radius, Theta and Chi values.
	 * I swapped the Z and Y equations with each other to work in my coordinate system.... TODO might need to change this back...
	 * @see "https://en.wikipedia.org/wiki/Spherical_coordinate_system"
	 */
	public void updatePosition() {
		this.worldPosition.x = radius * ((float) Math.cos(phi)) * ((float) Math.sin(theta));
		this.worldPosition.y = radius * ((float) Math.cos(theta));
		this.worldPosition.z = radius * ((float) Math.sin(phi)) * ((float) Math.sin(theta));
		/*
		//old code
		this.worldPosition.y = radius * ((float) Math.sin(phi)) * ((float) Math.sin(theta));
		this.worldPosition.z = radius * ((float) Math.cos(theta));
		*/
	}
	
	/**
	 * Updates the Phi angle used by the camera to determine its position.
	 * The Phi angle ranges between 0 and 2 * PI radians.
	 * The angle will wrap around to remain in these bounds if 'angle' causes Phi to exceed the bounds.
	 * @param angle
	 *  The amount that will be added to the angle IN DEGREES.
	 *  Can be negetive.
	 */
	public void addToPhi(float angle) {
		float amount = (float)Math.toRadians(angle);
		phi += amount;
		if(phi >= (2.0 * Math.PI)) phi = 0.0f;
		if(phi < 0.0f) phi = (float)(1.999 * Math.PI); //according to the wiki page, 2PI value is invalid
	}
	
	/**
	 * Updates the Theta angle used by the camera to determine its position.
	 * The Theta angle can range between 0 and PI radians. I am reducing this range to between 0 and PI * 0.5.
	 * The angle will be clipped to remain in these bounds if 'angle' causes Theta to exceed the bounds.
	 * @param angle
	 *  The amount that will be added to the angle IN DEGREES.
	 *  Can be negetive.
	 */
	public void addToTheta(float angle) {
		float amount = (float)Math.toRadians(angle);
		theta += amount;
		if(theta > 0.5 * Math.PI) theta = (float)(0.5 * Math.PI);
		if(theta < 0.0f) theta = 0.01f;
	}
	
	
}
