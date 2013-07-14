package model;

import java.util.ArrayList;
import java.util.List;

/**
 * A track from a midi file.
 */
public class MidiTrack {
	private List<Note> notes = new ArrayList<Note>();
	private String name;
	private boolean active;
	private boolean autoplay;
	private int number;
	
	public MidiTrack(int number) {
		this.number = number;
		this.name = "Track " + number;
	}
	
	public void addNote(Note note) {
		notes.add(note);
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isAutoplay() {
		return autoplay;
	}

	public void setAutoplay(boolean autoplay) {
		this.autoplay = autoplay;
	}

	public boolean hasNotes() {
		return !notes.isEmpty();
	}
	
	public List<Note> getNotes() {
		return notes;
	}
}
