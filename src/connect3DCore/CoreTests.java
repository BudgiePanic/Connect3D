package connect3DCore;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test the core functionality of the program.
 * @author Benjamin
 *
 */
class CoreTests {

	Board makeTestBoard(int size) {
		return new ArrayBoard(size);
	}
	
	@Test
	void testCreateArrBoard() {
		Board b = makeTestBoard(4);
		for(int x = 0; x < 4; x++) {
			for(int y = 0; y < 4; y++) {
				for(int z = 0; z < 4; z++) {
					assertTrue(b.getPieceAt(x, y, z) == Piece.EMPTY);
				}
			}
		}
	}
	
	@Test
	void testFullBoard() {
		Board b = makeTestBoard(4);
		for(int x = 0; x < 4; x++) {
			for(int y = 0; y < 4; y++) {
				for(int z = 0; z < 4; z++) {
					b.placePieceAt(x, y, z, Piece.RED);
				}
			}
		}
		assertTrue(b.isBoardFull());
	}
	
	@Test
	void testpartialBoard() {
		Board b = makeTestBoard(4);
		for(int x = 0, y = 0, z = 0; x < 4; x++) {
			b.placePieceAt(x, y, z, Piece.RED);
		}
		assertFalse(b.isBoardFull());
	}
	
	@Test
	void testEmptyBoard() {
		Board b = makeTestBoard(4);
		assertFalse(b.isBoardFull());
	}
	
	@Test
	void testSimpleWinHorizon() {
		Board b = makeTestBoard(4);
		for(int i = 0; i < 4; i++) {
			b.placePieceAt(i, 0, 0, Piece.RED);
		}
		assertTrue(b.hasSomeoneWon());
		assertTrue(b.getWinner() == Piece.RED);
		assertTrue(b.getWinningPieceLocations().isEmpty() == false);
		System.out.println(b.getWinningPieceLocations());
	}

}
