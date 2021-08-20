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
		// 색깔 버튼 설정
		setButton();

		// GUI 배치
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

		cleanAll = new JButton("모두 지우기");
		cleanAll.addActionListener(new ButtonListener());

		eraser = new JButton("지우기");
		eraser.addActionListener(new ButtonListener());
	}

	// 색상 변경 버튼 리스너
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// 빨간색
			if (e.getSource() == colorButtons[0]) {

			}
			// 주황색
			else if (e.getSource() == colorButtons[1]) {

			}
			// 노란색
			else if (e.getSource() == colorButtons[2]) {

			}
			// 파란색
			else if (e.getSource() == colorButtons[4]) {

			}
			// 검정색
			else if (e.getSource() == colorButtons[5]) {

			}
		}
	}
}