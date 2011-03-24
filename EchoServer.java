


// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import java.util.*;

import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer 
{
	//Class variables *************************************************

	/**
	 * The default port to listen on.
	 */
	final public static int DEFAULT_PORT = 5555;
	private HashMap<String,ArrayList<String>> blockList = new HashMap<String,ArrayList<String>>();
	private ArrayList<String> userList = new ArrayList<String>();

	//Constructors ****************************************************

	/**
	 * Constructs an instance of the echo server.
	 *
	 * @param port The port number to connect on.
	 */
	public EchoServer(int port) 
	{
		super(port);
		userList.add("server");
	}


	//Instance methods ************************************************

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg The message received from the client.
	 * @param client The connection from which the message originated.
	 */
	public void handleMessageFromClient (Object msg, ConnectionToClient client)
	{

		System.out.println("Message received: " + msg + " from " + client);
		if( ((String) msg).charAt(0) == '#' ){
			String cmd = ((String) msg).substring(1);

			String c[] = cmd.split(" ");
			if( c[0].equals("login") ){
				client.setInfo("id",c[1]);
				client.setInfo("ip", client.getInetAddress().getHostAddress());
				client.setInfo("hostname", client.getInetAddress().getHostName());
				userList.add(c[1]);
				sendToAllClients(client.getInfo("id") + " Connected");
			}else if(c[0].equals("block") ){
				if(userList.contains(c[1])){
					ArrayList<String> block = blockList.get(c[1]);
					if(block != null){
						block.add((String) client.getInfo("id"));
					}else{
						block = new ArrayList<String>();
						block.add((String) client.getInfo("id"));
						blockList.put(c[1], block);
					}
					try {
						client.sendToClient("Messages from " + c[1] + " will be blocked.");
					} catch (IOException e) {}
				}else{
					try {
						client.sendToClient("User " + c[1] + " does not exist.");
						client.sendToClient(c[1]);
					} catch (IOException e) {}
				}
					
			}else if(c[0].equals("unblock") ){
				ArrayList<String> block = blockList.get(c[1]);
				block.remove(client.getInfo("id"));				
			}else if(c[0].equals("whoblocksme") ){
				ArrayList<String> block = blockList.get(client.getInfo("id"));
				String blockme = "";
				if(block != null){
					for(String s : block){
						try {
							client.sendToClient("Messages to " + s + " are being blocked.");
						} catch (IOException e) {}
					}
				}
					try {
						client.sendToClient(blockme);
					} catch (IOException e) {}
			}

		}else{
			this.sendToAllClients( client.getInfo("id") + "> " + msg );
		}
	}

	/**
	 * This method overrides the one in the superclass.  Called
	 * when the server starts listening for connections.
	 */
	protected void serverStarted()
	{
		System.out.println
		("Server listening for connections on port " + getPort());
	}
	public void clientException( ConnectionToClient client, Throwable exception) {
		System.out.println(client.getInfo( "ip" )+ " ("+ client.getInfo("id")+")"+ " Disconnected");
		userList.remove(client.getInfo("id"));
		blockList.remove( client.getInfo("id"));
		sendToAllClients(client.getInfo("id" )+ " Disconnected");
	}

	/**
	 * This method overrides the one in the superclass.  Called
	 * when the server stops listening for connections.
	 */
	protected void serverStopped()
	{
		System.out.println("Server has stopped listening for connections.");
	}
	public void clientConnected(ConnectionToClient client){
		System.out.println(client.toString()+" Connected.");
	}
	public void clientDisconnected(ConnectionToClient client){
		System.out.println(client.getInfo("ip") + " ("+ client.getInfo("id")+")"+ " Disconnected.");
		userList.remove(client.getInfo("id"));
		blockList.remove( client.getInfo("id"));
		
		sendToAllClients(client.getInfo("id") + " Disconnected.");
	}
	//Class methods ***************************************************

	/**
	 * This method is responsible for the creation of 
	 * the server instance (there is no UI in this phase).
	 *
	 * @param args[0] The port number to listen on.  Defaults to 5555 
	 *          if no argument is entered.
	 */
	public static void main(String[] args) 
	{
		int port = 0; //Port to listen on

		try
		{
			port = Integer.parseInt(args[0]); //Get port from command line
		}
		catch(Throwable t)
		{
			port = DEFAULT_PORT; //Set port to 5555
		}

		ServerConsole sc = new ServerConsole(port);

		try {
			sc.commandListen();
		} catch (IOException e) {}
	}
}
//End of EchoServer class
