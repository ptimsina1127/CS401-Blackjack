//Black Jack GUI
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.List;


public class BlackjackGUI {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private String iconPath = "Cards/icon.png"; // Path to the icon image
    private String credentials;
    private JTextArea gameListArea, playerListArea;
    private boolean playButtonClicked = false;
    private boolean viewGamesButtonClicked = false;
    private boolean viewPlayersButtonClicked = false;
    private Semaphore loginSemaphore = new Semaphore(0);
    private Semaphore buttonClicksemaphore = new Semaphore(0);
    private List<fakeCard> dealerCards = new ArrayList<>();
    private List<fakeCard> playerCards = new ArrayList<>();
    private JPanel dealerPanel;
    private JPanel playerPanel;

    public BlackjackGUI() {
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("BLACKJACK");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(new Color(32, 32, 32)); // A dark background for the card panel

        initializeLoginPanel();
        initializeGamePanel();
        initializeBlackjackTablePanel();
        initializeGameListPanel();
        initializePlayerListPanel();

        frame.add(cardPanel);
        frame.setVisible(true);
    }

    private void initializeLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(32, 32, 32)); // Dark background
        GridBagConstraints gbc = new GridBagConstraints();

        // Adding the icon to the side of the login panel
        ImageIcon icon = new ImageIcon(iconPath);
        JLabel iconLabel = new JLabel(icon);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        loginPanel.add(iconLabel, gbc);

        // Resetting constraints for other components
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel welcomeLabel = new JLabel("WELCOME");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 1;
        gbc.insets = new Insets(10, 0, 20, 0);
        loginPanel.add(welcomeLabel, gbc);

        JLabel signInLabel = new JLabel("Sign In");
        signInLabel.setForeground(Color.WHITE);
        signInLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridy++;
        loginPanel.add(signInLabel, gbc);

        JTextField usernameField = new JTextField(20);
        customizeComponent(usernameField);
        gbc.gridy++;
        loginPanel.add(usernameField, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        customizeComponent(passwordField);
        gbc.gridy++;
        loginPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("LOGIN");
        customizeButton(loginButton);
        gbc.gridy++;
        loginPanel.add(loginButton, gbc);
        
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();  // Fetch the username entered
            String password = new String(passwordField.getPassword());  // Fetch the password entered
            credentials = username + ":" + password;  // Combine username and password with a colon separator 
            notifyLogin(); // Notify the client that login is complete
            cardLayout.show(cardPanel, "Game");  // Move to the game panel
        });

        JButton registerButton = new JButton("SIGN UP NOW");
        customizeButton(registerButton);
        gbc.gridy++;
        loginPanel.add(registerButton, gbc);

        cardPanel.add(loginPanel, "Login");
        loginButton.addActionListener(e -> cardLayout.show(cardPanel, "Game"));
        registerButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Registration not implemented"));
    }

    private void initializeGamePanel() {
        JPanel gamePanel = new JPanel(new GridBagLayout());
        gamePanel.setBackground(new Color(0, 102, 0)); // Set to a green resembling a blackjack table
        GridBagConstraints gbcGame = new GridBagConstraints();
        gbcGame.gridwidth = GridBagConstraints.REMAINDER;
        gbcGame.anchor = GridBagConstraints.CENTER;
        gbcGame.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcomeToBlackjack = new JLabel("Welcome to BLACKJACK!", SwingConstants.CENTER);
        welcomeToBlackjack.setForeground(Color.WHITE); 
        welcomeToBlackjack.setFont(new Font("Arial", Font.BOLD, 40));
        gbcGame.insets = new Insets(20, 0, 20, 0);
        gamePanel.add(welcomeToBlackjack, gbcGame);

        // Quick Join Button replaces Play button
        JButton quickJoinButton = new JButton("Quick Join");
        customizeButton(quickJoinButton);
        quickJoinButton.addActionListener(e -> {
            quickJoin(); // Handle quick join functionality
        });
        gbcGame.insets = new Insets(10, 0, 10, 0);
        gamePanel.add(quickJoinButton, gbcGame);

        JButton viewGamesButton = new JButton("View Game List");
        customizeButton(viewGamesButton);
        viewGamesButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Game List");
        });
        gamePanel.add(viewGamesButton, gbcGame);
        
        JButton openGameButton = new JButton("Open Game");
        customizeButton(openGameButton);
        openGameButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "BlackjackTable"); 
        });
        gbcGame.insets = new Insets(10, 0, 10, 0);
        gamePanel.add(openGameButton, gbcGame);

        JButton viewPlayersButton = new JButton("View Player List");
        customizeButton(viewPlayersButton);
        viewPlayersButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Player List");
        });
        gamePanel.add(viewPlayersButton, gbcGame);

        JButton exitButton = new JButton("EXIT");
        customizeButton(exitButton);
        exitButton.addActionListener(e -> frame.dispose());
        gamePanel.add(exitButton, gbcGame);

        JLabel footerLabel = new JLabel("This game is brought to you by Group 5", SwingConstants.CENTER);
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gamePanel.add(footerLabel, gbcGame);

        cardPanel.add(gamePanel, "Game");
    }

    private void initializeBlackjackTablePanel() {
    	dealerPanel = new JPanel();
        playerPanel = new JPanel();
        JPanel blackjackTablePanel = new JPanel(new BorderLayout());
        blackjackTablePanel.setBackground(new Color(0, 102, 0)); // Green table background

        // Dealer area
        JPanel dealerPanel = new JPanel();
        dealerPanel.setBackground(new Color(0, 150, 0));
        JLabel dealerLabel = new JLabel("Dealer: ");
        dealerLabel.setForeground(Color.WHITE);
        dealerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        dealerPanel.add(dealerLabel);


        JLabel testLabel = new JLabel(new ImageIcon("Cards/icon.png"));//testing purposes
        dealerPanel.add(testLabel);
        dealerPanel.revalidate();
        dealerPanel.repaint();
        

        // Assuming dealerCards is a List<fakeCard> holding the dealer's cards
        displayCards(dealerPanel, dealerCards); // You need to manage when and how to update this list
        blackjackTablePanel.add(dealerPanel, BorderLayout.NORTH);

        // Player area
        JPanel playerPanel = new JPanel();
        playerPanel.setBackground(new Color(0, 150, 0));
        playerPanel.setLayout(new GridLayout(0, 1)); // Vertical layout for multiple players
        JLabel playerLabel = new JLabel("Player: ");
        playerLabel.setForeground(Color.WHITE);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        playerPanel.add(playerLabel);

        // Assuming playerCards is a List<fakeCard> holding the player's cards
        displayCards(playerPanel, playerCards); // Update this similarly

        blackjackTablePanel.add(playerPanel, BorderLayout.CENTER);

        // Status and control area
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(new Color(0, 100, 0));
        JLabel statusLabel = new JLabel("Status: Waiting for players...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusPanel.add(statusLabel);
        blackjackTablePanel.add(statusPanel, BorderLayout.SOUTH);

        cardPanel.add(blackjackTablePanel, "BlackjackTable");
    }
    
    private void initializeGameListPanel() {
        JPanel gameListPanel = new JPanel(new BorderLayout());
        gameListArea = new JTextArea(10, 50);
        gameListArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(gameListArea);
        gameListPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshGamesButton = new JButton("Refresh Games");
        refreshGamesButton.addActionListener(e -> refreshGames());
        gameListPanel.add(refreshGamesButton, BorderLayout.SOUTH);

        cardPanel.add(gameListPanel, "Game List");
    }
    
    private void initializePlayerListPanel() {
        JPanel playerListPanel = new JPanel(new BorderLayout());
        playerListArea = new JTextArea(10, 50);
        playerListArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(playerListArea);
        playerListPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshPlayersButton = new JButton("Refresh Players");
        refreshPlayersButton.addActionListener(e -> refreshPlayers());
        playerListPanel.add(refreshPlayersButton, BorderLayout.SOUTH);

        cardPanel.add(playerListPanel, "Player List");
    }
    
    
    private void displayCards(JPanel panel, List<fakeCard> cards) {
        panel.removeAll(); // Clear previous card images
        if (cards != null) {
            for (fakeCard card : cards) {
                ImageIcon icon = new ImageIcon(card.getImage().getImage());
                JLabel cardLabel = new JLabel(icon);
                panel.add(cardLabel);
            }
        }
        panel.revalidate();
        panel.repaint();
    }
    
    
    public void dealToDealer(fakeCard card) {
        dealerCards.add(card);
        displayCards(dealerPanel, dealerCards); // Assuming dealerPanel is accessible here
    }

    public void dealToPlayer(fakeCard card) {
        playerCards.add(card);
        displayCards(playerPanel, playerCards); // Assuming playerPanel is accessible here
    }
    
    
    
    private void quickJoin() {
        // Mock implementation, replace with actual server call
        Message message = new Message(); // Assume Message is a valid type
        List<Game> games = Server.getGames(); // This call would actually be to the client, which talks to the server

        String gameID = null;
        if (games == null || games.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "There are no open Games!", "Quick Join Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (Game g : games) {
            if (g.getTableStatus() == TableStatus.Open) {
                gameID = g.getID();
                // Assuming addPlayer is a method that adds a player to the game
                g.addPlayer(playerUser); 
                break;
            }
        }

        if (gameID != null) {
            JOptionPane.showMessageDialog(frame, "Joined game: " + gameID, "Quick Join Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "No open games available.", "Quick Join Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
    private void refreshGames() {
        // Simulation: Fetch game list from server
        List<Game> games = Server.getGames(); 
        StringBuilder sb = new StringBuilder();
        for (Game game : games) {
            sb.append(game.toString()).append("\n"); 
        }
        gameListArea.setText(sb.toString());
    }

    private void refreshPlayers() {
        // Simulation: Fetch player list from server
        List<Player> players = Server.getOnlinePlayers(); 
        StringBuilder sb = new StringBuilder();
        for (Player player : players) {
            sb.append(player.getPlayerName()).append("\n"); 
        }
        playerListArea.setText(sb.toString());
    }
    

    private void customizeComponent(JComponent component) {
        component.setPreferredSize(new Dimension(150, 30));
        component.setFont(new Font("Arial", Font.PLAIN, 14));
        component.setForeground(Color.WHITE);
        component.setBackground(new Color(64, 64, 64));
        if (component instanceof JTextField) {
            ((JTextField) component).setCaretColor(Color.WHITE);
        }
    }

    private void customizeButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(0, 123, 255));
        button.setForeground(Color.WHITE);
    }

    public void navigateToTablePanel() {
        cardLayout.show(cardPanel, "BlackjackTable");
    }// for testing purposes
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(BlackjackGUI::new);
    }

	public String getLoginCredentials() {
		// Somehow we should manage to return string to client in string format. 
		return credentials;
	}
	
	public void notifyLogin() {
        loginSemaphore.release();
    }
	public String waitForLogin() {
        try {
            loginSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return credentials;
    }
	public String buttonClicks() {
	//Logic to send what button is clicked to Client in string format	
		 // Check which button was clicked and return the corresponding action
		waitForButtonClick();
	    if (playButtonClicked) {
	        return "playButtonClicked";
	    } else if (viewGamesButtonClicked) {
	        return "viewGamesButtonClicked";
	    } else if (viewPlayersButtonClicked) {
	        return "viewPlayersButtonClicked";
	    } else {
	        // Default action if no button is clicked
	        return "noButtonClick";
	    }
	}

	public void waitForButtonClick() {
		try {
	        buttonClicksemaphore.acquire(); // Wait until a button is clicked
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
		
	}
}