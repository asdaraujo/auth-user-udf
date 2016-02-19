package com.cloudera.hive.udf;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.apache.sentry.binding.hive.conf.HiveAuthzConf;
import org.apache.hadoop.hive.ql.session.SessionState;

public final class HiveSentrySubjectNameUDF extends UDF {
  public Text evaluate() {
    return new Text(SessionState.get().getConf().get(HiveAuthzConf.HIVE_SENTRY_SUBJECT_NAME));
  }
}
