package me.elyowon.ArraysAndStrings;

//회문 : 앞으로 읽거나 뒤로 읽었을때 똑같은 문자열
//치환 : permutation 같은 string으로 구성되었지만 str의 순서가다른 문자열

/*
*   회문이면서 치환이 가능한 문자열을 알아보려면 매우 많은 경우의 수를 예로 들수있다.
*   그러므로 이를 수학적인 방법을 통해서 접근해야한다.
*   회문은 좌우 대칭인 문자열이고 하나를 기준으로 좌우대칭이된다.
*   그러므로 알파벳의 갯수가 홀수개인 알파벳이 하나 이상이면 회문이 될수가 없고 치환이 될수가 없는것이다.
*   이를 일반적인 함수방법과 비트연산을 통해서 풀었다 .
* */

public class StringPS3 {
    public static void main(String[] args) {
        System.out.println(isPermutationOfPalindromeBITOP("aa bb cc dd"));
        System.out.println(isPermutationOfPalindromeBITOP("aa bb cc dd e"));
        System.out.println(isPermutationOfPalindromeBITOP("aa bb cc dd e fff"));
        System.out.println(isPermutationOfPalindromeBITOP("babccb"));
    }
    // 비트연산으로 해결했다.
    private static boolean isPermutationOfPalindromeBITOP(String s){
        int bitVector = creatBitVector(s);
        return bitVector == 0 || checkExactlyOneBitSet(bitVector);
    }

    private static int creatBitVector(String s){
        int bitVector = 0;
        for(char c : s.toCharArray()){
            int x = getCharNumber(c);
            bitVector = toggle(bitVector,x);
        }
        return bitVector;
    }

    private static int toggle(int bitVector, int index){
        if(index < 0) return bitVector;
        int mask = 1 << index;
        if((bitVector & mask) == 0){
            bitVector |= mask;
        } else {
            bitVector &= ~mask;
        }
        return bitVector;
    }
    private  static boolean checkExactlyOneBitSet(int bitVector){
        return (bitVector & (bitVector-1))== 0;
    }
    private static int getCharNumber(Character c){
        int a = Character.getNumericValue('a');
        int z = Character.getNumericValue('z');
        int val = Character.getNumericValue(c);
        if( a <= val && val<=z){
            return val - a;
        } return -1;
    }

//
//    private static boolean isPermutationOfPalindrome(String s){
//        int[] table = buildCharFrequencyTable(s);
//        return checkMaxOneOdd(table);
//    }
//    private static int[] buildCharFrequencyTable(String s){
//        int[] table = new int[Character.getNumericValue('z')
//                - Character.getNumericValue('a')+1];
//        for(char c : s.toCharArray()){
//            int x = getCharNumber(c);
//            if(x!= -1){
//                table[x]++;
//            }
//        }
//        return table;
//    }
//    private static int getCharNumber(Character c){
//        int a = Character.getNumericValue('a');
//        int z = Character.getNumericValue('z');
//        int val = Character.getNumericValue(c);
//        if( a <= val && val<=z){
//            return val - a;
//        } return -1;
//    }
//    private static boolean checkMaxOneOdd(int[] table){
//        boolean foundOdd = false;
//        for(int count : table) {
//            if(count % 2 == 1){
//                if(!foundOdd){
//                    foundOdd = true;
//                } else {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
}
