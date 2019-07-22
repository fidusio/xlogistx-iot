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

public class GPSInputTest
{


	public static void main(String[] args)
	{
		
		String command = "$PMTK" + "251,57600";
		System.out.println(command);
		System.out.println(SharedStringUtil.byteToHex(null, null, (byte) GPSMessage.checkSum(command)));
		command = command + "*" + SharedStringUtil.byteToHex(null, null, (byte) GPSMessage.checkSum(command));
		System.out.println(command);
		
		
		
		
		GPSPMTKCommand pmtk = new GPSPMTKCommand();
		
		pmtk.setPktType(605);

//		String result = null;
//		long delta1 = 0;
//		long delta2 = 0;
//		
//		for ( int i=0; i < 10; i++)
//		{
//		delta1 = System.nanoTime();
//		result  = pmtk.oldToCanonicalID();
//		delta1 = System.nanoTime() - delta1;
//
//		System.out.println(result + " : " + delta1 + " old");
//		
//		delta2 = System.nanoTime();
//		result = pmtk.toCanonicalID();
//		delta2 = System.nanoTime() - delta2;
//
//		System.out.println(result + " : " + delta2 + " diff " + (delta2 - delta1));
//		}
//		
		
		
	}
}
