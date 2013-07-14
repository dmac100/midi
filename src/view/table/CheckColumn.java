package view.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * A column that displays a checkbox and toggles its state on a mouse click.
 */
public abstract class CheckColumn<T> extends Column<T> {
	public CheckColumn(String title, int width) {
		super(title, width);
	}

	@Override
	public void handleMeasureItem(Event event, T row) {
		event.width = 20;
		event.height = 20;
	}

	@Override
	public void handlePaintItem(Event event, T row) {
		int x = event.x;
		int y = event.y;
		
		GC gc = event.gc;
		Display display = event.display;

		Color white = new Color(display, 255, 255, 255);
		Color grey = new Color(display, 170, 170, 170);
		Color check = new Color(display, 88, 109, 131);
		
		// Draw checkbox square.
		gc.setBackground(white);
		gc.setForeground(grey);
		gc.fillRectangle(x + 4, y + 4, 12, 12);
		gc.drawRectangle(x + 4, y + 4, 12, 12);

		// Draw check if the checkbox is checked.
		if(isChecked(row)) {
			gc.setForeground(check);
			gc.setLineWidth(2);
			gc.setAntialias(SWT.ON);
			
			gc.drawLine(x + 7, y + 11, x + 10, y + 14);
			gc.drawLine(x + 9, y + 14, x + 14, y + 7);
		}
		
		white.dispose();
		grey.dispose();
		check.dispose();
	}
	
	@Override
	public void handleMouseUp(int x, int y, T row) {
		// Toggle checked if the mouse is inside the checkbox square.
		if(x > 6 && x < 18) {
			boolean checked = !isChecked(row);
			setChecked(row, checked);
		}
	}
	
	public abstract boolean isChecked(T row);
	
	public abstract void setChecked(T row, boolean checked);
}