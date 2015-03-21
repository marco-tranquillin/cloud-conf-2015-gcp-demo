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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import com.googlecode.objectify.ObjectifyService;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.TableDataInsertAllRequest;
import com.google.api.services.bigquery.model.TableDataInsertAllRequest.Rows;
import com.google.api.services.bigquery.model.TableDataInsertAllResponse;
import com.google.api.services.bigquery.model.TableRow;

import eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Message;
import eu.revevol.cloudConf2015.gcp_demo.shared.Constants;
import eu.revevol.cloudConf2015.gcp_demo.shared.Utils;

public class PostMessageBigQuery extends HttpServlet {
  /**
	 * 
	 */
	private static final long serialVersionUID = -7394260602938306597L;
	
	final Logger LOG = Logger.getLogger(PostMessageBigQuery.class.getName());
	
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
        String sender=map.get("sender");
        int value=Integer.parseInt(map.get("value"));
        long timestamp=Long.parseLong(map.get("timestamp"));
        
        
        //write data into DataStore
        Message msg=new Message();
        	msg.setSender(sender);
        	msg.setValue(value);
        	msg.setTimestamp(timestamp);
        	
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
		String[] SERVICE_ACCOUNT_SCOPES = { "https://www.googleapis.com/auth/bigquery" };
		
		GoogleCredential credential = new GoogleCredential.Builder()
			.setTransport(HTTP_TRANSPORT)
			.setJsonFactory(JSON_FACTORY)
			.setServiceAccountId(Constants.SERVICE_ACCOUNT_ID)
			.setServiceAccountScopes(Arrays.asList(SERVICE_ACCOUNT_SCOPES))
			.setServiceAccountPrivateKey(myOwnKey)
			.build();
		
		
		credential.refreshToken();
	
		Bigquery bq = new Bigquery.Builder(HTTP_TRANSPORT, JSON_FACTORY,credential)
		.setApplicationName("BigQuery-Service-Accounts/0.1")
		.setHttpRequestInitializer(credential).build();
		
		//insert row into BigQuery
		TableRow row = new TableRow();
			row.set("SENDER", sender);
			row.set("VALUE",""+value );
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
			row.set("TIME_POSTED", dt.format(timestamp));
		
		TableDataInsertAllRequest.Rows rows = new TableDataInsertAllRequest.Rows();
			rows.setInsertId(UUID.randomUUID().toString());
			rows.setJson(row);
		
		List<Rows> rowList =new ArrayList();
			rowList.add(rows);
		
		TableDataInsertAllRequest content = new TableDataInsertAllRequest().setRows(rowList);
		TableDataInsertAllResponse bqRes =
		    bq.tabledata().insertAll(
		        Constants.PROJECT_ID, 
		        Constants.BQ_DATASET_ID, 
		        Constants.BQ_TABLE_ID, 
		        content)
		    .execute();
	
		LOG.info("Added: " + bqRes.toPrettyString());
	 	resp.setContentType("text/plain");
	 	resp.getWriter().println("["+this.getClass().getName()+"] message data stored correctly" );
	  }
	  catch (Exception e) {
			// print stack trace
			e.printStackTrace();
			// log stack trace
			LOG.severe(Utils.printStackStrace(e));
		}
  }
}
