package application;

import java.io.IOException;

import java.io.InputStream; 
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	Socket socket; // 소켓이 있어야 컴퓨터가 네트워크 상에서 통신을 할수가있다.

	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}

	// 클라이언트로부터 메시지를 전달 받는 메소드이다
	public void receive() {
		Runnable thread = new Runnable() {// 일반적으로 하나의 쓰레드를 만들때는 런어블을 많이 사용 또한 런어블라이브러리는
											// 내브적으로 반드시 run() 함수를 반드시 가지고있어야한다. 즉 하나의 쓰레드가 어떠한 모듈로써 동작하는지 run안에서 설정을
											// 해야한다

			@Override
			public void run() {
				try {
					while (true) { //반복적으로 클라이언트로부터 어떤 내용을 전달받을수있도록 하기위함
						InputStream in = socket.getInputStream();  //어떤 내용을 전달받을수있도록 input객체를 사용
						byte[] buffer = new byte[512]; //한번에 512바이트만큼 전달받을 수 있도록 한다.
						int length = in.read(buffer); //클라이언트로부터 전달받은 내용을 버퍼에 담아준다., length는 담긴 메시지의 크기를 의미
						
						while(length == -1) throw new IOException(); // 메시지를 읽다가 오류가 발생하면 알려주는것이다.
						System.out.println("[메시지 수신성공]" + socket.getRemoteSocketAddress()
						+" : "+ Thread.currentThread().getName());   //getRemoteSocketAddress는 현재 접속한 클라이언트의 ip주소와 같은 쥬소정보출력 , currentThread().getName()는 쓰레드의 고유한 정보 출력
						String message = new String(buffer, 0 , length, "UTF-8");
						for(Client client : Main.clients) { //전달받은메시지를 다른 클라이언트에게 전달 한다.
							client.send(message);
							
						}
						try {
							
						}catch(Exception e) {
							try {
								System.out.println("메시지 수신 오류"+socket.getRemoteSocketAddress() + " : " + Thread.currentThread().getName() );
							}catch(Exception e2) {
								e2.printStackTrace();
							}
							
						}
						
						
						
							
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		};
		Main.threadPool.equals(thread); // 스레풀에 만들어진 하나의 쓰레드를 등록시켜주는것이다.,만들어진 스레드를 안정적으로 관리하기위하여 쓰레드풀을 사용
	}

	//
	public void send(String message) {
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte [] buffer = message.getBytes("utf-8");
					out.write(buffer);  //버퍼에담긴내용을 서버에서 클라이언트로 전송
					out.flush(); //반드시해야한다 그래야 여기까지 성공적으로 전송을 했다는것을 알려줄수잇다.
				}catch(Exception e) {
					try {
						System.out.println("[메시지 송신 오류]"
								+socket.getRemoteSocketAddress()
								+ " : " + Thread.currentThread().getName());
						Main.clients.remove(Client.this); //Main에 있는 클라이언트의 정보를 담는 배열에서  현재 존재하는 클라이언트를 제거한다 즉 오류가 발생해서 해당 클라이언트가 서버로의 접속이 끊겨서 당연히 서버안에서도  해당 클라이언트가 접속이 끊겼따는것을  처리하게하는것이다.
						socket.close(); //오류가 생긴 클라이언트의 소켓을 닫아버린다
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
			
		};
		Main.threadPool.equals(thread); // 스레풀에 만들어진 하나의 쓰레드를 등록시켜주는것이다.,만들어진 스레드를 안정적으로 관리하기위하여 쓰레드풀을 사용
		
	}
}
