## AUTH_USER UDF for Impala

### Build

1. Install prerequisite packages
See detailed instructions at [Impala User-Defined Functions](http://www.cloudera.com/documentation/enterprise/latest/topics/impala_udf.html):
```
sudo yum install gcc-c++ cmake boost-devel
sudo yum install impala-udf-devel
```
2. Build the shared library:
```
cmake .
make
```

### Deploy

Copy the shared library from the `build_output` directory to the appropriate location at HDFS.
