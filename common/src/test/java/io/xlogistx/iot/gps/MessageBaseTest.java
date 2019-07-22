package io.xlogistx.iot.gps;

import org.zoxweb.shared.data.DataDAO;

public class MessageBaseTest {

	public static void main(String[] args){
		
		DataDAO message = new DataDAO();

		
		message.setCreationTime(System.currentTimeMillis());
		message.setData("Mustapha".getBytes());
		message.setSourceID("MessageBaseTest");
				
		
	
		String phrase = "the music made   it \n  hard      to        concentrate \n";
		String delims = "\n";
		String[] tokens = phrase.split(delims);
		System.out.println(phrase);
		System.out.println(tokens.length);
		
		
		for (int i = 0; i < tokens.length; i++)
		    System.out.println(i + ":" + tokens[i]);
		
		
		System.out.println(message.getCreationTime());
		System.out.println(message.getData());
		System.out.println(message.getSourceID());
		System.out.println(message.toString());
		
		
		
	}
	
}
