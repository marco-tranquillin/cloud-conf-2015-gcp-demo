/**
 * Copyright (C) 2015 marco.tranquillin@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package eu.revevol.cloudConf2015.gcp_demo.shared;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.MimeHeaders;

public class Utils {
	
	/**
	 * Get all headers of a HttpServletRequest
	 * @param req the HttpServletRequest
	 * @return a MimeHeaders object that contains all headers data
	 */
	public static MimeHeaders getHeaders(HttpServletRequest req) {
	    @SuppressWarnings("rawtypes")
		Enumeration headerNames = req.getHeaderNames();
	    MimeHeaders headers = new MimeHeaders();
	    while (headerNames.hasMoreElements()) {
	      String headerName = (String) headerNames.nextElement();
	      String headerValue = req.getHeader(headerName);
	      StringTokenizer values = new StringTokenizer(headerValue, ",");
	      while (values.hasMoreTokens()) {
	        headers.addHeader(headerName, values.nextToken().trim());
	      }
	    }
	    return headers;
	  }
	
	/**
	 * Generate a Map based on InputStream
	 * @param query - inputStream
	 * @return the Map with data
	 * @throws UnsupportedEncodingException
	 */
    public static Map<String, String> makeQueryMap(String query) throws UnsupportedEncodingException {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for( String param : params ) {
            String[] split = param.split("=");
            if(split.length==2)
            	map.put(URLDecoder.decode(split[0], "UTF-8"), URLDecoder.decode(split[1], "UTF-8"));
        }
        return map;
    }
	
	/**
	 * Check if the passed string has been encoded using Base64 algo
	 * @param text the text String that you have to check
	 * @return true if the passed String is base64 encoded, false otherwise
	 */
	public static boolean isBase64(String text){
		final String BASEE_64_CHARS="^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
		return text.matches(BASEE_64_CHARS);
	}
	
	/**
	 * Print the stack trace of an exception
	 * 
	 * @param e Exception for which you want to print out the stack trace
	 * @return stack trace that you want to print out
	 */
	public static String printStackStrace(Exception e) {
		StringBuilder exceptionStackTrace = new StringBuilder();
		//add localized message
		exceptionStackTrace.append("\t" + e.toString());
		exceptionStackTrace.append("\n");
		//add specific message
		for (int i = 0; i < e.getStackTrace().length; i++) {
			exceptionStackTrace.append("\t" + e.getStackTrace()[i]);
			exceptionStackTrace.append("\n");
		}
		return exceptionStackTrace.toString();
	}
	
	/**
	 * Convert an array of String into a single string
	 * @param input the String array that you want to convert
	 * @param separator the character that you want to use to separate strings
	 * @return
	 */
	public static String arrayToString(String[] input,char separator){
		StringBuffer response=new StringBuffer("");
		if(input!=null){
			for(int i=0;i<input.length;i++){
				if(i>0)
					response.append(separator);
				response.append(input[i]);
			}
		}
		return response.toString();
	}
	
	/**
	 * Returns a pseudo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimum value
	 * @param max Maximum value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
}
