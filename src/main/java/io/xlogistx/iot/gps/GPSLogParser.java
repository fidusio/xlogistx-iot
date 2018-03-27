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

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to filter GPS log files.
 * @author mzebib
 *
 */
public class GPSLogParser 
{

	/**
	 * This method parses GPS log files by removing unrelated
	 * lines/text based on a specified delimiter and stores
	 * the GPS messages in an array list.
	 * @param list
	 * @param delimiter
	 * @return
	 */
	public static List<String> logParser(List<String> list, String delimiter)
	{
		
		List<String> filteredList = new ArrayList<String>();
		
		for(String toFilter:list)
		{
			int matchIndex = toFilter.indexOf(delimiter);
			if(matchIndex != -1)
			{
				filteredList.add(toFilter.substring(matchIndex));
			}
			
		}	
		
		return filteredList;
		
	}
	
}
