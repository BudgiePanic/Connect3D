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
import java.util.Collections;
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
	 * The dimension of the board we are drawing.
	 * This field is used to convert mouse clicks into user input.
	 */
	private final int dimension;
	
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
	private final List<Draw> drawRequests = new ArrayList<Draw>();
	
	
	/**
	 * Package private constructor so only the Render Factory can instantiate it.
	 * @param dimension
	 *  The d * d * d size of the board.
	 *  Used for converting mouse inputs into board space locations. 
	 */
	SwingRenderer(int dimension){
		assert dimension >= 4;
		this.dimension = dimension;
	}

	@Override
	public void drawCylinderAt(int x, int y, int z, float radius, float height) {}

	@Override
	public void drawCubeAt(int x, int y, int z, float width, float height) {}

	@Override
	public void drawSphereAt(int x, int y, int z, float radius) {
		//this.drawRequests.add(new Draw(x,y,z,SPHERE));
	}

	@Override
	public void drawMessage(String msg) {
		assert msg != null;
		this.drawRequests.add(new Draw() {
			@Override
			public int compareTo(Draw o) { return 1; }
			@Override
			public void draw(Graphics g) { g.drawString(msg, 5,25); }
		});
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
				window.add(new DrawPanel());
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
		this.drawRequests.clear();
		for(Component c : drawables) {
			c.draw(this); //collect draw requests from the scene
		}
		
		try {
			SwingUtilities.invokeAndWait(() -> {
				//sort the draw Requests, furtherest away first.
				Collections.sort(this.drawRequests);
				//window.revalidate();
				window.repaint(); //window itself will traverse the list and draw the objects
			}
			);
		} catch (InvocationTargetException | InterruptedException b) {
			throw new RuntimeException(b);
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

	/**
	 * Panel that performs IO for the swing renderer.
	 * Input via a mouse listener.
	 * @author Benjamin
	 *
	 */
	private class DrawPanel extends JPanel {
		private static final long serialVersionUID = -6549532872100015668L;
		/**
		 * Amount of pixels needed to achieve evenly spaced lines horizontally.
		 */
		private final int spaceX;
		/**
		 * Amount of pixels needed to achieve evenly spaced lines vertically.
		 */
		private final int spaceY;
		/**
		 * The column that the mouse is being hovered in
		 */
		private volatile int cursorX;
		/**
		 * The row that the mouse is being hovered in
		 */
		private volatile int cursorY;
		
		/**
		 * Create a new panel.
		 * Comes initialized with a mouse listener that reports user events.
		 */
		DrawPanel(){
			init();
			int paddingColumns = 2;
			spaceX = (SwingRenderer.WIDTH / (dimension + paddingColumns))+1;
			spaceY = (SwingRenderer.HEIGHT / (dimension + paddingColumns));
			assert spaceX != 0;
			assert spaceY != 0;
		}
		
		/**
		 * Get the draw panel ready.
		 */
		private void init() {
			setSize(WIDTH,HEIGHT);
			setPreferredSize(new Dimension(WIDTH,HEIGHT));
			setOpaque(true);
			setBackground(Color.WHITE);
			addMouseListener(new MouseListener()
				{
					@Override
					public void mouseClicked(MouseEvent e) {
						//System.out.println(e.getX()+" "+e.getY());
						//System.out.println(toBoardSpaceX(e.getX())+" "+toBoardSpaceY(e.getY())+"\n");
						SwingRenderer.this.x = toBoardSpaceX(e.getX()) - 1;
						SwingRenderer.this.z = toBoardSpaceY(e.getY()) - 1;
						SwingRenderer.this.message = "place";
						SwingRenderer.this.notifyObservers();
					}
					@Override
					public void mousePressed(MouseEvent e) {}
					@Override
					public void mouseReleased(MouseEvent e) {}
					@Override
					public void mouseEntered(MouseEvent e) {
						cursorX = toBoardSpaceX(e.getX());
						cursorY = toBoardSpaceY(e.getY());
					}
					@Override
					public void mouseExited(MouseEvent e) {}
				}
			);
			addMouseMotionListener(new MouseMotionListener() 
				{
					@Override
					public void mouseDragged(MouseEvent e) {}

					@Override
					public void mouseMoved(MouseEvent e) {
						int x,y;
						x = toBoardSpaceX(e.getX());
						y = toBoardSpaceY(e.getY());
						if(x != cursorX || y != cursorY) {
							cursorX = x; cursorY = y;
							//System.out.println("MouseMoved->"+x+" "+y);
							SwingRenderer.this.x = x - 1;
							SwingRenderer.this.z = y - 1;
							SwingRenderer.this.message = "hover";
							SwingRenderer.this.notifyObservers();
						}
					}
				}
			);
		}
		
		/**
		 * Converts a screen x coordinate to a selection space x coordinate
		 * @param x
		 *  A screen space X location
		 * @return
		 *  The board space equivalent of this location
		 */
		private int toBoardSpaceX(int x) {
			float screenX = x;
			float columnWidth = this.spaceX;
			return (int) (screenX / columnWidth);
		}
		
		/**
		 * Converts a screen y coordinate to a selection space y coordinate
		 * @param y
		 *  A screen space Y location
		 * @return
		 *  The board space equivalent of this location
		 */
		private int toBoardSpaceY(int y) {
			float screenY = y;
			float rowWidth = this.spaceY;
			return (int) (screenY / rowWidth);
		}
		
		//PAINT CODE BELOW THIS POINT

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			paintInputLayerH(g, dimension+1);
			paintInputLayerV(g, dimension+1);
			//g.drawString("message",5,25);
			//the draw requests should already be sorted
			for(Draw d : SwingRenderer.this.drawRequests) {
				d.draw(g);
			}
		}
		
		/**
		 * Draw the vertical lines that are used to show the grid for valid inputs.
		 * @param g
		 *  The graphics context
		 * @param numbLines 
		 *  The number of lines to draw
		 */
		private void paintInputLayerV(Graphics g, int numbLines) {
			if(numbLines <= 0) {return;}
			int depth = numbLines * this.spaceY;
			int x1,y1,x2,y2;
			y1 = depth;
			y2 = y1;
			x1 = this.spaceX;
			x2 = SwingRenderer.WIDTH - this.spaceX;
			g.drawLine(x1,y1,x2,y2);
			paintInputLayerV(g, numbLines-1);
		}

		/**
		 * Draw the horizontal lines that are used to show the grid for valid inputs.
		 * @param g
		 *  Graphics context
		 * @param numbLines 
		 *  The number of lines to draw
		 */
		private void paintInputLayerH(Graphics g, int numbLines) {
			if(numbLines <= 0) {return;}
			int offset = numbLines * this.spaceX;
			int x1,y1,x2,y2;
			x1 = offset;
			x2 = x1;
			y1 = this.spaceY;
			y2 = SwingRenderer.HEIGHT - spaceY;
			g.drawLine(x1,y1,x2,y2);
			paintInputLayerH(g, numbLines-1);
		}
		
	}
	
	private interface Draw extends Comparable<Draw> {
		/**
		 * Execute this draw request.
		 * @param g
		 * The graphics context that can perform the drawing.
		 */
		void draw(Graphics g);
	}
}
