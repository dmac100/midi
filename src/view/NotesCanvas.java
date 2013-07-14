package view;

import java.util.*;
import java.util.List;

import model.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.*;

/**
 * A canvas that displays notes in a piano-roll style.
 */
public class NotesCanvas {
	private int keyWidth = 9;
	private int scale = 1;
	
	private List<Note> notes = new ArrayList<Note>();
	private MidiFile midiFile;
	private Canvas canvas;
	
	private int totalHeight;
	private int visibleHeight;
	private int visibleWidth;
	
	private Colors colors;
	
	private List<Integer> repeats = new ArrayList<Integer>();
	
	public NotesCanvas(Composite parent, Colors colors) {
		this.canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		this.colors = colors;
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				paint(event.display, event.gc);
			}
		});
		
		canvas.pack();
	}
	
	/**
	 * Paint the canvas with a piano-roll style display of notes.
	 */
	private void paint(Display display, GC gc) {
		Color white = colors.getWhite();
		Color black = colors.getBlack();
		Color grey220 = colors.getGrey220();
		Color grey240 = colors.getGrey240();

		int clientWidth = keyWidth * 105 + 10;
		int leftMargin = (visibleWidth - clientWidth - 20) / 2 + 1;
		
		// Get total height needed to display every note.
		totalHeight = 0;
		for(Note note:notes) {
			totalHeight = Math.max(totalHeight, note.getStartTime() / scale + note.getDuration() / scale);
		}
		// Add one extra screen height to allow scrolling the last note to the bottom of the window.
		totalHeight += visibleHeight;
		
		// Set total canvas size.
		canvas.setSize(2000, (int)totalHeight);
		
		// Find top and bottom of canvas currently visible.
		int canvasTop = -canvas.getBounds().y;
		int canvasBottom = canvasTop + visibleHeight;
		
		// Draw white background.
		gc.setBackground(white);
		gc.fillRectangle(leftMargin, canvasTop, clientWidth, visibleHeight);
		
		// Draw grey guide lines showing the position of the black notes.
		for(int x = 0; x < 105; x++) {
			gc.setForeground(grey220);
			// Check whether this is a black note.
			if(x % 14 == 1 || x % 14 == 3 || x % 14 == 13 || x % 14 == 7 || x % 14 == 9) {
				// Use a lighter color for the group of 2 black notes.
				if(x % 14 == 7 || x % 14 == 9) {
					gc.setBackground(grey240);
				} else {
					gc.setBackground(grey220);
				}
				// Draw the rectangle.
				gc.fillRectangle(leftMargin + x * keyWidth - 2, canvasTop, keyWidth, visibleHeight);
			}
		}
		
		// Draw repeat lines.
		for(Integer repeat:repeats) {
			gc.setForeground(colors.getRed());
			gc.drawLine(leftMargin, repeat, leftMargin + clientWidth - 2, repeat);
		}
		
		if(midiFile != null) {
			// Draw horizontal bar lines.
			TimeSignature timeSignature = midiFile.getTimeSignature();
			int wholeNotesInBar = midiFile.getResolution() * timeSignature.getNumerator() * 4;
			double beatLength = wholeNotesInBar / timeSignature.getDenominator() / timeSignature.getNumerator();
			int beat = -1;
			for(int y = 0; y < totalHeight * scale; y += beatLength) {
				beat = (beat + 1) % timeSignature.getNumerator();
				
				int y2 = totalHeight - (y / scale);
				if(y2 < canvasTop || y2 > canvasBottom) continue;
				
				gc.setForeground(black);
				
				if(beat == 0) {
					gc.setForeground(colors.getBlack());
				} else {
					gc.setForeground(colors.getGrey220());
				}
				
				gc.drawLine(leftMargin, y2, leftMargin + clientWidth - 2, y2);
			}
			
			// Draw the note markers.
			for(Note note:notes) {
				// Skip invisible tracks.
				if(!note.getTrack().isActive()) continue;
				
				int pitch = note.getPitch().getMidiNumber();
				int duration = note.getDuration() / scale;
				
				int y = getNoteTop(note);
			
				// Skip notes that aren't currently visible (off the top of bottom of the screen).
				if(y + duration < canvasTop || y > canvasBottom) continue;
				
				// Set the main note color.
				gc.setBackground(colors.getNoteColor(note));
				
				// Draw a filled rectangle for the note, with a black outline.
				gc.setForeground(black);
				gc.fillRectangle(leftMargin + getPosition(pitch) - 1, y, 5, duration);
				gc.drawRectangle(leftMargin + getPosition(pitch) - 1, y, 5, duration);
			}
		}
		
		// Draw a border around the whole canvas.
		gc.setForeground(black);
		gc.drawLine(leftMargin, canvasTop, leftMargin, canvasBottom);
		gc.drawLine(leftMargin, 0, leftMargin + clientWidth - 2, 0);
		gc.drawLine(leftMargin + clientWidth - 2, canvasTop, leftMargin + clientWidth - 2, canvasBottom);
		gc.drawLine(leftMargin + clientWidth - 1, canvasTop, leftMargin + clientWidth - 1, canvasBottom);
	}
	
	/**
	 * Returns all the notes down at a position, adjusting for scaling.
	 */
	public Set<Note> getNotesAtPosition(int position) {
		HashSet<Note> currentNotes = new HashSet<Note>();
		
		for(Note note:notes) {
			int duration = note.getDuration() / scale;
			
			int y = getNoteTop(note);
			
			if(position >= y && position < y + duration) {
				currentNotes.add(note);
			}
		}
		
		return currentNotes;
	}
	
	/**
	 * Returns all the notes that are directly after a position.
	 */
	public Set<Note> getNotesAfterPosition(int position) {
		HashSet<Note> nextNotes = new HashSet<Note>();
		
		int nextPosition = Integer.MIN_VALUE;
		
		for(Note note:notes) {
			int duration = note.getDuration() / scale;
			
			int y = getNoteTop(note);

			if(y < position) {
				if(y + duration > nextPosition) {
					nextPosition = y + duration;
					nextNotes.clear();
				}
			}
			
			if(y + duration == nextPosition) {
				nextNotes.add(note);
			}
		}

		return nextNotes;
	}
	
	/**
	 * Returns the number of ticks a position is from the beginning.
	 */
	public int getTicksFromBeginning(int position) {
		return (totalHeight - position) * scale;
	}
	
	/**
	 * Returns the position of the top of a note, adjusting for scaling.
	 */
	private int getNoteTop(Note note) {
		int startTime = note.getStartTime() / scale;
		int duration = note.getDuration() / scale;
		
		return totalHeight - startTime - duration;
	}
	
	/**
	 * Returns the horizontal position that a note of some pitch should be centered on.
	 */
	private int getPosition(int pitch) {
		int position = 10;
		
		// Add a key width for each note above A0, or 2 key widths around the black key gaps between B/C and E/F.
		for(int x = 0; x <= pitch - 21; x++) {
			if(x % 12 == 3 || x % 12 == 8) {
				position += keyWidth;
			}
			position += keyWidth;
		}
		
		return position;
	}

	/**
	 * Sets the file to display on this canvas.
	 */
	public void setMidiFile(MidiFile file) {
		this.midiFile = file;
		
		this.notes = new ArrayList<Note>();
		for(MidiTrack track:file.getTracks()) {
			notes.addAll(track.getNotes());
		}
		
		canvas.redraw();
	}
	
	/**
	 * Sets the height of the currently visible portion of the canvas.
	 */
	public void setVisibleHeight(int height) {
		this.visibleHeight = height;
		getWidget().redraw();
	}
	
	/**
	 * Sets the width of the currently visible portion of the canvas.
	 */
	public void setVisibleWidth(int width) {
		this.visibleWidth = width;
		getWidget().redraw();
	}

	/**
	 * Returns the total height of the canvas.
	 */
	public int getTotalHeight() {
		return totalHeight;
	}

	/**
	 * Gets the vertical scaling used on the notes.
	 */
	public int getScale() {
		return scale;
	}
	
	/**
	 * Sets the vertical scaling to use on the notes.
	 */
	public void setScale(int scale) {
		this.scale = scale;
	}
	
	/**
	 * Returns the main widget for this control.
	 */
	public Composite getWidget() {
		return canvas;
	}

	public void setRepeats(List<Integer> repeats) {
		this.repeats = repeats;
		
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				canvas.redraw();
			}
		});
	}
}