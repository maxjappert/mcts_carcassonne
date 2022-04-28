public class Main {

    public static void main(String[] args) throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();
        Player player1;
        Player player2;
        long deckRandomSeed;

        ArgParser argParser = new ArgParser();

        if (args.length == 0) {
            player1 = new MCTSPlayer(stateSpace, 1, 2f, 500, -1, 0.5f, 0, "uct", false, 0f, true);
            //player2 = new MCTSPlayer(stateSpace, 2, 2f, 50, -1, 0.5f, 0, "uct", true, 0);
            //player1 = new RandomPlayer(stateSpace, 1, -1);
            player2 = new RandomPlayer(stateSpace, 2, -1);
            deckRandomSeed = -1;
            Engine.verbose = true;
        } else {
            Player[] players = argParser.assignPlayers(args);
            player1 = players[0];
            player2 = players[1];
            deckRandomSeed = argParser.getDeckRandomSeed();
        }

        Engine engine = new Engine(player1, player2, deckRandomSeed);

        engine.play();
    }
}


