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

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.ObjectifyService;

import eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Message;
import eu.revevol.cloudConf2015.gcp_demo.shared.Constants;
import eu.revevol.cloudConf2015.gcp_demo.shared.Utils;

public class LaunchGCEinstanceStart extends HttpServlet {
  /**
	 * 
	 */
	private static final long serialVersionUID = -7394260602938306597L;
	
	final Logger LOG = Logger.getLogger(LaunchGCEinstanceStart.class.getName());
	
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
        
        String instanceType=Constants.GCE_MACHINE_TYPE_F1_MICRO;
		if(map.get("instanceType")!=null){
			instanceType=map.get("instanceType");
		}
		
		String instanceZone=Constants.GCE_MACHINE_DEFAULT_ZONE;
		if(map.get("instanceZone")!=null){
			instanceZone=map.get("instanceZone");
		}
		
		String messagePostNumber=Constants.GCE_METADATA_MESSAGE_POST_NUMBER_VALUE;
		if(map.get("messagePostNumber")!=null){
			messagePostNumber=map.get("messagePostNumber");
		}
		
		String instanceNumber="1";
		if(map.get("instanceNumber")!=null){
			instanceNumber=map.get("instanceNumber");
		}
		
		String baseInstanceName=Constants.GCE_DEFAULT_INSTANCE_NAME;
		if(map.get("instanceName")!=null){
			baseInstanceName=map.get("instanceName");
		}
		
		Queue queue = QueueFactory.getDefaultQueue();
        	queue.add(TaskOptions.Builder.withUrl("/api/gce/instance/start")
        			.param("instanceType",instanceType)
        			.param("instanceZone", instanceZone)
        			.param("messagePostNumber", messagePostNumber)
        			.param("instanceNumber", instanceNumber)
        			.param("instanceName", baseInstanceName));

        resp.setContentType("text/plain");
        resp.getWriter().println("["+this.getClass().getName()+"] message data stored correctly" );
  }
}
