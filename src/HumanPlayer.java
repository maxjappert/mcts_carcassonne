//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import java.util.List;
import java.util.Scanner;

public class HumanPlayer extends Player {

//    static final //logger //logger = //loggerFactory.get//logger("HumanPlayer//logger");

    public HumanPlayer(GameStateSpace stateSpace, int playerID) {
        super(stateSpace, playerID);
    }

    Coordinates decideOnNextMove(GameState state, Tile tile, List<Tile> deck) throws Exception {
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

            Coordinates move;

            // Tries to convert the input string into coordinates. If the NumberFormatException is thrown, the loop starts
            // from the beginning and the user gets another chance to input coordinates. 97 is subtracted from the first
            // conversion because the ASCII value of 'a' is 97. 1 is subtracted from the second conversion because the
            // board starts indexing at 1, yet we need indexing starting at 0 to access the tiles in the list representation
            // of the board.
            try {
                int co1 = (int) (input.charAt(0) - 97);
                int co2 = (int) (Integer.parseInt(input.substring(1)) - 1);

                move = new Coordinates(co1, co2);

            } catch (NumberFormatException nfe) {
                System.out.println("You haven't provided valid coordinates as input. For example the top right corner" +
                        "is denoted by the coordinates 'a1'.");
                continue;
            }

            boolean legalMove = false;

            List<Move> legalSuccessors = stateSpace.succ(state, tile);

            if (legalSuccessors.isEmpty()) {
                return new Coordinates(-1, -1);
            }

            for (Move successor : legalSuccessors) {
                if (successor.getCoords().x == move.x && successor.getCoords().y == move.y) {
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

                    boolean legalMeeple = stateSpace.legalMeeples(state, tile, move, playerID).contains(point);

                    if (legalMeeple) {
                        state.placeMeeple(point, playerID, tile);
                        //logger.info("Player {} places meeple on point {}. {} meeples remaining", tile.getMeeple()[1], tile.getMeeple()[0], state.getNumMeeples(playerID));

                    } else {
                        System.out.println("You can't place a meeple there. Please try again.");
                        continue;
                    }
                }

                return move;
            }
        }
    }
}
