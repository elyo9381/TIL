package me.elyowon.ArraysAndStrings;

public class ArrayPS1_twoMatrix {
    public static void main(String[] args) {
        int [][] matrix = {
                {1,1,1,1},
                {1,0,1,1},
                {1,1,1,0},
                {1,0,1,1}
        };
        printImage(matrix);
        setZeroToAllZero(matrix);
        printImage(matrix);
    }
    private static void setZeroToAllZero(int[][] matrix){
        int fc = -1;
        int fr = -1;
        for(int row = 0; row<matrix.length; row++){
            for(int col =0; col<matrix[row].length; col++){
                if(matrix[row][col] == 0){
                    if(fc == -1){
                        fc = col;
                        fr = row;
                    }
                    matrix[fr][col] = 0;
                    matrix[row][fc] = 0;
                }
            }
        }
        if(fc == -1) return;
        //먼저 열을 0을 만들어줘야한다 !
        // 왜? 행을 먼저 0으로 만들어버리면 행에 포함되어잇는 정보들이 0으로 되어버려
        // 0이 있는 열의 부분을 제거 할수없으므로 열을 먼저 0으로 만들어야한다.
        for(int col = 0; col<matrix[0].length; col++){
            if(matrix[fr][col] == 0 && col !=fc)
                setColsToZero(col,matrix);
        }
        for(int row = 0; row<matrix.length; row++){
            if(matrix[row][fc] == 0)
                setRowsToZero(row,matrix);
        }
        setColsToZero(fc,matrix);
    }
    private static void setColsToZero(int col, int[][] matrix) {
        for(int row = 0; row < matrix.length; row++){
            matrix[row][col] = 0;
        }
    }
    private static void setRowsToZero(int row, int[][] matrix) {
        for(int col = 0; col<matrix[row].length; col++){
            matrix[row][col] = 0;
        }
    }
    private static void printImage(int[][] matrix){
        for(int i = 0; i<matrix.length; i++){
            for(int j = 0; j<matrix[i].length; j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
