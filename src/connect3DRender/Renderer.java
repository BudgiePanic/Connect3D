package connect3DRender;

/**
 * Describes the operations a renderer needs to be able to perform.
 * Renderer will redraw all the components registered to it on the redraw call.
 * @author Benjamin
 *
 */
public interface Renderer extends Graphics, Subject {
	
	/**
	 * Initialize the renderer.
	 * @throws InitializationException
	 *  Thrown if the renderer could not initialize itself. 
	 */
	public void initialize() throws InitializationException;
	
	/**
	 * Release any resources the renderer is using prior to disposal.
	 */
	public void destroy();
	
	/**
	 * Respond to any user input this frame.
	 * Notify observers of relevant events. 
	 * Such as updating the camera location in response to a mouse drag
	 * or notifying of a mouse click.
	 * @throws IllegalStateException
	 *  Thrown if renderer is not initialized. 
	 */
	public void pollEvents() throws IllegalStateException;
	
	/**
	 * Redraw all the components that have been registered to this renderer.
	 * The specifics of how the redrawing is performed is left to the implementation.
	 * @throws IllegalStateException
	 *  Thrown if the renderer has not been initialized.
	 */
	public void redraw() throws IllegalStateException;
	
	/**
	 * Add a new component for the renderer to draw each frame.
	 * @param c
	 *  The new drawable component.
	 */
	public void addComponent(Component c);
	
	/**
	 * Remove a component so the renderer does not draw it anymore.
	 * @param c
	 *  The component to be removed.
	 * @return true if the component was removed.
	 */
	public boolean removeComponent(Component c);

	/**
	 * Determine whether this renderer is still actively being used by the player or not.
	 * @return
	 *  False if the renderer has been closed / disabled by the user.
	 */
	public boolean isActive();
	
}
