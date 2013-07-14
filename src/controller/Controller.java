package controller;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import model.*;

import org.eclipse.swt.widgets.Display;

import view.*;

/**
 * Keep track of what notes we're waiting for the user to press.
 */
class WaitingNotes {
	private List<Note> waitingForNotes = new ArrayList<Note>();
	private Set<Pitch> earlyNotes = new HashSet<Pitch>();
	
	private Runnable autoplayCallback;

	/**
	 * Returns whether we are waiting for any active non-autoplayed notes.
	 */
	public boolean waiting() {
		return (getWaitingNotes().size() > 0);
	}

	/**
	 * Updates waiting notes after a position change. 
	 */
	public void setWaitingNotes(Set<Note> newNotes) {
		if(newNotes.size() == 0) {
			return;
		}
		
		waitingForNotes.clear();
		for(Note note:newNotes) {
			MidiTrack track = note.getTrack();
			if(track.isActive() && !track.isAutoplay()) {
				if(!earlyNotes.contains(note.getPitch())) {
					waitingForNotes.add(note);
				}
			}
		}
		earlyNotes.clear();
	}

	/**
	 * Returns the set of notes that we are waiting for.
	 */
	public Set<Note> getWaitingNotes() {
		return Controller.filterWaitable(waitingForNotes);
	}

	/**
	 * Removes pitch from waiting notes.
	 */
	public void noteOn(Pitch pitch) {
		if(waitingForNotes.isEmpty()) {
			earlyNotes.add(pitch);
		} else {
			List<Note> newWaitingForNotes = new ArrayList<Note>();
			for(Note note:waitingForNotes) {
				if(!note.getPitch().equals(pitch)) {
					newWaitingForNotes.add(note);
				}
			}
			waitingForNotes = newWaitingForNotes;
		}
		
		if(!waiting()) {
			callAutoplayCallback();
		}
	}

	/**
	 * Set callback to run when there are no more waiting notes.
	 */
	public void setAutoplayCallback(Runnable runnable) {
		this.autoplayCallback = runnable;
		
		if(!waiting()) {
			callAutoplayCallback();
		}
	}
	
	private void callAutoplayCallback() {
		if(autoplayCallback != null) {
			autoplayCallback.run();
			autoplayCallback = null;
		}
	}
}

public class Controller implements PositionChangedHandler, TempoChangedHandler, ScaleChangedHandler, NoteHandler {	
	private TracksController tracksController;
	private MainView mainView;
	
	private Set<Note> oldNotes = new HashSet<Note>();
	private int tempo;
	
	private WaitingNotes waitingNotes = new WaitingNotes();
	
	private Set<Pitch> notesOn = new HashSet<Pitch>();
	
	private MidiKeyboard midiKeyboard;
	
	private boolean playing = false;
	private MidiFile midiFile;
	private List<Integer> repeats = new ArrayList<Integer>();

	public Controller(final MainView mainView) throws MidiUnavailableException, InvalidMidiDataException {
		this.mainView = mainView;
		
		this.tracksController = new TracksController(mainView);
		
		this.midiKeyboard = new MidiKeyboard();

		// Scroll forward through the file continuously.
		Display.getCurrent().timerExec(200, new Runnable() {
			public void run() {
				if(playing) {
					if(!waitingNotes.waiting()) {
						mainView.getScrolledNotesCanvas().scrollForward();
					}
				}
				Display.getCurrent().timerExec(80 + 2 - tempo, this);
			}
		});
		
		Tempo tempoScale = mainView.getTempoScale();
		NoteScale noteScale = mainView.getNoteScale();
		
		this.tempo = 65;
		tempoScale.setTempo(tempo);
		tempoScale.setRange(40, 80);
		
		noteScale.setRange(1, 8);
		noteScale.setScale(2);
		scaleChanged(2);
		
		mainView.getScrolledNotesCanvas().addPositionChangeHandler(this);
		tempoScale.addTempoChangedHandler(this);
		noteScale.addScaleChangedHandler(this);
		
		midiKeyboard.addNoteHandler(this);
	}
	
	public void close() {
		midiKeyboard.close();
	}
	
	@Override
	public void positionChanged(int newPosition) {
		ScrolledNotesCanvas scrolledNotesCanvas = mainView.getScrolledNotesCanvas();
		
		if(playing && repeats.size() == 2) {
			if(newPosition < repeats.get(1)) {
				scrolledNotesCanvas.scrollToPosition(repeats.get(0));
				return;
			}
		}
		
		// Get the notes at the new position.
		Set<Note> notes = scrolledNotesCanvas.getNotesAtPosition(newPosition);

		Set<Pitch> pitches = new HashSet<Pitch>();
		for(Note note:notes) {
			pitches.add(note.getPitch());
		}
		
		// Select every pitch on the piano.
		PianoCanvas pianoCanvas = mainView.getPianoCanvas();
		pianoCanvas.setSelectedNotes(notes);
		
		// Find notes that are different from last time.
		final Set<Note> newNotes = new HashSet<Note>(notes);
		newNotes.removeAll(oldNotes);
		
		waitingNotes.setWaitingNotes(newNotes);
		
		// Play new notes.
		waitingNotes.setAutoplayCallback(new Runnable() {
			public void run() {
				try {
					for(Note note:newNotes) {
						if(note.getTrack().isActive()) {
							if(note.getTrack().isAutoplay()) {
								midiKeyboard.play(note.getPitch(), note.getVelocity(), note.getDuration());
							}
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Display waiting notes as guide lights, or all notes if all tracks are autoplayed.
		if(anyActiveNonAutoplay()) {
			if(waitingNotes.waiting()) {
				// Display the notes we are waiting for.
				midiKeyboard.setGuideLightsFromNotes(waitingNotes.getWaitingNotes());
			} else {
				// Display next notes if we aren't waiting for any right now.
				Set<Note> nextNotes = scrolledNotesCanvas.getNotesAfterPosition(newPosition);
				midiKeyboard.setGuideLightsFromNotes(Controller.filterWaitable(nextNotes));
			}
		} else {
			midiKeyboard.setGuideLightsFromNotes(getActiveNotes(notes));
		}
		
		// Update oldNotes.
		oldNotes = notes;
		
		ScoreCanvas scoreCanvas = mainView.getScoreCanvas();
		scoreCanvas.positionChanged(scrolledNotesCanvas.getTicksFromBeginning(newPosition));
	}
	
	/**
	 * Return the notes in allNotes that we need to wait for (within an active track that is not autoplayed).
	 */
	static Set<Note> filterWaitable(Collection<Note> allNotes) {
		Set<Note> waitableNotes = new HashSet<Note>();
		for(Note note:allNotes) {
			MidiTrack track = note.getTrack();
			if(track.isActive() && !track.isAutoplay()) {
				waitableNotes.add(note);
			}
		}
		return waitableNotes;
	}
	
	private Set<Note> getActiveNotes(Set<Note> notes) {
		Set<Note> activeNotes = new HashSet<Note>();
		
		for(Note note:notes) {
			if(note.getTrack().isActive()) {
				activeNotes.add(note);
			}
		}
		
		return activeNotes;
	}

	/**
	 * Returns whether there are any active non-autoplayed tracks loaded.
	 */
	private boolean anyActiveNonAutoplay() {
		if(midiFile == null) return false;
		
		for(MidiTrack track:midiFile.getTracks()) {
			if(!track.isAutoplay() && track.isActive()) {
				return true;
			}
		}
		return false;
	}

	public void setScale(int scale) {
		mainView.getScrolledNotesCanvas().setScale(9 - scale);
		mainView.getScoreCanvas().setScale(9 - scale);
	}

	@Override
	public void tempoChanged(int newTempo) {
		setTempo(newTempo);
	}
	
	public void openFile(String filename) throws IOException, InvalidMidiDataException {
		ScrolledNotesCanvas scrolledNotesCanvas = mainView.getScrolledNotesCanvas();
		ScoreCanvas scoreCanvas = mainView.getScoreCanvas();
		
		playing = false;
		
		mainView.setTitle("Midi: " + new File(filename).getName());
		
		// Get tracks from a file and add them to the views.
		this.midiFile = new MidiFile(new File(filename));
		List<MidiTrack> tracks = midiFile.getTracks();
		
		scrolledNotesCanvas.setMidiFile(midiFile);
		scoreCanvas.setMidiFile(midiFile);
		tracksController.setTracks(tracks);
	}

	public void playPause() {
		playing = !playing;
	}

	public void setTempo(int tempo) {
		this.tempo = tempo;
	}

	@Override
	public void scaleChanged(int newScale) {
		setScale(newScale);
		mainView.getScrolledNotesCanvas().redraw();
	}

	@Override
	public void noteOn(Pitch pitch) {
		PianoCanvas pianoCanvas = mainView.getPianoCanvas();
		
		notesOn.add(pitch);
		pianoCanvas.setPlayerPitches(notesOn);
		waitingNotes.noteOn(pitch);
	}
	
	@Override
	public void noteOff(Pitch pitch) {
		notesOn.remove(pitch);
		mainView.getPianoCanvas().setPlayerPitches(notesOn);
	}

	public void scrollUp() {
		mainView.getScrolledNotesCanvas().pageForward();
	}

	public void scrollDown() {
		mainView.getScrolledNotesCanvas().pageBackward();
	}

	public void clearLights() throws InvalidMidiDataException {
		midiKeyboard.clearLights();
	}

	public void transposeLights(int transpose) {
		midiKeyboard.transposeLights(transpose);
	}

	public void markRepeat() {
		ScrolledNotesCanvas scrolledNotesCanvas = mainView.getScrolledNotesCanvas();
		
		int position = scrolledNotesCanvas.getPosition();
		
		if(!repeats.contains(position)) {
			repeats.add(position);
			if(repeats.size() > 2) {
				repeats.remove(0);
			}
			
			scrolledNotesCanvas.setRepeats(repeats);
		}
	}
	
	public void clearRepeats() {
		repeats = new ArrayList<Integer>();
		mainView.getScrolledNotesCanvas().setRepeats(repeats);
	}
}
