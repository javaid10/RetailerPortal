package com.mora.murabaha.javaservice.authentication;

public class test {
	public static void main(String ar[]) {
		
	        String[] AlphaNumericArray = {"01234ABCDEFGHIJKL56789MNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
	                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKL56789MNOPQRSTUVWXYZ01234"};
	        String AlphaNumericString = AlphaNumericArray[(int) (AlphaNumericArray.length * Math.random())];
	       
	        StringBuilder sb = new StringBuilder(17);
	        sb.append("MORA-");
	        for (int i = 0; i < 11; i++) {
	            if (i == 6) {
	                sb.append("-");
	            }
	            int index = (int) (AlphaNumericString.length() * Math.random());
	            System.out.println(index);
	            System.out.println(AlphaNumericString.charAt(index));
	            sb.append(AlphaNumericString.charAt(index));
	        }
	    System.out.println(sb.toString());
	}
}
