#!/bin/bash
#redirect all commands into file
exec 2>&1 > /tmp/mylog.log
INSTANCE_NAME=$(curl http://metadata/computeMetadata/v1beta1/instance/attributes/instance-name)
INSTANCE_ZONE=$(curl http://metadata/computeMetadata/v1beta1/instance/attributes/instance-zone)
STOP_INSTANCE_API=$(curl http://metadata/computeMetadata/v1beta1/instance/attributes/instance-stop)
MESSAGE_POST_API=$(curl http://metadata/computeMetadata/v1beta1/instance/attributes/message-post)
NUMBER_OF_MESSAGE=$(curl http://metadata/computeMetadata/v1beta1/instance/attributes/message-post-number)
TOPIC=$(curl http://metadata/computeMetadata/v1beta1/instance/attributes/topic)
CURRENT_MILLIS=$((`date +%s`))
URL_BASE='https://MY_APP_ID.appspot.com/';

#move into the correct folder
cd /tmp
mkdir pubsub
cd pubsub
wget http://storage.googleapis.com/MY_GCS_BUCKET/ServiceAccountPK.p12
wget http://storage.googleapis.com/MY_GCS_BUCKET/GoogleCloudPubSubPublisher.jar

#execute command
java -jar GoogleCloudPubSubPublisher.jar $INSTANCE_NAME $TOPIC ServiceAccountPK.p12 $NUMBER_OF_MESSAGE

#stop machine
curl -d "instanceName=$INSTANCE_NAME&instanceZone=$INSTANCE_ZONE" $URL_BASE$STOP_INSTANCE_API
