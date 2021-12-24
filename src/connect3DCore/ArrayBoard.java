package connect3DCore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Map.entry;
import static connect3DCore.Piece.*;
import static connect3DCore.Direction.*;

/**
 * Implements board functionality using 3Dimesional array of Piece.
 * @author Benjamin
 *
 */
final class ArrayBoard implements Board {
	
	/**
	 * Store all of the pairs of directions that can be used to form four in a row.
	 * There are 13 pairs of directions.
	 */
	private static final Map<Direction, Direction> directionsPairs = Map.ofEntries(
			entry(UP, DOWN),
			entry(LEFT, RIGHT),
			entry(TOWARDS, AWAY),
			//Flat diagonals
			entry(LeftANDaway, RightANDtowards),
			entry(LeftANDtowards, RightANDaway),
			//Left right height
			entry(LeftANDdown, RightANDup),
			entry(LeftANDup, RightANDdown),
			//Towards away height
			entry(TowardsANDup, AwayANDdown),
			entry(TowardsANDdown, AwayANDup),
			//Diag 1 height
			entry(LeftANDtowardsANDdown, RightANDawayANDup),
			entry(LeftANDtowardsANDup, RightANDawayANDdown),
			//Diag 2 height
			entry(LeftANDawayANDup, RightANDtowardsANDdown),
			entry(LeftANDawayANDdown, RightANDtowardsANDup)
	);
	
	/**
	 * A 3D array storing the pieces.
	 */
	private Piece[][][] pieces;
	/**
	 * The location of the last piece that was placed.
	 */
	private int lastX, lastY, lastZ;
	/**
	 * The location of the piece that caused a player to make four in a row.
	 */
	private int winnerX, winnerY, winnerZ;
	/**
	 * record the number of times the placePiece method is called.
	 */
	private int piecesPlaced;
	
	/**
	 * Create a board.
	 * @param dim 
	 * 	board will have dim * dim * dim dimensions.
	 */
	ArrayBoard(int dim){
		if(dim < 4) {
			throw new IllegalArgumentException("Minimum board size is four! ->"+dim);
		}
		pieces = new Piece[dim][dim][dim];
		Arrays.setAll(pieces, i -> EMPTY); //TODO needs testing
		winnerX = -1;
		winnerY = -1;
		winnerZ = -1;
		
		lastX = -1;
		lastY = -1;
		lastZ = -1;
		
		piecesPlaced = 0;
	}

	@Override
	public boolean hasSomeoneWon() {
		return winnerX != -1;
	}

	@Override
	public Piece getWinner() {
		if(hasSomeoneWon()) {
			return pieces[winnerX][winnerY][winnerZ];
		} else {
			return EMPTY;
		}
	}

	@Override
	public Piece getPieceAt(int x, int y, int z) {
		if(!isLocValid(x, y, z)) throw new IllegalArgumentException(x+" "+y+" "+z+" is an invalid location!\n"
				+ "Valid range is: 0 ->"+(pieces.length-1));
		return pieces[x][y][z];
	}

	@Override
	public boolean isBoardFull() {
		int d = pieces.length;
		return piecesPlaced == (d * d * d);
	}

	@Override
	public boolean placePieceAt(int x, int y, int z, Piece p) throws IllegalArgumentException {
		if(!isLocValid(x, y, z)) throw new IllegalArgumentException(x+" "+y+" "+z+" is an invalid location!\n"
			+ "Valid range is: 0 ->"+(pieces.length-1));
		if(p == EMPTY) throw new IllegalArgumentException("Cannot place empty!");
		if(pieces[x][y][z] != EMPTY) return false; //can't place on occupied area
		
		piecesPlaced++;
		
		lastX = x;
		lastY = y;
		lastZ = z;
		
		pieces[x][y][z] = p;
		
		/*for(Function<tuple, tuple> dir : directions) {
			if(hasSomeoneWon()) break;
			countDirection(dir, new tuple(x,y,x), p);
		}*/ //TODO sum complementary directions
		countDirection(directions.get(0), new tuple(x,y,x), p, 0);
		
		Direction up = (t) -> new Tuple(t.x, t.y, t.z);
		
		return true;
	}
	
	/**
	 * Checks the location at lastX, lastY, lastZ and count how many instances of p lie in that direction.
	 * Accomplishes this using recursion, looking in direction.
	 * Continues recursing until it encounters EMPTY OR runs off the edge of the board.
	 * @param direction
	 *  The direction through the board this method uses to check for four in a row. 
	 * @param t
	 *  The location we should search from 
	 * @param p 
	 *  The piece type we are checking if they won
	 * @param count 
	 *  The number of p's we have encountered so far.
	 * @return 
	 *  The number of p on the direction.
	 */
	private int countDirection(Function<tuple, tuple> direction, tuple t, Piece p, int count) {
		return -1;
	}
	
	
	
	/**
	 * Check an x,y,z position is in the bounds of the board.
	 * @param x
	 * 	first array index
	 * @param y
	 *  second array index
	 * @param z
	 *  third array index
	 * @return true if x,y,z parameters constitute a valid board location
	 */
	private boolean isLocValid(int x, int y, int z) {
		int dim = pieces.length;
		return
				x >= 0 && y >= 0 && z >= 0 &&
				x < dim && y < dim && z < dim;
	}
}
