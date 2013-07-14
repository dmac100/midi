package controller;

import java.util.List;

import model.MidiTrack;

import org.eclipse.swt.graphics.Color;

import view.MainView;
import view.table.CheckColumn;
import view.table.ColorColumn;
import view.table.CustomTable;
import view.table.TextColumn;

/**
 * Displays tracks in a table view, and handles selection changes.
 */
public class TracksController {
	private MainView mainView;

	public TracksController(final MainView mainView) {
		this.mainView = mainView;

		CustomTable<MidiTrack> tracksTable = mainView.getTracksTable();

		tracksTable.addColumn(new ColorColumn<MidiTrack>("Color", 40) {
			public Color getColor(MidiTrack track) {
				return mainView.getColors().getTrackColor(track);
			}
		});
		
		tracksTable.addColumn(new TextColumn<MidiTrack>("Name", 80) {
			public String getText(MidiTrack track) {
				return track.getName();
			}
		});
		
		tracksTable.addColumn(new CheckColumn<MidiTrack>("Active", 50) {
			public boolean isChecked(MidiTrack track) {
				return track.isActive();
			}
			
			public void setChecked(MidiTrack track, boolean checked) {
				track.setActive(checked);
				selectedTracksChanged();
			}
		});
		
		tracksTable.addColumn(new CheckColumn<MidiTrack>("Autoplay", 50) {
			public boolean isChecked(MidiTrack track) {
				return track.isAutoplay();
			}

			public void setChecked(MidiTrack track, boolean checked) {
				track.setAutoplay(checked);
				selectedTracksChanged();
			}
		});
	}
	
	public void setTracks(List<MidiTrack> tracks) {
		for(MidiTrack track:tracks) {
			track.setActive(true);
			track.setAutoplay(true);
		}
		
		mainView.getTracksTable().setRows(tracks);
		selectedTracksChanged();
	}
	
	public void selectedTracksChanged() {
		mainView.redraw();
	}
}
