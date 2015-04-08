package ru.kpfu.itis.group11403.vlasov.ellipticchat;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import ru.kpfu.itis.group11403.vlasov.ellipticcurves.EllipticCrypto;
import ru.kpfu.itis.group11403.vlasov.ellipticcurves.Point;

public class SocketChat extends EncryptedChat implements AutoCloseable {

	private Socket socket;
	
	private ChatView view;
	
	// Если происходит какая-то ошибка, то флажок меняется на false
	private boolean isSuccess = true;
	
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
		return socket.isConnected() && !socket.isClosed() && isSuccess;
	}
	
	@Override
	protected void sendRawMessage(byte[] message) {
	
		if(!socket.isConnected()) {
			return;
		}
		
		try {
			
			OutputStream os = socket.getOutputStream();
			
			os.write(message);
		
			os.flush();
			
		} catch (IOException e) {
			view.showInfoMessage(e.toString());
			isSuccess = false;
		}
		
	}

	@Override
	protected EllipticCrypto.CryptedByte[] recieveRawMessage() {
	
		if(!socket.isConnected()) {
			return null;
		}
		
		try {
			
			InputStream is = socket.getInputStream();
			
			ObjectInputStream objectInputStream = new ObjectInputStream(is);
			
			EllipticCrypto.CryptedByte[] cryptedBytes =
					(EllipticCrypto.CryptedByte[]) objectInputStream.readObject();
			
			return cryptedBytes;
			
		} catch (IOException e) {
			
			view.showInfoMessage(e.toString());
			e.printStackTrace();
			
			isSuccess = false;
			
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		
			isSuccess = false;
			
		}
		
		return null;
	
	}

	@Override
	protected void sendPublicKey(Point publicKey) {
		
		try {
			
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
			
			view.showInfoMessage("Sending public key...");
			
			objectOutputStream.writeObject(publicKey);
			
			os.flush();
			
		} catch (IOException e) {
			
			view.showInfoMessage(e.toString());
			e.printStackTrace();
		
			isSuccess = false;
			
		}
		
	}

	@Override
	protected Point receiveRecipientPublicKey() {
		
		Point publicKey = null;
		
		try {
			
			InputStream is = socket.getInputStream();
			ObjectInputStream objectInputStream = new ObjectInputStream(is);
			
			publicKey = (Point) objectInputStream.readObject();
			
			publicKey.setEllipticCurve(EllipticCrypto.P192.getEc());
			publicKey.setMod(EllipticCrypto.P192.getEc().getMod());
			
			view.showInfoMessage("Open key received");
			
		} catch (IOException e) {
		
			view.showInfoMessage(e.toString());
		
			isSuccess = false;
			
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
			
			isSuccess = false;
			
		}	
		
		return publicKey;
		
	}

}
