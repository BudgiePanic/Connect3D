package connect3DCore;
/**
 * Helper class, stores x,y,z components of a location on the board.
 * @author Benjamin
 *
 */
public class Tuple {
	/**
	 * lateral
	 */
	public final int x;
	/**
	 * height
	 */
	public final int y;
	/**
	 * depth
	 */
	public final int z;
	/**
	 * Create a new tuple
	 * @param x 
	 *  the lateral position
	 * @param y 
	 *  the height
	 * @param z 
	 *  the depth
	 */
	public Tuple(int x, int y, int z) {
		this.x = x; this.y = y; this.z = z;
	}
	
	@Override
	public String toString() {
		return x + " " + y + " " + z;
	}
}