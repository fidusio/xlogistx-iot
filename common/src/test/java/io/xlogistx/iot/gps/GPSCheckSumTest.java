/*
 * Copyright (c) 2012-2014 ZoxWeb.com LLC.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.xlogistx.iot.gps;

import org.zoxweb.shared.util.SharedStringUtil;

/**
 * @author mzebib
 *
 */
public class GPSCheckSumTest
{

	
	 public static int checkSum(String message) 
	 {
	  int check = 0;
	  // iterate over the string, XOR each byte with the total sum:
	  byte[] checkArray = message.getBytes();
	  for (int c = 0; c < checkArray.length; c++) 
	  {
	    check = (int) ((check) ^ checkArray[c]);
	  } 
	  	  
	  return check;
	
	 }
	 


	 
	 public static void main(String[] args)
	 {
		 
		 System.out.println(SharedStringUtil.byteToHex(null, null, (byte) checkSum("PGCMD,33,1")));
		 
		 
	 }
	 
}
