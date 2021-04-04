public class MagicSquare {

    private int order;
    private int[][] square;

    private int[] getNextPos(int[] cntPos){
        int[] nextPos = new int[2];
        if (cntPos[0] == 0 && cntPos[1] != this.order - 1){
            nextPos[0] = this.order - 1;
            nextPos[1] = cntPos[1] + 1;
        }
        if (cntPos[0] != 0 && cntPos[1] == this.order - 1){
            nextPos[0] = cntPos[0] - 1;
            nextPos[1] = 0;
        }
        if (cntPos[0] == 0 && cntPos[1] == this.order - 1){
            nextPos[0] = cntPos[0] + 1;
            nextPos[1] = cntPos[1];
        }
        if (cntPos[0] != 0 && cntPos[1] != this.order - 1){
            nextPos[0] = cntPos[0] - 1;
            nextPos[1] = cntPos[1] + 1;
        }
        return nextPos;
    }

    public MagicSquare(int order) throws Exception{
        if (order % 2 == 0)
            throw new Exception("Order should be odd!");
        this.order = order;
        this.square = new int[order][order];
    }

    public int[][] generate(){
        int value = 1;
        int[] cntPos = new int[2];
        cntPos[0] = 0;
        cntPos[1] = this.order / 2;
        this.square[cntPos[0]][cntPos[1]] = value;
        value++;

        while (value <= this.order * this.order){
            int[] nextPos=this.getNextPos(cntPos);
            if (this.square[nextPos[0]][nextPos[1]] == 0){
                cntPos = nextPos;
            }
            else{
                cntPos[0]++;
            }
            this.square[cntPos[0]][cntPos[1]] = value;
            value++;
        }

        return this.square;
    }

}
