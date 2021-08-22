import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Timer;

public class Server {
	String[] words = { "�ٳ���", "������", "���", "å", "�⸰", "�ϸ�", "��", "�ڸ��", "������", "����", "��ö��", "����", "��", "����", "��", "����", "����", "�ƺ�", "����", "����", "�ҸӴ�", "�Ҿƹ���", "��", "�����", "��", "�ô�", "����", "�ۼ�Ʈ", "�", "�б�", "�����", "����", "��", "ģ��", "����", "��õ����б�", "��", "��â", "����", "�뷡", "�ٸ�", "�Ѱ�", "���ǽ���", "�汸��", "���", "�Ź�", "�̾߱�", "�޶ѱ�", "���", "������", "�ڷγ�", "���缮", "��ġ", "�Թ�", "�ǾƳ�", "PC��", "��ǻ��", "��Ʈ��", "�ΰ���", "����", "����ȭȣ", "KTX", "�б�", "��", "ġŲ", "«��", "����", "������", "����", "��", "�Ƴʰ�", "���", "Į", "���̿���", 
			"��Ʃ��", "����", "����", "����", "�Ȱ�", "���۶�", "���", "��", "����Ⱓ", "�Ŷ��", "������", "������", "����", "����", "�ڵ���", "������", "����", "����", "����", "����", "����", "�ٴ�", "��", "ķ��", "���", "��������", "����", "����", "��ġ", "����ũ", "����", "���찳", "������", "����", "�ڵ���", "USB", "����", "Ű����", "���콺", "�����", "������", "�޷�", "�����", "����", "Ʈ��", "��ħ", "MBTI", "�ֱ���", "����", "�౸��", "�Ź�", "�ｺ��", "��ġ", "����", "��̴��Ӵ�", "�ٶ�", "�����", "���̷α�", "������ġ", "Ŀ��"}; // �ܾ� ���
	
	String answer; // ������ �� ����

	// userName-ObjectOutputStream ���� Ŭ���̾�Ʈ OutputStream �������
	HashMap<String, ObjectOutputStream> clientOutputStreams = new HashMap<String, ObjectOutputStream>();
	ArrayList<String> users = new ArrayList<>(); // ��� userName�� �������
	String turnUser; // ���ʰ� �Ǵ� user

	// ������ Ŭ���̾�Ʈ�� 2�� �̻��� ��, 10�� ī��Ʈ���ִ� Timer
	TimerClass timerClass = new TimerClass();
	Timer timer = new Timer(1000, timerClass);

	// Server�� PORT ��ȣ
	final private int PORT = 9999;

	public static void main(String[] args) {
		Server server = new Server();
		server.go();
	}

	public void go() {
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);

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

					// �޽���
					if (type == Message.MsgType.CLIENT_MSG) {
						// ������ ���� ���
						if (message.getMessage().equals(answer)) {
							// turnUser�� �����ڷ� �����ϰ�, ������ �������� ����
							turnUser = message.getSender();
							answer = words[(int) (Math.random() * (words.length))];

							broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "",
									message.getSender() + "���� ������ ���߼̽��ϴ�!"));

							// ���ο� ������ ����
							broadCastMessage(new Message(Message.MsgType.GAME_START, "", turnUser, answer));
						}

						// �Ϲ����� �޽���
						broadCastMessage(
								new Message(Message.MsgType.SERVER_MSG, message.getSender(), "", message.getMessage()));
					}
					// �α��� ��û
					else if (type == Message.MsgType.LOGIN) {
						handleLogin(message.getSender(), writer);
					}
					// �α׾ƿ� ��û
					else if (type == Message.MsgType.LOGOUT) {
						handleLogout(message.getSender());
						users.remove(message.getSender());

						writer.close();
						reader.close();
						socket.close();
						return;
					}
					// �׸��� ��û
					else if (type == Message.MsgType.DRAW) {
						broadCastMessage(message);
					}
					// ��� ����� ��û
					else if (type == Message.MsgType.CLEAR) {
						broadCastMessage(message);
					} else if (type == Message.MsgType.NO_ACT) {
						continue;
					} else {
						throw new Exception("Server: Ŭ���̾�Ʈ���� �� �� ���� �޽��� ����");
					}
				}
			}
			// ����� Ŭ���̾�Ʈ ���� �� ���ܹ߻�
			catch (Exception e) {
				System.out.println(e);
				System.out.println("Server: Ŭ���̾�Ʈ ���� ����");
			}
		}
	}

	// �α��� ó�� �޼ҵ�
	private synchronized void handleLogin(String user, ObjectOutputStream writer) {
		// �̹� ������ �̸��� ����ڰ� �ִ� ���
		try {
			if (clientOutputStreams.containsKey(user)) {
				writer.writeObject(new Message(Message.MsgType.LOGIN_FAILURE, "", "", "����� �̹� ����"));
				return;
			}
		} catch (Exception e) {
			System.out.println("Server: �������� �۽� �� �̻� �߻�");
			e.printStackTrace();
		}

		// �������� ���
		clientOutputStreams.put(user, writer);
		users.add(user);
		
		broadCastMessage(new Message(Message.MsgType.LOGIN_LIST, "", "", makeClientList()));
		broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", user + "���� �����ϼ̽��ϴ�."));

		// ������ Ŭ���̾�Ʈ ���� 2�� �̻��� ��� 10�� Timer ����
		// ���ο� Ŭ���̾�Ʈ�� ���� ��� ó������ count
		if (clientOutputStreams.size() > 1) {
			timer.stop();
			timerClass.time = 0;

			timer.start();
			broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", "10�� �� ������ �����մϴ�."));
		}
	}

	// �α׾ƿ� ó�� �޼ҵ�
	private synchronized void handleLogout(String user) {
		clientOutputStreams.remove(user);
		users.remove(user);
		
		broadCastMessage(new Message(Message.MsgType.LOGIN_LIST, "", "", makeClientList()));
		broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", user + "���� �����̽��ϴ�."));
	}

	// ��� Ŭ���̾�Ʈ���� �۽��ϴ� �޼ҵ�
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
				System.out.println("Server: �������� �۽� �� �̻� �߻�");
				e.printStackTrace();
			}
		}
	}

	private String makeClientList() {
		Set<String> s = clientOutputStreams.keySet(); // ���� ��ϵ� ����ڵ��� ����
		Iterator<String> it = s.iterator();
		String userList = "";

		while (it.hasNext()) {
			userList += it.next() + "/"; // ��Ʈ�� ����Ʈ�� �߰��ϰ� ������ ���
		}

		return userList;
	}

	// ���� ���� �޼ҵ�
	private void gameStart() {
		turnUser = users.get((int) (Math.random() * (users.size())));
		answer = words[(int) (Math.random() * (words.length))];
		System.out.println(words.length);

		broadCastMessage(new Message(Message.MsgType.GAME_START, "", turnUser, answer));
		broadCastMessage(
				new Message(Message.MsgType.SERVER_MSG, "Server", "", "������ �����մϴ�. ù ��° ������ \"" + turnUser + "\"�Դϴ�."));
	}

	// ���� ���� �� ī�����ϴ� Ŭ����
	class TimerClass implements ActionListener {
		int time = 0;

		@Override
		public void actionPerformed(ActionEvent e) {
			time++;

			if (time >= 10) {
				time = 0;
				timer.stop();
				gameStart();
			}
		}
	}
}
