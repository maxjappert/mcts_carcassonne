import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MinimaxPlayer extends Player {
    Random random;
    int depth;
    float meeplePlacementProbability;

    protected MinimaxPlayer(GameStateSpace stateSpace, int playerID, long seed, int depth, float meeplePlacementProbability) {
        super(stateSpace, playerID);

        random = seed == -1 ? new Random() : new Random(seed);
        this.depth = depth;
        this.meeplePlacementProbability = meeplePlacementProbability;
    }

    @Override
    Pair decideOnNextMove(GameState state, Tile tile, List<Tile> deck, List<Move> legalMoves) {
        List<Move> successors = stateSpace.placementSucc(state, tile);
        List<Integer> values = new ArrayList<>(successors.size());
        int indexOfMaxValue = -1;
        int maxValue = Integer.MIN_VALUE;

        for (int i = 0; i < successors.size(); i++) {
            Move move = successors.get(i);
            GameState newState = new GameState(state);
            Tile newTile = new Tile(tile);
            newTile.rotateBy(move.getRotation());

            newState.updateBoard(move.getCoords(), newTile);

            int value = minimax(newState, deck, 0, true, Integer.MIN_VALUE, Integer.MAX_VALUE);

            values.add(value);

            if (value > maxValue) {
                maxValue = value;
                indexOfMaxValue = i;
            }
        }

        int meeplePlacement = -1;


        if (random.nextFloat() < meeplePlacementProbability) {
            Move move = successors.get(indexOfMaxValue);
            Tile newTile = new Tile(tile);
            newTile.rotateBy(move.getRotation());
            List<Integer> meepleSuccessors = stateSpace.meepleSucc(state, newTile, move.getCoords(), playerID);
            int index = stateSpace.getIndexOfBestMeeplePlacement(state, newTile, meepleSuccessors, state.getPlayer(), random);
            meeplePlacement = meepleSuccessors.get(index);
            System.out.println("");
        }

        return new Pair(indexOfMaxValue, meeplePlacement);
    }

    private int minimax(GameState state, List<Tile> originalDeck, int depth, boolean isMaximisingPlayer, int alpha, int beta) {
        List<Tile> deck = new ArrayList<>(List.copyOf(originalDeck));

        if (stateSpace.isGoal(state) || depth == this.depth) {
            return defaultPolicy(state, deck);
        }

        Tile drawnTile = Engine.drawTile(deck);
        List<Move> moves = stateSpace.placementSucc(state, drawnTile);

        if (isMaximisingPlayer) {
            int value = Integer.MIN_VALUE;
            for (Move move : moves) {
                GameState newState = new GameState(state);
                Tile tile = new Tile(drawnTile);
                tile.rotateBy(move.getRotation());

                if (random.nextFloat() < meeplePlacementProbability) {
                    List<Integer> meepleSuccessors = stateSpace.meepleSucc(newState, tile, move.getCoords(), playerID == 1 ? 1 : 2);
                    tile.placeMeeple(meepleSuccessors.get(stateSpace.getIndexOfBestMeeplePlacement(newState, tile, meepleSuccessors, newState.getPlayer(), random)), playerID);
                }

                newState.updateBoard(move.getCoords(), tile);
                newState.checkForScoreAfterRound(false);
                value = Math.max(value, minimax(newState, deck, depth+1, false, alpha, beta));
                if (value >= beta) {
                    break;
                }
                alpha = Math.max(alpha, value);
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            for (Move move : moves) {
                GameState newState = new GameState(state);
                Tile tile = new Tile(drawnTile);
                tile.rotateBy(move.getRotation());

                if (random.nextFloat() < meeplePlacementProbability) {
                    List<Integer> meepleSuccessors = stateSpace.meepleSucc(newState, tile, move.getCoords(), playerID == 1 ? 2 : 1);
                    tile.placeMeeple(meepleSuccessors.get(stateSpace.getIndexOfBestMeeplePlacement(newState, tile, meepleSuccessors, newState.getPlayer(), random)), playerID == 1 ? 2 : 1);
                }

                newState.updateBoard(move.getCoords(), tile);
                newState.checkForScoreAfterRound(false);
                value = Math.min(value, minimax(newState, deck, depth+1, true, alpha, beta));
                if (value <= alpha) {
                    break;
                }
                beta = Math.min(beta, value);
            }
            return value;
        }
    }

    private int defaultPolicy(GameState originalState, List<Tile> originalDeck) {
        GameState state = new GameState(originalState);
        List<Tile> deck = new ArrayList<>(List.copyOf(originalDeck));

        while (deck.size() > 0) {
            Tile drawnTile = Engine.drawTile(deck);
            Tile tile = new Tile(drawnTile);

            List<Move> actions = stateSpace.placementSucc(state, tile);

            if (actions.isEmpty()) {
                deck.add(tile);
                Collections.shuffle(deck, random);
                continue;
            }

            Move action = actions.get(random.nextInt(actions.size()));
            for (int i = 0; i < action.getRotation(); i++) {
                tile.rotate();
            }

            if (random.nextFloat() < 0.5) {
                List<Integer> legalMeeples = stateSpace.meepleSucc(state, tile, action.getCoords(), playerID);

                if (!legalMeeples.isEmpty()) {
                    int meeplePlacement = legalMeeples.get(random.nextInt(legalMeeples.size()));

                    if (meeplePlacement > -1) {
                        tile.placeMeeple(meeplePlacement, state.getPlayer());
                    }
                }
            }

            state.updateBoard(action.getCoords(), tile);

            state.checkForScoreAfterRound(false);
        }

        state.assignPointsAtEndOfGame();
        return state.getScore()[playerID - 1];
    }

    @Override
    String getTypeAsString() {
        return "minimax";
    }
}
