package view;

import model.MidiTrack;

import org.eclipse.swt.widgets.Shell;

import view.table.CustomTable;

/**
 * Contains all views that should be accessible from the rest of the program.
 */
public class MainView {
	private Shell shell;
	private PianoCanvas pianoCanvas;
	private ScrolledNotesCanvas scrolledNotesCanvas;
	private ScoreCanvas scoreCanvas;
	private CustomTable<MidiTrack> tracksTable;
	private Colors colors;

	private Tempo tempoScale;
	private NoteScale noteScale;

	public MainView(Shell shell, PianoCanvas pianoCanvas, ScrolledNotesCanvas scrolledNotesCanvas, ScoreCanvas scoreCanvas, CustomTable<MidiTrack> tracksTable, Tempo tempoScale, NoteScale noteScale, Colors colors) {
		this.shell = shell;
		this.pianoCanvas = pianoCanvas;
		this.scrolledNotesCanvas = scrolledNotesCanvas;
		this.scoreCanvas = scoreCanvas;
		this.tracksTable = tracksTable;
		this.colors = colors;

		this.tempoScale = tempoScale;
		this.noteScale = noteScale;
	}

	public Shell getShell() {
		return shell;
	}

	public PianoCanvas getPianoCanvas() {
		return pianoCanvas;
	}

	public ScrolledNotesCanvas getScrolledNotesCanvas() {
		return scrolledNotesCanvas;
	}

	public ScoreCanvas getScoreCanvas() {
		return scoreCanvas;
	}

	public CustomTable<MidiTrack> getTracksTable() {
		return tracksTable;
	}

	public Tempo getTempoScale() {
		return tempoScale;
	}

	public NoteScale getNoteScale() {
		return noteScale;
	}

	public void setTitle(String title) {
		shell.setText(title);
	}

	public Colors getColors() {
		return colors;
	}
	
	public void setColor(Colors colors) {
		this.colors = colors;
	}

	public void redraw() {
		scrolledNotesCanvas.redraw();
		scoreCanvas.redraw();
		pianoCanvas.redraw();
	}
}
