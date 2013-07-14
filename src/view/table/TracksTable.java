package view.table;

import java.util.ArrayList;
import java.util.List;

import model.MidiTrack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TracksTable<T> implements Listener {
	private Table table;
	
	private List<T> rows = new ArrayList<T>();
	private List<Column<T>> columns = new ArrayList<Column<T>>();
	
	public TracksTable(Composite parent) {
		this.table = new Table(parent, SWT.NONE);
		table.setHeaderVisible(true);
		
		table.addListener(SWT.MeasureItem, this);
		table.addListener(SWT.PaintItem, this);
		table.addListener(SWT.MouseUp, this);
	}

	public void setRows(List<T> rows) {
		table.removeAll();
		this.rows = rows;
		for(T t:rows) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(t);
			
			for(int i = 0; i < columns.size(); i++) {
				item.setText(i, columns.get(i).getText(t));
			}
		}
	}
	
	public void addColumn(Column<T> column) {
		columns.add(column);
		
		TableColumn accompColumn = new TableColumn(table, SWT.NONE);
		accompColumn.setText(column.getTitle());
		accompColumn.setWidth(column.getWidth());
	}
		
	@Override
	public void handleEvent(Event event) {
		T row = getRow(event.y + 5);
		if(row == null) return;
		
		if(event.type == SWT.MeasureItem) {
			handleMeasureItem(event, row);
		} else if(event.type == SWT.PaintItem) {
			handlePaintItem(event, row);
		} else if(event.type == SWT.MouseUp) {
			handleMouseUp(event, row);
		}
	}
	
	private void handleMeasureItem(Event event, T row) {
		int index = event.index;
		if(index < columns.size()) {
			columns.get(index).handleMeasureItem(event, row);
		}
	}
	
	private void handlePaintItem(Event event, T row) {
		int index = event.index;
		if(index < columns.size()) {
			columns.get(index).handlePaintItem(event, row);
		}
	}
	
	private void handleMouseUp(Event event, T row) {
		int x = event.x;
		int y = event.y;

		int i = 0;
		
		for(TableColumn tableColumn:table.getColumns()) {
			int width = tableColumn.getWidth();
			x -= width;
			if(x < 0) {
				x += width;
				
				columns.get(i).handleMouseUp(x, y, row);
				table.redraw();
				
				return;
			}
			
			i++;
		}
	}
		
	private T getRow(int y) {
		for(TableItem item:table.getItems()) {
			Rectangle bounds = item.getBounds(0);
			if(y > bounds.y && y < bounds.y + bounds.height) {
				return (T)item.getData();
			}
		}
		return null;
	}
		
	public Composite getWidget() {
		return table;
	}
}
