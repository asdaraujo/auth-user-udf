#include "impala-user-shortname-udf.h"
#include <string>

using namespace std;

StringVal ImpalaUserShortNameUDF(FunctionContext* context) {
  string username(context->effective_user());
  int pos = username.find("@");
  if (pos == string::npos) {
    return StringVal(username.c_str());
  } else {
    return StringVal(username.substr(0, pos).c_str());
  }
}

