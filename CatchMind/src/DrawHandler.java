import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DrawHandler implements Runnable {
	Socket socket; // Ŭ���̾�Ʈ ����� ����
	ObjectInputStream reader; // ���ſ� ��Ʈ��
	ObjectOutputStream writer; // �۽ſ� ��Ʈ��
	
	public DrawHandler(Socket clientSocket) {
		
	}

	@Override
	public void run() {
		
	}
}
