import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        play();
    }

    public static void play() throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();
        GameState state = new GameState();

        Player player1 = new HumanPlayer(1);
        Player player2 = new HumanPlayer(2);

        while (true) {
            state.displayBoard();

            //Tile drawnTile = state.drawTile();
            Tile drawnTile = new Tile(8, false);

            int[] move = player1.decideOnNextMove(state, stateSpace, drawnTile);

            state.updateBoard(move, drawnTile);
        }
    }
}


