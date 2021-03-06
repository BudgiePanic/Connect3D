package connect3DCore;

import java.util.List;

import connect3DRender.Component;

/**
 * Public facing board interface.
 * The board contains the internal representation of the connect3D game.
 * @author Benjamin
 *
 */
public interface Board extends Component {
	
	/**
	 * Query the board to see if a piece has won.
	 * @return true if a piece has made four in a row, except EMPTY.
	 */
	public boolean hasSomeoneWon();
	
	/**
	 * Returns whether the board can fit anymore pieces.
	 * @return true if the board is full and cannot fit anymore pieces.
	 */
	public boolean isBoardFull();
	
	/**
	 * Get the piece that won. returns EMPTY if there is no winner.
	 * @return The piece that won.
	 */
	public Piece getWinner();
	
	/**
	 * If someone has won, this method will return the location of their winning pieces.
	 * @return The location of the winning pieces OR an empty list if no one has won.
	 */
	public List<Tuple> getWinningPieceLocations();
	
	/**
	 * Adds a piece at a location if the placement is valid.
	 * Height of placement is handled internally by the implementation.
	 * Checks if the placement caused a player to win. 
	 * Updates internal fields after a placement.
	 * 
	 * @param x 
	 * 	x coordinate the piece should be placed at. Lateral.
	 * @param z
	 *  z coordinate the piece should be placed at. Depth.
	 * @param p
	 * 	The piece that is being placed.
	 * @return returns true if the placement succeeded, false if placement failed.
	 * @throws IllegalArgumentException 
	 *  Throws exception if x,y,z specifies a location outside the board's range.
	 * @throws IllegalStateException 
	 *  Thrown if a piece is placed after the game has ended.
	 */
	public boolean placePieceAt( int x, int z, Piece p) throws IllegalArgumentException, IllegalStateException;
	
	/**
	 * Check if an (x,z) column is valid for this board.
	 * @param x
	 *  The lateral location component.
	 * @param z
	 *  The depth location component.
	 * @return
	 *  True if this location is within the board.
	 */
	public boolean isXZvalid(int x, int z);
	
	/**
	 * Get a piece at a location.
	 * @param x
	 * 	coordinate of the piece.
	 * @param y
	 *  coordinate of the piece.
	 * @param z
	 *  coordinate of the piece.
	 * @return the piece at the location, may be EMPTY.
	 * @throws IllegalArgumentException 
	 *  Throws exception if x,y,z specifies a location outside the board's range.
	 */
	public Piece getPieceAt(int x, int y, int z) throws IllegalArgumentException;
	
	/**
	 * Get the next free Y location in an (x,z) column.
	 * @param x
	 * The horizontal location of the column.
	 * @param z
	 * The depth of the column.
	 * @return
	 * The next free Y element in the (x,z) column OR -1 if the column is full.
	 * @throws IllegalArgumentException
	 * Thrown if the (x,z) location is not valid for the board.
	 */
	public int getNextFree(int x, int z) throws IllegalArgumentException;
	
}
