import java.io.Serializable;

public class Message implements Serializable {
	public enum MsgType {
		NO_ACT, LOGIN, LOGOUT, LOGIN_FAILURE, CLIENT_MSG, SERVER_MSG, LOGIN_LIST, GAME_START
	};

	public static final String ALL = "전체";
	private MsgType type;
	private String sender, receiver, message;

	public Message() {
		this(MsgType.NO_ACT, "", "", "");
	}

	public Message(MsgType type, String sender, String receiver, String msg) {
		this.type = type;
		this.sender = sender;
		this.receiver = receiver;
		this.message = msg;
	}

	public void setType(MsgType type) {
		this.type = type;
	}

	public MsgType getType() {
		return this.type;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSender() {
		return this.sender;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getReceiver() {
		return this.receiver;
	}

	public void setMessage(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return this.message;
	}

	public String toString() {
		return ("메시지 종류 : " + type + "\n" + "송신자         : " + sender + "\n" + "수신자         : " + receiver + "\n"
				+ "메시지 내용 : " + message + "\n");
	}
}
