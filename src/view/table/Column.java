package view.table;

import org.eclipse.swt.widgets.Event;

/**
 * Abstract column for a CustomTable, parameterized on the row type. 
 */
public abstract class Column<T> {
	private String title;
	private int width;

	public Column(String title, int width) {
		this.setTitle(title);
		this.setWidth(width);
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	/**
	 * Returns the text to render on this column for some row.
	 */
	public String getText(T row) {
		return "";
	}

	/**
	 * Adjusts the size required to paint this column.
	 * @param event Event to set the width and height properties of.
	 * @param row Row to measure.
	 */
	public void handleMeasureItem(Event event, T row) {
		return;
	}

	/**
	 * Paint the column of a row.
	 */
	public void handlePaintItem(Event event, T row) {
	}
	
	/**
	 * Handle a mouse up event on this column for a row.
	 * @param x Mouse x position from the beginning of the column.
	 * @param y Mouse y position from the top of the table.
	 */
	public void handleMouseUp(int x, int y, T row) {
	}
}