public class Main {

    public static void main(String[] args) throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();
        Player player1;
        Player player2;
        long deckRandomSeed;

        ArgParser argParser = new ArgParser();

        if (args.length == 0) {

            player1 = new MCTSPlayer(stateSpace, 1, 50f, 50, 1455086940, 0.5f, 0, "decaying-uct", "random", 50, false, 1, 1, false);
            player2 = new MCTSPlayer(stateSpace, 2, 2f, 50, -1, 0.5f, 0, "uct", "random", 0, false, 1, 1, false);
            //player2 = new RandomPlayer(stateSpace, 2, 963432227);
            deckRandomSeed = 0;
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


