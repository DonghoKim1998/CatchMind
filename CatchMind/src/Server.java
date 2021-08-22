import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Timer;

public class Server {
	TimerClass timerClass = new TimerClass();
	Timer timer = new Timer(1000, timerClass);

	// userName-ObjectOutputStream 쌍의 클라이언트 OutputStream 저장공간
	HashMap<String, ObjectOutputStream> clientOutputStreams = new HashMap<String, ObjectOutputStream>();

	public static void main(String[] args) {
		Server server = new Server();
		server.go();
	}

	public void go() {
		try {
			ServerSocket serverSocket = new ServerSocket(9999);

			while (true) {
				Socket clientSocket = serverSocket.accept();

				Thread thread = new Thread(new ClientHandler(clientSocket));

				thread.start();

				System.out.println("Server: 클라이언트 연결 성공");
			}
		} catch (Exception e) {
			System.out.println("Server: 클라이언트 연결 중 이상 발생");
			e.printStackTrace();
		}
	}

	private class ClientHandler implements Runnable {
		Socket socket; // 클라이언트 연결용 소켓
		ObjectInputStream reader; // 수신용 스트림
		ObjectOutputStream writer; // 송신용 스트림

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

					// 클라이언트로부터 메시지 수신
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
					} else if (type == Message.MsgType.DRAW) {
						broadCastMessage(message);
						System.out.println("Server");
					} else if (type == Message.MsgType.NO_ACT) {
						continue;
					} else {
						throw new Exception("Server: 클라이언트에서 알 수 없는 메시지 도착");
					}
				}
			}
			// 연결된 클라이언트 종료 시 예외발생
			catch (Exception e) {
				System.out.println(e);
				System.out.println("Server: 클라이언트 접속 종료");
			}
		}
	}

	private synchronized void handleLogin(String user, ObjectOutputStream writer) {
		try {
			// 이미 동일한 이름의 사용자가 있는 경우
			if (clientOutputStreams.containsKey(user)) {
				writer.writeObject(new Message(Message.MsgType.LOGIN_FAILURE, "", "", "사용자 이미 있음"));
				return;
			}
		} catch (Exception e) {
			System.out.println("Server: 서버에서 송신 중 이상 발생");
			e.printStackTrace();
		}

		// 해쉬테이블에 userName-writer 쌍을 추가하고
		clientOutputStreams.put(user, writer);
		// 새로운 로그인 리스트를 전체에게 보내 줌
		broadCastMessage(new Message(Message.MsgType.LOGIN_LIST, "", "", makeClientList()));
		broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", user + "님이 접속하셨습니다."));

		if (clientOutputStreams.size() > 1) {
			timer.stop();
			timerClass.time = 0;

			timer.start();
			broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", "10초 후 게임을 시작합니다."));
		}
	}

	private synchronized void handleLogout(String user) {
		clientOutputStreams.remove(user);
		broadCastMessage(new Message(Message.MsgType.LOGIN_LIST, "", "", makeClientList()));
		broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", user + "님이 나가셨습니다."));
	}

	private synchronized void handleMessage(String sender, String receiver, String msg) {
		// 모두에게 보내는 경우
		if (receiver.equals(Message.ALL)) {
			broadCastMessage(new Message(Message.MsgType.SERVER_MSG, sender, "", msg));
			return;
		}

		// 특정 대상에게 보내는 경우
		ObjectOutputStream write = clientOutputStreams.get(receiver);
		try {
			write.writeObject(new Message(Message.MsgType.SERVER_MSG, sender, "", msg));
		} catch (Exception e) {
			System.out.println("Server: 서버에서 송신 중 이상 발생");
			e.printStackTrace();
		}
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
				System.out.println("Server: 서버에서 송신 중 이상 발생");
				e.printStackTrace();
			}
		}
	}

	private String makeClientList() {
		Set<String> s = clientOutputStreams.keySet(); // 먼저 등록된 사용자들을 추출
		Iterator<String> it = s.iterator();
		String userList = "";

		while (it.hasNext()) {
			userList += it.next() + "/"; // 스트링 리스트에 추가하고 구분자 명시
		}

		return userList;
	}

	private void gameStart() {
		broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", "게임을 시작합니다."));
	}

	// 게임 시작 전 카운팅하는 클래스
	class TimerClass implements ActionListener {
		int time = 0;

		@Override
		public void actionPerformed(ActionEvent e) {
			time++;
			System.out.println(time);

			if (time >= 10) {
				time = 0;
				timer.stop();
				gameStart();
			}
		}
	}
}
