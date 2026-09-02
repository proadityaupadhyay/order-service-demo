# mock-service

Dummy stand-in for a real downstream backend the OrderService integration
would call (e.g. an inventory or pricing service). Not part of the Maven
reactor and not built/deployed by the Jenkins pipeline — it's a local-only
aid for exercising the app without a real downstream dependency.

Run it directly with JDK 11+'s single-file source launcher:

```
java src/MockService.java
```

It listens on port 9090 and returns a canned JSON response from
`GET /inventory/{sku}`. `deploy/scripts/run-local.sh` starts it alongside the
real app when running locally.
