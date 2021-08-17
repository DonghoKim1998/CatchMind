import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Server {
	// userName-ObjectOutputStream ���� Ŭ���̾�Ʈ OutputStream �������
	HashMap<String, ObjectOutputStream> clientOutputStreams = new HashMap<String, ObjectOutputStream>();

	public void go() {
		try {
			ServerSocket serverSocket = new ServerSocket(9999);

			while (true) {
				Socket clientSocket = serverSocket.accept();

				Thread thread = new Thread(new ClientHandler(clientSocket));
				thread.start();

				System.out.println("Server: Ŭ���̾�Ʈ ���� ����");
			}
		} catch (Exception e) {
			System.out.println("Server: Ŭ���̾�Ʈ ���� �� �̻� �߻�");
			e.printStackTrace();
		}
	}

	private class ClientHandler implements Runnable {
		Socket socket; // Ŭ���̾�Ʈ ����� ����
		ObjectInputStream reader; // ���ſ� ��Ʈ��
		ObjectOutputStream writer; // �۽ſ� ��Ʈ��

		public ClientHandler(Socket clientSocket) {
			try {
				socket = clientSocket;
				writer = new ObjectOutputStream(clientSocket.getOutputStream());
				reader = new ObjectInputStream(clientSocket.getInputStream());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			Message message;
			Message.MsgType type;

			try {
				while (true) {
					message = (Message) reader.readObject();
					type = message.getType();

					// Ŭ���̾�Ʈ�κ��� �޽��� ����
					if (type == Message.MsgType.CLIENT_MSG) {
						handleMessage(message.getSender(), message.getReceiver(), message.getMessage());
					} else if (type == Message.MsgType.LOGIN) {
						handleLogin(message.getSender(), writer);
					} else if (type == Message.MsgType.LOGOUT) {
						handleLogout(message.getSender());
						writer.close();
						reader.close();
						socket.close();
						return;
					} else if (type == Message.MsgType.NO_ACT) {
						continue;
					} else {
						throw new Exception("Server: Ŭ���̾�Ʈ���� �� �� ���� �޽��� ����");
					}
				}
			}
			// ����� Ŭ���̾�Ʈ ���� �� ���ܹ߻�
			catch (Exception e) {
				System.out.println("Server: Ŭ���̾�Ʈ ���� ����");
			}
		}
	}

	private synchronized void handleLogin(String user, ObjectOutputStream writer) {
		try {
			// �̹� ������ �̸��� ����ڰ� �ִ� ���
			if (clientOutputStreams.containsKey(user)) {
				writer.writeObject(new Message(Message.MsgType.LOGIN_FAILURE, "", "", "����� �̹� ����"));
				return;
			}
		} catch (Exception e) {
			System.out.println("Server: �������� �۽� �� �̻� �߻�");
			e.printStackTrace();
		}

		// �ؽ����̺��� userName-writer ���� �߰��ϰ�
		clientOutputStreams.put(user, writer);
		// ���ο� �α��� ����Ʈ�� ��ü���� ���� ��
		broadCastMessage(new Message(Message.MsgType.LOGIN_LIST, "", "", makeClientList()));
		broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", user+"���� �����ϼ̽��ϴ�."));
	}

	private synchronized void handleMessage(String sender, String receiver, String msg) {
		// ��ο��� ������ ���
		if (receiver.equals(Message.ALL)) {
			broadCastMessage(new Message(Message.MsgType.SERVER_MSG, sender, "", msg));
			return;
		}

		// Ư�� ��󿡰� ������ ���
		ObjectOutputStream write = clientOutputStreams.get(receiver);
		try {
			write.writeObject(new Message(Message.MsgType.SERVER_MSG, sender, "", msg));
		} catch (Exception e) {
			System.out.println("Server: �������� �۽� �� �̻� �߻�");
			e.printStackTrace();
		}
	}

	private synchronized void handleLogout(String user) {
		clientOutputStreams.remove(user);
		broadCastMessage(new Message(Message.MsgType.LOGIN_LIST, "", "", makeClientList()));
	}

	private void broadCastMessage(Message message) {
		String user;
		Set<String> users = clientOutputStreams.keySet();
		Iterator<String> iterator = users.iterator();

		while (iterator.hasNext()) {
			user = iterator.next();

			try {
				ObjectOutputStream writer = clientOutputStreams.get(user);
				writer.writeObject(message);
				writer.flush();
			} catch (Exception e) {
				System.out.println("Server: �������� �۽� �� �̻� �� ��");
				e.printStackTrace();
			}
		}
	}
	
	private void broadCastLogin(String str) {
		String user;
		Set<String> users = clientOutputStreams.keySet();
		Iterator<String> iterator = users.iterator();

		while (iterator.hasNext()) {
			user = iterator.next();

			try {
				ObjectOutputStream writer = clientOutputStreams.get(user);
				writer.writeObject(str);
				writer.flush();
			} catch (Exception e) {
				System.out.println("Server: �������� �۽� �� �̻� �� ��");
				e.printStackTrace();
			}
		}
	}

	private String makeClientList() {
		Set<String> s = clientOutputStreams.keySet(); // ���� ��ϵ� ����ڵ��� ����
		Iterator<String> it = s.iterator();
		String userList = "";

		while (it.hasNext()) {
			userList += it.next() + "/"; // ��Ʈ�� ����Ʈ�� �߰��ϰ� ������ ����
		}

		return userList;
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.go();
	}

}