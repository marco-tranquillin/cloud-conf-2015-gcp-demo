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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MimeHeaders;

import com.google.gson.Gson;
import com.googlecode.objectify.ObjectifyService;

import eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Message;
import eu.revevol.cloudConf2015.gcp_demo.shared.Utils;

public class GetMessageCount extends HttpServlet {
  /**
	 * 
	 */
	private static final long serialVersionUID = -7394260602938306597L;
	
	final Logger LOG = Logger.getLogger(GetMessageCount.class.getName());
	
	static{
		ObjectifyService.register(Message.class);
	}

@Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
	
	  //get parameter
	  String sender=req.getParameter("sender");
	  
	  //define response type
	  resp.setContentType("application/json");
	  
	  //get list of message
	  List<Message> messageList=ObjectifyService.ofy().load().type(Message.class).filter("sender",sender).list();
	  resp.getWriter().print(new Gson().toJson(sender+":"+messageList.size()));
  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
	  
	  resp.setContentType("text/plain");
      resp.getWriter().println("["+this.getClass().getName()+"] Yeah, I am alive!" );
  }
}
