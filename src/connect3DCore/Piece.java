package connect3DCore;

import java.awt.Color;
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

		@Override
		public Color color() { return Color.GREEN; }
	},
	WHITE {
		@Override
		public char charRep() { return 'W'; }

		@Override
		public Color color() { return Color.WHITE; }
	},
	BLUE {
		@Override
		public char charRep() { return 'B'; }

		@Override
		public Color color() { return Color.BLUE; }
	},
	RED {
		@Override
		public char charRep() { return 'R'; }

		@Override
		public Color color() { return Color.RED; }
	},
	ORANGE {
		@Override
		public char charRep() { return 'O'; }

		@Override
		public Color color() { return Color.ORANGE;	}
	},
	PURPLE {
		@Override
		public char charRep() { return 'P'; }

		@Override
		public Color color() { return new Color(103, 58, 183); }
	},
	YELLOW {
		@Override
		public char charRep() { return 'Y'; }

		@Override
		public Color color() { return Color.YELLOW; }
	},
	EMPTY {
		@Override
		public char charRep() { return 'X'; }

		@Override
		public Color color() { return null; }
	};
	
	/**
	 * Returns a char that represents this color.
	 * @return
	 *  A char that represents this Piece's color.
	 */
	public abstract char charRep();
	
	/**
	 * Returns a color that can be used for rendering the piece.
	 * @return
	 *  The piece's color.
	 */
	public abstract Color color();
}
