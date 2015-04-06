package ru.kpfu.itis.group11403.vlasov.ellipticchat;

import java.math.BigInteger;

import ru.kpfu.itis.group11403.vlasov.ellipticcurves.EllipticCrypto;
import ru.kpfu.itis.group11403.vlasov.ellipticcurves.Point;

public abstract class EncryptedChat {

	private final static EllipticCrypto.CryptoInfo INFO = EllipticCrypto.P192;

	private BigInteger privateKey = EllipticCrypto.generatePrivateKey(INFO);
	private Point publicKey = EllipticCrypto.generateOpenKey(privateKey, INFO);

	private Point recipientPublicKey;

	public void sendMessage(String msg) {

		byte[] cryptedMsg = EllipticCrypto.encrypt(msg.getBytes(),
				recipientPublicKey, INFO);
		
		sendRawMessage(cryptedMsg);

	}

	public String receiveMessage() {

		byte[] cryptedMsg = recieveRawMessage();

		if(cryptedMsg == null) {
			return null;
		}
		
		byte[] decryptedMsg = EllipticCrypto.decrypt(cryptedMsg, privateKey,
				INFO);

		return new String(decryptedMsg);

	}
	
	public abstract boolean isConnected();

	protected void exchangeKeys() {

		sendPublicKey(publicKey);
		recipientPublicKey = receiveRecipientPublicKey();

	}

	protected abstract void sendRawMessage(byte[] message);

	protected abstract byte[] recieveRawMessage();

	protected abstract void sendPublicKey(Point publicKey);

	protected abstract Point receiveRecipientPublicKey();

}
