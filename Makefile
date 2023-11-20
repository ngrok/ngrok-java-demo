.PHONY: package demo-simple demo-socket demo-jetty demo-jetty-forward

package:
	mvn clean package

demo-simple: package
	mvn exec:java@demo --projects demo-simple

demo-socket: package
	mvn exec:java@demo --projects demo-socket

demo-jetty: package
	mvn exec:java@demo --projects demo-jetty

demo-jetty-forward: package
	mvn exec:java@demo-forward --projects demo-jetty

.PHONY: quickstart
quickstart: package
	mvn exec:java@quickstart --projects quickstart
