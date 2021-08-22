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
	String[] words = { "바나나", "원숭이", "사람", "책", "기린", "하마", "얼굴", "박명수", "강아지", "게임", "제철소", "우유", "때", "씨앗", "소", "여자", "남자", "아빠", "엄마", "가족", "할머니", "할아버지", "말", "고양이", "닭", "시대", "역사", "퍼센트", "운동", "학교", "대통령", "정부", "돈", "친구", "마음", "순천향대학교", "몸", "신창", "괴물", "노래", "다리", "한강", "오피스텔", "방구석", "라면", "거미", "이야기", "메뚜기", "양궁", "김제덕", "코로나", "유재석", "김치", "먹방", "피아노", "PC방", "컴퓨터", "노트북", "턱걸이", "기차", "무궁화호", "KTX", "학교", "달", "치킨", "짬뽕", "우주", "아이유", "레옹", "뱀", "맥너겟", "돈까스", "칼", "싸이월드", 
			"유튜브", "교복", "순대", "새우", "안경", "선글라스", "잠옷", "술", "시험기간", "컵라면", "편의점", "볶음밥", "군대", "종강", "자동차", "자전거", "대파", "양파", "마늘", "삼겹살", "돼지", "바다", "산", "캠핑", "펜션", "테이저건", "맥주", "소주", "망치", "마스크", "휴지", "지우개", "형광펜", "연필", "핸드폰", "USB", "필통", "키보드", "마우스", "모니터", "에어팟", "달력", "비빔면", "낮잠", "트름", "군침", "MBTI", "애국가", "좀비", "콧구멍", "신발", "헬스장", "참치", "고등어", "쇼미더머니", "바람", "김범수", "브이로그", "샌드위치", "커피"}; // 단어 목록
	
	String answer; // 정답이 될 변수

	// userName-ObjectOutputStream 쌍의 클라이언트 OutputStream 저장공간
	HashMap<String, ObjectOutputStream> clientOutputStreams = new HashMap<String, ObjectOutputStream>();
	ArrayList<String> users = new ArrayList<>(); // 모든 userName의 저장공간
	String turnUser; // 차례가 되는 user

	// 접속한 클라이언트가 2명 이상일 때, 10초 카운트해주는 Timer
	TimerClass timerClass = new TimerClass();
	Timer timer = new Timer(1000, timerClass);

	// Server의 PORT 번호
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

					// 메시지
					if (type == Message.MsgType.CLIENT_MSG) {
						// 정답을 맞춘 경우
						if (message.getMessage().equals(answer)) {
							// turnUser을 정답자로 설정하고, 정답은 랜덤으로 변경
							turnUser = message.getSender();
							answer = words[(int) (Math.random() * (words.length))];

							broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "",
									message.getSender() + "님이 정답을 맞추셨습니다!"));

							// 새로운 게임이 시작
							broadCastMessage(new Message(Message.MsgType.GAME_START, "", turnUser, answer));
						}

						// 일반적인 메시지
						broadCastMessage(
								new Message(Message.MsgType.SERVER_MSG, message.getSender(), "", message.getMessage()));
					}
					// 로그인 요청
					else if (type == Message.MsgType.LOGIN) {
						handleLogin(message.getSender(), writer);
					}
					// 로그아웃 요청
					else if (type == Message.MsgType.LOGOUT) {
						handleLogout(message.getSender());
						users.remove(message.getSender());

						writer.close();
						reader.close();
						socket.close();
						return;
					}
					// 그리기 요청
					else if (type == Message.MsgType.DRAW) {
						broadCastMessage(message);
					}
					// 모두 지우기 요청
					else if (type == Message.MsgType.CLEAR) {
						broadCastMessage(message);
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

	// 로그인 처리 메소드
	private synchronized void handleLogin(String user, ObjectOutputStream writer) {
		// 이미 동일한 이름의 사용자가 있는 경우
		try {
			if (clientOutputStreams.containsKey(user)) {
				writer.writeObject(new Message(Message.MsgType.LOGIN_FAILURE, "", "", "사용자 이미 있음"));
				return;
			}
		} catch (Exception e) {
			System.out.println("Server: 서버에서 송신 중 이상 발생");
			e.printStackTrace();
		}

		// 정상적인 경우
		clientOutputStreams.put(user, writer);
		users.add(user);
		
		broadCastMessage(new Message(Message.MsgType.LOGIN_LIST, "", "", makeClientList()));
		broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", user + "님이 접속하셨습니다."));

		// 접속한 클라이언트 수가 2명 이상인 경우 10초 Timer 시작
		// 새로운 클라이언트가 들어올 경우 처음부터 count
		if (clientOutputStreams.size() > 1) {
			timer.stop();
			timerClass.time = 0;

			timer.start();
			broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", "10초 후 게임을 시작합니다."));
		}
	}

	// 로그아웃 처리 메소드
	private synchronized void handleLogout(String user) {
		clientOutputStreams.remove(user);
		users.remove(user);
		
		broadCastMessage(new Message(Message.MsgType.LOGIN_LIST, "", "", makeClientList()));
		broadCastMessage(new Message(Message.MsgType.SERVER_MSG, "Server", "", user + "님이 나가셨습니다."));
	}

	// 모든 클라이언트에게 송신하는 메소드
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

	// 게임 시작 메소드
	private void gameStart() {
		turnUser = users.get((int) (Math.random() * (users.size())));
		answer = words[(int) (Math.random() * (words.length))];
		System.out.println(words.length);

		broadCastMessage(new Message(Message.MsgType.GAME_START, "", turnUser, answer));
		broadCastMessage(
				new Message(Message.MsgType.SERVER_MSG, "Server", "", "게임을 시작합니다. 첫 번째 순서는 \"" + turnUser + "\"입니다."));
	}

	// 게임 시작 전 카운팅하는 클래스
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
