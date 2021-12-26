package connect3DCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import connect3DRender.Graphics;

import static java.util.Map.entry;
import static connect3DCore.Piece.*;
import static connect3DCore.Direction.*;

/**
 * Implements board functionality using 3Dimesional array of Pieces.
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
	private Tuple lastLocation;
	/**
	 * The locations of the pieces that caused a player to make four (or more) in a row.
	 */
	private List<Tuple> winningPieceLocations;
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
			throw new IllegalArgumentException("Minimum board size is four for \"four in a row\"! ->"+dim);
		}
		pieces = new Piece[dim][dim][dim];
		for(int x = 0; x < dim; x++) {
			for(int y = 0; y < dim; y++) {
				for(int z = 0; z < dim; z++) {
					pieces[x][y][z] = EMPTY;
				}
			}
		}
		winningPieceLocations = List.of(); //default empty
		
		lastLocation = new Tuple(-1,-1,-1);
		
		piecesPlaced = 0;
	}
	
	@Override
	public boolean placePieceAt(int x, int z, Piece p) throws IllegalArgumentException, IllegalStateException {
		int y = getNextFree(x,z);
		if(y < 0) return false;
		return insertPieceAt(x, y, z, p);
	}

	/**
	 * Helper method, looks at the (x,z) column and returns the next available Z value.
	 * @param x
	 *  The lateral component of the column being checked.
	 * @param z
	 *  The depth of the column being checked.
	 * @return
	 *  The Z value of the next free point in the (x,z) column.
	 * @throws IllegalArgumentException
	 *  Thrown if (x,z) column is full to signal to the caller that it is full.
	 */
	private int getNextFree(int x, int z) throws IllegalArgumentException {
		//Start at y == 0 and work up until failure.
		for(int y = 0; y < pieces.length; y++) {
			if(getPieceAt(x, y, z) == EMPTY) return y;
		}
		return -1;
	}

	@Override
	public boolean hasSomeoneWon() {
		assert winningPieceLocations != null;
		assert winningPieceLocations.isEmpty() ? true : (winningPieceLocations.size() >= 4);
		
		return !winningPieceLocations.isEmpty();
	}

	@Override
	public Piece getWinner() {
		if(hasSomeoneWon()) {
			assert winningPieceLocations.isEmpty() == false;
			Tuple w = winningPieceLocations.get(0);
			return pieces[w.x][w.y][w.z];
		} else {
			assert winningPieceLocations.isEmpty() == true;
			return EMPTY;
		}
	}

	@Override
	public Piece getPieceAt(int x, int y, int z) {
		if(!isLocValid(x, y, z)) throw new IllegalArgumentException(x+" "+y+" "+z+" is an invalid location!\n"
				+ "Valid range is: 0 ->"+(pieces.length-1));
		return pieces[x][y][z];
	}
	
	/**
	 * Override of getPieceAt for Tuples
	 * @param tuple
	 *  the location we are retrieving from
	 * @return
	 *  the piece at the location
	 */
	private Piece getPieceAt(Tuple tuple) {
		return getPieceAt(tuple.x, tuple.y, tuple.z);
	}

	@Override
	public boolean isBoardFull() {
		int d = pieces.length; //TODO needs testing.
		return piecesPlaced == (d * d * d);
	}

	/**
	 * Insert a piece onto the board at a specified location.
	 * @param x
	 *  The lateral component of the position.
	 * @param y
	 *  The height component of the position.
	 * @param z
	 *  The depth component of the position.
	 * @param p
	 *  The piece to be inserted.
	 * @return
	 *  True if the insertion succeeded. 
	 * @throws IllegalArgumentException
	 *  Thrown if an invalid piece or location is supplied.
	 */
	private boolean insertPieceAt(int x, int y, int z, Piece p) throws IllegalArgumentException {
		assert p != null;
		if(hasSomeoneWon() || isBoardFull()) throw new IllegalStateException("Cannot place piece after game has ended");
		if(!isLocValid(x, y, z)) throw new IllegalArgumentException(x+" "+y+" "+z+" is an invalid location!\n"
			+ "Valid range is: 0 ->"+(pieces.length-1));
		if(p == EMPTY) throw new IllegalArgumentException("Cannot place empty!");
		if(pieces[x][y][z] != EMPTY) return false; //can't place on occupied area
		
		piecesPlaced++;
		lastLocation = new Tuple(x,y,z);
		pieces[x][y][z] = p; //actually assign the piece on the board.
		
		//This could be called from somewhere else because it is purely side effects.
		winningPieceLocations = checkForWin(lastLocation); 
		
		return true;
	}
	
	/**
	 * Check each direction and see if there are any 'four in a rows' originating from point tuple.
	 * @param tuple 
	 *  The location to check from. Should be valid. Should not be empty.
	 * @return
	 *  A list of locations of pieces that comprise the winning 'four in a row'
	 *  OR and empty list, if there is no four in a row.
	 */
	private List<Tuple> checkForWin(Tuple tuple) {
		assert isLocValid(tuple);
		assert getPieceAt(tuple) != EMPTY;
		Piece p = getPieceAt(tuple);
		for(Map.Entry<Direction, Direction> e : directionsPairs.entrySet()) {
			List<Tuple> result = 
			countDirection(e.getKey(), tuple, p); //collect all "p" locations in direction one
			result.addAll(countDirection(e.getValue(), tuple, p)); //collect all "p" locations in direction two
			result.add(tuple); //collect the p at "this" location
			if(result.size() >= 4) {
				return result;
			}
		}
		return List.of();
	}

	/**
	 * Collects the locations of instances of 'p' in the given direction from point 't'.
	 * Does NOT include the piece AT location 't'.
	 * Continues recursing until it encounters non-p OR runs off the edge of the board.
	 * @param direction
	 *  The direction through the board this method uses to search for instances of p. 
	 * @param t
	 *  The location we should search from. Does not collect an instance AT location 't', if it exists.
	 * @param p 
	 *  The piece type we are collecting the locations of. Should NOT be EMPTY.
	 * @return 
	 *  The a list of locations that contain p in the given direction.
	 *  List will be empty if there are no p in the given direction from location t.
	 */
	private List<Tuple> countDirection(Direction direction, Tuple t, Piece p) {
		assert p != EMPTY;
		List<Tuple> answer = new ArrayList<>();
		Tuple next = direction.next(t);
		if(isLocValid(next) && getPieceAt(next) == p) {
			answer.add(next);
			answer.addAll(countDirection(direction, next, p));
		}
		return answer;
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
	/**
	 * Overload of isLocValid method.
	 * @param tuple
	 *  a location.
	 * @return
	 *  true if the location is valid for this board.
	 */
	private boolean isLocValid(Tuple tuple) {
		return isLocValid(tuple.x, tuple.y, tuple.z);
	}

	@Override
	public List<Tuple> getWinningPieceLocations() {
		return new ArrayList<>(winningPieceLocations); //give a copy so outside forces can't change it.
	}

	@Override
	public void draw(Graphics g) {
		assert pieces.length >= 4;
		float unit = 1.0f;
		float width = pieces.length * unit;
		//Draw the base below 0,0,0
		g.setActiveColor(EMPTY);
		g.drawCubeAt(-1, -2, -1, unit + width + unit, unit);
		//draw the pieces as spheres
		for(int x = 0; x < pieces.length; x++) {
			for(int y = 0; y < pieces.length; y++) {
				for(int z = 0; z < pieces.length; z++) {
					Piece p = getPieceAt(x, y, z);
					if(p != EMPTY) {
						g.setActiveColor(p);
						g.drawSphereAt(x, y, z, unit);
					}
				}
			}
		}
	}

	@Override
	public boolean isXZvalid(int x, int z) {
		return isLocValid(x, 0, z);
	}
}
