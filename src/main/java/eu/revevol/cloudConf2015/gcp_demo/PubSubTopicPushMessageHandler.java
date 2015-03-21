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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MimeHeaders;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.PubsubScopes;
import com.google.api.services.pubsub.model.ListTopicsResponse;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PublishResponse;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.api.services.pubsub.model.Topic;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.googlecode.objectify.ObjectifyService;

import eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Message;
import eu.revevol.cloudConf2015.gcp_demo.shared.Constants;
import eu.revevol.cloudConf2015.gcp_demo.shared.Utils;

public class PubSubTopicPushMessageHandler extends HttpServlet {
  /**
	 * 
	 */
	private static final long serialVersionUID = -7394260602938306597L;
	
	final Logger LOG = Logger.getLogger(PubSubTopicPushMessageHandler.class.getName());
	

@Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
	
	try{
        
        ServletInputStream reader = req.getInputStream();
        // Parse the JSON message to the POJO model class.
        JsonParser parser =
                JacksonFactory.getDefaultInstance().createJsonParser(reader);
        parser.skipToKey("message");
        PubsubMessage message = parser.parseAndClose(PubsubMessage.class);
        // Base64-decode the data and work with it.
        String data = new String(message.decodeData(), "UTF-8");
        
        Message msg=new Gson().fromJson(data, Message.class);
        
        //launch the task to store the message into the datastore
        int nextQueue=Utils.randInt(0, 9);
        Queue queue = QueueFactory.getQueue("queue-"+nextQueue);
    	queue.add(TaskOptions.Builder.withUrl("/api/message/post")
    			.param("sender",msg.getSender())
    			.param("value", ""+msg.getValue())
    			.param("timestamp", ""+msg.getTimestamp())
    			);
    	//launch the task to stream the message into BigQuery
    	nextQueue=Utils.randInt(0, 9);
        queue = QueueFactory.getQueue("queue-"+nextQueue);
    	queue.add(TaskOptions.Builder.withUrl("/api/message/post/bigquery")
    			.param("sender",msg.getSender())
    			.param("value", ""+msg.getValue())
    			.param("timestamp", ""+msg.getTimestamp())
    			);
        
        // Respond with a 20X to acknowledge receipt of the message.
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        resp.getWriter().close();
	  }
	  catch(Exception e){
		// print stack trace
		e.printStackTrace();
		// log stack trace
		LOG.severe(Utils.printStackStrace(e));
	  }

  }
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
	  	resp.setContentType("text/plain");
	  	resp.getWriter().println("["+this.getClass().getName()+"] Yeah, I am alive!" );
  }
}
