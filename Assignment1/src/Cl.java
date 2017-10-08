
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;

/**
 * @author Rafael Andreou - Chrysovalantis Christodoulou
 *
 */
public class Cl extends Thread {
	
	static int requestsNumber; //the number of requests for each client
	static int clientsNumber=10; //the number of clients

	/**
	 * @author Rafael Andreou - Chrysovalantis Christodoulou
	 *
	 */
	private static class Packet {

		String message;
		
		Packet(String msg) {

			message = msg;

		}

	}

	static String serverIp ; //the server's ip
	int port = 8081; //the port to connect
	static int ID = 0; //the user's id
	int userID = 0;
	static double sumLatency=0;//the total amount of latency
	static double avgLatency=0;//the average latency

	/**
	 *Create a new client 
	 */
	Cl() {
		ID++;
		userID = ID;
	}

	@Override
	public void run() {
		
		try {

			Socket socket = new Socket(serverIp, port); //create a new socet to ccommunicate with server
			socket.setSoTimeout(1000000000);
			
			for (int i = 0; i < requestsNumber; i++) { 

				DataOutputStream output = new DataOutputStream(socket.getOutputStream()); //create a new data output stream to write the request
				BufferedReader server = new BufferedReader(new InputStreamReader(socket.getInputStream())); //create a new buffered reader to read server's response
				
				//create a new packet
				Packet packet = new Packet( "HELLO" + " " + serverIp + " " + port + " " + this.userID + System.lineSeparator());
				
				long before= System.nanoTime();//take the time when just send the packet
				output.writeBytes(packet.message);//send the packet
				String response = server.readLine();//get server's response
				long after= System.nanoTime();//take the time when just received the response
				double curLatancy=((double)after-before)/1000000000.0; //calculate current latency
				sumLatency+=curLatancy;//add the total latency
				System.out.println("[" + new Date() + "] Received: " + response);

			}
			if(Thread.activeCount()==2){ //if all clients have finished their requests
				avgLatency=sumLatency/requestsNumber; //calculate the average latency
				
				//print average latency
				System.out.print("Average Latancy: ");
				System.out.printf("%.2f", avgLatency);
				System.out.println("s");
				
			}

			socket.close(); //close the socket

		} catch (Exception e) {
			
			System.out.println("Invalid ip address of server");
			return;
		}
		
	}
	
	/** Check that given ip is valid
	 * 
	 * @param ip the ip address given as argument from user
	 * @return true if ip is valid, false false if ip is invalid
	 */
	private static boolean validIP (String ip) {
    try {
        if ( ip == null || ip.isEmpty() ) {
            return false;
        }

        String[] parts = ip.split( "\\." );
        if ( parts.length != 4 ) {
            return false;
        }

        for ( String s : parts ) {
            int i = Integer.parseInt( s );
            if ( (i < 0) || (i > 255) ) {
                return false;
            }
        }
        if ( ip.endsWith(".") ) {
            return false;
        }

        return true;
    } catch (NumberFormatException nfe) {
        return false;
    }
}

	public static void main(String args[]) {
		
		//check that user entered the two arguments
		if(args.length != 2){
			
			System.out.println("Please enter the following two arguments in that format : Server's_IP_address Total_number_of_requests");
			return;
			
		}

		try{
			
			requestsNumber = Integer.parseInt(args[1])/clientsNumber; //get the number of requests for each client
		}
		
		catch(NumberFormatException e){
			
			System.out.println("Invalid number of requests");
			return;
		}
			
		serverIp = args[0]; //get the server's ip address
		if(!validIP(serverIp)){ //if ip is not valid print error message and end program
			System.out.println("Invalid ip address");
			return;
		}
		
		//create 10 client threads
		for (int i = 0; i < clientsNumber; i++) {
			Cl cl = new Cl();
			cl.start();
		}

	}

}
