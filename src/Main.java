import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static final Logger logger = LoggerFactory.getLogger("MainLogger");

    public static void main(String[] args) throws Exception {
        play();
    }

    public static void play() throws Exception {
        logger.info("play() started.");

        GameStateSpace stateSpace = new GameStateSpace();
        GameState state = stateSpace.init();

        Player player1 = new HumanPlayer(1);
        Player player2 = new AIPlayer(2);

        do {
            state.displayBoard();

            Tile drawnTile = new Tile(12, false);//state.drawTile();
            drawnTile.printTile();

            int[] move = new int[]{-1, -1};

            while (move[0] == -1) {
                if (/*state.deckSize() % 2 == 0*/ true) {
                    move = player1.decideOnNextMove(state, stateSpace, drawnTile);
                } else {
                    move = player2.decideOnNextMove(state, stateSpace, drawnTile);
                }

                // In the rare case that the drawn tile cannot legally be placed, the tile is added back to the deck
                // and a new tile is drawn.
                if (move[0] == -1) {
                    logger.info("Player {} draws tile with no possible legal moves.", ((state.deckSize() % 2) + 1));
                    state.addToDeck(drawnTile);
                    drawnTile = state.drawTile();
                    System.out.println("The drawn tile cannot legally be placed. Therefore the tile has been added back" +
                            "to the deck and a new tile has been drawn:");
                    drawnTile.printTile();
                }
            }

            state.updateBoard(move, drawnTile);

            state.checkForPointsAfterRound(player1, player2);

         } while (!stateSpace.isGoal(state));

        state.displayBoard();

        state.assignPointsAtEndOfGame(player1, player2);

        System.out.println("Player 1 has " + player1.getPoints() + " points.");
        System.out.println("Player 2 has " + player2.getPoints() + " points.");

        logger.info("play() finished.");
    }
}


