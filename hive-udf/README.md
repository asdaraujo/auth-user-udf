## AUTH_USER UDF for Hive

### Build

```
mvn clean package
```

### Deploy

Copy the jar file from the `target` directory to the **Hive Aux Jars Path** directory on the HiveServer2 host. The HiveServer2 service will need to be restarted to be able to use the new jar file.
