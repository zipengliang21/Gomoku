import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * This game is to function as a server service of the game 
 * This class will execute the command for the game and record it properly
 * This class implement the Runnable and GameConstants interface
 * @author Zipeng Liang
 * @version 2018.4.5 10.00am
 */
public class GameService implements Runnable, GameConstants
{
	//instance variables
		private GameServer log;
		private int numOfPlayers;
		
		private Socket[] sockets;
		private DataInputStream[] fromClients;
		private DataOutputStream[] toClients;
		
		private int currentPlayer;
		
		private int[][] tableArrays = new int[BOARD_SIZE][BOARD_SIZE];
		
		private boolean blackWin;
		private boolean whiteWin;

		
/**
 * The constructor of the game service
 * get the input from the players and also give the ouput to the players
 * Also, call the shuffle card method to shuffle the cards
 * @param log the server for this game
 * @param numOfPlayers the number of players in one game
 * @param sockets the sockets between the server and the players
 * @throws IOException if the exception happens, throw it
 */		
	public GameService(GameServer log, int numOfPlayers, Socket[] sockets) throws IOException 
	{	
		//initialize the instance variable
		this.log = log;
		this.numOfPlayers = numOfPlayers;
		this.sockets = sockets;
				
		//set the array 
		fromClients = new DataInputStream[numOfPlayers];
		toClients = new DataOutputStream[numOfPlayers];
				
		//get the input and output from or to the players
		for (int i = 0; i < numOfPlayers; i++)
		{
			fromClients[i] = new DataInputStream(sockets[i].getInputStream());
			toClients[i] = new DataOutputStream(sockets[i].getOutputStream());
		}
				
		currentPlayer = 0; 
											
	}
	
/**
 * Implement the run method of the interface runnable
 * This method is to execute the commands of the game
 * This method also calls other helper methods
 */
	@Override
	public void run() 
	{
		try
		{
			setPlayers(); // call the helper method to set the player
			
			executeCommand(); // call the helper method to execute the command of the game
			
			win();            // call the helper method to indicate the winner
			
			for (Socket s: sockets) //when it comes to this line, it means the game is over, so close all the socket.
				s.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * A helper method to set the players and record it in the log
	 * ALso, send the information to the player
	 * @throws IOException throw an exception if there is one
	 */
	private void setPlayers() throws IOException 
	{
		for (int i = 0; i < numOfPlayers; i++)
		{
			toClients[i].writeInt(SETPLAYER);
			toClients[i].writeInt(i);
			toClients[i].flush();
			log.record("Server --> player " + i + ":     SETPLAYER " + i);
			
			toClients[i].writeInt(PLAYING);
			toClients[i].writeInt(currentPlayer);
			toClients[i].flush();
		}
		log.record("server --> all players: PLAYING " + currentPlayer);
		
	}

	/**
	 * A helper method to execute command
	 * @throws IOException throw an exception if there is one
	 */
	private void executeCommand() throws IOException {
		while(true) 
		{
			 int color = fromClients[currentPlayer].readInt();
			 int location = fromClients[currentPlayer].readInt();
			 int y = location/15;
			 int x = location%15;
			 tableArrays[x][y] = color;
			 
			 for (int i = 0; i< numOfPlayers; i++)
			 {
					toClients[i].writeInt(UPDATE);
					toClients[i].writeInt(x);
					toClients[i].writeInt(y);
					toClients[i].writeInt(color);
					toClients[i].flush();
			 }
			 
			 
			 blackWin = checkWinner(BLACK);
			 whiteWin = checkWinner(WHITE);
			 if(blackWin || whiteWin) {
				 return;
			 }
			 nextPlayerTurn(x, y);

		}
		
	}
	
	/**
	 * A helper method to make the turn to the next player, when the previous player finishes its turn
	 * It will also record it and sent it to the players
	 *@param x the x coordinator of the chess which was put by the player
	 *@param y the y coordinator of the chess which was put by the player
	 * @throws IOException throw an exception if there is one
	 */
	private void nextPlayerTurn(int x, int y) throws IOException
	{
		currentPlayer++;
		if (currentPlayer >= numOfPlayers)
			currentPlayer = 0;
		
		for (int i = 0; i < numOfPlayers; i++)
		{
			toClients[i].writeInt(PLAYING);
			toClients[i].writeInt(currentPlayer);
			toClients[i].flush();
		}
		log.record("server --> all players: PLAYING" + currentPlayer);
	}

	/**
	 * A helper method to determine whether there is a winner
	 * @param color  the chess color of the player
	 * @return  true if there is a winner
	 */
	private boolean checkWinner(int color) {
		int coord[] = { 0, 0, 0, 0 };

		for (int i = 0; i < 15; i++) 
		{
			for (int j = 0; j < 15; j++)
			{
				//check vertical
				if (tableArrays[i][j] == color)
				{
					coord[0] += 1;
				} 
				else
				{
					coord[0] = 0;
				}
				if (coord[0] >= 5)
				{
					return true;
				}
				//check horizontal
				if (tableArrays[j][i] == color) 
				{
					coord[1] += 1;
				} 
				else 
				{
					coord[1] = 0;
				}
				if (coord[1] >= 5)
				{
					return true;
				}
			}
		}
		for (int i = 10; i >= 0; i--)
		{
			//check diagonal
			for (int i1 = i, j1 = i; j1 < 15; j1++, i1++)
			{
				if (tableArrays[j1 - i][i1] == color)
				{
					coord[2] += 1;
				} 
				else
				{
					coord[2] = 0;
				}
				if (coord[2] >= 5) 
				{
					return true;
				}
			}
			//check diagonal
			for (int i1 = i, j1 = 14; i1 < 15; j1--, i1++)
			{
				if (tableArrays[j1][i1] == color) 
				{
					coord[3] += 1;
				} 
				else
				{
					coord[3] = 0;
				}
				if (coord[3] >= 5)
				{
					return true;
				}
			}
		}
		return false;
		
	}

	/**
	 * A helper method to indicate the winner
	 */
	private void win() throws IOException {
		int winnerNo = 3;
		if(blackWin)   winnerNo = 0;
		else
		{
			winnerNo = 1;
		
		}
		for (int i = 0; i < numOfPlayers; i++)
		{
			toClients[i].writeInt(WINNER);
			toClients[i].writeInt(winnerNo);
			toClients[i].flush();
		}
		log.record("server --> all players: WINNER " + winnerNo);
		log.record("This game is over");
	}

}