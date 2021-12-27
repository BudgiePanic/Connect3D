package connect3DRender;

/**
 * Observers want to know the location of events user cause
 * 
 * @author Benjamin
 *
 */
public interface Observer {

	/**
	 * @param x
	 *  lateral location of the event
	 * @param y
	 *  height of the event
	 * @param z
	 *  the depth of the event
	 * @param type
	 *  the type of the event
	 */
	void update(int x, int y, int z, String type);
}
