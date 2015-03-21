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
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MimeHeaders;

import com.googlecode.objectify.ObjectifyService;

import eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Message;
import eu.revevol.cloudConf2015.gcp_demo.shared.Utils;

public class PostMessageDatastore extends HttpServlet {
  /**
	 * 
	 */
	private static final long serialVersionUID = -7394260602938306597L;
	
	final Logger LOG = Logger.getLogger(PostMessageDatastore.class.getName());
	
	static{
		ObjectifyService.register(Message.class);
	}

@Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

      resp.setContentType("text/plain");
      resp.getWriter().println("["+this.getClass().getName()+"] Yeah, I am alive!" );
  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
	  
		//get all the headers from the HTTP request
		MimeHeaders headers = Utils.getHeaders(req);
				
		//read input data
		InputStream is = req.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int r=0;
        while( r >= 0 ) {
            r = is.read(buf);
            if( r >= 0 ) os.write(buf, 0, r);
        }
        String inputData = new String(os.toByteArray(), "UTF-8");
        Map<String,String> map = Utils.makeQueryMap(inputData);
        
        //get parameters
        String sender=map.get("sender");
        int value=Integer.parseInt(map.get("value"));
        long timestamp=Long.parseLong(map.get("timestamp"));
        
        //LOG.info("Sender: " + sender + "\nValue: " + value + " \nTimestamp: " + timestamp);
        
        //write data into DataStore
        Message msg=new Message();
        	msg.setSender(sender);
        	msg.setValue(value);
        	msg.setTimestamp(timestamp);
        	
        //save data into datastore
        ObjectifyService.ofy().save().entity(msg).now();
        //LOG.info("Message stored correctly into the datastore");

        resp.setContentType("text/plain");
        resp.getWriter().println("["+this.getClass().getName()+"] message data stored correctly" );
  }
}
