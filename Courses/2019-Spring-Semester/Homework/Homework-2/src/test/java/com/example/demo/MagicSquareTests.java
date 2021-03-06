package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MagicSquareTests {

    @Test
    public void testGenerate() {
        int order = 5;
        try {
            MagicSquare ms = new MagicSquare(order, true);
            int[][] square = ms.generate();
            int sum = 0;
            for (int i = 0; i < order; i++)
                sum += square[0][i];

            //test row
            for (int i = 1; i < order; i++) {
                int rowSum = 0;
                for (int j = 0; j < order; j++) {
                    rowSum += square[i][j];
                }
                assertEquals(sum, rowSum);
            }

            //test column
            for (int i = 0; i < order; i++) {
                int colSum = 0;
                for (int j = 0; j < order; j++) {
                    colSum += square[j][i];
                }
                assertEquals(sum, colSum);
            }

            //test diagonal
            int leftDiagSum = 0, rightDiagSum = 0;
            for (int i = 0; i < order; i++) {
                leftDiagSum += square[i][i];
                rightDiagSum += square[order - 1 - i][i];
            }
            assertEquals(sum, leftDiagSum);
            assertEquals(sum, rightDiagSum);
        }
        catch (Exception e){
            System.out.print(e);
        }
    }

}
