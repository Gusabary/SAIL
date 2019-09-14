import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            int order = 3;
            Scanner scan = new Scanner(System.in);
            if (scan.hasNextInt()){
                order = scan.nextInt();
            }

            MagicSquare ms = new MagicSquare(order);
            int[][] square = ms.generate();

            for (int i = 0; i < square.length; i++) {
                for (int j = 0; j < square[i].length; j++) {
                    System.out.print(square[i][j]);
                    System.out.print('\t');
                }
                System.out.println();
            }
        }
        catch (Exception e){
            System.out.print(e);
        }
    }
}
