package com.simon.dockerfilepractice;

public class CalcUtil {
    public Integer binarySearch(String[] arr, String target){
        int low = 0;
        int high = arr.length - 1;
        while(low <= high){
            int mid = (low + high) / 2;
            if(arr[mid].equals(target)){
                return mid;
            }else if(arr[mid].compareTo(target) > 0){
                high = mid - 1;
            }else{
                low = mid + 1;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        CalcUtil util =new
                CalcUtil();
        String[] arr = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
        for(int i = 0; i < 100000000; i++){
            int x = util.binarySearch(arr, "a");
            System.out.println(x);
        }
    }


}
