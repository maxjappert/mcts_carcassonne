import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Tile {
    private final boolean pennant;

    /**
     *  Starting from the bottom edge, counterclockwise.
     *
     *  0: grass
     *  1: city
     *  2: road
     */
    private int[] sides;

    /**
     * Starting from bottom right.
     */
    private int[] points;

    /**
     * To which connected areas to the points belong to?
     */
    private int[] areas;

    /**
     * Describes a possible meeple on the tile. Index 0 denotes the side on which the meeple was placed and index 1
     * denotes the ID of the player whom the meeple belongs to. If there's no meeple on the tile, then both values
     * are -1.
     */
    private int[] meeple;

    private int type;

    /**
     * 0: grass
     * 1: city
     * 2: road
     * 3: intersection
     * 4: monastery
     */
    private int middle;

    private int middleArea;

    private int rotation;

    /**
     * The possible types are the following:
     * 0: City and straight road (4, including starting tile)
     * 1: City and angular road left (3)
     * 2: City and angular road right (3)
     * 3: City and intersection (3)
     * 4: 1/4 city (5)
     * 5: 2 * 1/4 city opposite to each other (3)
     * 6: 2 * 1/4 city next to each other (3)
     * 7: straight road (8)
     * 8: angular road (9)
     * 9: triple intersection (4)
     * 10: quadruple intersection (1)
     * 11: monastery with road (2)
     * 12: monastery without road (4)
     * 13: two-ended city (3)
     * 14: half a city without road (5)
     * 15: half a city with road(5)
     * 16: 3/4 city with road (3)
     * 17: 3/4 city without road (4)
     * 18: city (1)
     */
    public Tile(int type, boolean pennant) {
        this.pennant = pennant;
        this.type = type;
        this.rotation = 0;

        areas = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

        meeple = new int[]{-1, -1};

        switch (type) {
            case 0 -> {
                sides = new int[]{0, 2, 1, 2};
                points = new int[]{0, 0, 0, 0, 2, 0, 1, 1, 1, 0, 2, 0};
                middle = 2;
            }
            case 1 -> {
                sides = new int[]{2, 0, 1, 2};
                points = new int[]{0, 2, 0, 0, 0, 0, 1, 1, 1, 0, 2, 0};
                middle = 2;
            }
            case 2 -> {
                sides = new int[]{2, 2, 1, 0};
                points = new int[]{0, 2, 0, 0, 2, 0, 1, 1, 1, 0, 0, 0};
                middle = 2;
            }
            case 3 -> {
                sides = new int[]{2, 2, 1, 2};
                points = new int[]{0, 2, 0, 0, 2, 0, 1, 1, 1, 0, 2, 0};
                middle = 3;
            }
            case 4 -> {
                sides = new int[]{0, 0, 1, 0};
                points = new int[]{0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0};
                middle = 0;
            }
            case 5 -> {
                sides = new int[]{1, 0, 1, 0};
                points = new int[]{1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0};
                middle = 0;
            }
            case 6 -> {
                // TODO: this leads to a misleading representation.
                sides = new int[]{0, 0, 1, 1};
                points = new int[]{0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1};
                middle = 0;
            }
            case 7 -> {
                sides = new int[]{0, 2, 0, 2};
                points = new int[]{0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0};
                middle = 2;
            }
            case 8 -> {
                sides = new int[]{2, 0, 0, 2};
                points = new int[]{0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0};
                middle = 2;
            }
            case 9 -> {
                sides = new int[]{2, 2, 0, 2};
                points = new int[]{0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0};
                middle = 3;
            }
            case 10 -> {
                sides = new int[]{2, 2, 2, 2};
                points = new int[]{0, 2, 0, 0, 2, 0, 0, 2, 0, 0, 2, 0};
                middle = 3;
            }
            case 11 -> {
                sides = new int[]{2, 0, 0, 0};
                points = new int[]{0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                middle = 4;
            }
            case 12 -> {
                sides = new int[]{0, 0, 0, 0};
                points = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                middle = 4;
            }
            case 13 -> {
                sides = new int[]{0, 1, 0, 1};
                points = new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1};
                middle = 1;
            }
            case 14 -> {
                // TODO: this leads to a misleading representation.
                sides = new int[]{0, 0, 1, 1};
                points = new int[]{0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1};
                middle = 1;
            }
            case 15 -> {
                // There could be a problem here regarding the connection of the two roads, since the middle has to be a city.
                sides = new int[]{2, 2, 1, 1};
                points = new int[]{0, 2, 0, 0, 2, 0, 1, 1, 1, 1, 1, 1};
                middle = 1;
            }
            case 16 -> {
                sides = new int[]{2, 1, 1, 1};
                points = new int[]{0, 2, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1};
                middle = 1;
            }
            case 17 -> {
                sides = new int[]{0, 1, 1, 1};
                points = new int[]{0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1};
                middle = 1;
            }
            case 18 -> {
                sides = new int[]{1, 1, 1, 1};
                points = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
                middle = 1;
            }
        }
    }

    /**
     * Copy constructor.
     * @param tile The tile for which a deep copy should be created.
     */
    public Tile(Tile tile) {
        this.sides = Arrays.copyOf(tile.getSides(), tile.getSides().length);
        this.points = Arrays.copyOf(tile.getPoints(), 12);
        this.areas = Arrays.copyOf(tile.getAreas(), 12);
        this.meeple = Arrays.copyOf(tile.getMeeple(), 2);
        this.middle = tile.getMiddle();
        this.middleArea = tile.getMiddleArea();
        this.pennant = tile.hasPennant();
        this.rotation = tile.rotation;
        this.type = tile.type;
    }

    /**
     * Generates a printable format of the tile. The following characters denote the following things:
     * '.' -> field
     * '-' -> road
     * '|' -> road
     * '@' -> monastery
     * '#' -> city
     * '§' -> pennant
     * '1' -> meeple of player 1
     * '2' -> meeple of player 2
     * @return A 2D-char-array which when printed represents the given visually in an ASCII-format.
     * @throws Exception Is thrown if for some reason an invalid type number is used.
     */
    public char[][] getPrintFormatOfTile() throws Exception {
        char[][] output = new char[5][5];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                output[i][j] = '$';
            }
        }

        // First the edges on the top and bottom

        boolean road = false;
        char c = '£';
        if(sides[2] == 0) {
            c = '.';
        } else if (sides[2] == 1) {
            c = '#';
        } else if (sides[2] == 2) {
            output[0][2] = '|';
            output[1][2] = '|';
            road = true;
        } else {
            throw new Exception("Wrong tile code passed.");
        }

        if(!road) {
            for(int i = 0; i < 5; i++) {
                output[0][i] = c;
            }

            for(int i = 1; i < 4; i++) {
                output[1][i] = c;
            }
        }

        road = false;
        if(sides[0] == 0) {
            c = '.';
        } else if (sides[0] == 1) {
            c = '#';
        } else if (sides[0] == 2) {
            output[4][2] = '|';
            output[3][2] = '|';
            road = true;
        } else {
            throw new Exception("Wrong tile code passed.");
        }

        if(!road) {
            for(int i = 0; i < 5; i++) {
                output[4][i] = c;
            }

            for(int i = 1; i < 4; i++) {
                output[3][i] = c;
            }
        }

        // Then the edges on the sides
        road = false;
        if(sides[1] == 0) {
            c = '.';
        } else if (sides[1] == 1) {
            c = '#';
        } else if (sides[1] == 2) {
            output[2][4] = '-';
            output[2][3] = '-';
            road = true;
        } else {
            throw new Exception("Wrong tile code passed.");
        }

        if(!road) {
            for(int i = 0; i < 5; i++) {
                output[i][4] = c;
            }

            for(int i = 1; i < 4; i++) {
                output[i][3] = c;
            }

            output[2][2] = c;
        }

        road = false;
        if(sides[3] == 0) {
            c = '.';
        } else if (sides[3] == 1) {
            c = '#';
        } else if (sides[3] == 2) {
            output[2][0] = '-';
            output[2][1] = '-';
            road = true;
        } else {
            throw new Exception("Wrong tile code passed.");
        }

        if(!road) {
            for(int i = 0; i < 5; i++) {
                output[i][0] = c;
            }

            for(int i = 1; i < 4; i++) {
                output[i][1] = c;
            }

            output[2][2] = c;
        }

        if (middle == 0) {
            c = '.';
        } else if (middle == 1) {
            if (pennant) {
                c = '§';
            } else {
                c = '#';
            }
        } else if (middle == 2) {
            c = '.';
        } else if (middle == 3) {
            c = 'x';
        } else if (middle == 4) {
            c = '@';
        } else {
            throw new Exception("Wrong input for the middle of the tile.");
        }

        for(int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (output[i][j] == '$') {
                    output[i][j] = '.';
                }
            }
        }

        if (meeple[0] == 4) {
            output[2][2] = Character.forDigit(meeple[1], 10);
        } else {
            output[2][2] = c;
        }

        if (meeple[1] != -1) {
            char digit = Character.forDigit(meeple[1], 10);

            if (meeple[0] == 0) {
                output[4][1] = digit;
            } else if (meeple[0] == 1) {
                output[4][2] = digit;
            } else if (meeple[0] == 2) {
                output[4][3] = digit;
            } else if (meeple[0] == 3) {
                output[3][4] = digit;
            } else if (meeple[0] == 4) {
                output[2][4] = digit;
            } else if (meeple[0] == 5) {
                output[1][4] = digit;
            } else if (meeple[0] == 6) {
                output[0][3] = digit;
            } else if (meeple[0] == 7) {
                output[0][2] = digit;
            } else if (meeple[0] == 8) {
                output[0][1] = digit;
            } else if (meeple[0] == 9) {
                output[1][0] = digit;
            } else if (meeple[0] == 10) {
                output[2][0] = digit;
            } else if (meeple[0] == 11) {
                output[3][0] = digit;
            } else if (meeple[0] == 12) {
                output[2][2] = digit;
            }
        }

        return output;
    }

    public void printTile() throws Exception {
        char[][] printFormat = getPrintFormatOfTile();
        StringBuilder tileString = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {

                if (printFormat[i][j] == '-' && j != 4) {
                    tileString.append("--");
                } else {
                    tileString.append(printFormat[i][j]);
                    tileString.append(' ');
                }
            }
            tileString.append('\n');
        }

        System.out.println(tileString);
    }

    /**
     *  Rotates the tile 90 degrees counterclockwise.
     */
    public void rotate() {

        if (rotation == 3) {
            rotation = 0;
        } else {
            rotation++;
        }

        int temp1;
        int temp2;

        temp1 = sides[1];
        sides[1] = sides[0];
        temp2 = sides[2];
        sides[2] = temp1;
        temp1 = sides[3];
        sides[3] = temp2;
        sides[0] = temp1;

        if (meeple [0] >= 0 && meeple[0] < 3) {
            meeple[0] += 1;
        } else if (meeple[0] == 3) {
            meeple[0] = 0;
        }

        List<int[]> toRotate = new ArrayList<>();
        toRotate.add(points);
        toRotate.add(areas);

        int temp3;

        for (int[] array : toRotate) {
            assert (array.length == 12);

            temp1 = array[9];
            temp2 = array[10];
            temp3 = array[11];

            for (int i = 8; i >= 2; i -= 3) {
                array[i+3] = array[i];
                array[i+2] = array[i-1];
                array[i+1] = array[i-2];
            }

            array[0] = temp1;
            array[1] = temp2;
            array[2] = temp3;
        }
    }

    /**
     * @return The types of sides the tile has, starting from the bottom edge running counterclockwise.
     */
    public int[] getSides() {
        return sides;
    }

    public int getMiddle() {
        return middle;
    }

    public void placeMeeple(int side, int playerID) {
        assert (side >= 0 && side < 5);
        assert (playerID == 1 || playerID == 2);

        meeple = new int[]{side, playerID};
    }

    public int[] getMeeple() {
        return meeple;
    }

    public void setAreas(int[] areas) {
        this.areas = areas;
    }

    public int[] getAreas() {
        return areas;
    }

    public int getSide(int index) {
        return sides[index];
    }


    public int getPoint(int point) {
        if (point == 12) {
            return middle;
        }

        return points[point];
    }

    public int getArea(int index) {
        if (index == 12) {
            return middleArea;
        }

        return areas[index];
    }

    public void setArea(int point, int area) {
        areas[point] = area;
    }

    public boolean containsRoad() {
        for (int sideType : sides) {
            if (sideType == 2) {
                return true;
            }
        }

        return false;
    }

    public int[] getPoints() {
        return points;
    }

    public int getMiddleArea() {
        return middleArea;
    }

    public void setMiddleArea(int area) {
        middleArea = area;
    }

    public boolean hasPennant() {
        return pennant;
    }

    public boolean hasMeeple() {
        return meeple[0] != -1;
    }

    public int getType() {
        return type;
    }

    public void removeMeeple() {
        meeple = new int[]{-1, -1};
    }

    public int getRotation() {
        return rotation;
    }
}
