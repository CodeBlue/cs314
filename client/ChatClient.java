package client;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 


import ocsf.client.*;
import java.io.*;
import java.util.*;

import common.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */
public class ChatClient extends AbstractClient
{
	//Instance variables **********************************************

	/**
	 * The interface type variable.  It allows the implementation of 
	 * the display method in the client.
	 */
	ChatIF clientUI; 
	private String loginId;
	private ArrayList <String> blockList = new ArrayList<String>();


	//Constructors ****************************************************

	/**
	 * Constructs an instance of the chat client.
	 *
	 * @param host The server to connect to.
	 * @param port The port number to connect on.
	 * @param clientUI The interface type variable.
	 */

	public ChatClient(String host, int port, ChatIF clientUI,String loginId) 
	throws IOException 
	{
		super(host, port); //Call the superclass constructor
		this.clientUI = clientUI;
		this.loginId = loginId;
		openConnection();
		sendToServer("#login "+ loginId);
	}


	//Instance methods ************************************************

	/**
	 * This method handles all data that comes in from the server.
	 *
	 * @param msg The message from the server.
	 */
	public void handleMessageFromServer(Object msg) 
	{
		if(blockList.contains(msg))
			blockList.remove(msg);
		else{
			String [] temp = ((String) msg).split(">");
			if(!blockList.contains(temp[0]) )
				clientUI.display(msg.toString());
		}
	}

	/**
	 * This method handles all data coming from the UI            
	 *
	 * @param message The message from the UI.    
	 */
	public void handleMessageFromClientUI(String message)
	{

		if(message.charAt(0) == '#')
			try {
				processCommand(message.substring(1));
			} catch (Exception e1) {}
			else{

				try
				{
					if(message != null)
						sendToServer(message);
				}
				catch(IOException e)
				{
					clientUI.display("Could not send message to server. Terminating client.");
					quit();
				}
			}
	}

	private void processCommand(String command) throws IOException {
		String[] c = command.split(" ");
		if( c[0].equalsIgnoreCase("quit")){
			for(String s : blockList)
				sendToServer("#unblock " + s);
			quit();
		}else if ( c[0].equalsIgnoreCase("logoff") ){
			closeConnection();

		}else if ( c[0].equalsIgnoreCase("login") ){
			if( !isConnected()){
				openConnection();
				sendToServer("#login "+ loginId);
			}else
				clientUI.display("Cannot have more than one connection.");

		}else if ( c[0].equalsIgnoreCase("gethost") )
			clientUI.display(getHost());
		else if ( c[0].equalsIgnoreCase("getport") )
			clientUI.display(Integer.toString(getPort()));
		else if ( c[0].equalsIgnoreCase("sethost") )			
			setHost(c[1]);
		else if ( c[0].equalsIgnoreCase("setport") )
			setPort(Integer.parseInt(c[1]));
		else if ( c[0].equalsIgnoreCase("block") ){
			if (blockList.contains(c[1])){
				clientUI.display("Messages from " + c[1] + " were already blocked.");
			}
			else if(!loginId.equals(c[1])){
				if(c[1].equals("server") )
					blockList.add("SERVER MSG");
				else
					blockList.add(c[1]);
				sendToServer("#"+c[0]+" "+c[1]);
			}else
				clientUI.display("You cannot block the sending of messages to yourself.");
		}else if ( c[0].equalsIgnoreCase("unblock") ){
			if (c.length > 1)
			{
				if(c[1].equals("server"))
					blockList.remove("SERVER MSG");
				else if (blockList.contains(c[1])){
					blockList.remove(c[1]);
					sendToServer("#"+c[0]+" "+c[1]);
					clientUI.display("Messages from " + c[1] + " will now be displayed.");
				}else{
					clientUI.display("Messages from " + c[1] + " were not blocked.");
				}
			}
			else
			{
				if (!blockList.isEmpty()){
					for (String s : blockList){
						clientUI.display("Messages from " + s + " will now be displayed.");
					}
					blockList.clear();
				}
				else{
					clientUI.display("No blocking is in effect.");
				}
			}
		}else if ( c[0].equalsIgnoreCase("whoiblock") ){
			if(!blockList.isEmpty())
				for (String s : blockList){
					clientUI.display("Messages from " + s + " are blocked.");
				}
			else{
				clientUI.display("No blocking is in effect.");
			}
		}else if ( c[0].equalsIgnoreCase("whoblocksme") ){
			sendToServer("#"+c[0]);
		}else
			clientUI.display("Invalid client command!");
	}


	/**
	 * This method terminates the client.
	 */
	public void quit()
	{
		try
		{
			closeConnection();
		}
		catch(IOException e) {}
		System.exit(0);
	}

	public void connectionClosed(){
		clientUI.display("Client connection closed.");
	}
	public void connectionException(Exception e){
		clientUI.display("Connection to server lost.");
		quit();
	}
}
//End of ChatClient class
