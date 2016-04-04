package gui;

import javax.swing.JFrame;

public class ApplicationManager extends JFrame{
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	ApplicationManager frame=new ApplicationManager();
                frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                frame.enterScreen(new HomePanel(frame));
                frame.setVisible(true);
            }
        });
    }
    public void enterScreen(ComponentPanel screenPanel){
    	getContentPane().removeAll();
    	invalidate();
    	getContentPane().add(screenPanel);
    	revalidate();
    	pack();
    	repaint();
    }
}
