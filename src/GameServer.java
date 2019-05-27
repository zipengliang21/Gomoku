import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This class is for making a window which pretends the server 
 * @author Zipeng Liang
 * @version 2018.4.5 10.00am
 */
public class GameServer extends JFrame implements GameConstants
{
	
	/**
	 * @param args support the line argument for the number of player(by -num followed by the numbers)
	 * @throws IOException throw the exception if it happens
	 */
	public static void main(String[] args) throws IOException
	{
	    //Construct an object of GameServer which is called to record the message or report errors
	    GameServer log = new GameServer();	   
	    
	    //server socket
	    ServerSocket server = new ServerSocket(PORT);
	    
	    int gameNo = 0; //the number of the game
	    //wait for enough player and start the game when it is enough
	    while(true)
	    {
	    	gameNo++;
	    	
	    	Socket[] sockets = new Socket[2];
	    	for (int i = 0; i < 2; i++)
	    	{
	    		sockets[i] = server.accept();
	    		log.record("\nplayer " + i + " joined game NO." + gameNo);
	    	}
	    	
	    	log.record("\ngame No." + gameNo + " is starting");
	    	int numOfPlayers = 2 ;
			Runnable gameService = new GameService(log, numOfPlayers , sockets);
	    	new Thread(gameService).start();
	    }
	}
	
	//set the constants for the size of the window(frame), and set the font
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 1000;
	private static final Font TEXT_FONT = new Font("ITALIC", Font.BOLD, 25);

	//instance variables
    private JTextArea textArea;

	/**
	 * Construct a game server
	 * To record the message
	 */
	public GameServer()
	{
		textArea = new JTextArea();
		textArea.setFont(TEXT_FONT);
		add(new JScrollPane(textArea));
		
		//set the window(frame)
		setTitle("Server Log");
		setSize(WIDTH, HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		
	}

	/**
	 * The method that help to record the message
	 * @param msg the message that needs to be recorded 
	 */
	public void record(String msg)
	{
		textArea.append(msg + "\n");
	}
	
}
