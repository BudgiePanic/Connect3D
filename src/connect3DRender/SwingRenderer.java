//Rendering strategy
//On initialize, create a swing window with a drawing pane
//swing events get accumulated in a list and are consumed in the poll events method
//redraw collects draw requests, processes them and then draws the circles only

package connect3DRender;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import connect3DCore.Piece;

/**
 * Uses java swing to display the game to the players and get player input.
 * @author Benjamin
 *
 */
public final class SwingRenderer implements Renderer {
	
	/**
	 * Store list of objects that are interested in receiving updates from the renderer.
	 */
	private final List<Observer> observers = new ArrayList<>(1);
	
	/**
	 * List of components this renderer will attempt to draw.
	 */
	private final List<Component> drawables = new ArrayList<>();
	
	/**
	 * Root GUI element.
	 */
	private JFrame window;
	
	/**
	 * The width of the window in pixels.
	 */
	private final int WIDTH = 640;
	/**
	 * The height of the window in pixels.
	 */
	private final int HEIGHT = 480;
	
	//message passing variables. These variables are passed to observers on notifyObservers()
	private volatile int x,y,z;
	private volatile String message;
	
	/**
	 * Package private constructor so only the Render Factory can instantiate it.
	 */
	SwingRenderer(){}

	@Override
	public void drawCylinderAt(int x, int y, int z, float radius, float height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawCubeAt(int x, int y, int z, float width, float height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawSphereAt(int x, int y, int z, float radius) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawMessage(String msg) {
		// TODO TEMP
		assert msg != null;
		System.out.println(msg);
	}

	@Override
	public void setActiveColor(Piece p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addObserver(Observer o) {
		observers.add(o);
	}

	@Override
	public void removeObserver(Observer o) {
		observers.remove(o);
	}

	@Override
	public void notifyObservers() {
		assert message != null;
		for(Observer o : observers) {
			o.update(x,y,z,message);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			SwingUtilities.invokeAndWait(() -> {
				window = new JFrame("Connect3D");
				window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				window.setSize(WIDTH,HEIGHT);
				window.setLayout(new FlowLayout());
				window.getContentPane().add(
					new JButton("play 0 0") { //add a new custom button to the window that plays at (0,0) when clicked
						private static final long serialVersionUID = 1L;
							{
								addActionListener((e)->{
									x = 0;
									z = 0;
									message = "place";
									notifyObservers();
								});
							}
					}
				);
				window.getContentPane().add(
						new JButton("play 0 1") { //add a new custom button to the window that plays at (0,0) when clicked
							private static final long serialVersionUID = 2L;
								{
									addActionListener((e)->{
										x = 0;
										z = 1;
										message = "place";
										notifyObservers();
									});
								}
						}
				);
				window.setVisible(true);
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new InitializationException();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new InitializationException();
		} //end of SU.invL8R
	}

	@Override
	public void destroy() {
		if(window != null) {
			window.dispose();
			window = null;
			System.gc();
		}
	}

	@Override
	public void pollEvents() throws IllegalStateException {}

	@Override
	public void redraw() throws IllegalStateException {
		//Stage 0 drawing, don't do anything.
		//Just visit the component objects
		for(Component c : drawables) {
			c.draw(this);
		}
	}

	@Override
	public void addComponent(Component c) {
		this.drawables.add(c);
	}

	@Override
	public boolean removeComponent(Component c) {
		return this.drawables.remove(c);
	}

}
