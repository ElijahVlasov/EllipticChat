package ru.kpfu.itis.group11403.vlasov.ellipticchat;

public class ChatException extends Exception {

	public ChatException() {
		super();
	}

	public ChatException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ChatException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChatException(String message) {
		super(message);
	}

	public ChatException(Throwable cause) {
		super(cause);
	}
	
}
