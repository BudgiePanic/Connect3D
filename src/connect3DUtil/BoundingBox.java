package connect3DUtil;

import java.util.Optional;
import org.joml.Vector3i;


/**
 * The bounding box is used for scene selection.
 * @author Benjamin
 *
 */
public class BoundingBox extends Model {

	/**
	 * The (x,y,z) position of this bounding box relative to the connect 4 game.
	 * This may be different to the model position as the model may have been translated.
	 */
	protected final Vector3i gameCoords;
	
	/**
	 * The BoundingBox above this box.
	 */
	protected Optional<BoundingBox> aboveMe;
	
	/**
	 * A bounding Box.
	 * @param mesh
	 * The box to use.
	 * @param gameCoords
	 *  The game space location of this bounding box. 
	 */
	public BoundingBox(Mesh mesh, Vector3i gameCoords) {
		super(mesh, null);
		this.gameCoords = gameCoords;
		aboveMe = Optional.empty();
	}

	/**
	 * Adds the above box.
	 * @param b
	 */
	public void setAbove(BoundingBox b) {
		aboveMe = Optional.of(b);
	}
	
	/**
	 * Gets the Bounding box at the top of this stack.
	 * @return
	 *  Returns the bounding box at the top of this stack.
	 *  Returns 'this' if it is the top of the stack
	 */
	public BoundingBox getTop(){
		if(aboveMe.isEmpty()) return this;
		return aboveMe.get().getTop();
	}
	
	/**
	 * Gets the top game coord of this bounding box stack.
	 * @return
	 * The top point of this bounding box stack
	 */
	public Vector3i top() {
		if(aboveMe.isEmpty()) return gameCoords;
		return aboveMe.get().top();
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		BoundingBox other = (BoundingBox) obj;
		int x,y,z;
		x = other.gameCoords.x;
		y = other.gameCoords.y;
		z = other.gameCoords.z;
		if(gameCoords.equals(x, y, z)) {
			return true;
		}
		
		return false;
	}
	
	

}
