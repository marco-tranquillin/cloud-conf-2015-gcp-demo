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

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

import eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Instance;
import eu.revevol.cloudConf2015.gcp_demo.datastore.entity.Message;
import eu.revevol.cloudConf2015.gcp_demo.shared.Utils;


@SuppressWarnings("serial")
public class DeleteInstance extends HttpServlet{

	private static final Logger	LOG	= Logger.getLogger(DeleteInstance.class.getName());
	
	static{
		ObjectifyService.register(Instance.class);
	}
	

	/**
	 * POST Method
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

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
        
        //search for input parameters if present
        String instanceName=null;
        if(map.get("sender")!=null){
        	instanceName=map.get("instanceName");
        }
        
        
        //delete messages 
        List<Key<Instance>> keys=null;
        if(instanceName!=null){
        	keys = ObjectifyService.ofy().load().type(Instance.class).filter("instanceName",instanceName).keys().list();
        }
        else{
        	keys = ObjectifyService.ofy().load().type(Instance.class).keys().list();
        }
		ObjectifyService.ofy().delete().keys(keys).now();
	}
}
