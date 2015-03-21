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
import java.util.Random;
import java.util.logging.Logger;

import javax.servlet.http.*;
import javax.xml.soap.MimeHeaders;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.AttachedDisk;
import com.google.api.services.compute.model.AttachedDiskInitializeParams;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceList;
import com.google.api.services.compute.model.Metadata;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.ServiceAccount;
import com.google.gson.Gson;
import com.googlecode.objectify.ObjectifyService;

import eu.revevol.cloudConf2015.gcp_demo.pojo.*;
import eu.revevol.cloudConf2015.gcp_demo.shared.*;


@SuppressWarnings("serial")
public class GCEinstanceStart extends HttpServlet {
	
	static{
		ObjectifyService.register(eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Instance.class);
	}
	// private variables
	private static final Logger	LOG	= Logger.getLogger(GCEinstanceStart.class.getName());
	private String baseInstanceName="";
	private String instanceName="";
	private String instanceType="";
	private String instanceZone="";
	private String messagePostNumber="";
	private int instanceNumber=1;
	
	/**
	 * POST Method
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	
		
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
	        
	        //define instance constants
			
			instanceType=Constants.GCE_MACHINE_TYPE_F1_MICRO;
			if(map.get("instanceType")!=null){
				instanceType=map.get("instanceType");
			}
			
			instanceZone=Constants.GCE_MACHINE_DEFAULT_ZONE;
			if(map.get("instanceZone")!=null){
				instanceZone=map.get("instanceZone");
			}
			
			messagePostNumber=Constants.GCE_METADATA_MESSAGE_POST_NUMBER_VALUE;
			if(map.get("messagePostNumber")!=null){
				messagePostNumber=map.get("messagePostNumber");
			}
			
			if(map.get("instanceNumber")!=null){
				instanceNumber=Integer.parseInt(map.get("instanceNumber"));
			}
			
			baseInstanceName=Constants.GCE_DEFAULT_INSTANCE_NAME;
			if(map.get("instanceName")!=null){
				baseInstanceName=map.get("instanceName");
			}
	        
	        
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
				.setServiceAccountScopes(Arrays.asList(ComputeScopes.COMPUTE))
				.setServiceAccountPrivateKey(myOwnKey)
				.build();
			
			LOG.info("[Authorization] credentials built");

			//access to Compute Engine
			// Create compute engine object for listing instances
		      Compute compute = new Compute.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
		      	.setApplicationName(Constants.APPLICATION_NAME)
		      	.setHttpRequestInitializer(credential)
		      	.build();
			
		      for (int i = 0; i < instanceNumber; i++) {
					
		    	  	/**
		    	  	 * INSTANCE NAME
		    	  	 * */
		    	  //generate random instance_id
					Random rnd=new Random();
					long instanceId=rnd.nextLong();
					
		    	  	instanceName="gce-"+i+"-"+baseInstanceName+"-"+instanceId;
					
					
		    	  	/**
					 * DISK
					 */
					//define disks
					AttachedDiskInitializeParams newDiskInitParams = new AttachedDiskInitializeParams();
						newDiskInitParams.setDiskName("instance-disk-"+instanceId);
						newDiskInitParams.setSourceImage(Constants.GCE_LINUX_JAVA);

					AttachedDisk newDisk = new AttachedDisk();
						newDisk.setBoot(true); //boot the instance using this disk
						newDisk.setAutoDelete(true); //delete the disk when the associated instance will be deleted
						newDisk.setInitializeParams(newDiskInitParams);

					List<AttachedDisk> disks = new ArrayList<AttachedDisk>();
						disks.add(newDisk);

					/**
					 * NETWORK
					 */
					//define network
					NetworkInterface net = new NetworkInterface();
						net.setNetwork(Constants.GCE_NETWORK_TYPE);
					//define access config
					AccessConfig ac=new AccessConfig();
						ac.setType(Constants.GCE_NETWORK_EMPHERAL);
					List<AccessConfig> acs=new ArrayList<AccessConfig>();
						acs.add(ac);	
					net.setAccessConfigs(acs);
					List<NetworkInterface> networks = new ArrayList<NetworkInterface>();
						networks.add(net);

					/**
					 * METADATA STARTUP SCRIPT
					 */
					//Define metadata items
					
					//startup script
					Metadata.Items itemStartupScript=new Metadata.Items();
						itemStartupScript.setKey(Constants.GCE_METADATA_STARTUP_KEY);
						itemStartupScript.setValue(Constants.GCE_METADATA_STARTUP_VALUE);
					//input instance name
					Metadata.Items instanceNameMetadata=new Metadata.Items();
						instanceNameMetadata.setKey(Constants.GCE_METADATA_INSTANCE_NAME);
						instanceNameMetadata.setValue(instanceName);
					//message post API
					Metadata.Items messagePostAPI=new Metadata.Items();
						messagePostAPI.setKey(Constants.GCE_METADATA_MESSAGE_POST);
						messagePostAPI.setValue(Constants.GCE_METADATA_MESSAGE_POST_VALUE);
					//message post number
					Metadata.Items messagePostNumberMetadata=new Metadata.Items();
						messagePostNumberMetadata.setKey(Constants.GCE_METADATA_MESSAGE_POST_NUMBER);
						messagePostNumberMetadata.setValue(messagePostNumber);
					//message topic
					Metadata.Items messageTopicMetadata=new Metadata.Items();
						messageTopicMetadata.setKey(Constants.GCE_METADATA_TOPIC_NAME);
						messageTopicMetadata.setValue(Constants.GCE_METADATA_TOPIC_VALUE);
					//input instance stop instance URL
					Metadata.Items instanceZoneMetadata=new Metadata.Items();
						instanceZoneMetadata.setKey(Constants.GCE_METADATA_INSTANCE_ZONE);
						instanceZoneMetadata.setValue(instanceZone);
					//input instance stop instance URL
					Metadata.Items itemStopMethodUrl=new Metadata.Items();
						itemStopMethodUrl.setKey(Constants.GCE_METADATA_INSTANCE_STOP_METHOD_NAME);
						itemStopMethodUrl.setValue(Constants.GCE_METADATA_INSTANCE_STOP_METHOD_VALUE);
					
					//create array of metadata
					List<Metadata.Items> items=new ArrayList<Metadata.Items>();
						items.add(itemStartupScript);
						items.add(instanceNameMetadata);
						items.add(messageTopicMetadata);
						items.add(messagePostAPI);
						items.add(messagePostNumberMetadata);
						items.add(instanceZoneMetadata);
						items.add(itemStopMethodUrl);
					Metadata metadata=new Metadata();
						metadata.setItems(items);
						
					
						
					/**
					 * SERVICE ACCOUNT SCOPES
					 */
					
					ServiceAccount serviceAccount=new ServiceAccount();
						serviceAccount.setScopes(Arrays.asList(Constants.OAUTH_SCOPES));
						serviceAccount.setEmail(Constants.SERVICE_ACCOUNT_EMAIL);
					List<ServiceAccount> listServiceAccount=new ArrayList<ServiceAccount>();
						listServiceAccount.add(serviceAccount);
						
				   /**
					 * INSTANCE definition
					 */
					Instance newInstance = new Instance();
						newInstance.setName(instanceName);
						newInstance.setMachineType(
								Constants.GCE_MACHINE_TYPE_FULL_URL
								.replaceAll("xxxZONExxx", instanceZone)
								.replaceAll("xxxMACHINE_TYPExxx", instanceType)
								);
						newInstance.setDisks(disks);
						newInstance.setNetworkInterfaces(networks);
						newInstance.setMetadata(metadata);
						newInstance.setServiceAccounts(listServiceAccount);

					/**
					 * BOOTSTRAP machine
					 */
					int retry=0;
					while(retry < 5){
						try{
							compute.instances().insert(Constants.PROJECT_ID, instanceZone, newInstance).execute();
							retry=5;
						}
						catch(Exception ex){
							//exponential backoff
							LOG.warning("Exception in creating machine - I will retry again - Iteration# " + retry);
							try{
								Thread.sleep((1 << retry) * 1000 + rnd.nextInt(1001));
							}
							catch(Exception exSleep){
								;
							}
							
							retry++;
						}
					}
					LOG.info("[Application] machine " + instanceName + " bootstrapped");
					
					//save into datastore
					eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Instance dataStoreInstance=new eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Instance();
						dataStoreInstance.setInstanceName(instanceName);
						dataStoreInstance.setTimestamp(new java.util.Date().getTime());
					ObjectifyService.ofy().save().entity(dataStoreInstance).now();
		      }
				   
			    RESTresponse respToRet=new RESTresponse("OK");
			    String response = new Gson().toJson(respToRet);
			    //return result
			    LOG.info("[Application] Print out results");
			    //return data in JSon format
				resp.setContentType("application/json");
				resp.getWriter().print(response);
		}
		catch (Exception e) {
			// print stack trace
			e.printStackTrace();
			// log stack trace
			LOG.severe(Utils.printStackStrace(e));
		}
	}
}
