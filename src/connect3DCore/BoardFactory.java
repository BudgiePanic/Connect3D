package connect3DCore;

/**
 * Conceal the instantion of boards.
 * @author Benjamin
 *
 */
public final class BoardFactory {
	/**
	 * 
	 * @param size
	 *  The size of the board that is desired.
	 * @return
	 *  A board that has size * size * size dimensions.
	 * @throws IllegalArgumentException 
	 *  Thrown if the size is invalid.
	 */
	public static Board board(int size) throws IllegalArgumentException{
		return new ArrayBoard(size);
	}
}
