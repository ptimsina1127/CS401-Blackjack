import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {
	static Table table;
	static Dealer dealer;
	static TableStatus tableStatus;
	static List<Player> lobby;
	private String timeStamp;
	private static Scanner gameManager = new Scanner(System.in);
	
	private static int count = 1;
    private final int id;

	public Game(Dealer dealer, List<Player> lobby) {
		this.dealer = dealer;
		this.lobby = lobby;
		table = new Table(dealer, lobby);
		tableStatus = TableStatus.Open;
		
		this.timeStamp = new Date().getCurrentDate();
		this.id = count++;	// Need a way to track game number.
	}

	public Table getTable() {
		return table;
	}
	
	public Dealer getDealer() {
		return dealer;
	}
	
	public TableStatus getTableStatus() {
		return tableStatus;
	}
	
	public List<Player> getLobby() {
		return lobby;
	}
	
	public String getTimeStamp() {
		return timeStamp;
	}
	
	public int getID() {
		return id;
	}

	public boolean isTableFull() {
		return tableStatus == TableStatus.Full;
	}

	public boolean isGameOpen() {
		return tableStatus == TableStatus.Open;
	}

	public void setDealer(Dealer dealer) {
		this.dealer = dealer;
	}

	public static void removePlayer(Player player) {
		if (tableStatus == TableStatus.Full) {
			tableStatus = TableStatus.Open;
		}
		table.clearPlayerHand(player);
		table.players.remove(player);
		lobby.remove(player);

	}

	public void addPlayer(Player player) {
		if (tableStatus != TableStatus.Full) {
			lobby.add(player);
			table.players.add(player);
			if (table.players.size() == 7) {
				tableStatus = TableStatus.Full;
			}
		}
	}

	public static void getBets() {
		double betValue;
		for (int i = 0; i < table.players.size(); i++) {
			if (table.players.get(i).getPlayerFunds() > 0) {
				do {
					System.out.print("How much do you want to bet " + table.players.get(i).getPlayerName()
							+ (" (1-" + table.players.get(i).getPlayerFunds()) + ")? ");
					betValue = gameManager.nextDouble();
					table.players.get(i).setBet(betValue);
				} while (!(betValue > 0 && betValue <= table.players.get(i).getPlayerFunds()));
				System.out.println("");
			}

		}
	}

	public static void checkBlackjack() {
		// System.out.println();

		if (dealer.doesTheDealerHaveBlackJack()) {
			System.out.println("Dealer has BlackJack!");
			for (int i = 0; i < table.players.size(); i++) {
				if (table.players.get(i).calculateHandTotal() == 21) {
					System.out.println(table.players.get(i).getPlayerName() + " pushes");
					table.players.get(i).pushed();
				} else {
					System.out.println(table.players.get(i).getPlayerName() + " loses");
					table.players.get(i).lostBet();
				}
			}
		} else {
			if (dealer.peek()) {
				System.out.println("Dealer peeks and does not have a BlackJack");
			}

			for (int i = 0; i < table.players.size(); i++) {
				if (table.players.get(i).calculateHandTotal() == 21) {
					System.out.println(table.players.get(i).getPlayerName() + " has blackjack!");
					table.players.get(i).calculateHandTotal();
				}
			}
		}
	}

	public static void hitOrStand() {
		String command;
		char c;
		for (int i = 0; i < table.players.size(); i++) {
			if (table.players.get(i).getBet() > 0) {
				System.out.println();
				System.out
						.println(table.players.get(i).getPlayerName() + " has " + table.players.get(i).hand.toString());

				do {
					do {
						System.out.print(table.players.get(i).getPlayerName() + " (H)it or (S)tand? ");
						command = gameManager.next();
						c = command.toUpperCase().charAt(0);
					} while (!(c == 'H' || c == 'S'));
					if (c == 'H') {
						table.addCardToPlayerHand(table.players.get(i), table.deal());
						System.out.println(table.players.get(i).getPlayerName() + " has "
								+ table.players.get(i).getPlayerHand().toString());
					}
				} while (c != 'S' && table.players.get(i).calculateHandTotal() <= 21);
			}
		}
	}

	public static void settleBets() {
		System.out.println();

		for (int i = 0; i < table.players.size(); i++) {
			if (table.players.get(i).getBet() > 0) {
				if (table.players.get(i).calculateHandTotal() > 21) {
					System.out.println(table.players.get(i).getPlayerName() + " has busted");
					table.players.get(i).lostBet();
				} else if (table.players.get(i).calculateHandTotal() == dealer.calculateHandTotal()) {
					System.out.println(table.players.get(i).getPlayerName() + " has pushed");
					table.players.get(i).pushed();
				} else if (table.players.get(i).calculateHandTotal() < dealer.calculateHandTotal()
						&& dealer.calculateHandTotal() <= 21) {
					System.out.println(table.players.get(i).getPlayerName() + " has lost");
					table.players.get(i).lostBet();
				} else if (table.players.get(i).calculateHandTotal() == 21) {
					System.out.println(table.players.get(i).getPlayerName() + " has won with blackjack!");
					table.players.get(i).hasBlackjack();
				} else {
					System.out.println(table.players.get(i).getPlayerName() + " has won");
					table.players.get(i).wonBet();

				}
			}
		}

	}

	public static void dealerTurn() {

		System.out.println();
		while (dealer.calculateHandTotal() <= 16) {
			System.out.println("Dealer has " + dealer.calculateHandTotal() + " and hits");
			table.addCardToDealerHand(dealer, table.deck.dealACard());
			System.out.println("Dealer " + dealer.getDealerHand().toString());
		}
		if (dealer.calculateHandTotal() > 21) {
			System.out.println("Dealer busts. " + dealer.getDealerHand().toString());
		} else {
			System.out.println("Dealer stands. " + dealer.getDealerHand().toString());
		}

	}

	public static void printFunds() {
		for (int i = 0; i < table.players.size(); i++) {
			if (table.players.get(i).getPlayerFunds() > 0) {
				System.out.println(
						table.players.get(i).getPlayerName() + " has " + table.players.get(i).getPlayerFunds());
			}
			if (table.players.get(i).getPlayerFunds() == 0) {
				System.out.println(table.players.get(i).getPlayerName() + " has "
						+ table.players.get(i).getPlayerFunds() + " and is out of the game.");
				removePlayer(table.players.get(i));
			}
		}
	}

	public static void clearHands() {
		for (int i = 0; i < table.players.size(); i++) {
			table.players.get(i).clearHand();
		}
		dealer.clearHand();
	}

	public static void printHands() {
		for (int i = 0; i < table.players.size(); i++) {
			if (table.players.get(i).getPlayerFunds() > 0) {
				System.out.println(table.players.get(i).getPlayerName() + " has "
						+ table.players.get(i).getPlayerHand().toString());
			}
		}
		System.out.println("Dealer has " + dealer.hand.toString());
	}

	public static void main(String[] args) {
		Dealer dealer = new Dealer("Billy", 1000);
		List<Player> testPlayers = new ArrayList<Player>();

		Player testPlayer = new Player("Bob", 1000);
		testPlayers.add(testPlayer);

		Game testGame = new Game(dealer, testPlayers);

		table.shuffleCards();
		getBets();
		table.dealCards();

		checkBlackjack();
		hitOrStand();
		dealerTurn();
		settleBets();
		printFunds();
		clearHands();

		/*
		 * testGame.table.dealCards(); testGame.table.dealCards();
		 * testGame.table.dealCards();
		 * 
		 * testPlayer.setBet(500); dealer.setBet(500);
		 * 
		 * System.out.println("Player, " + testPlayer.getPlayerName() + " has bet: " +
		 * testPlayer.getBet()); System.out.println("Dealer, " + dealer.getDealerName()
		 * + " has bet: " + dealer.getBet());
		 * 
		 * testPlayer.wonBet(); dealer.lostBet();
		 * 
		 * System.out .println("Player, " + testPlayer.getPlayerName() +
		 * " has total funds: " + testPlayer.getPlayerFunds());
		 * System.out.println("Dealer, " + dealer.getDealerName() + " has total funds: "
		 * + dealer.getCasinoFunds());
		 */

	}
}
