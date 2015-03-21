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
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MimeHeaders;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.PubsubScopes;
import com.google.api.services.pubsub.model.Topic;
import com.googlecode.objectify.ObjectifyService;

import eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Message;
import eu.revevol.cloudConf2015.gcp_demo.shared.Constants;
import eu.revevol.cloudConf2015.gcp_demo.shared.Utils;

public class PubSubTopicCreate extends HttpServlet {
  /**
	 * 
	 */
	private static final long serialVersionUID = -7394260602938306597L;
	
	final Logger LOG = Logger.getLogger(PubSubTopicCreate.class.getName());
	

@Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

      resp.setContentType("text/plain");
      resp.getWriter().println("["+this.getClass().getName()+"] Yeah, I am alive!" );
  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
	  
	  try{
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
        String topic=map.get("topic");
        
     // define HTTP_TRANSPORT and JSON_FACTORY
		final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
		final JsonFactory JSON_FACTORY = new JacksonFactory();

		// load SERVICE ACCOUNT private key
		InputStream privateKeyStream = null;
		privateKeyStream = new FileInputStream(this.getServletContext().getRealPath(Constants.SERVICE_ACCOUNT_KEY_PATH));
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(privateKeyStream, "notasecret".toCharArray());
		PrivateKey myOwnKey = (PrivateKey) ks.getKey("privatekey", "notasecret".toCharArray());
		LOG.info("[Authorization] Service Account private key loaded");

		// create OAuth 2.0 credentials based on PrivateKey
		GoogleCredential credential = new GoogleCredential.Builder()
			.setTransport(HTTP_TRANSPORT)
			.setJsonFactory(JSON_FACTORY)
			.setServiceAccountId(Constants.SERVICE_ACCOUNT_ID)
			.setServiceAccountScopes(Arrays.asList(PubsubScopes.PUBSUB,PubsubScopes.CLOUD_PLATFORM))
			.setServiceAccountPrivateKey(myOwnKey)
			.build();
     	
		//create pub/sub client
        Pubsub pubsub = new Pubsub.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
		        .setApplicationName(Constants.APPLICATION_NAME)
		      	.setHttpRequestInitializer(credential)
		      	.build();
        
        //crate new topic
        Topic newTopic = pubsub.projects().topics()
                .create("projects/"+Constants.PROJECT_ID+"/topics/"+topic, new Topic())
                .execute();
        

        resp.setContentType("text/plain");
        resp.getWriter().println(newTopic.toPrettyString());
	  }
	  catch(Exception e){
		// print stack trace
		e.printStackTrace();
		// log stack trace
		LOG.severe(Utils.printStackStrace(e));
	  }
  }
}
