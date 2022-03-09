import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        play();
    }

    public static void play() throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();
        GameState state = new GameState();

        Player player1 = new AIPlayer(1);
        Player player2 = new AIPlayer(2);

        do {
            state.displayBoard();

            Tile drawnTile = state.drawTile();
            //Tile drawnTile = new Tile(4, false);

            int[] move = new int[]{-1, -1};

            while (move[0] == -1) {
                if (state.deckSize() % 2 == 0) {
                    move = player1.decideOnNextMove(state, stateSpace, drawnTile);
                } else {
                    move = player2.decideOnNextMove(state, stateSpace, drawnTile);
                }
            }

            state.updateBoard(move, drawnTile);

            state.checkForPointsAfterRound(player1, player2);

        } while (state.deckSize() > 0);
    }
}


