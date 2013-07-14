package controller;

/**
 * A class that can be notified of position changes.
 */
public interface TempoChangedHandler {
	/**
	 * Called when the tempo is changed.
	 */
	void tempoChanged(int newTempo);
}
