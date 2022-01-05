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
	public Coord(float x, float y, float z, float w){
		this.x = x; this.y = y; this.z = z; this.w = w;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	public Coord(int x, int y, int z, int w){
		this.x = (float)x;
		this.y = (float)y;
		this.z = (float)z;
		this.w = (float)w;
	}
	
	@Override 
	public String toString() {
		return x+" "+y+" "+z+" "+w;
	}
}
