package RemoteBT;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WifiThread implements Runnable {
	ServerSocket socket = null;
	Socket clientSocket = null;
	DataInputStream DIS = null;
	public WifiThread() {
		System.out.println("WiFi server started...");
	}
	
	@Override
	public void run() {
		try {
			socket = new ServerSocket(1755);
			clientSocket = socket.accept();
		} catch (IOException e) { 
			e.printStackTrace();
		}
		//while(true){
			try {
			DIS = new DataInputStream(clientSocket.getInputStream());
			while(true){
				String msg_received = DIS.readUTF();
				System.out.println(msg_received);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				clientSocket.close();
				socket.close();
			} catch (IOException e) {
				// 
				e.printStackTrace();
			}
			
		}
		//}
		
	}
}
