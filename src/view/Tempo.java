package view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

import controller.TempoChangedHandler;

public class Tempo {
	private Composite composite;
	private Scale scale;
	private List<TempoChangedHandler> tempoChangedHandlers = new ArrayList<TempoChangedHandler>();

	public Tempo(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Tempo:");
		
		this.scale = new Scale(composite, SWT.NONE);
		scale.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		scale.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				for(TempoChangedHandler handler:tempoChangedHandlers) {
					handler.tempoChanged(scale.getSelection());
				}
			}
		});
	}
	
	public void addTempoChangedHandler(TempoChangedHandler handler) {
		tempoChangedHandlers.add(handler);
	}
	
	public void setRange(int min, int max) {
		scale.setMinimum(min);
		scale.setMaximum(max);
	}
	
	public void setTempo(int tempo) {
		scale.setSelection(tempo);
	}

	public Composite getWidget() {
		return composite;
	}
}
