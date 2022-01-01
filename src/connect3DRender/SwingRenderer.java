//Rendering strategy
//On initialize, create a swing window with a drawing pane
//swing events get accumulated in a list and are consumed in the poll events method
//redraw collects draw requests, processes them and then draws the circles only

package connect3DRender;

import java.awt.*;
import java.awt.Graphics;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

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
	private static final int WIDTH = 640;
	/**
	 * The height of the window in pixels.
	 */
	private static final int HEIGHT = 480;
	
	//message passing variables. These variables are passed to observers on notifyObservers()
	private volatile int x,y,z;
	private volatile String message;
	
	//drawing fields
	
	
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
				window.setResizable(false);
				window.setPreferredSize(new Dimension(WIDTH,HEIGHT));
//				window.add(new DrawPanel());
				window.add(new JPanel() {
					{
						setSize(WIDTH,HEIGHT);
						setPreferredSize(new Dimension(WIDTH,HEIGHT));
						setOpaque(true);
						setBackground(Color.WHITE);
					}
					@Override
					public void paintComponent(Graphics g) {
						super.paintComponent(g);
						g.setColor(Color.BLUE);
						g.fillOval(10,10,30,30);
					}
				});
				window.validate();
				window.pack();
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
		
		/*try {
			SwingUtilities.invokeAndWait(() -> {
				//window.revalidate();
				//window.repaint();
			}
			);
		} catch (InvocationTargetException | InterruptedException b) {
			throw new RuntimeException(b);
		}*/
	}

	@Override
	public void addComponent(Component c) {
		this.drawables.add(c);
	}

	@Override
	public boolean removeComponent(Component c) {
		return this.drawables.remove(c);
	}

	/*private class DrawPanel extends JPanel {
		DrawPanel(){
			setSize(WIDTH,HEIGHT);
			setPreferredSize(new Dimension(WIDTH,HEIGHT));
			setOpaque(true);
			setBackground(Color.WHITE);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.BLUE);
			g.fillOval(10,10,30,30);
		}
	}*/
}
