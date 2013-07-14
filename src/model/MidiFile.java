package model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.*;

/**
 * Class to store pitch and channel to identify note-on / note-off pairs.
 */
class PitchAndChannel {
	private int pitch;
	private int channel;

	public PitchAndChannel(int pitch, int channel) {
		this.pitch = pitch;
		this.channel = channel;
	}

	public int hashCode() {
		return pitch * 31 + channel;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			PitchAndChannel p = (PitchAndChannel) obj;
			return p.pitch == pitch && p.channel == channel;
		}
		return false;
	}
}

/**
 * Class to read a midi file.
 */
public class MidiFile {
	private Map<PitchAndChannel, Note> heldNotes = new HashMap<PitchAndChannel, Note>();
	
	private List<MidiTrack> tracks = null;
	private int resolution;
	private int totalTime = 0;
	private TimeSignature timeSignature = new TimeSignature(4, 4);

	public MidiFile(File file) throws InvalidMidiDataException, IOException {
		this.tracks = new ArrayList<MidiTrack>();
		
		Sequence sequence = MidiSystem.getSequence(file);
		
		resolution = sequence.getResolution();

		int trackNo = 0;

		for (Track track:sequence.getTracks()) {
			heldNotes.clear();
			
			trackNo += 1;
			MidiTrack t = new MidiTrack(trackNo);
			
			for (int i = 0; i < track.size(); i++) {
				MidiEvent event = track.get(i);

				int time = (int) event.getTick();

				MidiMessage message = event.getMessage();

				// Find midi message type.
				if (message instanceof ShortMessage) {
					ShortMessage shortMessage = (ShortMessage)message;

					if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
						noteOn(t, shortMessage, time - 16);
					} else if (shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
						noteOff(t, shortMessage, time - 16);
					}
				} else if(message instanceof MetaMessage) {
					MetaMessage metaMessage = (MetaMessage)message;

					// Track name change event.
					if(metaMessage.getType() == 3) {
						// Update track name from data.
						String trackName = new String(metaMessage.getData(), "ascii");
						
						if(trackName.length() > 0) {
							t.setName(trackName);
						}
					} else if(metaMessage.getType() == 0x58) {
						byte[] data = metaMessage.getData();
						
						this.timeSignature = new TimeSignature(data[0], 1 << data[1]);
					}
				}
			}
			
			if(t.hasNotes()) {
				tracks.add(t);
			}
		}
	}
	
	public List<MidiTrack> getTracks() {
		return tracks;
	}

	/**
	 * Called for each note-on event in the file, ordered by time.
	 */
	private void noteOn(MidiTrack track, ShortMessage shortMessage, int time) {
		int pitch = shortMessage.getData1();
		int vel = shortMessage.getData2();

		// A note-on event with 0 velocity is the same as a note-off event.
		if (vel == 0) {
			noteOff(track, shortMessage, time);
			return;
		}

		// Store the note as a held note.
		Note note = new Note(track, new Pitch(pitch), vel, (int) time, 0);
		heldNotes.put(new PitchAndChannel(pitch, shortMessage.getChannel()), note);
	}

	/**
	 * Called from each note-off event in the file, ordered by time.
	 */
	private void noteOff(MidiTrack track, ShortMessage shortMessage, int time) {
		int pitch = shortMessage.getData1();

		// Check if the note is being held.
		PitchAndChannel key = new PitchAndChannel(pitch, shortMessage.getChannel());
		Note note = heldNotes.get(key);
		if (note != null) {
			// Add the note to the list.
			note.setEndTime(time);
			heldNotes.remove(key);
			track.addNote(note);
			
			totalTime = Math.max(totalTime, note.getEndTime());
		}
	}
	
	public int getResolution() {
		return resolution;
	}
	
	public TimeSignature getTimeSignature() {
		return timeSignature;
	}

	public int getTotalTime() {
		return totalTime;
	}
}