package matchpackage.application;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import jade.core.AID;

public class LoginGUI extends JFrame implements ActionListener {


		private JLabel name;
		private JLabel password;
		private JLabel headerLabel;
		
		private JTextArea nameText;
		private JTextArea passwordText;
		
		private JButton submit;
		private JPanel header, bottom, overall, total;

		private GUIAgent guiAgent;
		
		

		public LoginGUI(GUIAgent guiAgent) {

			this.guiAgent = guiAgent;
			overall = new JPanel();
			total = new JPanel();
			total.setLayout(new BoxLayout(total, BoxLayout.PAGE_AXIS));

			headerLabel = new JLabel("Please enter the details for signing up");
			submit = new JButton("Submit");
			submit.addActionListener(this);

			name = new JLabel("Name");
			password = new JLabel("Password");

			nameText = new JTextArea("");
			passwordText = new JTextArea("");

			String[] choices = { "Provider", "Client" };

			overall.setLayout(new GridLayout(2, 2, 1, 1));
			overall.add(name);
			overall.add(nameText);

			overall.add(password);
			overall.add(passwordText);

			total.add(headerLabel);
			total.add(overall);
			total.add(submit);

			getContentPane().add(total);
			setTitle("Login Interface");
			setSize(300, 300);
			setVisible(true);
			System.out.println("This is the login interface!");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == submit) {
				System.out.println("Submit button clicked for logging in");
				guiAgent.showCustomerProviderGUI(nameText.getText());	
			}
		}

	}
