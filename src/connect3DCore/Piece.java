package connect3DCore;
/**
 * Symbolic constants to refer to the different pieces on the game board.
 * Each player places a different piece.
 * @author Benjamin
 *
 */
public enum Piece {
	GREEN {
		@Override
		public char charRep() { return 'G'; }
	},
	WHITE {
		@Override
		public char charRep() { return 'W'; }
	},
	BLUE {
		@Override
		public char charRep() { return 'B'; }
	},
	RED {
		@Override
		public char charRep() { return 'R'; }
	},
	ORANGE {
		@Override
		public char charRep() { return 'O'; }
	},
	PURPLE {
		@Override
		public char charRep() { return 'P'; }
	},
	YELLOW {
		@Override
		public char charRep() { return 'Y'; }
	},
	EMPTY {
		@Override
		public char charRep() { return 'X'; }
	};
	
	/**
	 * Returns a char that represents this color.
	 * @return
	 *  A char that represents this Piece's color.
	 */
	public abstract char charRep();
}
