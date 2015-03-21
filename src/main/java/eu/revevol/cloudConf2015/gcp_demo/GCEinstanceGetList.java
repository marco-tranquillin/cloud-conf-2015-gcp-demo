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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MimeHeaders;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceList;
import com.google.gson.Gson;

import eu.revevol.cloudConf2015.gcp_demo.shared.Constants;
import eu.revevol.cloudConf2015.gcp_demo.shared.Utils;

public class GCEinstanceGetList extends HttpServlet {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private static final Logger	log	= Logger.getLogger(InstanceList.class.getName());
	private String instanceZone="";

	/**
	 * POST Method
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			
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
	        
	        instanceZone=Constants.GCE_MACHINE_DEFAULT_ZONE;
			if(map.get("instanceZone")!=null){
				instanceZone=map.get("instanceZone");
			}
			
			List<String> instanceDetails=new ArrayList<String>();
			
			// define HTTP_TRANSPORT and JSON_FACTORY
			final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
			final JsonFactory JSON_FACTORY = new JacksonFactory();

			// load SERVICE ACCOUNT private key
			InputStream privateKeyStream = null;
			privateKeyStream = new FileInputStream(this.getServletContext().getRealPath(Constants.SERVICE_ACCOUNT_KEY_PATH));
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(privateKeyStream, "notasecret".toCharArray());
			PrivateKey myOwnKey = (PrivateKey) ks.getKey("privatekey", "notasecret".toCharArray());

			// create OAuth 2.0 credentials based on PrivateKey
			GoogleCredential credential = new GoogleCredential
					.Builder()
					.setTransport(HTTP_TRANSPORT)
					.setJsonFactory(JSON_FACTORY)
					.setServiceAccountId(Constants.SERVICE_ACCOUNT_ID)
					.setServiceAccountScopes(Arrays.asList(ComputeScopes.COMPUTE))
					.setServiceAccountPrivateKey(myOwnKey)
					.build();

			// access to Compute Engine
			// Create compute engine object for listing instances
			Compute compute = new Compute
					.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
					.setApplicationName(Constants.APPLICATION_NAME)
					.setHttpRequestInitializer(credential)
						.build();

			//list instances
			String nextPageToken=null;
			InstanceList instanceList=compute.instances().list(Constants.PROJECT_ID, instanceZone).setMaxResults(Constants.GCE_LIST_MAX_NUMBER).setPageToken(nextPageToken).execute();
			nextPageToken=instanceList.getNextPageToken();
			if(instanceList.getItems()!=null){
				for(Instance instance:instanceList.getItems()){
					instanceDetails.add(instance.toPrettyString());
				}
			}
			//if there are more token to elaborate
			while(nextPageToken!=null){
				//get instances
				instanceList=compute.instances().list(Constants.PROJECT_ID, instanceZone).setMaxResults(Constants.GCE_LIST_MAX_NUMBER).setPageToken(nextPageToken).execute();
				//update nextPageToken 
				nextPageToken=instanceList.getNextPageToken();
				//save instance information
				if(instanceList.getItems()!=null){
					for(Instance instance:instanceList.getItems()){
						instanceDetails.add(instance.toPrettyString());
					}
				}
			}
			
			// return data in JSon format
			List<String> result=new ArrayList<String>();
			for(int i=0;i<instanceDetails.size();i++){
					result.add(instanceDetails.get(i));
			}
			String response = new Gson().toJson(instanceDetails.toArray());
			resp.setContentType("application/json");
			resp.getWriter().print(response);

		}
		catch (Exception e) {
			// print stack trace
			e.printStackTrace();
			// log stack trace
			log.severe(Utils.printStackStrace(e));
		}

	}
}