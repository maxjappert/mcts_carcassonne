public class Main {

    public static void main(String[] args) throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();
        Player player1;
        Player player2;
        long deckRandomSeed;
        boolean verbose;

        ArgParser argParser = new ArgParser();

        if (args.length == 0) {
            player1 = new MCTSPlayer(stateSpace, 1, 2f, 150, 55, 0.5f, 0, "uct", false, 0f);
            player2 = new MCTSPlayer(stateSpace, 2, 2f, 150, 66, 0.5f, 0, "uct", false, 0.05f);
            //player1 = new RandomPlayer(stateSpace, 1, -1);
            //player2 = new RandomPlayer(stateSpace, 2, -1);
            deckRandomSeed = 77;
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


