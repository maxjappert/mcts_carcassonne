import org.jetbrains.annotations.NotNull;

import java.util.List;

abstract class Player {
    protected Tile drawnTile;

    public void draw(@NotNull List<Tile> deck) {
        drawnTile = deck.remove(0);
    }

    public Tile getDrawnTile() {
        return drawnTile;
    }

    /**
     * Checks if a move is legal.
     * @param board The board on which the move should be executed.
     * @param move The coordinates of where the tile should be placed.
     * @return True if the move is legal.
     */
//    protected boolean checkLegalMove(List<List<Tile>> board, int[] move) {
//        assert (move.length == 2);
//
//        // Even if the translation from input string to integer coordinates was successful, we still need to check
//        // if the integers are valid coordinates for the board. We add 2 to the coordinates due to the fact that
//        // we need to consider the buffer for actually placing new tiles. TODO: Implement the game rules' constraints.
//        int[] boardDimensions = Game.getBoardDimensions(board);
//        if (!(move[0] >= 0 && move[0] < boardDimensions[0] + 2 && move[1] >= 0 && move[1] < boardDimensions[1] + 2)) {
//            return false;
//        }
//
//        // This array represents the four edges and their value corresponds to if they are attached to another tile,
//        // i.e., if they need to be evaluated in regard to their legality.
//        boolean[] attached = new boolean[]{true, true, true, true};
//
//        if (Game.getTile(board, new int[]{move[0] + 1, move[1]}) == null) {
//            attached[0] = false;
//        }
//
//        if (Game.getTile(board, new int[]{move[0], move[1] + 1}) == null) {
//            attached[1] = false;
//        }
//
//        if (Game.getTile(board, new int[]{move[0] - 1, move[1]}) == null) {
//            attached[2] = false;
//        }
//
//        if (Game.getTile(board, new int[]{move[0], move[1] - 1}) == null) {
//            attached[3] = false;
//        }
//
//        // For all sides which are attached to another tile, we need to check if the touching sides of the tiles are
//        // of the same type. If that is not the case for at least one of the tiles, then we return false, as the move
//        // is not legal.
//
//        if (attached[0]) {
//            if (drawnTile.getSides()[0] != Game.getTile(board, new int[]{move[0] + 1, move[1]}).getSides()[2]) {
//                return false;
//            }
//        }
//
//        if (attached[1]) {
//            if (drawnTile.getSides()[1] != Game.getTile(board, new int[]{move[0], move[1] + 1}).getSides()[3]) {
//                return false;
//            }
//        }
//
//        if (attached[2]) {
//            if (drawnTile.getSides()[2] != Game.getTile(board, new int[]{move[0] - 1, move[1]}).getSides()[0]) {
//                return false;
//            }
//        }
//
//        if (attached[3]) {
//            if (drawnTile.getSides()[3] != Game.getTile(board, new int[]{move[0], move[1] - 1}).getSides()[1]) {
//                return false;
//            }
//        }
//
//        // If this method hasn't returned false yet, then the sufficient criteria for the move being legal are met.
//        return true;
//    }

    // TODO: For now all moves are legals
    protected boolean checkLegalMove(List<List<Tile>> board, int[] move) {
        return true;
    }

    public void removeTile() {
        drawnTile = null;
    }

    abstract int[] decideOnNextMove(List<List<Tile>> board) throws Exception;
}
