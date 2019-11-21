package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application { 
	public static ExecutorService threadPool;  //다양한 클리이언트가 접속했을때  쓰레드들을 효과적으로 관리하기 위하ㅐ서 쓰레드풀을 만든다
	//ExecutorService는 여러개의 쓰레드를 효율적으로 관리하기 위해 사용하는 라이브러리이다. 쓰레드 플로
	//쓰레드를 처리하게되면 기본적으로 쓰레드의 수에 제한을 두기때문에 갑작스러운 클라이언트의 수가 폭등하더라도
	//쓰레드의 숫자에는 제한이 있기때문에 서버의 성능저하를 막을 수 있다.
	

	public static Vector<Client> clients =new Vector<Client>();//접속한 클라이언트를 관리한다.
	ServerSocket serverSocket;
	
	//서버를 구동시켜서 클라이언트의 연결을 기다리는 메소드입니다.
	public void startServer(String IP,int port) {  //어떠한 ip로 , 어떠한 포트를 열어서 클라이언트와 통신을 할것인지를 적는것이다.
		try {
			serverSocket = new ServerSocket(); //소켓객체생성
			serverSocket.bind(new InetSocketAddress(IP,port)); //바인트를 사용해서  서버컴퓨터역할을 수행하는 그 컴퓨터가 자신의 ip주소 , 포트번호로 특정 클라이언트의 접속을 기다리게 할수있다. 
		}catch(Exception e) { //오류가 나면 
			e.printStackTrace();
			if(!serverSocket.isClosed()) {//서버소켓이 닫혀있는 상태가 아니라면 
				stopServer(); //서버 종료 
				
			}
			return;
		}
		
		//오규가 안나면 클라이언트가 접속할때가 계속 기다리는 쓰레드임
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				while(true) { //계속해서 새로운 클라이언트가 접속할수 있도록  만들어준다
					try {
						Socket socket = serverSocket.accept();//  새로운 클라이언트가 접속할수 있도록  만들어준다
						clients.add(new Client(socket)); //클라이언트가 접속을 했다면  클라이언트 배열에 새롭게 접속한 클라이언트를  추가
						System.out.println("[클라이언트 접속]" 
						+ socket.getRemoteSocketAddress()
						+ " : " + Thread.currentThread().getName()); //로그출력
					}catch(Exception e) {
						if(!serverSocket.isClosed()) { //서버 소켓에 문제가 발생한경우
							stopServer(); //서버를 작동 중지 
							
						}
						break;
					}
				}
				
			}
			
		};
		threadPool = Executors.newCachedThreadPool(); //쓰레드 초기화
		threadPool.submit(thread);  //현재 라이언트가 접속할때가 계속 기다리는 쓰레드를 넣어준다.
		
	}
	//서버의 작동을 중지시키는 메소드입니다.
	public void stopServer() {
		try {
			// 현재 작동 중인 모든 소켓 닫기
			Iterator<Client> iterator = clients.iterator(); 
			while(iterator.hasNext()) { //한명 한명 클라이언트에 접근을 한다.
				Client client =iterator.next(); // 특정 클라이언트에 접근
				client.socket.close();  // 해당 클라이언트의 소켓을 닫음
				iterator.remove();  //연결이 끊기 클라이언트를 제
				
			}
			//서버 소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//쓰레드 풀 종료하기 
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();  //자원 할당 해제 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	//Ui를 생성하고, 실질적으로 프로그램을 동작시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		try {
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//프로그램의 진입점이다.
	public static void main(String[] args) {
		launch(args);
	}
}
