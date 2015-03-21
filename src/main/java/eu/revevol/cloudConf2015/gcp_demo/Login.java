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

package eu.revevol.cloudConf2015.gcp_demo;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.googlecode.objectify.ObjectifyService;

import eu.revevol.cloudConf2015.gcp_demo.pojo.*;
import eu.revevol.cloudConf2015.gcp_demo.shared.*;


public class Login extends HttpServlet {
	
	
	// private variables
	private static final Logger	LOG	= Logger.getLogger(Login.class.getName());
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String msg=null;
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if(user!=null){
			if (!user.getEmail().equals(Constants.OAUTH_USER)){
				msg="/noAccess.html";
			}
			else{
				msg="200 OK";
				
			}
		}
		else{
			msg=userService.createLoginURL(req.getRequestURI());
		}
		
		RESTresponse respToRet=new RESTresponse(msg);
	    String response = new Gson().toJson(respToRet);
	    //return data in JSon format
		resp.setContentType("application/json");
		resp.getWriter().print(response);
	}		
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String msg=null;
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if(user!=null){
			if (!user.getEmail().equals(Constants.OAUTH_USER)){
				// Set response content type
			      resp.setContentType("text/html");
			      // New location to be redirected
			      String site = new String("/noAccess.html");
			      resp.setStatus(resp.SC_MOVED_TEMPORARILY);
			      resp.setHeader("Location", site);
			}
			else{
				// Set response content type
			      resp.setContentType("text/html");

			      // New location to be redirected
			      String site = new String("/");

			      resp.setStatus(resp.SC_MOVED_TEMPORARILY);
			      resp.setHeader("Location", site);
			}
		}
		else{
			msg=userService.createLoginURL(req.getRequestURI());
		}
		
		RESTresponse respToRet=new RESTresponse(msg);
	    String response = new Gson().toJson(respToRet);
	    //return data in JSon format
		resp.setContentType("application/json");
		resp.getWriter().print(response);
	}

}
