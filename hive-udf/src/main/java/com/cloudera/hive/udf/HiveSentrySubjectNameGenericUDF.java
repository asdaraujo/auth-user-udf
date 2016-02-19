package com.cloudera.hive.udf;

import java.lang.Exception;
import org.apache.hadoop.hive.ql.exec.MapredContext;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.sentry.binding.hive.conf.HiveAuthzConf;

class HiveSentrySubjectNameGenericUDF extends GenericUDF {

  String mapredUser = null;

  @Override
  public String getDisplayString(String[] arg0) {
    return "HiveSentrySubjectNameGenericUDF()";
  }

  @Override
  public void configure(MapredContext context) {
    mapredUser = context.getJobConf().get(HiveAuthzConf.HIVE_SENTRY_SUBJECT_NAME);
  }

  @Override
  public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
    if (arguments.length != 0) {
      throw new UDFArgumentLengthException("HiveSentrySubjectNameGenericUDF takes no arguments");
    }
    return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
  }
  
  @Override
  public Object evaluate(DeferredObject[] arguments) {
    if (mapredUser != null) {
      return mapredUser;
    } else {
      try {
        return SessionState.get().getConf().get(HiveAuthzConf.HIVE_SENTRY_SUBJECT_NAME);
      } catch (Exception e) {
        return null;
      }
    }
  }
  
}
