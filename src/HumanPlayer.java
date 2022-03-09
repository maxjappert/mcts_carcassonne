import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HumanPlayer extends Player {

    public HumanPlayer(int playerID) {
        super(playerID);
    }

    int[] decideOnNextMove(GameState state, GameStateSpace stateSpace, Tile tile) throws Exception {
        List<List<Tile>> board = state.getBoard();

        tile.printTile();

        System.out.println("This is the tile you've drawn:");

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("Press r to rotate or enter the coordinates of where you'd like to place the tile (e.g. 'a1'):");

            String input = sc.next().toLowerCase();

            if (input.equalsIgnoreCase("r")) {
                tile.rotate();
                tile.printTile();
                continue;
            }

            int[] move;

            // Tries to convert the input string into coordinates. If the NumberFormatException is thrown, the loop starts
            // from the beginning and the user gets another chance to input coordinates. 97 is subtracted from the first
            // conversion because the ASCII value of 'a' is 97. 1 is subtracted from the second conversion because the
            // board starts indexing at 1, yet we need indexing starting at 0 to access the tiles in the list representation
            // of the board.
            try {
                int co1 = input.charAt(0) - 97;
                int co2 = Integer.parseInt(input.substring(1)) - 1;

                move = new int[]{co1, co2};

            } catch (NumberFormatException nfe) {
                System.out.println("You haven't provided valid coordinates as input. For example the top right corner" +
                        "is denoted by the coordinates 'a1'.");
                continue;
            }

            boolean legalMove = false;

            List<ActionRotationStateTriple> legalSuccessors = stateSpace.succ(state, tile);

            for (ActionRotationStateTriple successor : legalSuccessors) {
                if (successor.getAction()[0] == move[0] && successor.getAction()[1] == move[1]) {
                    legalMove = true;
                    break;
                }
            }

            // If the move isn't legal the loop jumps to the top.
            if (!legalMove) {
                System.out.println("The move you have entered is not allowed. Please try again.");
                continue;
            }

            while (true) {
                System.out.print("Would you like to place a meeple? [y/n] ");

                if (sc.next().equalsIgnoreCase("y")) {
                    System.out.print("On which corner would you like to place the meeple? [0-12] ");
                    input = sc.next().toLowerCase();

                    int point = Integer.parseInt(input);

                    assert (point >= 0 && point <= 12);

                    boolean legalMeeple = stateSpace.legalMeeples(state, tile, move).contains(point);
                    //boolean legalMeeple = true;

                    if (legalMeeple) {
                        tile.placeMeeple(point, playerID);
                        numberOfMeeples -= 1;
                    } else {
                        System.out.println("You can't place a meeple there. Please try again.");
                        continue;
                    }
                }

                System.out.println("** end of decideOnNextMove(...)");

                return move;
            }
        }
    }
}
