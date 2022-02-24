public class HumanPlayer extends Player {

    @Override
    public String decideOnNextMove() throws Exception {
        System.out.println("This is the tile you've drawn:");

        drawnTile.printTile();

        System.out.println("Press r to rotate ");

        return "";
    }
}
