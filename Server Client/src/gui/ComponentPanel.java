package gui;

import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class ComponentPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	protected ApplicationManager frame;
	public ComponentPanel(ApplicationManager frame){
		this.frame=frame;
		initComponents();
	}
	protected abstract void initComponents();
}
