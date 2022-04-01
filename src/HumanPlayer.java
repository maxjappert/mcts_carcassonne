import java.util.List;
import java.util.Scanner;

public class HumanPlayer extends Player {

    public HumanPlayer(GameStateSpace stateSpace, int playerID) {
        super(stateSpace, playerID);
    }

    Pair decideOnNextMove(GameState state, Tile originalTile, List<Tile> deck, List<Move> legalMoves) {
        System.out.println("This is the tile you've drawn:");

        Scanner sc = new Scanner(System.in);

        Tile tile = new Tile(originalTile);

        Move move;
        int meeplePlacement = -1;

        while (true) {
            System.out.print("Press r to rotate or enter the coordinates of where you'd like to place the tile (e.g. 'a1'):");

            String input = sc.next().toLowerCase();

            if (input.equalsIgnoreCase("r")) {
                tile.rotate();
                tile.printTile();
                continue;
            }

            // Tries to convert the input string into coordinates. If the NumberFormatException is thrown, the loop starts
            // from the beginning and the user gets another chance to input coordinates. 97 is subtracted from the first
            // conversion because the ASCII value of 'a' is 97. 1 is subtracted from the second conversion because the
            // board starts indexing at 1, yet we need indexing starting at 0 to access the tiles in the list representation
            // of the board.
            try {
                int co1 = (input.charAt(0) - 97);
                int co2 = (Integer.parseInt(input.substring(1)) - 1);

                move = new Move(new Coordinates(co1, co2), tile.getRotation());
            } catch (NumberFormatException nfe) {
                System.out.println("You haven't provided valid coordinates as input. For example the top right corner" +
                        "is denoted by the coordinates 'a1'.");
                continue;
            }

            System.out.print("Would you like to place a meeple? [y/n] ");

            if (sc.next().equalsIgnoreCase("y")) {
                System.out.print("On which corner would you like to place the meeple? [0-12] ");
                input = sc.next().toLowerCase();

                meeplePlacement = Integer.parseInt(input);
            }
            break;
        }

        for (Move legalMove : legalMoves) {
            if (legalMove.isEqualTo(move)) {
                return new Pair(legalMoves.indexOf(legalMove), meeplePlacement);
            }
        }

        System.out.println("Illegal move.");

        return decideOnNextMove(state, originalTile, deck, legalMoves);
    }

    public String getTypeAsString() {
        return "Human Player";
    }
}
