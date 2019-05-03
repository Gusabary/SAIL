package com.example.demo;

import com.alibaba.fastjson.JSONArray;

public class MagicSquare {

    private int order = 0;
    private int[][] square;
    private String error = "";

    public MagicSquare(int order, boolean auth){
        if (!auth)
            this.error = "Authentication failed!";
        else {
            if (order <= 0)
                this.error = "Order should be positive!";
            else if (order % 2 == 0)
                this.error = "Order should be odd!";
            else {
                this.order = order;
                this.square = new int[order][order];
                this.generate();
            }
        }
    }

    public String getMessage(){
        if (error == "")
            return "Generate magic square successfully!";
        return error;
    }

    public JSONArray getSquare(){
        if (error != "")
            return null;
        JSONArray resp = new JSONArray();
        for (int i = 0; i < order; i++) {
            JSONArray row = new JSONArray();
            for (int j = 0; j < order; j++) {
                row.add(square[i][j]);
            }
            resp.add(row);
        }
        return resp;
    }

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
