## Hive/Impala user-based data masking and filtering

### Goal

We want to be able to implement user-based data masking and/or filtering using views in Impala and Hive. To be able to do that we need to have a way to find out who's the user running the query in a Kerberos-enabled cluster (in other words, Hive impersonation is disabled).

### Challenge

Impala does provide a `EFFECTIVE_USER()` function that returns the effective user for the session. If the connection is authenticated without delegation the function will return the user name in the form `<username>@<REALM>`. If a `DelegationUID` is specified in the JDBC connection to Impala, the function will return the string specified in the `DelegationUID` parameter, which doesn't contain the `REALM` component.

Hive Beeline though doesn't provide an easy and consistent way to get that information. It exposes the function `CURRENT_USER()`, which returns the user currently connected to the Beeline session. When Hive impersonation is disabled, though, this function always return `hive`, and is not of much help.

An alternative is to use the Hive configuration property `${hiveconf:hive.sentry.subject.name}`. This variable is replaced by Beeline by the shortname of the Kerberos-authenticated user. This solution, though, also has its problems:
* When a view is created with a query that references the variable, the substitution is performed at the time of the view creation, and the username for the user that creates the view becomes hard-coded in the view text
* The variable substitution doesn't work for Impala, so different views would have to be created for Hive and Impala

### Solution

The adopted solution involved creating a UDF for Hive (Java) and one for Impala (C++) to return the name of the effective user and use that function to create a single view that can be used either in Hive or Impala.

The Impala UDF implementation is very simple. It calls the builtin `USER()` function and removes its realm, leaving only the user short name. This is needed to ensure the Hive and Impala functions have consistent return values.

The Hive UDF is a little bit more complex. It uses two different methods to get the effective user name. When Beeline executes the query locally (e.g. a very simple query like `select * from table`) the UDF uses `SessionState` to get the effective user name from the Beeline session state.

If the query is executed as a MapReduce job, the session state is not available to the MR task and the UDF gets the effective user name from the MapReduce context (`MapredContext`).

### Hive setup

Build the Hive UDF jar, as explained in the [`hive-udf` README file](hive-udf/README.md), copy the jar file to the **Hive Auxiliary Jars Path** directory on the HiveServer2 host and restart the Hive service. You must ensure that the Hive's **Hive Auxiliary Jars Path** configuration setting is set to point to that directory.

Then create the Hive UDF as per below:

```
drop function if exists auth_user;
create function auth_user
  as 'com.cloudera.hive.udf.HiveSentrySubjectNameGenericUDF';
```

### Hive setup

Build the Impala UDF shared library, as explained in the [`impala-udf` README file](impala-udf/README.md). Upload the shared library to the appropriate HDFS location.

Then create the Impala UDF as per below:

```
drop function if exists auth_user();
create function auth_user()
  returns string
  location '/path/to/libudfsample.so'
  symbol='ImpalaUserShortNameUDF';
```

### Example

The (very) small example below creates a view that does filter rows and masks columns based on their username alone. Obviously, more complex logic would be required to do anything more useful than this.

Run this in Impala:

```
drop table if exists alltransactions;
create table alltransactions (
  uid string,
  item string,
  price int
);
insert into alltransactions
select * from (
  select 'user1', 'book', 10
  union all
  select 'admin', 'laptop', 1000
  union all
  select 'user2', 'food', 30
) tx;
select * from alltransactions;

drop view if exists mytransactions;
create view mytransactions as
  select
    uid,
    item,
    case auth_user()
      when 'admin'
      then price
      else null
    end as price
  from alltransactions
  where uid = auth_user();
```

After the view is created you can query it from either Hive or Impala. The results will vary according to the user that's running the query, as show below:

```
$ kinit admin
Password for admin@HADOOP.COM: 

$ impala-shell -i impalaserver --ssl
[impalaserver:21000] > select * from mytransactions;
Query: select * from mytransactions
+-------+--------+-------+
| uid   | item   | price |
+-------+--------+-------+
| admin | laptop | 1000  |
+-------+--------+-------+

$ kinit user1
Password for user1@HADOOP.COM: 

$ impala-shell -i impalaserver --ssl
[impalaserver:21000] > select * from mytransactions;
Query: select * from mytransactions
+-------+------+-------+
| uid   | item | price |
+-------+------+-------+
| user1 | book | NULL  |
+-------+------+-------+

$ kinit user2
Password for user2@HADOOP.COM: 

$ impala-shell -i impalaserver --ssl
[impalaserver:21000] > select * from mytransactions;
Query: select * from mytransactions
+-------+------+-------+
| uid   | item | price |
+-------+------+-------+
| user2 | food | NULL  |
+-------+------+-------+
```