package controller;

/**
 * A class that can be notified of position changes.
 */
public interface PositionChangedHandler {
	/**
	 * Called when the position in a file has changed.
	 * The new position is given in midi ticks from the beginning.
	 */
	void positionChanged(int newPosition);
}
