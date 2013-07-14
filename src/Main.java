import model.MidiTrack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import view.*;
import view.table.CustomTable;
import controller.Controller;

public class Main {
	private Shell shell;
	private Controller controller;
	
	private ScoreCanvas scoreCanvas;
	private PianoCanvas pianoCanvas;
	private CustomTable<MidiTrack> tracksTable;
	private ScrolledNotesCanvas scrolledNotesCanvas;
	private Tempo tempo;
	private NoteScale noteScale;
	
	Colors colors = new Colors();
	
	/**
	 * Listen for global keyboard shortcuts.
	 */
	class KeyListener implements Listener {
		public void handleEvent(Event event) {
			if(event.character == ' ') {
				controller.playPause();
			} else if(event.keyCode == SWT.PAGE_UP) {
				controller.scrollUp();
			} else if(event.keyCode == SWT.PAGE_DOWN){
				controller.scrollDown();
			}
		}
	}
	
	/**
	 * Focuses the pianoCanvas on click.
	 */
	class FocusListener extends MouseAdapter {
		public void mouseUp(MouseEvent event) {
			//pianoCanvas.getWidget().setFocus();
		}
	}
	
	public Main(Shell shell, String[] args) throws Exception {
		this.shell = shell;
		
		addFileDropTarget(shell);
		
		SashForm sash = new SashForm(shell, SWT.HORIZONTAL);
		
		Composite sidePanel = new Composite(sash, SWT.NONE);
		Composite mainArea = new Composite(sash, SWT.NONE);
		
		sash.setWeights(new int[] { 20, 80 });
		
		addMainAreaWidgets(mainArea);
		addSidePanelWidgets(sidePanel);
		
		// Add menubar.
		addMenuBar();
		
		// Create controller.
		MainView mainView = new MainView(shell, pianoCanvas, scrolledNotesCanvas, scoreCanvas, tracksTable, tempo, noteScale, colors);
		this.controller = new Controller(mainView);
		
		// Add key listeners to each of the canvases.
		scoreCanvas.getWidget().addListener(SWT.KeyUp, new KeyListener());
		pianoCanvas.getWidget().addListener(SWT.KeyUp, new KeyListener());
		scrolledNotesCanvas.addListener(SWT.KeyUp, new KeyListener());
		pianoCanvas.getWidget().setFocus();
		
		// Handle arguments.
		if(args.length == 1) {
			String filename = args[0];
			openFile(filename);
		}
	}
	
	private void addSidePanelWidgets(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		this.tempo = new Tempo(parent);
		tempo.getWidget().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		this.noteScale = new NoteScale(parent);
		tempo.getWidget().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		this.tracksTable = new CustomTable<MidiTrack>(parent);
		tracksTable.getWidget().setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	private void addMainAreaWidgets(Composite parent) throws Exception {
		// Grid to display the keyboard and notes canvas together.
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		parent.setLayout(gridLayout);

		// Add score canvas at the top.
		this.scoreCanvas = new ScoreCanvas(parent, colors);
		GridData scoreCanvasGridData = new GridData();
		scoreCanvasGridData.horizontalAlignment = GridData.FILL;
		scoreCanvasGridData.grabExcessHorizontalSpace = true;
		scoreCanvasGridData.heightHint = scoreCanvas.getTotalHeight() + 1;
		scoreCanvas.getWidget().setLayoutData(scoreCanvasGridData);
		
		// Fill notes canvas underneath.
		this.scrolledNotesCanvas = new ScrolledNotesCanvas(parent, colors);
		GridData scrolledNotesGridData = new GridData();
		scrolledNotesGridData.horizontalAlignment = GridData.FILL;
		scrolledNotesGridData.verticalAlignment = GridData.FILL;
		scrolledNotesGridData.grabExcessHorizontalSpace = true;
		scrolledNotesGridData.grabExcessVerticalSpace = true;
		scrolledNotesCanvas.getWidget().setLayoutData(scrolledNotesGridData);
		
		// Add piano underneath.
		this.pianoCanvas = new PianoCanvas(parent, colors);
		GridData pianoCanvasGridData = new GridData();
		pianoCanvasGridData.horizontalAlignment = GridData.FILL;
		pianoCanvasGridData.grabExcessHorizontalSpace = true;
		pianoCanvasGridData.heightHint = pianoCanvas.getTotalHeight() + 1;
		pianoCanvas.getWidget().setLayoutData(pianoCanvasGridData);
		
		// Add toolbar underneath.
		Composite toolBar = new Composite(parent, SWT.NONE);
		GridData toolbarGridData = new GridData();
		toolbarGridData.horizontalAlignment = GridData.FILL;
		toolBar.setLayoutData(toolbarGridData);
		toolBar.setLayout(new FillLayout());
		
		addToolbarButtons(toolBar);
	}
	
	private void addMenuBar() {
		Menu menuBar = new Menu(shell, SWT.BAR);
		
		// File
		MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuItem.setText("&File");
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuItem.setMenu(fileMenu);

		// File->Open
		MenuItem fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenItem.setText("&Open\tCtrl+O");
		fileOpenItem.setAccelerator(SWT.CONTROL + 'O');
		fileOpenItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				openFile();
			}
		});

		// Separator
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		// File->Exit
		MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("E&xit");
		fileExitItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});
		
		// Midi
		MenuItem midiMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		midiMenuItem.setText("&Midi");
		Menu midiMenu = new Menu(shell, SWT.DROP_DOWN);
		midiMenuItem.setMenu(midiMenu);

		// Midi->Clear Lights
		MenuItem midiClearItem = new MenuItem(midiMenu, SWT.PUSH);
		midiClearItem.setText("&Clear Lights");
		midiClearItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					controller.clearLights();
				} catch (Exception e) {
					e.printStackTrace();
					displayErrorMessage("Error clearing lights: " + e.getMessage());
				}
			}
		});
		
		// Midi->Transpose
		MenuItem transposeMenuItem = new MenuItem(midiMenu, SWT.CASCADE);
		transposeMenuItem.setText("Transpose");
		Menu transposeMenu = new Menu(shell, SWT.DROP_DOWN);
		transposeMenuItem.setMenu(transposeMenu);
		
		// Midi->Transpose->Octave Up
		MenuItem transposeOctaveUpItem = new MenuItem(transposeMenu, SWT.PUSH);
		transposeOctaveUpItem.setText("Octave Up");
		transposeOctaveUpItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				controller.transposeLights(12);
			}
		});
		
		// Midi->Transpose->None
		MenuItem transposeNoneItem = new MenuItem(transposeMenu, SWT.PUSH);
		transposeNoneItem.setText("None");
		transposeNoneItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				controller.transposeLights(0);
			}
		});
		
		// Midi->Transpose->Octave Down
		MenuItem transposeOctaveDownItem = new MenuItem(transposeMenu, SWT.PUSH);
		transposeOctaveDownItem.setText("Octave Down");
		transposeOctaveDownItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				controller.transposeLights(-12);
			}
		});
		
		shell.setMenuBar(menuBar);
	}
	
	private void addToolbarButtons(Composite toolBar) {
		// Play / Pause button.
		Button playPauseButton = new Button(toolBar, SWT.NONE);
		playPauseButton.setText("Play / Pause");
		playPauseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				controller.playPause();
			}
		});
		
		// Mark repeat button.
		Button markRepeatButton = new Button(toolBar, SWT.NONE);
		markRepeatButton.setText("Mark Repeat");
		markRepeatButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				controller.markRepeat();
			}
		});
		
		// Clear repeat button.
		Button clearRepeatButton = new Button(toolBar, SWT.NONE);
		clearRepeatButton.setText("Clear Repeat");
		clearRepeatButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				controller.clearRepeats();
			}
		});
	}
	
	private void addFileDropTarget(Composite parent) {
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		
		DropTarget target = new DropTarget(parent, DND.DROP_COPY | DND.DROP_DEFAULT);
		target.setTransfer(new Transfer[] { fileTransfer });
		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				if(event.detail == DND.DROP_DEFAULT) {
					if((event.operations & DND.DROP_COPY) > 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}
			
			public void drop(DropTargetEvent event) {
				if(fileTransfer.isSupportedType(event.currentDataType)) {
					String[] files = (String[])event.data;
					for(String file:files) {
						try {
							controller.openFile(file);
						} catch(Exception e) {
							e.printStackTrace();
						
							displayErrorMessage("Error loading file: " + e.getMessage());
						}
					}
				}
			}
			
			public void dropAccept(DropTargetEvent event) {
			}
			
			public void dragOver(DropTargetEvent event) {
			}
			
			public void dragOperationChanged(DropTargetEvent event) {
			}
			
			public void dragLeave(DropTargetEvent event) {
			}
		});
	}
	
	/**
	 * Prompts the user for a midi file, and opens it.
	 */
	private void openFile() {
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setText("Open");
		fileDialog.setFilterExtensions(new String[] { "*.mid; *.midi", "*.*" });
		fileDialog.setFilterNames(new String[] { "Midi Files (*.mid, *.midi)", "All Files (*.*)" });
		
		String filename = fileDialog.open();
		openFile(filename);
	}
	
	/**
	 * Opens a midi file by name.
	 */
	private void openFile(String filename) {
		if(filename != null) {
			try {
				controller.openFile(filename);
			} catch(Exception e) {
				e.printStackTrace();
				displayErrorMessage("Error loading file: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Displays an error message to the user.
	 */
	private void displayErrorMessage(String message) {
		MessageBox alert = new MessageBox(shell, SWT.ERROR);
		alert.setText("Error");
		alert.setMessage(message);
		alert.open();
	}
	
	/**
	 * Creates the main window and runs the event loop until exit.
	 */
	public static void main(String[] args) throws Exception {
		Display display = new Display();
		
		Shell shell = new Shell(display);
		shell.setText("Midi");
		shell.setSize(1280, 900);
		shell.setLayout(new FillLayout());
		
		Main main = new Main(shell, args);
		
		//main.openFile("test.mid");
		
		shell.open();
		
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
		
		main.controller.close();
	}
}
