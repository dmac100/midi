package model;

import java.util.*;

import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;

class MidiReceiver implements Receiver {
	private List<NoteHandler> noteHandlers = new ArrayList<NoteHandler>();
	
	public void addNoteHandler(NoteHandler noteHandler) {
		noteHandlers.add(noteHandler);
	}
	
	@Override
	public void send(MidiMessage message, long timeStamp) {
		if (message instanceof ShortMessage) {
			ShortMessage shortMessage = (ShortMessage)message;

			if(shortMessage.getCommand() == ShortMessage.NOTE_ON) {
				int pitch = shortMessage.getData1();
				int vel = shortMessage.getData2();
				if(vel > 0) {
					noteOn(pitch);
				} else {
					noteOff(pitch);
				}
			} else if(shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
				int pitch = shortMessage.getData1();
				noteOff(pitch);
			}
		}
	}
	
	private void noteOn(int pitch) {
		for(NoteHandler handler:noteHandlers) {
			handler.noteOn(new Pitch(pitch));
		}
	}
	
	private void noteOff(int pitch) {
		for(NoteHandler handler:noteHandlers) {
			handler.noteOff(new Pitch(pitch));
		}
	}
	
	@Override
	public void close() {
	}
}

/**
 * Represents a midi keyboard: a device that can send and receive non-timestamped midi events, and
 * optionally displays guide lamps on channel 0.
 */
public class MidiKeyboard {
	private MidiDevice outputDevice;
	private MidiDevice inputDevice;
	private Receiver receiver;
	
	private MidiReceiver midiReceiver;
	
	private Set<Pitch> guideLightsOn = new HashSet<Pitch>();
	private Map<Pitch, Runnable> notesOnRunnables = new HashMap<Pitch, Runnable>();
	private int transposeLights = 0;
	
	/**
	 * Creates a new keyboard instance using the first found midi input device. 
	 */
	public MidiKeyboard() throws MidiUnavailableException, InvalidMidiDataException {
		outputDevice = getOutputDevice();
		if(outputDevice == null) {
			System.out.println("No midi output device found");
		} else {
			System.out.println("Using midi output: " + outputDevice.getDeviceInfo().getDescription());
			outputDevice.open();
			receiver = outputDevice.getReceiver();
		}
		
		inputDevice = getInputDevice();
		if(inputDevice == null) {
			System.out.println("No midi input device found");
		} else {
			System.out.println("Using midi input: " + outputDevice.getDeviceInfo().getDescription());
			inputDevice.open();
			
			this.midiReceiver = new MidiReceiver();
			inputDevice.getTransmitter().setReceiver(midiReceiver);
		}
		
		clearLights();
		
		// Close device on exit
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void start() {
				close();
			}
		});
	}
	
	public void addNoteHandler(NoteHandler noteHandler) {
		midiReceiver.addNoteHandler(noteHandler);
	}
	
	/**
	 * Closes the midi device. No other methods should be called after it is closed.
	 */
	public synchronized void close() {
		if(outputDevice != null) {
			clearLights();
			outputDevice.close();
		}
		if(inputDevice != null) {
			inputDevice.close();
		}
		outputDevice = null;
		inputDevice = null;
		receiver = null;
	}
	
	/**
	 * Sends a midi message to the current receiver.
	 */
	private synchronized void sendMessage(MidiMessage message) {
		if(receiver != null) {
			receiver.send(message, -1);
		}
	}
	
	/**
	 * Plays a note on the midi device.
	 * @param pitch The pitch as a midi pitch number (21-108 for a standard 88-key piano).
	 * @param velocity The velocity that of the key being pressed (1-127).
	 * @param duration The duration in milliseconds.
	 */
	public void play(final Pitch pitch, int velocity, final int duration) throws InvalidMidiDataException, MidiUnavailableException {
		// Set a piano instrument on channel 1.
		final ShortMessage programChange = new ShortMessage();
		programChange.setMessage(ShortMessage.PROGRAM_CHANGE, 1, 0, 0);
		
		// Create the note-on message for this note.
		final ShortMessage noteOn = new ShortMessage();
		noteOn.setMessage(ShortMessage.NOTE_ON, 1, pitch.getMidiNumber(), velocity);

		// Create the note-off message for this note.
		final ShortMessage noteOff = new ShortMessage();
		noteOff.setMessage(ShortMessage.NOTE_OFF, 1, pitch.getMidiNumber(), 0);
		
		// Create a thread to send the note-on event, sleep for the duration of the note, then send the note-off event.
		new Thread(new Runnable() {
			public void run() {
				// Save the runnable that created this note.
				synchronized(MidiKeyboard.this) {
					notesOnRunnables.put(pitch, this);
				}
				
				sendMessage(programChange);
				sendMessage(noteOff);
				sendMessage(noteOn);
				
				try {
					Thread.sleep(duration * 2);
				} catch (InterruptedException e) {
					return;
				}

				synchronized(MidiKeyboard.this) {
					// Don't send note-off if another thread played the same note after this one.
					if(notesOnRunnables.get(pitch) == this) {
						sendMessage(noteOff);
						notesOnRunnables.remove(pitch);
					}
				}
			}
		}).start();
	}
	
	/**
	 * Clear all the guide lamps on the keyboard.
	 */
	public void clearLights() {
		try {
			guideLightsOn.clear();
			
			// Send a note-off event on every pitch on channel 0.
			for(int x = 0; x < 127; x++) {
				ShortMessage message = new ShortMessage();
				message.setMessage(ShortMessage.NOTE_ON, 0, x, 0);
				sendMessage(message);
			}
		} catch(InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public void setGuideLightsFromNotes(Set<Note> notes) {
		Set<Pitch> guideLights = new HashSet<Pitch>();
		for(Note note:notes) {
			guideLights.add(note.getPitch());
		}
		setGuideLights(guideLights);
	}
	
	/**
	 * Set the guide lights to display exactly the pitches that are in notes.
	 * The keyboard device may not support more than 12 lights at one time though.
	 */
	public void setGuideLights(Set<Pitch> notes) {
		try {
			// Find the new notes that have been turned on (notes - guideLightsOn).
			Set<Pitch> notesOn = new HashSet<Pitch>(notes);
			notesOn.removeAll(guideLightsOn);

			// Send a note-on event on channel 0 to turn on the required lights.
			for(Pitch note:notesOn) {
				ShortMessage message = new ShortMessage();
				message.setMessage(ShortMessage.NOTE_ON, 0, note.getMidiNumber() + transposeLights, 1);
				sendMessage(message);
			}
			
			// Find the notes that have been turned off (guideLightsOn - notes).
			Set<Pitch> notesOff = new HashSet<Pitch>(guideLightsOn);
			notesOff.removeAll(notes);
			
			// Send a note-on event with velocity 0 and channel 0 to turn off the required lights.
			for(Pitch note:notesOff) {
				ShortMessage message = new ShortMessage();
				message.setMessage(ShortMessage.NOTE_ON, 0, note.getMidiNumber() + transposeLights, 0);
				sendMessage(message);
			}

			// Update guideLightsOn.
			guideLightsOn = new HashSet<Pitch>(notes);
		} catch(InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Return a midi device that has a midi output (an input device from the program's perspective).
	 * Prefer hardware devices to software ones.
	 */
	private MidiDevice getOutputDevice() throws MidiUnavailableException {
		MidiDevice softwareDevice = null;
		
		for(Info info:MidiSystem.getMidiDeviceInfo()) {
			MidiDevice device = MidiSystem.getMidiDevice(info);
			
			if(device.getMaxReceivers() != 0) {
				if(device instanceof Sequencer || device instanceof Synthesizer) {
					softwareDevice = device;
				} else {
					// Found hardware device. Use it.
					return device;
				}
			}
		}
		
		return softwareDevice;
	}
	
	/**
	 * Return a midi device that has a midi input (an output device from the program's perspective).
	 * Prefer hardware devices to software ones.
	 */
	private MidiDevice getInputDevice() throws MidiUnavailableException {
		MidiDevice softwareDevice = null;
		
		for(Info info:MidiSystem.getMidiDeviceInfo()) {
			MidiDevice device = MidiSystem.getMidiDevice(info);
			
			if(device.getMaxTransmitters() != 0) {
				if(device instanceof Sequencer || device instanceof Synthesizer) {
					softwareDevice = device;
				} else {
					// Found hardware device. Use it.
					return device;
				}
			}
		}
		
		return softwareDevice;
	}

	/**
	 * Transposes the position of the guide lights by the given amount.
	 */
	public void transposeLights(int transpose) {
		if(transpose < -12 || transpose > 12) throw new IllegalArgumentException("Invalid tranpose: " + transpose);
		
		this.transposeLights = transpose;
		
		// Transpose guide lights.
		Set<Pitch> guideLightsOn = new HashSet<Pitch>(this.guideLightsOn);
		clearLights();
		setGuideLights(guideLightsOn);
	}
}
