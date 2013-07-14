package model;

/**
 * A single note from a midi file.
 */
public class Note {
	private Pitch pitch;
	private int velocity;
	private MidiTrack track;
	private int startTime;
	private int endTime;
	
	public Note(MidiTrack track, Pitch pitch, int velocity, int startTime, int endTime) {
		this.pitch = pitch;
		this.velocity = velocity;
		this.track = track;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public Note(Note note) {
		this.pitch = note.pitch;
		this.velocity = note.velocity;
		this.track = note.track;
		this.startTime = note.startTime;
		this.endTime = note.endTime;
	}

	public void transpose(int transpose) {
		pitch = pitch.transpose(transpose);
	}

	public Pitch getPitch() {
		return pitch;
	}

	public void setPitch(Pitch pitch) {
		this.pitch = pitch;
	}

	public int getVelocity() {
		return velocity;
	}

	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}

	public MidiTrack getTrack() {
		return track;
	}

	public void setTrack(MidiTrack track) {
		this.track = track;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	
	public int getDuration() {
		return endTime - startTime;
	}
	
	public String toString() {
		return track.getName() + ": " + pitch + " (" + velocity + ") " + startTime + " - " + endTime;
	}

	public boolean equals(Object obj) {
		if(obj == null || getClass() != obj.getClass()) return false;
		
		Note other = (Note)obj;
		if(endTime != other.endTime) return false;
		if(!pitch.equals(other.pitch)) return false;
		if(startTime != other.startTime) return false;
		if(!track.equals(other.track)) return false;
		if(velocity != other.velocity) return false;
		
		return true;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endTime;
		result = prime * result + pitch.hashCode();
		result = prime * result + startTime;
		result = prime * result + track.hashCode();
		result = prime * result + velocity;
		return result;
	}
}