package ru.kpfu.itis.group11403.vlasov.ellipticchat;

public class ConsoleChatView implements ChatView {

	@Override
	public void showInfoMessage(String message) {
		System.out.println(message);
	}

	@Override
	public void showMessage(String userName, String message) {
		System.out.println(userName + ": " + message);
	}

}
