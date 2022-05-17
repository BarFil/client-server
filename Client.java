import java.io.*;
import java.net.*;
import java.util.*;
  
class Client {
    
    public static void main(String[] args)
    {
        try (Socket socket = new Socket("localhost", 1234)) {
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
  
            Scanner sc = new Scanner(System.in);
            String line = null;
			String time = null;
			MessageReceiver odbiornik = new MessageReceiver(socket);
			new Thread(odbiornik).start();

            while (true) {
				System.out.println("Podaj wiadomosc");
                line = sc.nextLine();
				if("exit".equalsIgnoreCase(line))System.exit(0);
				System.out.println("O ktorej wiadomosc ma wrocic");
  				time = sc.nextLine();
				
                out.println(line);
                out.flush();
				out.println(time);
                out.flush();
            }
        }
        catch (IOException e) {
			System.out.println("Serwer wylaczony");
            //e.printStackTrace();
        }
    }


	private static class MessageReceiver implements Runnable {
        private final Socket serverSocket;
		private BufferedReader in = null;
        public MessageReceiver(Socket socket)
        {
            this.serverSocket = socket;
        }
		  
        public void run()
        {
            
            try {
				in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));				
				while(true)
				{
					System.out.println("Server replied " + in.readLine());
				}
            }
            catch (IOException e) {
				System.out.println("Utracono polaczenie z serwerem");
                //e.printStackTrace();
            } 
        }
		
		}
                
}