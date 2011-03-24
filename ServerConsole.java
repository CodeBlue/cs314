

// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import common.*;

/**
 *
 * 
 *   
 * 
 * 
 */
public class ServerConsole implements ChatIF
{
	private boolean closed = false;
	private EchoServer es;
	
	
	
	
	public ServerConsole(int port) {
		es = new EchoServer(port);
		try 
		{
			es.listen(); //Start listening for connections
		} 
		catch (Exception ex) 
		{
			System.out.println("ERROR - Could not listen for clients!");
		}
	}
	public void commandListen() throws IOException{
		BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
		String message = fromConsole.readLine();

		while(message != null){
			if(message.charAt(0) == '#')
				processCommand(message.substring(1));
			else{
				es.sendToAllClients( "SERVER MSG> " + message );
				display("SERVER MSG> " + message);
			}
			message = fromConsole.readLine();
		}
	}

	private void processCommand(String cmd) throws IOException{
		String c[] = cmd.split(" ");

		if( c[0].equalsIgnoreCase("quit")){
			display("Server Terminating!");
			es.sendToAllClients("Server Terminating!");
			System.exit(0);
		}else if ( c[0].equalsIgnoreCase("stop") ){
			es.stopListening();
		}else if ( c[0].equalsIgnoreCase("close") ){
			es.close();
			closed = true;
		}else if ( c[0].equalsIgnoreCase("setport") ){
			if(closed)
				es.setPort(Integer.parseInt(c[1]));
			else
				System.out.print("Must close server before setting port!");
		}else if ( c[0].equalsIgnoreCase("start") ){
			closed = false;
			if(!es.isListening()){
				try {
					es.listen();
				} catch (IOException e) {}
			}else
				display("The server is already started.");

		}else if ( c[0].equalsIgnoreCase("getport") )
			display(Integer.toString(es.getPort()));
		else
			display("Invalid server command!");
	}

	/**
	 * This method overrides the method in the ChatIF interface.  It
	 * displays a message onto the screen.
	 *
	 * @param message The string to be displayed.
	 */
	public void display(String message) 
	{
		System.out.println(message);
	}

}
