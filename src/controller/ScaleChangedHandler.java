package controller;

/**
 * A class that can be notified of scale changes.
 */
public interface ScaleChangedHandler {
	/**
	 * Called when the scale is changed.
	 */
	void scaleChanged(int newScale);
}
