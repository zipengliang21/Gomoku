import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
/**
 * A class that joins the Gomoku game as a player, who plays with another player.
 * This player can either play with other plays in the same computer or in the different computer.
 * (The rule of this game can be found in the website: https://en.wikipedia.org/wiki/Gomoku)
 * @author Zipeng Liang
 * @version 2018.4.5 10.00am
 */
public class Player extends JFrame implements GameConstants, Runnable
{
	//instance variables
	//GUI stuffs
	private JLabel msgLabel;
	private JLabel yourMoveLabel;
	private Board board;
	private JLabel statusLabel;

	//table data variables
	private final int TABLE_WIDTH = 525;
	private final int TABLE_HEIGHT = 575;
	private final int RATE = TABLE_WIDTH / BOARD_SIZE;
	private final int X_OFFSET = 5;
	private final int Y_OFFSET = 10;
	private int[][] tableArrays = new int[BOARD_SIZE][BOARD_SIZE];
	
	// images
	BufferedImage table;
	BufferedImage selected;
	BufferedImage white;
	BufferedImage black;
	BufferedImage selected1;
	
	// mouse coordinates
	private int pointx = 0;
	private int pointy = 0;
	private int mouseX = -30;
	private int mouseY = -30;

	//chess color
	public int chessColor;

	//whether already click
	boolean click = false;

	//socket variables
	private Socket socket;
	private DataOutputStream toServer;
	private DataInputStream fromServer;
	
	//player ID
	private int playerNo;
	private int currentPlayer;

	//listeners
	private MousePress mousePress;
	private MouseMove mouseMove;
	
	/**
	 * Construct the Player object
	 */
	public Player()
	{
		initial();   //initiate the 2D array that stored the chess data
		buildGUI();
		openConnection();
		receiveResponses();
	}

	/**
	 * The main method that runs the Gomoku game
	 * @param command line argument
	 */
	public static void main(String[] args) 
	{
		new Player();
	}
	
	/**
	 * initial the chess 2D array values
	 */
	public void initial() 
	{
		for (int i = 0; i < BOARD_SIZE; i++)
		{
			for (int j = 0; j < BOARD_SIZE; j++) 
			{
				tableArrays[i][j] = POINT_EMPTY;
			}

		}
	}

	/**
	 * build the GUI and make the arrangement
	 */
	public void buildGUI() 
	{
		//set the GUI stuffs
		setLayout(null);
		msgLabel = new JLabel("");
		msgLabel.setFont(TEXT_FONT);
		msgLabel.setForeground(Color.red);
		msgLabel.setBounds(0,0,500,1100);
		
		yourMoveLabel = new JLabel("You can't move");
		yourMoveLabel.setFont(TEXT_FONT);
		yourMoveLabel.setForeground(Color.red);
		yourMoveLabel.setBounds(0,0,1000,1100);
		
		statusLabel = new JLabel("The Game is not started!");
		statusLabel.setFont(TEXT_FONT);
		statusLabel.setForeground(Color.black);
		statusLabel.setBounds(230,0,300,1100);
		

		try 
		{
			table = ImageIO.read(new File("image/board.jpg"));
			selected = ImageIO.read(new File("image/selected.gif"));
			selected1 = ImageIO.read(new File("image/selected1.gif"));
			white = ImageIO.read(new File("image/white.gif"));
			black = ImageIO.read(new File("image/black.gif"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	
		board = new Board();
		board.setBounds(0,0,TABLE_WIDTH,TABLE_HEIGHT);

		//set frame
		add(msgLabel);
		add(yourMoveLabel);
		add(statusLabel);
		add(board);
		setTitle("Waiting for receiving Player ID...");
	    setSize(TABLE_WIDTH+6, TABLE_HEIGHT+20);
	    setLocation(500,500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	

	}

	/**
	 * make the connection to the server
	 */
	public void openConnection() 
	{
		try 
		{
			try 
			{
				// server ip and address
				socket = new Socket(HOST, PORT);
				toServer = new DataOutputStream(socket.getOutputStream());
				fromServer = new DataInputStream(socket.getInputStream());
			} 
			catch (java.net.ConnectException e)
			{
				JOptionPane.showMessageDialog(null, "Connection Error!");
			}
		} 
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * the method that associate the listener with the event when the user click the window
	 */
	private void associateListener()
	{
		// associate the listener for the mouse when it press
		mousePress = new MousePress();
		board.addMouseListener(mousePress);

		// associate the listener for the mouse when it move
		mouseMove = new MouseMove();				
		board.addMouseMotionListener(mouseMove);
	}
	

	/**
	 * we make a new class Board so that it can inherit the function of JPanel, 
	 * and also, it can has some new behaviors 
	 */
	private class Board extends JPanel 
	{
		@Override
		public void paint(Graphics g)
		{
			// draw board
			g.drawImage(table, 0, 0, null);
			
			//draw chesses
			for (int i = 0; i < BOARD_SIZE; i++) 
			{
				for (int j = 0; j < BOARD_SIZE; j++) 
				{
					if (tableArrays[i][j] == POINT_WHITE)
					{
						g.drawImage(white, i * RATE + X_OFFSET, j * RATE + Y_OFFSET, null);
					}
					if (tableArrays[i][j] == POINT_BLACK)
					{
						g.drawImage(black, i * RATE + X_OFFSET, j * RATE + Y_OFFSET, null);
					}
				}
			}
				//draw the icons to indicate whether player can move
				if (click == true)
				{
					if(pointy * RATE <=TABLE_HEIGHT-80) 
					{
						g.drawImage(selected1, pointx * RATE + X_OFFSET, pointy * RATE + Y_OFFSET, null);
					}				
				} 
				else 
				{
					if(pointy * RATE <=TABLE_HEIGHT-80) 
					{
						g.drawImage(selected, pointx * RATE + X_OFFSET, pointy * RATE + Y_OFFSET, null);
					}					
				}



		}
	}

	
	/**
	 * get the response from the server and do the corresponding action
	 */
	@Override
	public void run() {
		try
		{
			int response;
			do 
			{
				response = fromServer.readInt();
				
				switch(response)
				{
				case SETPLAYER:   setPlayer(fromServer.readInt());    break;
				
				case PLAYING:     playing(fromServer.readInt());      break;
				
				case WINNER:      winner(fromServer.readInt());       break;
				
				case UPDATE:      update(fromServer.readInt(), fromServer.readInt(), fromServer.readInt());       break;
				
				default:          System.out.println("unknown response");
				}
			} while(response != WINNER);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}


	/**
     * Start the thread to run the run method
    */
    private void receiveResponses() 
    {
	    if (socket != null && !socket.isClosed())
		new Thread(this).start();
    }
    
	/**
	 * Set the corresponding number to the corresponding player 
	 * Also, change the window title 
	 * @param num the given number of the player 
	 */
	private void setPlayer(int num) 
	{
		playerNo = num;
		chessColor = num + 1;
		if(num == 0) 
		{
			click = false;
		}
		else 
		{
			click = true;
		}
		setTitle("Player No." + playerNo);
		msgLabel.setOpaque(false);
		msgLabel.setText("");
	}

	/**
	 * Make the player play when it comes to this player's turn
	 * @param n1 the given number of the player 
	 */
	private void playing(int num)
	{
		currentPlayer = num;

		if (playerNo == currentPlayer)
		{
			click = false;
			statusLabel.setText("The game is running");
			statusLabel.setBounds(280,0,300,1100);
			yourMoveLabel.setText("Your move");
			try
			{
				toServer.writeInt(chessColor);
				toServer.flush();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			associateListener();

		}
		else 
		{
			click = true;
			statusLabel.setText("The game is running");
			statusLabel.setBounds(280,0,300,1100);
			yourMoveLabel.setText("Not your move");
			associateListener();

		}
	}

	/**
	 * Changing the GUI stuffs in the window to indicate the winner
	 * @param num the given number of the winner
	 */
	private void winner(int num) 
	{
		msgLabel.setOpaque(false);

		if (playerNo == num)
			msgLabel.setText(" Your won!");
		else
			msgLabel.setText(" Your Lost...");

		statusLabel.setBounds(320,0,300,1100);
		statusLabel.setText("The game is over");
		yourMoveLabel.setText("");

		board.removeMouseListener(mousePress);
		board.removeMouseMotionListener(mouseMove);

	}

	/**
	 * undate the chess 2D array data 
	 * @param x the x coordinator of the chess which was put by the player
	 * @param y the y coordinator of the chess which was put by the player
	 * @param color   the chess color of the player
	 */
	private void update(int x, int y, int color) {
		tableArrays[x][y] = color;
		board.repaint();
	}
	
	
	/**
	 * making the mouse press listener 
	 *
	 */
	private class MousePress implements MouseListener
	{

		@Override
		public void mouseClicked(MouseEvent arg0) {}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mouseReleased(MouseEvent arg0) {}
		
		void POINT(int x)
		{
			tableArrays[mouseX][mouseY] = x;
			click = true;
			// coordinates x y
			// pass them through socket
		    int output = mouseX + mouseY*15;
			try {
				toServer.writeInt(output);
				toServer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			board.repaint();
			board.removeMouseListener(mousePress);
			board.removeMouseMotionListener(mouseMove);
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			if (click == false) 
			{

				mouseX = (int) ((e.getX() - X_OFFSET) / RATE);
				mouseY = (int) ((e.getY() - Y_OFFSET) / RATE);
				if (tableArrays[mouseX][mouseY] == 0)
				{						
					POINT(chessColor);
				}
			}
		}
		
	}
	
	
	/**
	 * makeing the mouse move listener 
	 */
	private class MouseMove implements MouseMotionListener
	{
		public void mouseDragged(MouseEvent e){}

		// move
		public void mouseMoved(MouseEvent e)
		{
			pointx = (int) ((e.getX() - X_OFFSET) / RATE);
			pointy = (int) ((e.getY() - Y_OFFSET) / RATE);

			board.repaint();
		}
	}
}
