import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

public class Client {
	JFrame frame;
	JTextArea chatLog, chat;
	JScrollPane chatLogScroller, chatScroller, userListScroller;
	JList userList;
	JButton logButton;

	String userName;
	String frameTitle = "CatchMind";

	Game game;
	boolean turn = false;

	Socket socket;
	ObjectInputStream reader; // 수신용 스트림
	ObjectOutputStream writer; // 송신용 스트림

	public static void main(String[] args) {
		Client client = new Client();
		client.go();
	}

	private void go() {
		frame = new JFrame();
		game = new Game();

		// 채팅 로그
		chatLog = new JTextArea(25, 10);
		chatLog.setLineWrap(true);
		chatLog.setWrapStyleWord(true);
		chatLog.setEditable(false);
		chatLogScroller = new JScrollPane(chatLog);
		chatLogScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		chatLogScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// 참가자 목록
		String[] list = { Message.ALL };
		userList = new JList(list);
		userList.setSelectedIndex(0);
		userListScroller = new JScrollPane(userList);
		userListScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		userListScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		userList.setVisibleRowCount(5);
		userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userList.setFixedCellWidth(100);

		// 메시지 입력 창2
		chat = new JTextArea(15, 25);
		chat.addKeyListener(new EnterKeyListener());
		chat.setLineWrap(true);
		chat.setWrapStyleWord(true);
		chat.setEditable(true);
		chatScroller = new JScrollPane(chat);
		chatScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		chatScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// 패널, 패널 배치
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

		JPanel statePanel = new JPanel();
		statePanel.setLayout(new BoxLayout(statePanel, BoxLayout.Y_AXIS));
		statePanel.setPreferredSize(new Dimension(400, 600));

		JPanel stateNorthPanel = new JPanel();
		stateNorthPanel.setLayout(new BoxLayout(stateNorthPanel, BoxLayout.PAGE_AXIS));
		stateNorthPanel.add(chatLogScroller);

		JPanel userPanel = new JPanel();
		userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
		userPanel.add(new JLabel("  참여자 목록"));
		userPanel.add(userListScroller);
		JPanel chatPanel = new JPanel();
		chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
		chatPanel.add(new JLabel("                메시지 입력"));
		chatPanel.add(chatScroller);

		JPanel stateSouthPanel = new JPanel();
		stateSouthPanel.setLayout(new BoxLayout(stateSouthPanel, BoxLayout.X_AXIS));

		stateSouthPanel.add(userPanel);
		stateSouthPanel.add(chatPanel);

		statePanel.add(stateNorthPanel);
		statePanel.add(stateSouthPanel);

		mainPanel.add(game);
		mainPanel.add(statePanel);
		frame.getContentPane().add(mainPanel);

		// 서버와 통신 시도
		setUpNetWorking();
		Thread readerThread = new Thread(new MessageReader());
		readerThread.start();

		// 프레임 설정
		frame.addWindowListener(new CloseListener());

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(0, 0, 1200, 900);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void setUpNetWorking() {
		try {
			socket = new Socket("127.0.0.1", 9999);
			reader = new ObjectInputStream(socket.getInputStream());
			writer = new ObjectOutputStream(socket.getOutputStream());

			login();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "서버 접속에 실패하였습니다. 접속을 종료합니다.");
			e.printStackTrace();
			frame.dispose();
		}
	}

	private void login() {
		userName = JOptionPane.showInputDialog("사용자 이름을 입력하세요");

		try {
			writer.writeObject(new Message(Message.MsgType.LOGIN, userName, "", ""));
			writer.flush();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "로그인 중 서버접속에 문제가 발생했습니다.");
			e.printStackTrace();
		}

		frame.setTitle(frameTitle + "(" + userName + ")");
	}

	public class MessageReader implements Runnable {
		public void run() {
			Message message;
			Message.MsgType type;

			try {
				while (true) {
					message = (Message) reader.readObject();
					type = message.getType();

					// 메시지 받은 경우
					if (type == Message.MsgType.SERVER_MSG) {
						// 보낸 사람이 나인 경우
						if (message.getSender().equals(userName))
							continue;
						// 서버 메시지인 경우
						if (message.getSender().equals("Server"))
							chatLog.append("[Server]: " + message.getMessage() + "\n");
						else
							chatLog.append(message.getSender() + ": " + message.getMessage() + "\n");
					} else if (type == Message.MsgType.LOGIN_FAILURE) {
						JOptionPane.showMessageDialog(null, "이미 있는 아이디입니다. 다시 로그인하세요");
						login();
					}
					// 유저리스트를 추출해서 userList에 삽입
					// 본인은 ""로 만들어 정렬 후 리스트 맨 앞으로 오게 함
					else if (type == Message.MsgType.LOGIN_LIST) {
						String[] users = message.getMessage().split("/");

						for (int i = 0; i < users.length; i++) {
							if (userName.equals(users[i]))
								users[i] = "";
						}

						users = sortUsers(users);
						users[0] = Message.ALL;
						userList.setListData(users);
						frame.repaint();
					} else if (type == Message.MsgType.NO_ACT) {
					} else
						throw new Exception("서버에서 알 수 없는 메시지 도착");
				}
			} catch (Exception e) {
				System.out.println("클라이언트 스레드 종료");
			}
		}
	}

	// 주어진 String 배열을 정렬한 새로운 배열 리턴
	private String[] sortUsers(String[] users) {
		String[] outList = new String[users.length];
		ArrayList<String> list = new ArrayList<String>();

		for (String s : users)
			list.add(s);

		Collections.sort(list); // Collections.sort를 사용해 한방에 정렬
		for (int i = 0; i < users.length; i++)
			outList[i] = list.get(i);

		return outList;
	}

	public class EnterKeyListener implements KeyListener {
		boolean pressCheck = false;

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SHIFT)
				pressCheck = true;

			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				if (chat.getText() == "")
					return;

				if (pressCheck) {
					String str = chat.getText() + "\r\n";
					chat.setText(str);
					pressCheck = false;
				} else {
					e.consume();
					pressCheck = false;

					String target = (String) userList.getSelectedValue();
					if (target == null) {
						JOptionPane.showMessageDialog(null, "송신할 대상을 선택한 후 메시지를 보내세요");
						return;
					}

					try {
						chatLog.append(userName + ": " + chat.getText() + "\n"); // 나의 메시지 창에 보이기
						chatLog.setSelectionStart(chatLog.getText().length());
						chatLogScroller.getVerticalScrollBar()
								.setValue(chatLogScroller.getVerticalScrollBar().getMaximum());
						writer.writeObject(new Message(Message.MsgType.CLIENT_MSG, userName, target, chat.getText()));
						writer.flush();
						chat.setText("");
						chat.requestFocus();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, "메시지 전송중 문제가 발생하였습니다.");
						ex.printStackTrace();
					}
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
				pressCheck = false;
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}

	private class CloseListener implements WindowListener {
		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			JOptionPane.showMessageDialog(null, "게임을 종료합니다.");

			try {
				writer.writeObject(new Message(Message.MsgType.LOGOUT, userName, "", ""));
				writer.flush();

				writer.close();
				reader.close();
				socket.close();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "로그아웃 중 서버와의 문제가 발생하였습니다. 강제 종료합니다.");
				ex.printStackTrace();
			} finally {
				System.exit(1);
			}
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}

	}

}
