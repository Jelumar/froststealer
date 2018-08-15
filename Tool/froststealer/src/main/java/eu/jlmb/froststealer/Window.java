package eu.jlmb.froststealer;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 * Main Window for the Frost Stealer
 * @author Jean Baumgarten
 */
public class Window implements ActionListener {
	
	private JFrame frame;
	private Container contentPane;
	private SpringLayout layout;
	private JTextField urlField;
	private JButton button;
	private JTextField info;
	private JTextField max;
	private JTextField error;

	/**
	 * Default Constructor
	 */
	public Window() {
        buildGUI();
	}
	
	private void buildGUI() {
		this.frame = new JFrame("Frost Stealer");
        this.contentPane = this.frame.getContentPane();
        this.layout = new SpringLayout();
        this.contentPane.setLayout(layout);
        
        this.max = new JTextField("25000");
        contentPane.add(this.max);
        layout.putConstraint(SpringLayout.WEST, this.max, -100, SpringLayout.EAST, this.contentPane);
        layout.putConstraint(SpringLayout.EAST, this.max, -20, SpringLayout.EAST, this.contentPane);
        layout.putConstraint(SpringLayout.NORTH, this.max, 20, SpringLayout.NORTH, this.contentPane);
        
        this.urlField = new JTextField("http://example.sensorup.com/v1.0/");
        contentPane.add(this.urlField);
        layout.putConstraint(SpringLayout.WEST, this.urlField, 20, SpringLayout.WEST, this.contentPane);
        layout.putConstraint(SpringLayout.EAST, this.urlField, -20, SpringLayout.WEST, this.max);
        layout.putConstraint(SpringLayout.NORTH, this.urlField, 20, SpringLayout.NORTH, this.contentPane);
        
        this.error = new JTextField("");
        contentPane.add(this.error);
        layout.putConstraint(SpringLayout.WEST, this.error, 20, SpringLayout.WEST, this.contentPane);
        layout.putConstraint(SpringLayout.EAST, this.error, -20, SpringLayout.EAST, this.contentPane);
        layout.putConstraint(SpringLayout.SOUTH, this.error, -20, SpringLayout.SOUTH, this.contentPane);
        this.error.setEditable(false);
        
        this.info = new JTextField("Keine Aktion gestartet");
        contentPane.add(this.info);
        layout.putConstraint(SpringLayout.WEST, this.info, 20, SpringLayout.WEST, this.contentPane);
        layout.putConstraint(SpringLayout.EAST, this.info, -20, SpringLayout.EAST, this.contentPane);
        layout.putConstraint(SpringLayout.SOUTH, this.info, -20, SpringLayout.NORTH, this.error);
        this.info.setEditable(false);
        
        this.button = new JButton("Start stealing Data");
        this.button.setName("button");
        this.contentPane.add(this.button);
        this.layout.putConstraint(SpringLayout.WEST, this.button, 20, SpringLayout.WEST, this.contentPane);
        this.layout.putConstraint(SpringLayout.EAST, this.button, -20, SpringLayout.EAST, this.contentPane);
        this.layout.putConstraint(SpringLayout.NORTH, this.button, 20, SpringLayout.SOUTH, this.urlField);
        this.layout.putConstraint(SpringLayout.SOUTH, this.button, -20, SpringLayout.NORTH, this.info);
        this.button.addActionListener(this);
        
        this.frame.setSize(600, 240);
        this.frame.setResizable(false);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
	}

	/**
	 * Implements actionPerformed
	 * @param action is a Button click event
	 */
	public void actionPerformed(ActionEvent action) {
		String name = ((JButton) action.getSource()).getName();
		if ("button".equals(name)) {
			this.button.setEnabled(false);
			final String url = this.urlField.getText();
			final int max = Integer.parseInt(this.max.getText());
			Thread aThread = new Thread(new Runnable() {
	            public void run() {
	            	new Stealer(url, max, info, error);
	            }
	        });
	        aThread.start();
		}
	}

}
