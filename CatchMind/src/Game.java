import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.*;

public class Game extends JPanel {
	JButton[] colorButtons;
	JButton cleanAll, eraser;

	ObjectInputStream reader;
	ObjectOutputStream writer;

	public Game() {
		// ���� ��ư ����
		setButton();

		// GUI ��ġ
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel drawPanel = new JPanel();
		drawPanel.setBackground(Color.white);
		drawPanel.setPreferredSize(new Dimension(300, 600));

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBackground(Color.black);

		JPanel colorPanel = new JPanel();
		colorPanel.setBackground(Color.white);
		colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.X_AXIS));
		for (int i = 0; i < colorButtons.length; i++) {
			colorPanel.add(colorButtons[i]);
			colorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		}
		colorPanel.add(cleanAll);
		colorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		colorPanel.add(eraser);
		colorPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		bottomPanel.add(colorPanel);

		this.add(drawPanel);
		this.add(colorPanel);

		setPreferredSize(new Dimension(770, 900));
	}

	private void setButton() {
		colorButtons = new JButton[5];
		for (int i = 0; i < colorButtons.length; i++) {
			colorButtons[i] = new JButton();
			colorButtons[i].setPreferredSize(new Dimension(10, 50));
			colorButtons[i].addActionListener(new ButtonListener());
		}
		colorButtons[0].setBackground(Color.red);
		colorButtons[1].setBackground(Color.orange);
		colorButtons[2].setBackground(Color.yellow);
		colorButtons[3].setBackground(Color.blue);
		colorButtons[4].setBackground(Color.black);

		cleanAll = new JButton("��� �����");
		cleanAll.addActionListener(new ButtonListener());

		eraser = new JButton("�����");
		eraser.addActionListener(new ButtonListener());
	}

	// ���� ���� ��ư ������
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// ������
			if (e.getSource() == colorButtons[0]) {

			}
			// ��Ȳ��
			else if (e.getSource() == colorButtons[1]) {

			}
			// �����
			else if (e.getSource() == colorButtons[2]) {

			}
			// �Ķ���
			else if (e.getSource() == colorButtons[4]) {

			}
			// ������
			else if (e.getSource() == colorButtons[5]) {

			}
		}
	}
}