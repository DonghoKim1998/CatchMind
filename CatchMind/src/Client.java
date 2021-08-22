import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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
	File bgm = new File("src/sound/bgm.wav");
	Clip bgmClip;

	JFrame frame;
	JTextArea chatLog, chat;
	JScrollPane chatLogScroller, chatScroller, userListScroller;
	JList userList;
	JButton logButton;

	String userName;
	String frameTitle = "CatchMind";
	String word;

	Game game;

	Socket socket;
	ObjectInputStream reader; // ���ſ뽺Ʈ��
	ObjectOutputStream writer; // �۽ſ� ��Ʈ��

	final private String URL = "220.69.203.88";
	final private int PORT = 9999;

	public static void main(String[] args) {
		Client client = new Client();
		client.go();
	}

	private void go() {
		frame = new JFrame();
		game = new Game();

		// ä�� �α�
		chatLog = new JTextArea(25, 10);
		chatLog.setLineWrap(true);
		chatLog.setWrapStyleWord(true);
		chatLog.setEditable(false);
		chatLogScroller = new JScrollPane(chatLog);
		chatLogScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		chatLogScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// ������ ���
		String[] list = { Message.ALL };
		userList = new JList(list);
		userList.setSelectedIndex(0);
		userListScroller = new JScrollPane(userList);
		userListScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		userListScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		userList.setVisibleRowCount(5);
		userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userList.setFixedCellWidth(100);

		// �޽��� �Է� â2
		chat = new JTextArea(15, 25);
		chat.addKeyListener(new EnterKeyListener());
		chat.setLineWrap(true);
		chat.setWrapStyleWord(true);
		chat.setEditable(true);
		chatScroller = new JScrollPane(chat);
		chatScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		chatScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// �г�, �г� ��ġ
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
		userPanel.add(new JLabel("  ������ ���"));
		userPanel.add(userListScroller);
		JPanel chatPanel = new JPanel();
		chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
		chatPanel.add(new JLabel("                �޽��� �Է�"));
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

		// ������ ����
		setUpNetWorking();
		Thread readerThread = new Thread(new MessageReader());
		readerThread.start();

		// sound �ʱ�ȭ �� bgm ���
		try {
			bgmClip = AudioSystem.getClip();
			bgmClip.open(AudioSystem.getAudioInputStream(bgm));
			bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
			bgmClip.start();
		} catch (Exception e) {
		}

		// ������ ����
		frame.addWindowListener(new CloseListener());
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(0, 0, 1200, 900);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	// ���� ���� �޼ҵ�
	private void setUpNetWorking() {
		try {
			socket = new Socket(URL, PORT);
			reader = new ObjectInputStream(socket.getInputStream());
			writer = new ObjectOutputStream(socket.getOutputStream());
			game.reader = reader;
			game.writer = writer;

			login();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "���� ���ӿ� �����Ͽ����ϴ�. ������ �����մϴ�.");
			e.printStackTrace();
			frame.dispose();
		}
	}

	// �α��� ��û �޼ҵ�
	private void login() {
		userName = JOptionPane.showInputDialog(null, "", "Login", JOptionPane.PLAIN_MESSAGE);

		try {
			writer.writeObject(new Message(Message.MsgType.LOGIN, userName, "", ""));
			writer.flush();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "�α��� �� �������ӿ� ������ �߻��߽��ϴ�.");
			e.printStackTrace();
		}

		frame.setTitle(frameTitle + " (Login: " + userName + ")");
	}

	// �����κ��� ����
	public class MessageReader implements Runnable {
		public void run() {
			Message message;
			Message.MsgType type;

			try {
				while (true) {
					message = (Message) reader.readObject();
					type = message.getType();

					// �޽��� ���� ���
					if (type == Message.MsgType.SERVER_MSG) {
						// ���� ����� ���� ���
						if (message.getSender().equals(userName))
							continue;
						// ���� �޽����� ���
						else if (message.getSender().equals("Server"))
							chatLog.append("[Server]: " + message.getMessage() + "\n");
						else
							chatLog.append(message.getSender() + ": " + message.getMessage() + "\n");
					}
					// �α��� ������ ���
					else if (type == Message.MsgType.LOGIN_FAILURE) {
						JOptionPane.showMessageDialog(null, "�̹� �ִ� ���̵��Դϴ�. �ٽ� �α����ϼ���");
						login();
					}
					// ��� ����� ��û�� ���
					else if (type == Message.MsgType.CLEAR) {
						game.cleanAll();
					}
					// �α��� ����Ʈ ��û�� ���
					else if (type == Message.MsgType.LOGIN_LIST) {
						String[] users = message.getMessage().split("/");

						for (int i = 0; i < users.length; i++) {
							if (userName.equals(users[i]))
								users[i] = "";
						}

						users = sortUsers(users);
						users[0] = Message.ALL;
						userList.setListData(users);
					}
					// �׸��� ��û�� ���
					else if (type == Message.MsgType.DRAW) {
						game.setStartPoint(message.getStartPoint());
						game.setEndPoint(message.getEndPoint());
						game.setColor(message.getColor());
						game.thicknessInt = message.getThickness();

						game.draw(game.g);

						game.setStartPoint(game.getEndPoint());
					}
					// ���� ����
					else if (type == Message.MsgType.GAME_START) {
						game.cleanAll();

						// ���� ���ʰ� �ƴ� ���
						if (!message.getReceiver().equals(userName)) {
							System.out.println(message.getReceiver());
							game.isActiveDraw = false;
							game.isActiveButton = false;
							chat.setEditable(true);
						}
						// ���� ������ ���
						else {
							System.out.println(message.getReceiver());
							game.isActiveDraw = true;
							game.isActiveButton = true;
							chat.setEditable(false);
							chatLog.append(message.getMessage() + "�� �׷��ּ���!\n");
						}
					}
					// �ƹ��͵� �ƴ� ���
					else if (type == Message.MsgType.NO_ACT) {
					}
					// �� �� ���
					else
						throw new Exception("�������� �� �� ���� �޽��� ����");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Ŭ���̾�Ʈ ������ ����");
			}
		}
	}

	// �־��� String �迭�� ������ ���ο� �迭 ����
	private String[] sortUsers(String[] users) {
		String[] outList = new String[users.length];
		ArrayList<String> list = new ArrayList<String>();

		for (String s : users)
			list.add(s);

		Collections.sort(list); // Collections.sort�� ����� �ѹ濡 ����
		for (int i = 0; i < users.length; i++)
			outList[i] = list.get(i);

		return outList;
	}

	// Start of Listeners
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

					try {
						// �� ä�� �ø���
						chatLog.append(userName + ": " + chat.getText() + "\n");
						chatLog.setSelectionStart(chatLog.getText().length());
						chatLogScroller.getVerticalScrollBar()
								.setValue(chatLogScroller.getVerticalScrollBar().getMaximum());

						writer.writeObject(new Message(Message.MsgType.CLIENT_MSG, userName, "", chat.getText()));
						writer.flush();

						chat.setText("");
						chat.requestFocus();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, "�޽��� ������ ������ �߻��Ͽ����ϴ�.");
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
	} // End of KeyListener

	private class CloseListener implements WindowListener {
		@Override
		public void windowClosing(WindowEvent e) {
			JOptionPane.showMessageDialog(null, "������ �����մϴ�.");

			try {
				writer.writeObject(new Message(Message.MsgType.LOGOUT, userName, "", ""));
				writer.flush();

				writer.close();
				reader.close();
				socket.close();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "�α׾ƿ� �� �������� ������ �߻��Ͽ����ϴ�. ���� �����մϴ�.");
				ex.printStackTrace();
			} finally {
				System.exit(1);
			}
		}

		@Override
		public void windowOpened(WindowEvent e) {
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
	} // End of WindowListener
		// End of Listeners
}
