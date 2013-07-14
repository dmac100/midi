package view;

import model.MidiTrack;
import model.Note;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class Colors {
	private Color[][] trackColors = new Color[11][2];
	private Display display = Display.getCurrent();
	
	private Color white = new Color(display, 255, 255, 255);
	private Color black = new Color(display, 0, 0, 0);
	private Color red = new Color(display, 255, 50, 50);
	private Color grey30 = new Color(display, 30, 30, 30);
	private Color grey50 = new Color(display, 50, 50, 50);
	private Color grey80 = new Color(display, 80, 80, 80);
	private Color grey120 = new Color(display, 120, 120, 120);
	private Color grey220 = new Color(display, 220, 220, 220);
	private Color grey240 = new Color(display, 240, 240, 240);
	
	public Color getWhite() {
		return white;
	}

	public Color getBlack() {
		return black;
	}
	
	public Color getRed() {
		return red;
	}

	public Color getGrey30() {
		return grey30;
	}

	public Color getGrey50() {
		return grey50;
	}

	public Color getGrey80() {
		return grey80;
	}

	public Color getGrey120() {
		return grey120;
	}

	public Color getGrey240() {
		return grey240;
	}

	public Color getGrey220() {
		return grey220;
	}

	public Colors() {
		trackColors[0][0] = new Color(display, 220, 220, 80);
		trackColors[1][0] = new Color(display, 80, 220, 220);
		trackColors[2][0] = new Color(display, 220, 80, 220);
		trackColors[3][0] = new Color(display, 220, 80, 80);
		trackColors[4][0] = new Color(display, 80, 220, 80);
		trackColors[5][0] = new Color(display, 80, 80, 220);
		trackColors[6][0] = new Color(display, 250, 180, 0);
		trackColors[7][0] = new Color(display, 170, 210, 200);
		trackColors[8][0] = new Color(display, 230, 220, 120);
		trackColors[9][0] = new Color(display, 255, 240, 80);
		trackColors[10][0] = new Color(display, 220, 200, 230);
		
		for(int x = 0; x < 11; x++) { 
			trackColors[x][1] = darken(trackColors[x][0]);
		}
	}

	public Color getNoteColor(int x, int y) {
		return trackColors[x][y];
	}
	
	/**
	 * Returns the color that a note should be drawn with.
	 */
	public Color getNoteColor(Note note) {
		int track = note.getTrack().getNumber() % 11;
		int shade = note.getPitch().isBlackKey() ? 1 : 0;
		return trackColors[track][shade];
	}
	
	public Color getTrackColor(MidiTrack midiTrack) {
		int track = midiTrack.getNumber() % 11;
		return trackColors[track][0];
	}
	
	/**
	 * Returns a darker copy of the specified color.
	 */
	private Color darken(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		
		return new Color(display, (int)(r * 0.6), (int)(g * 0.6), (int)(b * 0.6));
	}

	/**
	 * Disposes all created colors.
	 */
	public void dispose() {
		for(int x = 0; x < trackColors.length; x++) {
			for(int y = 0; y < trackColors[x].length; y++) {
				if(trackColors[x][y] != null) {
					trackColors[x][y].dispose();
				}
			}
		}
		
		white.dispose();
		black.dispose();
		grey30.dispose();
		grey50.dispose();
		grey80.dispose();
		grey120.dispose();
		grey220.dispose();
		grey240.dispose();
	}
}
