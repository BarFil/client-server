import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;  
import java.text.ParseException;
import javafx.util.Pair;
import java.util.PriorityQueue;
import java.util.Comparator;

class Server {
    public static void main(String[] args)
    {
        ServerSocket server = null;
  
        try {
  
            server = new ServerSocket(1234);
            server.setReuseAddress(true);
  
            while (true) {
  
                Socket client = server.accept();
                System.out.println("New client connected " + client.getInetAddress().getHostAddress());
                ClientHandler clientSock = new ClientHandler(client);
                new Thread(clientSock).start();
            }
        }
		catch (IOException e) {
            System.err.println("IOException");
        }
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    System.err.println("IOException");
                }
            }
        }
    }
  
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
  
        public ClientHandler(Socket socket)
        {
            this.clientSocket = socket;
        }
  
        public void run()
        {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message;
				String time_str="";
				Date time;
				Calendar now = Calendar.getInstance();
				Date currTime;
				PriorityQueue <Pair <Date,String> > kolejka = new PriorityQueue <Pair <Date,String> > (Comparator.comparing(Pair::getKey));
				MessageSender wyslij = new MessageSender(clientSocket,kolejka);
				new Thread(wyslij).start();
                while ((message = in.readLine()) != null) {
					time_str = in.readLine();
					time = new SimpleDateFormat("H:mm").parse(time_str);
					now = Calendar.getInstance();
					currTime = new SimpleDateFormat("H:mm").parse(now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE));
					if(time.compareTo(currTime) == -1)
					{
						out.println("Musisz podac pozniejsza godzine niz terazniejsza");
					}
					else
					{
						System.out.printf("Received from the client: %s %s\n",message,time_str);
						Pair <Date,String> para = new Pair<>(time, message);
						wyslij.addMessage(para);
					}					
                }
				
            }
            catch (IOException e) {
				System.out.println("Client disconnected");
            }
			catch (ParseException ex) {
                out.println("Niepoprawny format daty");
				run();
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    System.err.println("Socket already closed");
                }
            }
        }
    }
	
	private static class MessageSender implements Runnable {
        private final Socket clientSocket;
		private final PriorityQueue <Pair <Date,String> > kolejka;
		
        public MessageSender(Socket socket, PriorityQueue <Pair <Date,String> > kol)
        {
            this.clientSocket = socket;
			this.kolejka = kol;
        }
		
		public void addMessage(Pair <Date,String> msg)
		{
			kolejka.add(msg);
		}
  
        public void run()
        {
            PrintWriter out = null;
            try {
				Calendar now = Calendar.getInstance();
				Date currData;
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				while(true)
				{
					now = Calendar.getInstance();
					currData = new SimpleDateFormat("H:mm").parse(now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE));
					if(kolejka.peek() != null)
					{
						if(currData.compareTo(kolejka.peek().getKey()) == 0)
						{
							out.println(kolejka.poll().getValue());
						}
					}
				}
				
            }
			catch (IOException e) {
				System.err.println("IOException");
            }
			catch (ParseException ex) {
                out.println("Niepoprawny format daty");
            }
          
            
        }
    }
}