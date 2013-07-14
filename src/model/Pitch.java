package model;
import java.util.Arrays;

/**
 * A pitch corresponding to a single key on a keyboard.
 */
public class Pitch {
	private static final String[] noteNames = "C, C#, D, D#, E, F, F#, G, G#, A, A#, B".split(", ");
	
	private int midiNumber;

	/**
	 * Creates a new pitch from a noteName.
	 * @param noteName The name of the note with an octave such as: 'A#0', 'C4'.
	 */
	public Pitch(String noteName) {
		int octave = noteName.charAt(noteName.length() - 1) - '0';
		String name = noteName.substring(0, noteName.length() - 1);
		
		int note = Arrays.asList(noteNames).indexOf(name.toUpperCase());
		
		if(octave < 0 || octave > 8) throw new IllegalArgumentException("Invalid note");
		if(note < 0) throw new IllegalArgumentException("Invalid note");
		
		this.midiNumber = octave * 12 + note + 12;
	}
	
	/**
	 * Creates a new pitch from a midi number (21-108 for a standard piano).
	 */
	public Pitch(int midiNumber) {
		this.midiNumber = midiNumber;
	}
	
	/**
	 * Create a new pitch as a copy of another.
	 */
	public Pitch(Pitch pitch) {
		this.midiNumber = pitch.midiNumber;
	}

	/**
	 * Returns the midi number for the pitch.
	 */
	public int getMidiNumber() {
		return midiNumber;
	}

	/**
	 * Returns the octave of the pitch.
	 */
	public int getOctave() {
		return (midiNumber - 12 + 60) / 12 - (60 / 12);
	}

	/**
	 * Returns whether this would be a black key if played on a keyboard. 
	 */
	public boolean isBlackKey() {
		return getNoteName().contains("#");
	}
	
	/**
	 * Returns the note name without the octave. For example: 'C' or 'D#'.
	 */
	public String getNoteName() {
		int name = (midiNumber - 12 + 60) % 12;
		return noteNames[name];
	}

	/**
	 * Returns the note name with the octave. For example: 'A#0' or 'C4'.
	 */
	public String getFullNoteName() {
		return getNoteName() + getOctave();
	}

	/**
	 * Return the pitch that is a semitone higher than this one.
	 */
	public Pitch nextSemitone() {
		return new Pitch(midiNumber + 1);
	}

	/**
	 * Return the pitch that is a semitone lower than this one.
	 */
	public Pitch previousSemitone() {
		return new Pitch(midiNumber - 1);
	}
	
	/**
	 * Returns the pitch transposed by the given amount.
	 */
	public Pitch transpose(int transpose) {
		return new Pitch(midiNumber + transpose);
	}

	/**
	 * Compares this pitch with another. Two pitches are equal if they have the same midi number.
	 */
	public boolean equals(Object obj) {
		if(obj != null && getClass() == obj.getClass()) {
			Pitch other = (Pitch)obj;
			if(midiNumber == other.midiNumber) {
				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		return midiNumber;
	}

	public boolean isAbove(Pitch pitch) {
		return midiNumber > pitch.midiNumber;
	}
	
	public boolean isBelow(Pitch pitch) {
		return midiNumber < pitch.midiNumber;
	}
	
	/**
	 * Returns the position this note would appear on the grand staff relative to middle C.
	 * For example: 'C4' and 'C#4' return 0, 'D4' and 'D#4' return 1, 'B3' returns -1.
	 */
	public int getStaffPosition() {
		char letter = getNoteName().charAt(0);
		int distance = letter - 'C';
		if(letter < 'C') distance += 7;
		
		distance += (getOctave() - 4) * 7;
		
		return distance;
	}
	
	public String toString() {
		return getNoteName();
	}
}
