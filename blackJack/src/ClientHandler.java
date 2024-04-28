//ClientHandler class


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
	private final Socket clientSocket;

	private static int count = 1;
    private final int id;
    
    private OutputStream outputStream;
    private ObjectOutputStream objectOutputStream;
    private InputStream inputStream;
    private ObjectInputStream objectInputStream;
    
    // make a SINGLE generic type to hold either Player or Dealer
    private Player playerUser;
    private Dealer dealerUser;

	// Constructor
	public ClientHandler(Socket socket) throws IOException
	{
		this.clientSocket = socket;
		this.id = count++;
		
		this.outputStream = this.clientSocket.getOutputStream();
		this.objectOutputStream = new ObjectOutputStream(this.outputStream);
		
		this.inputStream = this.clientSocket.getInputStream();
		this.objectInputStream = new ObjectInputStream(this.inputStream);
	}

	public void run()
	{
		try {
			System.out.println(Server.getServerName());
	    	System.out.println(Server.getCasinoFunds());
	    	System.out.println(Server.getValidDealers());
	    	System.out.println(Server.getOnlineDealers());
	    	System.out.println(Server.getValidPlayers());
	    	System.out.println(Server.getOnlinePlayers());
	        
	        // Get the first message from client. It should be a login message.
	        // Ignore anything else.
	        Message login = (Message) objectInputStream.readObject();
	        
	        // If we get a NEW login message, check the text supplied in the
	        // message and check the server account details in text file.
	        // Will also set the clientHandler to either a Player or Dealer.
	        login = validateUser(login);

	        
	        // If the login is validated then login is a success and we can send
	        // back the message to the client.
			if(login.getStatus() == Status.Success) {
				
				// Explicitly send message back to the Client b/c sendToClient()
				// does not handle logins.
				objectOutputStream.writeObject(login);
			}
			
			// If status of the message was not updated to Success then it has 
			// to be a failed login.
			// Close the client.
			else {
				System.out.println("Invalid credentials supplied, "
								   	+ "closing socket!");
				clientSocket.close();
			}

			// Keep reading for messages until we get a logout message.
			Message current = (Message) objectInputStream.readObject();

			// This is the main loop of the program.
			// All actions from the GUI will go through the client and send
			// requests to the server here.
			
			// On receipt of a ‘logout message’ should break out of the loop.
			// Then a status will be returned with ‘Success’, then the 
			// connection will be closed and the thread terminates.
			//
			// The Player or Dealer will be removed from the Server's 
			// onlinePlayers or onlineDealrs.
			while (!isLogginOut(current)) {
				
				// Send back updated message to the Client.
				sendToClient(current);

				// Get another message from the client
				// In the future this might change to a List of Message.
				current = (Message) objectInputStream.readObject();
			}

			// Don't forget to close the client durr.
			clientSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public int getClientID() {
		return id;
	}
	
	private Boolean isNewLogin(Message login) {
		
		// The message is valid if of Type Login and has a Status of New.
		if(login.getType() == Type.Login && login.getStatus() == Status.New) {
			return true;
		}
		
		return false;
	}
	
	
	// Checks the message to see if a Client is requesting to logout and then
	// updates the message.
	// Used to break out of the ClientHandler.
	private Boolean isLogginOut(Message msg) throws IOException {
		
		// If the message is of Type Logout and New return TRUE.
		if(msg.getType() == Type.Logout && msg.getStatus() == Status.New) {
			
			// Acknowledge logout Message
			msg.setStatus(Status.Success);
			
			// Username is supplied in the message.
			logoutUser(msg.getText());
			
			// Print message to the terminal (make a log of what happened).
			logMessage(msg);
			
			// Send updated LOGOUT message back to the client
			objectOutputStream.writeObject(msg);

			return true;
		}		

		// Else this message is not a logout message. Proceed to process the
		// message accordingly.
		return false;
	}
	
	
	// A Dealer or Player name is given and is removed from Server if valid.
	private void logoutUser(String user) {
		
		Player player = Server.getTargetPlayer(user);
		Dealer dealer = Server.getTargetDealer(user);
		
		// Remove Player
		if(player != null && dealer == null) {
			
			Server.getOnlinePlayers().remove(player);
			return;
		}
		
		// Remove Dealer
		if(dealer != null && player == null ) {
			
			Server.getOnlineDealers().remove(dealer);
		}		
	}

	
	private Message validateUser(Message login) {
		
        // If we get a NEW login message, check the text supplied in the
        // message and check the server account details in text file.
		if(isNewLogin(login)) {

	        // Login user will return a string as either a:
			// "dealer", "player" or "invalid".
        	String loginType = Server.loginUser(login.getText());
        	
        	// IF account details found Set status to success
	        if(loginType == "dealer" || loginType == "player") {
	        	
	        	String details[] = login.getText().split(":");
	    		String username = details[0];
	        	
	    		// Set target dealer to the client handler.
	    		// Set Player to null.
	    		// Get the Dealer from Server.getTargetDealer();
	        	if(loginType == "dealer") {
	        		
	        		dealerUser = Server.getTargetDealer(username);
	        		playerUser = null;
	        		
	        		
	        		// Print the list of the current online Dealers.
	        		//System.out.println(Server.getOnlineDealers());
	        	}
	        	
	        	// Set target player to client handler.
	        	// Set Dealer to null;
	        	// Get the player from Server.getTargetPlayer();
	        	if(loginType == "player") {
	        		
	        		playerUser = Server.getTargetPlayer(username);
	        		dealerUser = null;
	        		
	        		// Print the list of the current online Players.
	        		System.out.println(Server.getOnlinePlayers());
	        	}
	        	
	        	System.out.println("Login Successful -- <" + loginType
	        			+ "> Client #" + id + "\n");
	        	
	        	login.setStatus(Status.Success);
	        }
	        
	        // If neither player or dealer then the login is invalid
	        else {
	        	
	        	System.out.println("Login Failed -- Client #" + id + "\n");
	        	login.setStatus(Status.Failed);
	        }
	        
	        // Login should still contain the User Details.
	        login.setText(loginType);
		}
		// Print message to the terminal (make a log of what happened).
		logMessage(login);
		
		return login;		
	}
	
	// A general send Message back to Client function.
	// A Message request is supplied by the Client and gets handled by the 
	// message handler. Then updates the message accordingly and sends the 
	// response back to the Client.
	private void sendToClient(Message message) throws IOException {
		try {

			// Only brand new Message's with a Status of New will get handled.
			if(message.getStatus() == Status.New) {
				
				// If its a new message then handle that request from the Client
				handleMessage(message);
			}
			
			// If its not a brand New Message than its an invalid request from 
			// the Client.
			else {

				updateMessageFailed(message, 
						"Invalid Request from the Client!");
			}
			
			// Print message to the terminal (make a log of what happened).
			logMessage(message);
			
			// Send acknowledgment back to the client.
			objectOutputStream.writeObject(message);
			
	
		} catch (IOException e) {
			
			System.out.println("Something Borked! Closing socket!\n");
			clientSocket.close();
			
			e.printStackTrace();
		}

	}
	

	
	
	// Message handler
	private void handleMessage(Message message) {

		// Switch to handle all the various types of messages.
		// Controlled by the Message's Type.
		// Message request data is supplied in the Message text field. A servers
		// action should be the Message Type and data associated in the text
		// area.
		//
		// Build out the functions as needed and remember to update
		// the message before sending to the Client.
		//
		switch(message.getType()) {
			
			// Sends a list of all Games on the server.
			case ListGames:
				listGames(message);
				break;
			
			// Sends a list of all online Players on the Server.
			case ListPlayersOnline:
				listPlayersOnline(message);
				
			// Sends a list of all online Players on the Server.
			case ListDealersOnline :
				listDealersOnline(message);
				
			// Sends a list of all Players in a Game by its Game ID.
			case ListPlayersInGame:
				listPlayersInGame(message);
				break;
				
			// Opens a new game on the Server and returns the new Game ID.
			case OpenGame:
				openGame(message);
				break;
				
			// Closes a Game on the Server using a Game ID from the Client.
			case CloseGame:
				closeGame(message);
				break;
			
			// Player/Dealer is added to game. Client supplies the Game's ID.
			case JoinGame:
				joinGame(message);
				break;
			
			// Player/Dealer is removed from game. Client supplies Games' ID.
			case LeaveGame:
				leaveGame(message);
				break;
				
			// Sends back the Game ID of which game the Player was put into.
			case QuickJoin:
				quickJoin(message);
				break;
				
			default:
				// DO NOTHING
				break;
		}
	}	


	// Prints a log to the terminal saying what was sent to the Client. 
	private void logMessage(Message message) {
		
		Type request = message.getType();
		Status status = message.getStatus();
		String data = message.getText();
		String timeStamp = new Date().getCurrentDate();
		int id = getClientID();
		
		
		// Client# id <type>[status]: timeStamp 
		// data
		System.out.println("Client# " + id + " <" + request + ">[" + status 
						   + "]:" + timeStamp + "\n" + data);
	}


	// Updates the Message's Status to Success and sets whatever text in text
	// field.
	private void updateMessageSuccess(Message message, String text) {
		
		// Update the Status of the Message.
		message.setStatus(Status.Success);
		
		// Update the text area.
		message.setText(text);
	}
	
	
	// Updates the Message's Status to Success and sets whatever text in text
	// field.
	private void updateMessageFailed(Message message, String text) {
		
		// Update the Status of the Message.
		message.setStatus(Status.Failed);
		
		// Update the text area.
		message.setText(text);
	}
	

	// Sends a String back to the client with a list of all the games on the
	// Server with some details.
	private void listGames(Message message) {
		
		// Get list of games from Server.getGames()
		// Iterate through the list.
		// For Each Games concat a string: 
		//
		// GameID:TableStatus:DealerName:NumberOfPlayers\n
		// GameID:TableStatus:DealerName:NumberOfPlayers
		//
		
		String gameListString = null;
		List<Game> gameList = Server.getGames();
		
		// If there are no game send back to the Client a Failed message.
		if(gameList == null) {
			updateMessageFailed(message, "There are no active Games!");
			return;
		}
		
		Game lastGame = gameList.get(gameList.size() -1);
		
		for(Game g : gameList) {
			
			// If at last game on the list, print without newline character.
			if(g.equals(lastGame) ) {
				
				gameListString += g.getID() + ":" 
							    + g.getTableStatus() + ":"
							    + g.getDealer().getDealerName() + ":"
							    + g.getTable().getPlayers().size();
			}
			
			// Else add the details to the string WITH newline characters.
			gameListString += g.getID() + ":" 
							+ g.getTableStatus() + ":"
							+ g.getDealer().getDealerName() + ":"
							+ g.getTable().getPlayers().size() + "\n";
		}
		
		
		// Update the Status of the Message.
		// Update the text area with list of the games and details.
		updateMessageSuccess(message, gameListString);
	}
	
	
	// Lists all Players online in the Server or nothing at all.
	private void listPlayersOnline(Message message) {

		String playersOnlineString = null;
		List<Player> playersOnline = Server.getOnlinePlayers();
		
		// If no Players online send a Success message back to the client.
		if(playersOnline == null) {
			updateMessageSuccess(message, "There are no Players online!");
			return;
		}
		
		Player lastPlayer = playersOnline.get(playersOnline.size() -1);
		
		// Each player on the list gets printed.
		for(Player p : playersOnline) {
			
			// If at the last Player on the list print w/o the comma.
			if(p.equals(lastPlayer) ) {
				playersOnlineString += p.getPlayerName();
			}
			
			playersOnlineString += p.getPlayerName() + ",";
		}
		
		// Update the Status of the Message.
		// Update the text area with list of the games and details.
		updateMessageSuccess(message, playersOnlineString);
	}
	
	
	// List all Dealers online in the Server.
	private void listDealersOnline(Message message) {

		String dealersOnlineString = null;
		List<Dealer> dealersOnline = Server.getOnlineDealers();
		
		// If no Dealers online send a Success message back to the client.
		if(dealersOnline == null) {
			updateMessageSuccess(message, "There are no Dealers online!");
			return;
		}
		
		Dealer lastDealer = dealersOnline.get(dealersOnline.size() -1);
		
		// Each player on the list gets printed.
		for(Dealer d : dealersOnline) {
			
			// If at the last Player on the list print w/o the comma.
			if(d.equals(lastDealer) ) {
				dealersOnlineString += d.getDealerName();
			}
			
			dealersOnlineString += d.getDealerName() + ",";
		}
		
		// Update the Status of the Message.
		// Update the text area with list of the games and details.
		updateMessageSuccess(message, dealersOnlineString);
	}
	
	
	// Lists the Players within a certain game.
	// The text area should contain the game's ID that wants to display its 
	// players.
	//
	// The message will update the text area in the Message with a string with 
	// that game's players.
	private void listPlayersInGame(Message message) {
		
		// Get list of players.
		// Iterate through the list of players.
		// For each Player in the Game concat a string:
		//
		// PlayerName:Card,...,Card:Funds:CurrentBet\n
		// PlayerName:Card,...,Card:Funds:CurrentBet
		//
		// Where Card,...,Card is the players hand.
		
		String listOfPlayers = null;
		String gameID = message.getText();
		
		Game game = Server.getTargetGame(gameID);
		
		// If there is no game by supplied ID
		if(game == null) {
			updateMessageFailed(message, "Game Not Found!");
			return;
		}
		
		List<Player> players = game.getTable().getPlayers();
		
		Player lastPlayer = players.get(players.size() -1);
		
		for(Player p : players) {
			
			// If at the last player on list, print without newline character.
			if(p.equals(lastPlayer)) {
				listOfPlayers += p.getPlayerName() + ":"
							   + p.toStringPlayersHand() + ":"
							   + p.getPlayerFunds() + ":"
							   + p.getBet();
			}
			
			// Else add the details to the string WITH newline characters.
			listOfPlayers += p.getPlayerName() + ":"
					   + p.toStringPlayersHand() + ":"
					   + p.getPlayerFunds() + ":"
					   + p.getBet() + "\n";
		}
		
		// Update the Status of the Message.
		// Update the text area with list of the players and details.
		updateMessageSuccess(message, listOfPlayers);
	}
	

	// Opens/Creates a game and returns a Game ID.
	private void openGame(Message message) {

		Game newGame = new Game();
		Server.getGames().add(newGame);
		updateMessageSuccess(message, newGame.getID());
		
	}
	
	
	// Closes the game with the supplied Game ID and returns the same ID.
	private void closeGame(Message message) {

		Game gameToRemove = Server.getTargetGame(message.getText());
		
		// If we didn't find the game to remove.
		if(gameToRemove == null) {
			updateMessageFailed(message, "No Game with that ID!");
			return;
		}
		
		// Else remove the game.
		Server.getGames().remove(gameToRemove);
		updateMessageSuccess(message, message.getText());
	}		


	// The message will have the Game ID that the PlayerUser or DealerUser wants
	// to join.
	private void joinGame(Message message) {
		
		// If a Dealer wants to join a game
		if(dealerUser != null && playerUser == null) {
			
		}
		
		// If a Player wants to join a game
		if(playerUser != null && dealerUser == null) {
			
		}
		
	}

	
	// The message will have the Game ID. A playerUser or dealerUser will be 
	// removed from that game.
	private void leaveGame(Message message) {

		// If a Dealer wants to leave a game.
		if(dealerUser != null && playerUser == null) {
			
		}
		
		// If a Player wants to leave a game.
		if(playerUser != null && dealerUser == null) {
			
		}
		
	}
	
	// User join the first Open Game's Table.
	// Nothing is supplied by the message.
	// Return the Game's ID that the player has joined.
	private void quickJoin(Message message) {
		
		String gameID = null;
		List<Game> games = Server.getGames();
		
		// If there are no game send back to the Client a Failed message.
		if(games == null) {
			updateMessageFailed(message, "There are no open Games!");
			return;
		}
		
		// For every game on the server.
		for(Game g : games) {
			
			// If the Table is Open, add the player to the game/table.
			if(g.getTableStatus() == TableStatus.Open) {
				
				gameID = g.getID();
				g.addPlayer(playerUser);
				
				// Update the status of the Message as Success.
				// Send back the Client a Game's ID.
				updateMessageSuccess(message, gameID);
			}
		}
	}
}
