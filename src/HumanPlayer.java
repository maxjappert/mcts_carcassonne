import java.util.List;
import java.util.Scanner;

public class HumanPlayer extends Player {

    int[] decideOnNextMove(List<List<Tile>> board) throws Exception {
        System.out.println("This is the tile you've drawn:");

        drawnTile.printTile();

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("Press r to rotate or enter the coordinates of where you'd like to place the tile (e.g. 'a1'):");

            String input = sc.next().toLowerCase();

            if (input.equalsIgnoreCase("r")) {
                drawnTile.rotate();
                drawnTile.printTile();
                continue;
            }

            int[] move;

            // Tries to convert the input string into coordinates. If the NumberFormatException is thrown, the loop starts
            // from the beginning and the user gets another chance to input coordinates. 97 is subtracted from the first
            // conversion because the ASCII value of 'a' is 97. 1 is subtracted from the second conversion because the
            // board starts indexing at 1, yet we need indexing starting at 0 to access the tiles in the list representation
            // of the board. TODO: check the legality of these coordinates.
            try {
                int co1 = input.charAt(0) - 97;
                int co2 = Integer.parseInt(input.substring(1)) - 1;

                move = new int[]{co1, co2};

            } catch (NumberFormatException nfe) {
                System.out.println("You haven't provided valid coordinates as input. For example the top right corner" +
                        "is denoted by the coordinates 'a1'.");
                continue;
            }

            boolean legalMove = isLegalMove(move, drawnTile, board);

            // If the move isn't legal the loop jumps to the top.
            if (!legalMove) {
                System.out.println("The move you have entered is not allowed. Please try again.");
                continue;
            }

            return move;
        }
    }
}
