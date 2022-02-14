package connect3DUtil;

import java.util.Objects;
import java.util.Optional;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * A model is a Mesh combined with transformations that move it into world space when applied.
 * The transformations are mutable objects.
 * Multiple models can reuse the same mesh object.
 * @author Benjamin
 *
 */
public class Model {
	
	/**
	 * The mesh that this model is using.
	 * Multiple models can share the same mesh.
	 */
	private final Mesh mesh;
	/**
	 * The position of this model in the world.
	 */
	private final Vector3f position;
	/**
	 * The scale applied to the mesh to bring it into world scale.
	 */
	private float scale;
	/**
	 * The rotation applied to the mesh to bring it into the world position.
	 */
	private final Vector3f rotation;
	/**
	 * Optional flat color to be placed onto the mesh.
	 * Some Meshes have a texture which will be used instead.
	 */
	private final Optional<Vector3f> color;
	
	/**
	 * Create a new model.
	 * @param mesh
	 *  The mesh that this model will use.
	 * @param color
	 *  The color of this model. The mesh will be drawn with this color iff it does not have a texture.
	 *  This value can be null. 
	 */
	public Model(Mesh mesh, Vector3f color){
		this.mesh = mesh;
		this.position = new Vector3f(0,0,0);
		this.scale = 1.0f;
		this.rotation = new Vector3f(0,0,0);
		this.color = ((color == null) ? (Optional.empty()) : (Optional.of(color)));
	}
	
	/**
	 * Readies this model's mesh with its color information if there is no texture.
	 */
	public void ready() {
		Material material = mesh.getMaterial();
		if(material.texture.isEmpty() && color.isPresent()) {
			Vector4f col = new Vector4f(color.get(),1.0f);
			material.updateColor(col);
		}
	}

	/**
	 * Get the color that this model is using
	 * @return
	 *  The color of this model. May be null.
	 */
	public Optional<Vector3f> getColor() {
		return this.color;
	}
	
	/**
	 * Return the position of this model in world space.
	 * @return
	 *  The mutable position object of this model. 
	 */
	public Vector3f getPosition() {
		return position;
	}
	
	/**
	 * Return the rotation values this model uses.
	 * @return
	 *  The mutable rotation object of this model.
	 */
	public Vector3f getRotation() {
		return rotation;
	}
	
	/**
	 * Return the scale value of this model.
	 * @return
	 *  The scale being applied to this model.
	 */
	public float getScale() {
		return scale;
	}
	
	/**
	 * Update the transform that moves this model from model space to world space.
	 * @param x
	 *  The distance to move in the X dimension.
	 * @param y
	 *  The distance to move in the Y dimension.
	 * @param z
	 *  The distance to move in the Z dimension.
	 */
	public void updatePosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
	}
	
	/**
	 * Update the scale this model will use.
	 * @param scale
	 *  The new scale from model space to world space.
	 */
	public void updateScale(float scale) {
		this.scale = scale;
	}
	
	/**
	 * Update the rotation values this model will use.
	 * @param pitch
	 *  The rotation along the X axis IN DEGREES
	 * @param roll
	 *  The rotation along the Z axis IN DEGREES
	 * @param yaw
	 *  The rotation along the Y axis IN DEGREES
	 */
	public void updateRotation(float pitch, float roll, float yaw) {
		rotation.x = pitch;
		rotation.y = yaw;
		rotation.z = roll;
	}
	
	/**
	 * Get the mesh that this model is using.
	 * @return
	 *  The mesh that this model is using
	 */
	public Mesh getMesh() {
		return this.mesh;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mesh, position, rotation, scale);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Model other = (Model) obj;
		return Objects.equals(mesh, other.mesh) && Objects.equals(position, other.position)
				&& Objects.equals(rotation, other.rotation)
				&& Float.floatToIntBits(scale) == Float.floatToIntBits(other.scale);
	}
}
