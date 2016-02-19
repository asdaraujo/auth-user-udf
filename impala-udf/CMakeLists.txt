# Copyright 2012 Cloudera Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# CDH dirs
set(CDH_HOME "/opt/cloudera/parcels/CDH")
set(CDH_INCLUDE "${CDH_HOME}/include")
set(CDH_LIBPATH "${CDH_HOME}/lib64")
include_directories(${CDH_INCLUDE})
link_directories(${CDH_LIBPATH})

# where to put generated libraries
set(LIBRARY_OUTPUT_PATH "./build_output")
# where to put generated binaries
set(EXECUTABLE_OUTPUT_PATH "./build_output")
# Function to generate rule to cross compile a source file to an IR module.
# This should be called with the .cc src file and it will generate a
# src-file-ir target that can be built.
# e.g. COMPILE_TO_IR(test.cc) generates the "test-ir" make target.
set(IR_COMPILE_FLAGS "-emit-llvm" "-O3" "-c" "-I../" ${CLANG_BASE_FLAGS})
function(COMPILE_TO_IR SRC_FILE)
  get_filename_component(BASE_NAME ${SRC_FILE} NAME_WE)
  set(OUTPUT_FILE "${LIBRARY_OUTPUT_PATH}/${BASE_NAME}.ll")
  add_custom_command(
    OUTPUT ${OUTPUT_FILE}
    COMMAND ${LLVM_CLANG_EXECUTABLE} ${IR_COMPILE_FLAGS} ${CLANG_INCLUDE_FLAGS} ${SRC_FILE} -o ${OUTPUT_FILE}
    DEPENDS ${SRC_FILE} ${CDH_INCLUDE}/impala_udf/udf.h)
  add_custom_target(${BASE_NAME}-ir ALL DEPENDS ${OUTPUT_FILE})
endfunction(COMPILE_TO_IR)

# Build the UDA/UDFs into a shared library.
add_library(user_shortname SHARED impala-user-shortname-udf.cc)
add_dependencies(user_shortname thrift-deps)

# Custom targest to cross compile UDA/UDF to ir
COMPILE_TO_IR(impala-user-shortname-udf.cc)
add_dependencies(impala-user-shortname-udf-ir thrift-deps)

cmake_minimum_required(VERSION 2.8)

