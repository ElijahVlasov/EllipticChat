package ru.kpfu.itis.group11403.vlasov.ellipticchat;

import javax.swing.JTextPane;

public class JTextPaneChatView implements ChatView {

	private JTextPane jTextPane;
	
	public JTextPaneChatView(JTextPane jTextPane) {
		this.jTextPane = jTextPane;		
	}
	
	@Override
	public void showInfoMessage(String message) {
		
		jTextPane.setText(jTextPane.getText() + "\nInfo: " + message + "\n");

	}

	@Override
	public void showMessage(String userName, String message) {
		
		jTextPane.setText(jTextPane.getText() + "\n" + userName + ": " + message + "\n");
		
	}

}
