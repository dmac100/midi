package view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import model.MidiFile;
import model.MidiTrack;
import model.Note;
import model.Pitch;
import model.TimeSignature;

public class ScoreCanvas {
	private Colors colors;
	private Canvas canvas;
	
	private int scale;
	
	private int staffMargin = 15;
	private int lineSpacing = 7;
	private int staffStart = 30;
	private int staffSpacing = 20;
	
	private Pitch middleC = new Pitch("C4");
	
	private MidiFile midiFile;
	private int position;
	
	private Image trebleClefImage;
	private Image bassClefImage;
	private Image noteHeadImage;

	public ScoreCanvas(Composite parent, Colors colors) {
		this.colors = colors;
		this.canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		this.noteHeadImage = createNoteHeadImage();
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				paint(event.gc);
			}
		});
		
		trebleClefImage = loadSvg("/score/treble.svg", 50);
		bassClefImage = loadSvg("/score/bass.svg", 30);
	}
	
	private void paint(GC gc) {
		int totalWidth = getWidget().getBounds().width;
		
		gc.setBackground(colors.getWhite());
		gc.setForeground(colors.getBlack());
		gc.fillRectangle(0, 0, totalWidth, getTotalHeight() - 5);
		gc.drawRectangle(0, 0, totalWidth - 1, getTotalHeight() - 5);
		gc.setClipping(0, 0, totalWidth, getTotalHeight() - 5);
		
		gc.setForeground(colors.getBlack());
		
		int y = staffStart;
		
		for(int i = 0; i < 5; i++) {
			gc.drawLine(staffMargin, y, totalWidth - staffMargin, y);
			y += lineSpacing;
		}
		
		y += staffSpacing;
		
		for(int i = 0; i < 5; i++) {
			gc.drawLine(staffMargin, y, totalWidth - staffMargin, y);
			y += lineSpacing;
		}
		
		gc.drawLine(staffMargin, staffStart, staffMargin, y - lineSpacing);
		gc.drawLine(totalWidth - staffMargin, staffStart, totalWidth - staffMargin, y - lineSpacing);
		
		if(midiFile != null) {
			// Draw time signatures.
			drawTimeSignature(gc, staffMargin + 55, staffStart);
			drawTimeSignature(gc, staffMargin + 55, staffStart + lineSpacing * 5 + staffSpacing);

			// Draw bar lines.
			TimeSignature timeSignature = midiFile.getTimeSignature();
			int wholeNotesInBar = midiFile.getResolution() * timeSignature.getNumerator() * 4;
			int barLength = wholeNotesInBar / timeSignature.getDenominator();
			for(int x = barLength; x < midiFile.getTotalTime(); x += barLength) {
				int x2 = (x - position) * 2 / scale;
				
				x2 += 80;
				if(x2 < staffMargin + 80) continue;
				if(x2 > totalWidth - staffMargin * 2 - 5) continue;
				
				gc.drawLine(x2, staffStart, x2, staffStart + 9 * lineSpacing + staffSpacing);
			}
		
			List<MidiTrack> tracks = midiFile.getTracks();
			
			// Draw note heads.
			for(MidiTrack track:tracks) {
				if(!track.isActive()) continue;
				
				for(Note note:track.getNotes()) {
					int x = note.getStartTime() - position;
					
					x *= 2;
					x /= scale;
					
					x += 80;
					if(x < 80) continue;
					if(x > totalWidth - staffMargin * 2 - 5) continue;
					
					drawNote(gc, staffMargin + x, note.getPitch());
				}
			}
		}
		
		// Draw clefs.
		gc.drawImage(trebleClefImage, staffMargin + 10, staffStart - 10);
		gc.drawImage(bassClefImage, staffMargin + 9, staffStart + lineSpacing * 5 + staffSpacing - 2);
	}
	
	private void drawTimeSignature(GC gc, int x, int y) {
		TimeSignature timeSignature = midiFile.getTimeSignature();
		
		Font font = new Font(getWidget().getDisplay(), "Times", 16, SWT.BOLD);
		
		String numerator = String.valueOf(timeSignature.getNumerator());
		String denominator = String.valueOf(timeSignature.getDenominator());
		
		gc.setFont(font);
		gc.drawText(numerator, x, y, true);
		gc.drawText(denominator, x, y + 13, true);
		
		font.dispose();
		gc.setFont(null);
	}

	/**
	 * Draw a note with a head, accidentals, and ledger lines.
	 */
	private void drawNote(GC gc, int x, Pitch pitch) {
		// Set initial position to middle C.
		int noteHeadY = staffStart + lineSpacing * 5;
		
		if(pitch.isAbove(middleC.nextSemitone())) {
			int steps = pitch.getStaffPosition();
			
			// Adjust note position.
			noteHeadY -= (lineSpacing * steps) / 2;
			
			// Add ledger lines above treble staff.
			for(int y = staffStart; y >= noteHeadY; y -= lineSpacing) {
				gc.drawLine(x - 6, y, x + 6, y);
			}
		} else if(pitch.equals(middleC) || pitch.equals(middleC.nextSemitone())) {
			// Add ledger line for middle C / C#.
			gc.drawLine(x - 6, noteHeadY, x + 6, noteHeadY);
		} else {
			int steps = pitch.getStaffPosition();
			
			// Adjust for gap between staffs.
			noteHeadY += staffSpacing - lineSpacing;
			
			// Adjust note position.
			noteHeadY -= (lineSpacing * steps) / 2;
			
			// Add ledger lines below bass staff.
			for(int y = staffStart + lineSpacing * 10 + staffSpacing; y <= noteHeadY; y += lineSpacing) {
				gc.drawLine(x - 6, y, x + 6, y);
			}
		}
		
		// Draw note head.
		gc.drawImage(noteHeadImage, x - 10, (int)(noteHeadY) - 10);
		
		// Draw sharp if necessary.
		if(pitch.isBlackKey()) {
			gc.drawText("\u266f", x - 15, noteHeadY - 5, true);
		}
	}
	
	/**
	 * Create a note head image that can be rendered onto the staff as part of a note.
	 * The center of the image is at (10, 10).
	 */
	private Image createNoteHeadImage() {
		Image image = new Image(getWidget().getDisplay(), 20, 20);
		
		Transform transform = new Transform(getWidget().getDisplay());
		transform.translate(11, 11);
		transform.rotate(-35f);
		transform.scale(1, 1.2f);
		
		GC gc = new GC(image);
		gc.setTransform(transform);
		gc.setBackground(colors.getBlack());
		
		int r = lineSpacing - 3;
		gc.fillOval(-r, -r + 1, 2 * r, 2 * r - 3);
		gc.dispose();
		
		Image imageWithAlpha = whiteToAlpha(image);
		image.dispose();
		return imageWithAlpha;
	}
	
	/**
	 * Converts a greyscale image into all black plus an alpha channel,
	 * so that white is fully transparent, and black is opaque.
	 */
	private Image whiteToAlpha(Image image) {
		ImageData imageData = image.getImageData();
		
		int width = imageData.width;
		int height = imageData.height;

		PaletteData palette = imageData.palette;
		
		byte[] alphaData = new byte[width * height];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				RGB rgb = palette.getRGB(imageData.getPixel(x, y));
				
				imageData.setPixel(x, y, palette.getPixel(new RGB(0, 0, 0)));
				
				alphaData[x + y * width] = (byte)(255 - (rgb.red + rgb.green + rgb.blue) / 3);
			}
		}
		
		imageData.alphaData = alphaData;
		
		image.dispose();
		
		return new Image(getWidget().getDisplay(), imageData);
	}
	
	private Image loadSvg(String name, int height) {
		PNGTranscoder transcoder = new PNGTranscoder();
		transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, new Float(height));
		
		TranscoderInput input = new TranscoderInput(getResource(name));
		
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			TranscoderOutput output = new TranscoderOutput(outputStream);
			
			transcoder.transcode(input, output);
			
			InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			
			return new Image(getWidget().getDisplay(), inputStream);
		} catch (TranscoderException e) {
			throw new RuntimeException("Error loading SVG file: " + name, e);
		}
	}
	
	private InputStream getResource(String name) {
		return getClass().getResourceAsStream(name);
	}

	public int getTotalHeight() {
		return staffStart + lineSpacing * 9 + staffSpacing + staffStart;
	}
	
	public Composite getWidget() {
		return canvas;
	}

	public void setMidiFile(MidiFile midiFile) {
		this.midiFile = midiFile;
	}

	public void positionChanged(int newPosition) {
		this.position = newPosition;
		
		redraw();
	}

	public void setScale(int scale) {
		this.scale = scale;
	}
	
	public void redraw() {
		canvas.redraw();
	}
}
