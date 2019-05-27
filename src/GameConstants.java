import java.awt.Font;

/**
 * Interface for sharing constants and the protocol between the server and the clients.
 * The following is the protocol for the server and the clients
 * SETPLAYER = 3;	// server -> client, n1		: set a player number n1 to the client.
 * PLAYING = 4;		// server -> client, n1		: tell the client it's player number n1's move (turn).
 * WINNER = 5;		// server -> client, n1		: tell the client that the player No. n1 is the winner.
 * UPDATE = 6;      
 * 
 * Some constants used in the program 
 * @author Zipeng Liang
 * @version 2018.4.5 10.00am
 */
public interface GameConstants {
	Font TEXT_FONT = new Font("ITALIC", Font.BOLD, 25);
	
	public final static int POINT_EMPTY = 0;
	public final static int POINT_BLACK = 1;
	public final static int POINT_WHITE = 2;
	
	public final static int SETPLAYER = 3;
	public final static int PLAYING = 4;
	public final static int WINNER = 5;
	public final static int UPDATE = 6;
	
	String HOST = "localhost";
	int PORT = 1181;
	
	int BOARD_SIZE =15;
	
	int BLACK = 1;
	int WHITE = 2;
}