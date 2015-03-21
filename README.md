**Cloud Conf 2015 - Google Cloud Platform (GCP) Demo**

## Google Cloud Platform Demo
**Scaling with no pains - Exceed limits using GCP**

This demo has been presented at [CloudConf2015](http://2015.cloudconf.it/).

The goal of this demo is to show the scalability of the GCP.
The main page will allow you to:
* select the number of Linux machine that you want to create on GCE
* select the number of messages that each machine will generate and push to Cloud Pub/Sub
* launch the simulation and get access to result

To see the number of messages generated by a publisher and the content of each message simply
load the page indexSenderInfo.html?sender=SENDER_NAME where SENDER_NAME is the name of the generated
instance.

*PLEASE NOTE that if you insert a high number of machines and messages the cost of the simulation could be 
considerable. Pay attention to what you are doing ;-)*

#Steps to prepare, compile and execute the demo

1. create a project on your Google Cloud Console (https://console.developers.google.com/project)
2. check inside your GCE quota the number of cores that you can have at the same time on us-central-1 region: every instance comes with a core so if you want to setup a big cluster you need to have an adequate quota. Click on "Change request" to request more quota.
3. generate your ServiceAccount under "APIs and Auth" section
4. create a [custom GCE image](https://cloud.google.com/compute/docs/images) that contains Java environment 1.7+ , the java client needed to generate messages (see repository [marco-tranquillin/cloud-conf-2015-gcp-demo-java-client](https://github.com/marco-tranquillin/cloud-conf-2015-gcp-demo-java-client) and the service account
5. upload in Google Cloud Storage the startup script and make it public
6. configure the class eu.revevol.cloudConf2015.gcp_demo.shared.Constants.java with your parameters
7. mvn package to build the application
8. mvn appengine:update to deploy the application 
9. use the API /api/pubsub/topic/create to create the topic where messages will be pushed
10. create a BigQuery dataset inside your project and a table with following columns (SENDER,VALUE,TIME_POSTED)
11. load the main page and enjoy the power of GCP :-)

#Disclaimer

This is a DEMO application and its code should not be used in production applications.
You need a billing-enabled project to be able to execute this simulation. Please note that the final
cost could be considerable. 
