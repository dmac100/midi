package view.table;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * A column that displays a color.
 */
public abstract class ColorColumn<T> extends Column<T> {
	public ColorColumn(String title, int width) {
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

		Color grey = new Color(display, 170, 170, 170);
		Color color = getColor(row);
		
		// Draw checkbox square.
		gc.setBackground(color);
		gc.setForeground(grey);
		gc.fillRectangle(x + 4, y + 4, 12, 12);
		gc.drawRectangle(x + 4, y + 4, 12, 12);

		grey.dispose();
	}

	public abstract Color getColor(T row);
}