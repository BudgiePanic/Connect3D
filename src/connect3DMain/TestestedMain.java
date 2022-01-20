package connect3DMain;

import java.util.List;

import connect3DCore.Piece;
import connect3DGame.Game;
import connect3DRender.RenderFactory;

public class TestestedMain {

	public static void main(String[] args) {
		new Game(RenderFactory.Renderer("hardware", 4),List.of(Piece.GREEN),4).run();
	}

}
