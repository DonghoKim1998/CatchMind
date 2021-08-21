import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DrawHandler implements Runnable {
	Socket socket; // 클라이언트 연결용 소켓
	ObjectInputStream reader; // 수신용 스트림
	ObjectOutputStream writer; // 송신용 스트림
	
	public DrawHandler(Socket clientSocket) {
		
	}

	@Override
	public void run() {
		
	}
}
