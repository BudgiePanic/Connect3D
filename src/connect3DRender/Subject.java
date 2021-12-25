package connect3DRender;

/**
 * Use the observer pattern to allow the Renderer to notify interested parties of user events.
 * @author Benjamin
 *
 */
public interface Subject {
	
	/**
	 * Register a new observer to be notified of user events.
	 * @param o
	 *  The observer
	 */
	public void addObserver(Observer o);
	/**
	 * Remove an observer so it is no longer notified of user events that occur.
	 * @param o
	 *  The observer.
	 */
	public void removeObserver(Observer o);
	/**
	 * Notify the the registered observers of a user event.
	 */
	public void notifyObservers();
	
}
