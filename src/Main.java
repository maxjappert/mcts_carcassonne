//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Main {


    public static void main(String[] args) throws Exception {
        //createReport();
        playGame();
    }

    /**
     * The player parameters can either be of type UCTPlayer, HumanPlayer or RandomPlayer.
     */
    private static void playGame() throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();
        Engine engine = new Engine(new RandomPlayer(stateSpace, 1), new UCTPlayer(stateSpace, 2, 2f, 200));

        //Engine engine = new Engine(new HumanPlayer(stateSpace, 1), new HumanPlayer(stateSpace,2));

        int[] score = new int[]{0, 0};

        int[] roundScore = engine.play();
        score[0] += roundScore[0];
        score[1] += roundScore[1];

        System.out.printf("Score: [ %d : %d ]\n\n", score[0], score[1]);
    }

    /**
     * Tests different hyperparameters.
     */
    public static void createReport() throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        FileWriter fileWriter = new FileWriter("report_" + dtf.format(now));
        PrintWriter printWriter = new PrintWriter(fileWriter);

        int[] ti = new int[]{50, 100, 500, 1000, 5000};

        printWriter.println("Playing against a random opponent:");
        printWriter.flush();

        GameStateSpace stateSpace = new GameStateSpace();

        for (float c = 0f; c < 15; c += 0.5f) {
            for (int trainingIterations : ti) {
                Engine engine = new Engine(new UCTPlayer(stateSpace, 1, c, trainingIterations), new RandomPlayer(stateSpace, 2));
                int[] score = new int[]{0, 0};
                for (int i = 0; i < 10; i++) {
                    System.out.println("Round " + (i+1));
                    int[] roundScore = engine.play();
                    score[0] += roundScore[0];
                    score[1] += roundScore[1];
                }

                score[0] = score[0] / 10;
                score[1] = score[1] / 10;

                String line = trainingIterations + " training iterations, c = " + c + ": " + Arrays.toString(score) + "\n";
                printWriter.println(line);
                printWriter.flush();
            }
        }

        printWriter.close();
        }

}


