package view;

import java.util.*;
import java.util.List;

import model.Note;
import model.Pitch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.*;

/**
 * A canvas that displays the keyboard of a piano which can highlight notes in different colors.
 */
public class PianoCanvas {
	// Properties to adjust the proportions of the keyboard.
	private final int topMargin = 10;
	private final int keyWidth = 18;
	private final int keyHeight = 90;
	private final int blackKeyHeight = 50;
	private final int blackMargin = 5;
	private final int borderWidth = 9;
	private final int nWhiteKeys = 52;
	
	private Canvas canvas;
	
	private List<Note> selectedNotes = new ArrayList<Note>();
	private Set<Pitch> playerPitches = new HashSet<Pitch>();
	
	private Colors colors;
	
	public PianoCanvas(Composite parent, Colors colors) {
		this.canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		this.colors = colors;
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				paint(event.display, event.gc);
			}
		});
	}
	
	/**
	 * Draw the keyboard.
	 */
	private void paint(Display display, GC gc) {
		Color white = colors.getWhite();
		Color black = colors.getBlack();
		Color grey30 = colors.getGrey30();
		Color grey50 = colors.getGrey50();
		Color grey80 = colors.getGrey80();
		Color grey120 = colors.getGrey120();

		// Calculate left margin to center the keyboard.
		int clientWidth = nWhiteKeys * keyWidth + 10;
		int leftMargin = (getWidget().getBounds().width - clientWidth - 20) / 2 + 5;
		
		Pitch pitch;
		
		pitch = new Pitch("a0");

		// Draw the border of the keyboard.
		gc.setBackground(grey30);
		gc.fillRectangle(leftMargin - borderWidth, topMargin - borderWidth - 1, nWhiteKeys * keyWidth + borderWidth * 2 + 1, keyHeight + borderWidth + 2);
		
		// Draw a gradient above the keyboard.
		gc.setBackground(grey30);
		gc.setForeground(grey120);
		gc.fillGradientRectangle(leftMargin - borderWidth + 1, topMargin - borderWidth, nWhiteKeys * keyWidth + borderWidth * 2 - 1, topMargin, true);
		
		// Draw every white key.
		for(int x = 0; x < nWhiteKeys; x++) {
			Color selected = getSelectedColor(pitch);

			// Draw rectangle for the key.
			gc.setForeground(black);
			gc.setBackground((selected != null) ? selected : white);
			gc.fillRectangle(leftMargin + keyWidth * x, topMargin, keyWidth, keyHeight);
			gc.drawRectangle(leftMargin + keyWidth * x, topMargin, keyWidth, keyHeight);
			
			// Get the pitch of the next white key.
			pitch = pitch.nextSemitone();
			// Add an extra semitone except between B/C and E/F.
			if(x % 7 != 1 && x % 7 != 4) {
				pitch = pitch.nextSemitone();
			}
		}
		
		pitch = new Pitch("a#0");
		
		// Draw every black key.
		for(int x = 0; x < nWhiteKeys - 1; x++) {
			// Skip a black key between B/C and E/F.
			if(x % 7 == 1 || x % 7 == 4) {
				pitch = pitch.nextSemitone();
				continue;
			}
			
			Color selected = getSelectedColor(pitch);
			
			int left = leftMargin + keyWidth * x + keyWidth / 2 + blackMargin;
			int width = keyWidth - blackMargin * 2 + 1;
			
			// Draw rectangle for this key.
			gc.setForeground(black);
			gc.setBackground((selected != null) ? selected : black);
			gc.fillRectangle(left, topMargin, width, blackKeyHeight);
			gc.drawRectangle(left, topMargin, width, blackKeyHeight);
			
			if(selected == null) {
				// Add a lower highlight to the key.
				gc.setBackground(grey50);
				gc.fillRectangle(left + 1, topMargin + blackKeyHeight - 5, width - 1, 5);
				gc.setForeground(grey80);
				gc.drawLine(left + 1, topMargin + blackKeyHeight - 5, left + width - 1, topMargin + blackKeyHeight - 5);
			}
			
			// Get the pitch of the next black key.
			pitch = pitch.nextSemitone();
			pitch = pitch.nextSemitone();
		}
	}
	
	/**
	 * Returns the color to display a pitch if it is currently selected, or null if it isn't.
	 */
	private Color getSelectedColor(Pitch pitch) {
		for(Pitch playerPitch:playerPitches) {
			if(playerPitch.equals(pitch)) {
				return colors.getRed();
			}
		}
		for(Note note:selectedNotes) {
			if(note.getTrack().isActive() && note.getPitch().equals(pitch.transpose(0))) {
				return colors.getNoteColor(note);
			}
		}
		return null;
	}

	/**
	 * Returns the total height needed to display this control.
	 */
	public int getTotalHeight() {
		return topMargin + keyHeight;
	}

	/**
	 * Sets the selected notes.
	 */
	public void setSelectedNotes(Set<Note> notes) {
		selectedNotes = new ArrayList<Note>();
		for(Note note:notes) {
			selectedNotes.add(new Note(note));
		}
		redraw();
	}
	
	/**
	 * Returns the main widget for this control.
	 */
	public Control getWidget() {
		return canvas;
	}

	public void redraw() {
		canvas.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if(!canvas.isDisposed()) {
					canvas.redraw();
				}
			}
		});
	}

	public void setPlayerPitches(Set<Pitch> playerPitches) {
		this.playerPitches = new HashSet<Pitch>(playerPitches);
		redraw();
	}
}