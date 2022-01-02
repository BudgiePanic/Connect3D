package connect3DUtil;
/**
 * Store a location in 3D space.
 * @author Benjamin
 *
 */
public final class Coord {
	/**
	 * 
	 */
	public final float x,y,z,w;
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	Coord(float x, float y, float z, float w){
		this.x = x; this.y = y; this.z = z; this.w = w;
	}
	
	@Override 
	public String toString() {
		return x+" "+y+" "+z+" "+w;
	}
}
