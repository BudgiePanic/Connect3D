package connect3DCore;

/**
 * Public facing board interface.
 * The board contains the internal representation of the connect3D game.
 * @author Benjamin
 *
 */
public interface Board {
	
	/**
	 * Query the board to see if a piece has won.
	 * @return true if a piece has made four in a row, except EMPTY.
	 */
	public boolean hasSomeoneWon();
	
	/**
	 * Get the piece that won. Never returns EMPTY.
	 * @return The piece that won.
	 */
	public Piece getWinner();
	
	/**
	 * Adds a piece at a location if the placement is valid.
	 * Checks if the placement caused a player to win. 
	 * Updates internal fields after a placement.
	 * 
	 * @param x 
	 * 	x coordinate the piece should be placed at.
	 * @param y
	 *  y coordinate the piece should be placed at.
	 * @param z
	 *  z coordinate the piece should be placed at.
	 * @param p
	 * 	The piece that is being placed.
	 * @return returns true if the placement succeeded, false if invalid placement.
	 */
	public boolean placePieceAt( int x, int y, int z, Piece p);
	
	/**
	 * Get a piece at a location.
	 * @param x
	 * 	coordinate of the piece.
	 * @param y
	 *  coordinate of the piece.
	 * @param z
	 *  coordinate of the piece.
	 * @return the piece at the location, may be EMPTY.
	 */
	public Piece getPieceAt( int x, int y, int z);
	
}
