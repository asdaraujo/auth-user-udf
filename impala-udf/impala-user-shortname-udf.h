#ifndef IMPALA_UDF_SAMPLE_UDF_H
#define IMPALA_UDF_SAMPLE_UDF_H

#include <impala_udf/udf.h>

using namespace impala_udf;

StringVal ImpalaUserShortNameUDF(FunctionContext* context);

#endif
