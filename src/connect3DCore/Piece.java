package connect3DCore;

import java.awt.Color;

import org.joml.Vector3f;

import connect3DUtil.ColorVector;
/**
 * Symbolic constants to refer to the different pieces on the game board.
 * Each player places a different piece.
 * @author Benjamin
 *
 */
public enum Piece {
	RED {
		@Override
		public char charRep() { return 'R'; }

		@Override
		public Color color() { return Color.RED; }

		@Override
		public Vector3f colorVector() {
			return ColorVector.RED;
		}
	},
	BLUE {
		@Override
		public char charRep() { return 'B'; }

		@Override
		public Color color() { return Color.BLUE; }

		@Override
		public Vector3f colorVector() {
			return ColorVector.BLUE;
		}
	},	
	GREEN {
		@Override
		public char charRep() { return 'G'; }

		@Override
		public Color color() { return Color.GREEN; }

		@Override
		public Vector3f colorVector() {
			return ColorVector.GREEN;
		}
	},
	YELLOW {
		@Override
		public char charRep() { return 'Y'; }

		@Override
		public Color color() { return Color.YELLOW; }

		@Override
		public Vector3f colorVector() {
			return ColorVector.YELLOW;
		}
	},
	ORANGE {
		@Override
		public char charRep() { return 'O'; }

		@Override
		public Color color() { return Color.ORANGE;	}

		@Override
		public Vector3f colorVector() {
			return new Vector3f(1.0f, 0.3f, 0.0f);
		}
	},
	PURPLE {
		@Override
		public char charRep() { return 'P'; }

		@Override
		public Color color() { return new Color(103, 58, 183); }

		@Override
		public Vector3f colorVector() {
			return ColorVector.PURPLE;
		}
	},	
	WHITE {
		@Override
		public char charRep() { return 'W'; }

		@Override
		public Color color() { return Color.WHITE; }

		@Override
		public Vector3f colorVector() {
			return ColorVector.WHITE;
		}
	},
	EMPTY {
		@Override
		public char charRep() { return 'X'; }

		@Override
		public Color color() { return null; }

		@Override
		public Vector3f colorVector() {
			return null;
		}
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

	/**
	 * Returns a Vector3f representation of this piece's color.
	 * @return
	 * The color of this piece.
	 */
	public abstract Vector3f colorVector();
}
