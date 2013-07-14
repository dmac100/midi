package view;

import java.util.*;

import model.MidiFile;
import model.Note;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import controller.PositionChangedHandler;

/**
 * A canvas that display a NotesCanvas inside a scrollable control.
 * Updates the NotesCanvas when the window is resized. Calls a callback when the canvas is scrolled.
 */
public class ScrolledNotesCanvas {
	private ScrolledComposite scrolledComposite;
	private NotesCanvas notesCanvas;
	
	private List<PositionChangedHandler> positionChangeHandlers = new ArrayList<PositionChangedHandler>();
	
	public ScrolledNotesCanvas(Composite parent, Colors colors) {
		scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
		this.notesCanvas = new NotesCanvas(scrolledComposite, colors);
		scrolledComposite.setContent(notesCanvas.getWidget());
		
		// Increase speed that the scroll arrow scrolls.
		scrolledComposite.getVerticalBar().setIncrement(5);
		
		// Call the positionChangedHandlers with the new position when the canvas is scrolled.
		notesCanvas.getWidget().addControlListener(new ControlListener() {
			public void controlResized(ControlEvent event) {
			}
			
			public void controlMoved(ControlEvent event) {
				int newPosition = getPosition();
				for(PositionChangedHandler positionChangedHandler:positionChangeHandlers) {
					positionChangedHandler.positionChanged(newPosition);
				}
			}
		});
		
		// Update the notesCanvas's width and height when the scrolledComposite is resized.
		scrolledComposite.addControlListener(new ControlListener() {
			public void controlResized(ControlEvent event) {
				notesCanvas.setVisibleWidth(getWidget().getBounds().width);
				notesCanvas.setVisibleHeight(getWidget().getBounds().height);
			}
			
			public void controlMoved(ControlEvent event) {
			}
		});
	}

	/**
	 * Adds a positionChangeHandler. This will be notified with the new position when the canvas is scrolled.
	 */
	public void addPositionChangeHandler(PositionChangedHandler positionChangedHandler) {
		positionChangeHandlers.add(positionChangedHandler);
	}
	
	/**
	 * Returns the main widget for this control.
	 */
	public Composite getWidget() {
		return scrolledComposite;
	}

	/**
	 * Sets the file to display on the PianoCanvas.
	 */
	public void setMidiFile(MidiFile file) {
		notesCanvas.setMidiFile(file);
		scrollToBeginning();
	}
	
	/**
	 * Returns all the notes down at a position.
	 */
	public Set<Note> getNotesAtPosition(int position) {
		return notesCanvas.getNotesAtPosition(position);
	}
	
	/**
	 * Returns all the notes that are directly after a position.
	 */
	public Set<Note> getNotesAfterPosition(int position) {
		return notesCanvas.getNotesAfterPosition(position);
	}
	
	/**
	 * Returns the currently display position of the note canvas.
	 */
	public int getPosition() {
		return -notesCanvas.getWidget().getBounds().y + scrolledComposite.getBounds().height;
	}
	
	/**
	 * Scrolls to the bottom of the canvas.
	 */
	public void scrollToBeginning() {
		scrolledComposite.getDisplay().asyncExec(new Runnable() {
			public void run() {
				scrolledComposite.setOrigin(0, notesCanvas.getTotalHeight() - scrolledComposite.getBounds().height);
			}
		});
	}
	
	/**
	 * Sets the vertical scaling to be used on the notes.
	 */
	public void setScale(final int scale) {
		/*
		Composite w = notesCanvas.getWidget();
		final double y = ((double)w.getBounds().y) * (double)notesCanvas.getScale();
		*/
		
		notesCanvas.setScale(scale);
		scrollToBeginning();
		
		/*
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				double newy = -((y / notesCanvas.getScale()));
				System.out.println(newy);
				scrolledComposite.setOrigin(0, (int)(newy));
			}
		});
		*/
	}
	
	/**
	 * Scrolls a small step forward through the notes display.
	 */
	public void scrollForward() {
		scroll(-12);
	}
	
	public void pageForward() {
		scroll(-2000);
	}
	
	public void pageBackward() {
		scroll(2000);
	}
	
	private void scroll(int dy) {
		if(scrolledComposite.isDisposed()) return;
		
		int y = -notesCanvas.getWidget().getBounds().y;
		
		scrolledComposite.setOrigin(0, y + dy / notesCanvas.getScale());
	}

	public void scrollToPosition(final int position) {
		scrolledComposite.getDisplay().asyncExec(new Runnable() {
			public void run() {
				scrolledComposite.setOrigin(0, position - scrolledComposite.getBounds().height);
			}
		});
	}
	
	public void redraw() {
		notesCanvas.getWidget().redraw();
	}

	public int getTicksFromBeginning(int position) {
		return notesCanvas.getTicksFromBeginning(position);
	}
	
	public void addListener(int eventType, Listener listener) {
		notesCanvas.getWidget().addListener(eventType, listener);
	}

	public void setRepeats(List<Integer> repeats) {
		notesCanvas.setRepeats(repeats);
	}
}