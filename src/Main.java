//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

public class Main {

//    static final //logger //logger = //loggerFactory.get//logger("Main//logger");

    public static void main(String[] args) throws Exception {
        Engine engine = new Engine();

        int[] score = new int[]{0, 0};

        for (int i = 0; i < 10; i++) {
            System.out.println("Round " + (i+1));
            int[] roundScore = engine.play(new UCTPlayer(1, 2f, 100), new RandomPlayer(2));
            score[0] += roundScore[0];
            score[1] += roundScore[1];

            System.out.printf("Score after round %d: %d:%d", i+1, score[0], score[1]);
        }
    }

}


