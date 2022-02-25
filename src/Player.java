import org.jetbrains.annotations.NotNull;

import java.util.List;

abstract class Player {
    protected Tile drawnTile;

    public void draw(@NotNull List<Tile> deck) {
        drawnTile = deck.remove(0);
    }

    private boolean checkLegalMove(List<List<Tile>> board, int[] move) {
        assert (move.length == 2);

        // Even if the translation from input string to integer coordinates was successful, we still need to check
        // if the integers are valid coordinates for the board. We add 2 to the coordinates due to the fact that
        // we need to consider the buffer for actually placing new tiles. TODO: Implement the game rules' constraints.
        int[] boardDimensions = Game.getBoardDimensions(board);
        if (!(move[0] >= 0 && move[0] < boardDimensions[0] + 2 && move[1] >= 0 && move[1] < boardDimensions[1] + 2)) {
            return false;
        }

        // This array represents the four edges and if they are attached to another tile, i.e., if they need to be
        // evaluated in regard to their legality.
        boolean[] notAttached = new boolean[4];

        if (Game.getColumnSize(move[1], board) + 1 == move[0]) {
            notAttached[0] = true;
        }

        if (Game.getRowSize(move[0], board) + 1 == move[1]) {
            notAttached[1] = true;
        }

        if (move[0] == 0) {
            notAttached[2] = true;
        }

        if (move[1] == 0) {
            notAttached[3] = true;
        }

        // For all sides which are attached to another tile, we need to check if the touching sides of the tiles are
        // of the same type. If that is not the case for at least one of the tiles, then we return false, as the move
        // is not legal. TODO: Finish implementing this for all sides. Then we can use this method for checking the legality of the move.
        if (!notAttached[0] && drawnTile.getSides()[0] != board.get(move[0] + 1).get(move[1]).getSides()[2]) {
            return false;
        } else if (!notAttached[1])

        return false;
    }

    abstract int[] decideOnNextMove(List<List<Tile>> board) throws Exception;
}
