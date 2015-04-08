package ru.kpfu.itis.group11403.vlasov.ellipticchat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

public class Application {

	private final static int SERVER_PORT = 3333;
	
	private JFrame 		frame;
	private JTextPane 	messagePane;
	private JTextArea   inputTextArea;
	private JButton     sendButton;
	
	private ChatView view;
	
	private EncryptedChat chat;
	
	private boolean isServer;
	
	public Application() {
			
		SwingUtilities.invokeLater(()->initGui());
	
		int result = JOptionPane.showConfirmDialog(frame, "Запустить в режиме сервера", "Dialog", JOptionPane.YES_NO_OPTION);
	
		if(result == JOptionPane.CLOSED_OPTION) {
			frame.dispose();
			return;
		} else {	
			isServer = result == JOptionPane.YES_OPTION;	
		}
		
	}	
	
	public void run() {
		
		view = new JTextPaneChatView(messagePane);
		
		if(isServer) {
			runServer();
		} else {
			runClient();
		}
		
	}
	
	public static void main(String[] args) {
	
		Application app = new Application();
		app.run();
		
	}
	
	private void sendClicked() {
		
		if(inputTextArea.getText().isEmpty()) {
			return;
		}
		
		String message = inputTextArea.getText();
		
		if(chat != null && chat.isConnected()) {
			chat.sendMessage(message);
		} else {
			view.showInfoMessage("Can't send message");
			return;
		}
		
		view.showMessage("Me", message);
		inputTextArea.setText(""); // Очистить поле ввода
		
	}

	private void initGui() {
		
		frame 	    	= new JFrame("EllipticChat");
		messagePane 	= new JTextPane(); 
		inputTextArea   = new JTextArea();
		sendButton		= new JButton("Send");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 600);
		
		frame.setVisible(true);
		frame.setResizable(false);
		
		messagePane.setEditable(false);
		messagePane.setBackground(Color.LIGHT_GRAY);
		
		JScrollPane inputTextScrollPane = new JScrollPane(
				inputTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		inputTextScrollPane.setPreferredSize(new Dimension(300, 100));
		
		JScrollPane messagePaneScrollPane = new JScrollPane(
				messagePane, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);	
		
		messagePaneScrollPane.setPreferredSize(new Dimension(300, 500));
		
		sendButton.addActionListener((ActionEvent event)->{
			sendClicked();
		});
		
		frame.setLayout(new BorderLayout());
		frame.add(messagePaneScrollPane, BorderLayout.PAGE_START);
		frame.add(inputTextScrollPane);
		frame.add(sendButton, BorderLayout.PAGE_END);
		
	}
	
	private void runServer() {
		
		try(ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
		
			// Ждем клиента
			Socket sock = serverSocket.accept();
			
			try(SocketChat chat = new SocketChat(view, sock)) {
				
				this.chat = chat;
				
				while(chat.isConnected()) {
				
					// Пока подключены пытаемся получить сообщения от юзера
					
					String message = chat.receiveMessage();
					
					if(message == null) {
						continue;
					}
					
					view.showMessage("Partner", message);
					
				}
				
			}	
				
			
		} catch (IOException e) {
			view.showInfoMessage(e.toString());
			e.printStackTrace();
		} catch (ChatException e) {
			view.showInfoMessage(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private Socket connectToServer() throws IOException {
		
		String addr = JOptionPane.showInputDialog("Введите адрес сервера");
		
		if(addr == null) { // Если пользователь нажал отмену
			frame.dispose();
			return null;
		}
		
		addr = addr.trim();
		
		do {

			try {

				Socket sock = new Socket(addr, SERVER_PORT);

				return sock;
				
			} catch (UnknownHostException e) {

				JOptionPane.showMessageDialog(frame, 
						"Невозможно подключиться к серверу. Попробуйте снова", 
						"Ошибка", 
						JOptionPane.ERROR_MESSAGE);
			
				addr = JOptionPane.showInputDialog("Введите адрес сервера");
				
				if(addr == null) {
					frame.dispose();
					return null;
				}
				
				addr = addr.trim();
				
			}

		} while (true);

	}
	
	private void runClient() {	

		try (SocketChat chat = new SocketChat(view, connectToServer())) {
			
			this.chat = chat;

			while (chat.isConnected()) {

				String message = chat.receiveMessage();

				if(message == null) {
					continue;
				}
				
				view.showMessage("Partner", message);

			}

		} catch (IOException e) {
			view.showInfoMessage(e.toString());
			e.printStackTrace();
		} catch (ChatException e) {
			view.showInfoMessage(e.toString());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}
	
}
