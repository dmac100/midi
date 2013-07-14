package view.table;

/**
 * A column that only displays text.
 */
public abstract class TextColumn<T> extends Column<T> {
	public TextColumn(String title, int width) {
		super(title, width);
	}

	@Override
	public abstract String getText(T t);
}