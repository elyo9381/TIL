package me.elyowon.ArraysAndStrings;

public class ArrayRotateMatrix {
    public static void main(String[] args) {
        int [][] image ={
                {1,0,0,0,1},
                {0,1,0,1,0},
                {0,0,1,0,0},
                {0,0,0,0,0},
                {0,0,0,0,0}
        };
        printImage(image);
        rotation(image);
        printImage(image);
        rotation(image);
        printImage(image);
        rotation(image);
        printImage(image);
        rotation(image);
        printImage(image);

    }
    private static int[][] rotation(int [][] image){
        int tmp;
        for(int s = 0, e = image.length -1; s<e ; s++,e--){
            for(int i = s, j =e ; i<e; i++,j--){
                tmp = image[s][i];
                image[s][i] = image[i][e];
                image[i][e] = image[e][j];
                image[e][j] = image[j][s];
                image[j][s] = tmp;
            }
        }
        return image;
    }
    private static void printImage(int [][] image){
        for(int i = 0; i<image.length; i++){
            for(int j = 0; j<image[i].length; j++){
                System.out.print(image[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
