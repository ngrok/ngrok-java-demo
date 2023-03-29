.PHONY: demo-simple demo-socket demo-jetty

package:
	mvn package

demo-simple: package
	mvn exec:java@demo --projects demo-simple

demo-socket: package
	mvn exec:java@demo --projects demo-socket

demo-jetty: package
	mvn exec:java@demo --projects demo-jetty
