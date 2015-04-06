package ru.kpfu.itis.group11403.vlasov.ellipticchat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.lang3.SerializationUtils;

import ru.kpfu.itis.group11403.vlasov.ellipticcurves.EllipticCrypto;
import ru.kpfu.itis.group11403.vlasov.ellipticcurves.Point;

public class SocketChat extends EncryptedChat implements AutoCloseable {

	private Socket socket;
	
	private ChatView view;
	
	public SocketChat(ChatView view, Socket socket) throws IOException, ChatException {
		
		if(view == null) {
			throw new ChatException("view can't be null");
		}
		
		if(socket == null) {
			throw new ChatException("socket can't be null");
		}
		
		this.view   = view;
		this.socket = socket;
		
		exchangeKeys();
		
	}

	@Override
	public void close() throws Exception {
	
		if(socket != null) {
			socket.close();
		}
		
	}

	@Override
	public boolean isConnected() {
		return socket.isConnected() && !socket.isClosed();
	}
	
	@Override
	protected synchronized void sendRawMessage(byte[] message) {
	
		if(!socket.isConnected()) {
			return;
		}
		
		try {
			
			OutputStream os = socket.getOutputStream();
			
			os.write(message);
		
			os.flush();
			
		} catch (IOException e) {
			view.showInfoMessage(e.toString());
		}
		
	}

	@Override
	protected synchronized byte[] recieveRawMessage() {
	
		if(!socket.isConnected()) {
			return null;
		}
		
		try {
			
			InputStream is = socket.getInputStream();
			
			byte[] bytes = new byte[is.available()];
			
			is.read(bytes);
			
			if(bytes.length == 0) {
				return null;
			}
			
			return bytes;
			
		} catch (IOException e) {
			view.showInfoMessage(e.toString());
		}
		
		return null;
	
	}

	@Override
	protected void sendPublicKey(Point publicKey) {
		
		try {
			
			OutputStream os = socket.getOutputStream();
			
			view.showInfoMessage("Sending public key...");
			
			byte[] bytes = SerializationUtils.serialize(publicKey);
			
			System.out.println(bytes.length);
			os.write(bytes);
			os.flush();
			
		} catch (IOException e) {
			view.showInfoMessage(e.toString());
		}
		
	}

	@Override
	protected Point receiveRecipientPublicKey() {
		
		Point publicKey = null;
		
		try {
			
			InputStream is = socket.getInputStream();
				
			byte[] bytes;
			
			do {
			
				bytes = new byte[is.available()];
				
			} while(bytes.length == 0);
			
			is.read(bytes);
			
			publicKey = SerializationUtils.deserialize(bytes);
			
			publicKey.setEllipticCurve(EllipticCrypto.P192.getEc());
			publicKey.setMod(EllipticCrypto.P192.getEc().getMod());
			
			view.showInfoMessage("Open key received");
			
		} catch (IOException e) {
			view.showInfoMessage(e.toString());
		}	
		
		return publicKey;
		
	}

}
