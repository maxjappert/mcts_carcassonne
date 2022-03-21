import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static final Logger logger = LoggerFactory.getLogger("MainLogger");

    public static void main(String[] args) throws Exception {
        int[] score = new int[]{0, 0};

        for (int i = 0; i < 10; i++) {
            System.out.println("Round " + (i+1));
            short[] roundScore = play();
            score[0] += roundScore[0];
            score[1] += roundScore[1];
        }

        System.out.printf("Score: %d:%d", score[0], score[1]);
    }

    public static short[] play() throws Exception {
        logger.info("play() started.");

        GameStateSpace stateSpace = new GameStateSpace();
        GameState state = stateSpace.init();

        Player player1 = new UCTPlayer((byte) 1, 0.5f, (short) 150);
        Player player2 = new RandomPlayer(2);

        while (!stateSpace.isGoal(state)) {
            state.displayBoard();

            Tile drawnTile = state.drawTile();
            drawnTile.printTile();

            byte[] move = new byte[]{-1, -1};

            while (move[0] == -1) {
                if (state.deckSize() % 2 == 0) {
                    move = player1.decideOnNextMove(state, stateSpace, drawnTile);
                } else {
                    move = player2.decideOnNextMove(state, stateSpace, drawnTile);
                }

                // In the rare case that the drawn tile cannot legally be placed, the tile is added back to the deck
                // and a new tile is drawn.
                if (move[0] == -1) {
                    logger.info("Player {} draws tile with no possible legal moves. The tile is therefore redrawn.", ((state.deckSize() % 2) + 1));
                    state.addToDeck(drawnTile);
                    drawnTile = state.drawTile();
                    drawnTile.printTile();
                }
            }

            state.updateBoard(move, drawnTile);

            state.checkForScoreAfterRound();

         }

        state.displayBoard();

        state.assignPointsAtEndOfGame();

        System.out.println("Player 1 has " + state.getScore()[0] + " points.");
        System.out.println("Player 2 has " + state.getScore()[1] + " points.");

        logger.info("play() finished.");

        return state.getScore();
    }
}


