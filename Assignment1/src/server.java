
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

/**
 * @author Rafael Andreou / Chrysovalantis Christodoulou 
 * 			
 * This class implements the server functionality
 */
public class server {

	/**
	 * @author Rafael Andreou / Chrysovalantis Christodoulou 
	 *
	 */
	private static class TCPWorker implements Runnable {
		
		private static class Packet {

			String response; //the server's response message
			byte[] payload; //the payload user will download from server
			
			/** Creates a new packet
			 * 
			 * @param msg the message server will sent
			 * @param size the size of the message
			 */
			Packet(String msg, int size) {

				response = msg;
				payload = new byte[size];
				String msg1 = new String(payload);
				response = response + msg1 + " " + msg1.length()/1024 + "Kb" + System.lineSeparator();
	
			}

		}
		
		/** Generate random message size
		 * 
		 * @return the generated random message size
		 */
		private static int generateMsgSize() {
		
			Random rand = new Random(System.currentTimeMillis());
			int msgSize = rand.nextInt(1700) + 300;
			msgSize=msgSize * 1024; //multiply * 1024 to convert bytes to kilobytes
			return msgSize;
		}

		private Socket client; //the socket to communicate with the client
		private String clientbuffer; //the client's message-request
		
		static int requests=0; //the number of requests server receives
	
		static int finishedWorkers=0; //the number of TCP Workers that have finished their service
		static int numberOfClients=0;
		
		static long sumThroughput = 0;  //the total time server needed to serve all requests
		static double avgThroughput = 0; //the average throughput of server
		
		static double avgMemory =0;	//the average memory utilization of the server
		static double sumMemory = 0; //the total memory utilization of the server 

		/** Creates a new TCP Worker
		 * 
		 * @param client the client to communicate with
		 */
		public TCPWorker(Socket client) {
			this.client = client;
			this.clientbuffer = "";
		}
		
		
		@Override
		public void run() {

			try {
				
				System.out.println("Client connected with: " + this.client.getInetAddress() + "\n");

				DataOutputStream output = new DataOutputStream(this.client.getOutputStream()); //open data output stream to write response

				BufferedReader reader = new BufferedReader(new InputStreamReader(this.client.getInputStream())); // open buffered reader to read user's message
				numberOfClients++;
				while((this.clientbuffer = reader.readLine())!=null) { //while there are messages in the buffer

					long before = System.nanoTime(); //take the system time when a new request appears

					String[] request = clientbuffer.split(" "); //get message's fields
					
					sumMemory+=(double)Runtime.getRuntime().freeMemory()/Runtime.getRuntime().totalMemory(); //add to sum the current memory utilization
					

					System.out.println("[" + new Date() + "] Received:" + this.clientbuffer); //print received message
					
					Packet packet = new Packet ("WELCOME " + request[3] + " " , generateMsgSize()); //create a new packet
					
					output.writeBytes(packet.response); // write the response 
					
					requests++; //increase requests counter

					long after = System.nanoTime(); //take the system time when served a request

					sumThroughput += (after-before);

				}
				
				finishedWorkers++; //increase counter of finished workers 
				if (finishedWorkers==numberOfClients){ //when all requests have been server
					avgMemory= sumMemory/requests; //calculate the average memory usage
					
					//print memory usage
					System.out.print(System.lineSeparator() + "Average memory usage : ");
					System.out.printf("%.2f", avgMemory);
					System.out.println("%");
					
					//print throughput
					avgThroughput =  ( (double)60000000*requests)/sumThroughput ;
					System.out.print("Average Throughput : ");
					System.out.printf("%.2f", avgThroughput);
					System.out.println(" request/s");
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static ExecutorService TCP_WORKER_SERVICE = Executors.newFixedThreadPool(10); //create a new executor to handle 10 TCPWorkers

	public static void main(String args[]) {
		
		try {
			ServerSocket socket = new ServerSocket(8081); //create socket to accept requests
			System.out.println("Server listening to: " + socket.getLocalSocketAddress() + ":" + socket.getLocalPort());

			while (true) {
				Socket client = socket.accept();

				TCP_WORKER_SERVICE.submit(new TCPWorker(client));

			}
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
