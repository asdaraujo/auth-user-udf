export JAVA_HOME=/usr/java/jdk1.7.0_67-cloudera
export PATH=$JAVA_HOME/bin:$PATH
javac -cp /opt/cloudera/parcels/CDH/jars/sentry-binding-hive-1.4.0-cdh5.4.9.jar:/opt/cloudera/parcels/CDH/jars/hadoop-core-2.6.0-mr1-cdh5.4.9.jar:/opt/cloudera/parcels/CDH/jars/hive-exec-1.1.0-cdh5.4.9.jar:/opt/cloudera/parcels/CDH/jars/hadoop-common-2.6.0-cdh5.4.9.jar com/cloudera/hive/udf/*.java
jar -cf /var/lib/hive/myudfs.jar -C . .
