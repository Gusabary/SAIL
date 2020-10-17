# CMake Cookbook Notes

## Chapter 1  From a Simple Executable to Libraries

### 1.1  Compiling a single source file into an executable

+ The CMake language (e.g. the function name) is case insensitive while the arguments of the functions are case sensitive. For example, the `VERSION` below must be in upper case:

  ```cmake
  cmake_minimum_required(VERSION 3.5)
  ```

+ Typically `cmake -H . -B build` has a equivalent effect with the below commands:

  ```cmake
  mkdir -p build
  cd build
  cmake ..
  ```

+ CMake is just a build system **generator**, which is to say it's used to generate a build system and then use it to get the source code compiled. On Linux, the default build system is Makefile while on Windows, it's Visual Studio.

  Generically, we can use `cmake --build .` to run instructions of the chosen build system.

+ Alongside the target we specified with `add_excutable`, CMake also generates other ones such as `clean`, `depend`, etc. which can be listed using `cmake --build . --target help`

### 1.2  Switching generators

+ As mentioned above, CMake is just a collection of build system generators. We can switch to another generator to create different build systems such as Ninja. Use this:

  ```cmake
  cmake -G Ninja ..
  cmake --build .
  ```

  Remember to install Ninja before this.

### 1.3  Building and linking static and shared libraries

+ `add_library` accepts following arguments:

  + the first one is the name of the target (and also the library)
  + the second one is the way to create the library, which includes `STATIC`, `SHARED`, `OBJECT`, `MODULE`, `IMPORTED`, `INTERFACE` and `ALIAS`. (They will be introduced later)
  + the following ones are files to be compiled into the library

  One thing worth noting is that the library generated under `build` directory has a name of `lib` plus target name, with suffix of `.a` for static one and `.so` for dynamic one.

+ `OBJECT` as the second argument of `add_library` can be useful when creating both static and shared libraries:

  ```cmake
  add_library(message-objs OBJECT Message.hpp Message.cpp)
  add_library(message-shared SHARED $<TARGET_OBJECTS:message-objs>)
  add_library(message-static STATIC $<TARGET_OBJECTS:message-objs>)
  ```

  Note that `$<TARGET_OBJECTS:message-objs>` cannot be simply `message-objs` because arguments in this place should otherwise be a filename if not **generator expressions**.

### 1.4  Controlling compilation with conditionals

+ We can use `if`, `elseif`, `else` and `endif` to construct a branch control flow in CMake.
+ In CMake, `BUILD_SHARED_LIBS` is a built-in flag, which indicates whether `add_library` will build a static or shared library if the second argument of it is omitted. 

### 1.5  Presenting options to the user

+ Use `option` to expose a flag to users, letting them decide whether to set the flag, using `cmake -D FLAG=ON ..`
+ `cmake_dependent_option` in module `CMakeDependentOption` provides a way to expose a flag only when another flag is set or unset.

### 1.6  Specifying the compiler

+ At configure time, CMake chooses a suitable compiler for us according to the platform and the chosen build system generator. There are two ways to override the default compiler:
  + use `cmake -D CMAKE_CXX_COMPILER=`
  + export an environment variable `CXX` 
+ Also, `cmake --system-information` can be used to list some info about CMake system.

### 1.7  Switching the build type

+ CMake build type can be specified with `CMAKE_BUILD_TYPE` in `CMakeLists.txt` or `-D` option from command line

+ We can set the default value of `CMAKE_BUILD_TYPE` like below:

  ```cmake
  if(NOT CMAKE_BUILD_TYPE)
    	set(CMAKE_BUILD_TYPE Release)
  endif()
  
  message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")
  ```

### 1.8  Controlling compiler flags

+ CMake provides two ways to specify the compiler flags. The first is on a per-target basis:

  ```cmake
  list(APPEND flags "-fPIC" "-Wall")
  
  target_compile_options(geometry PRIVATE ${flags})
  ```

  With `target_compile_options`, we can specify compiler flags for each target, which is a relatively fine granularity.

  Note here we specify a `PRIVATE`, actually here can appear three kinds of values:

  + `PRIVATE`: the compiler options will only be applied to the target itself, not to other ones consuming it.
  + `INTERFACE`: the options will only be applied to targets consuming it.
  + `PUBLIC`: the options will be applied to both.

+ We can use `make -- VERBOSE=1` to verify the options are correctly applied to the corresponding targets

+ And the second approach is on a global basis, using `CMAKE_CXX_FLAGS` in the CMake file or `-D` option from the command line.

### 1.9  Setting the standard for the language

+ CMake provides two ways to specify the C++ standard you want to use. The first is still on a per-target basis:

  ```cmake
  set_target_properties(animal-farm
    PROPERTIES
      CXX_STANDARD 14
      CXX_EXTENSIONS OFF
      CXX_STANDARD_REQUIRED ON
    )
  ```

  If `CXX_STANDARD_REQUIRED` flag is set, the version specified in `CXX_STANDARD` is required, which is to say, C++14 has to be available. If it's unset, a latest version will be used as an alternative if C++14 isn't available.

+ And naturally, the second approach is on a global basis, using `CMAKE_CXX_STANDARD` and `CMAKE_CXX_STANDARD_REQUIRED`.

+ Interestingly, CMake even allows finer control over the language features (such as variadic templates, automatic return type deduction, etc.) with `target_compile_features`.

### 1.10  Using control flow constructs

+ We can use `foreach-endforeach` and `while-endwhile` to construct a loop control flow in CMake. `foreach` can be used in four ways:
  + `foreach(loop_var arg1 arg2)`, or `foreach(loop_var ${list})`
  + `foreach(loop_var RANGE total)`
  + `foreach(loop_var IN LISTS list)` (`list` here will be expanded automatically)
  + `foreach(loop_var IN ITEMS item)` (`item` here won't be expanded automatically)
+ Just like target, files also have properties in CMake, we can use `set_source_file_properties` and `get_source_file_property` to set and get them.

## Chapter 2  Detecting the Environment

### 2.1  Discovering the operating system

+ We can get info about the OS from variable `CMAKE_SYSTEM_NAME` :

  ```cmake
  if(CMAKE_SYSTEM_NAME STREQUAL "Linux")
  ```

+ We can always use forward slash (`/`) as path delimiter in CMake and it will automatically translate the slash for corresponding OS (e.g. for Windows, it's backward slash)

### 2.2  Dealing with platform-dependent source code

+ CMake provides two (yep, still two) ways to add definitions to be passed to the preprocessor (which can be checked by `#ifdef`). 

+ The first is on a per-target basis, using `target_compile_definitions`. While the second is on a global basis, using `add_compile_definitons` (also, `add_definitions` can be used with an extra `-D`):

  ```cmake
  target_compile_definitions(hello PUBLIC "IS_LINUX")
  add_compile_definitions("IS_LINUX")
  add_definitions(-DIS_LINUX)
  ```

### 2.3  Dealing with compiler-dependent source code

+ We can get info about compiler from variable `CMAKE_CXX_COMPILER_ID`:

  ```cmake
  target_compile_definitions(hello-world PUBLIC 			      "COMPILER_NAME=\"${CMAKE_CXX_COMPILER_ID}\"")
  ```

  ```c++
  std::cout << "compiler name is " COMPILER_NAME << std::endl;
  ```

### 2.4  Discovering the host processor architecture

+ We can get info about the processor (like architecture, 32 bit or 64 bit) from variables `CMAKE_HOST_SYSTEM_PROCESSOR` and `CMAKE_SIZEOF_VOID_P`.

+ When `add_executable`, it's not necessary to specify the source files immediately, we can defer it with `target_sources`:

  ```cmake
  add_executable(arch-dependent "")
  
  if(CMAKE_HOST_SYSTEM_PROCESSOR MATCHES "i386")
    target_sources(arch-dependent PRIVATE arch-dependent-i386.cpp)
  elseif(CMAKE_HOST_SYSTEM_PROCESSOR MATCHES "x86_64")
    target_sources(arch-dependent PRIVATE arch-dependent-x86_64.cpp)
  else()
    message(STATUS "host processor architecture is unknown")
  endif()
  ```

### 2.5  Discovering the host processor instruction set

+ We can get info about the system on which CMake runs, using `cmake_host_system_information`.
+ And we can use `configure_file` to configure a `.h` from a `.h.in`.

### 2.6  Enabling vectorization for the Eigen library

+ `check_cxx_compiler_flag` from module `CheckCXXCompilerFlag` can be used to check whether a compiler flag is available with the current compiler and store the result of check (true or false) to a variable.

  If the flag exists, we can then add it with `target_compile_options` introduced before.

## Chapter 3  Detecting External Libraries and Programs

### 3.1  Detecting the Python interpreter

+ `find_package` can be used to find a package located somewhere in your system. The CMake module which takes effect is `Find<name>.cmake` when `find_package(<name>)` is called.

  In addition to find the specified package, `find_package` will also set up some useful variables to indicate the result of finding:

  ```cmake
  find_package(PythonInterp REQUIRED)
  execute_process(COMMAND ${PYTHON_EXECUTABLE} "-c" "print('Hello, world!')")
  ```

+ We can use `cmake_print_variables` from `CMakePrintHelpers` module for better print format.

### 3.2  Detecting the Python library

+ Use `find_package` to find both Python interpreter and library. To ensure their versions corresponds, we can specify the version in `find_package`:

  ```cmake
  find_package(PythonInterp REQUIRED)
  find_package(PythonLibs ${PYTHON_VERSION_MAJOR}.${PYTHON_VERSION_MINOR} EXACT REQUIRED)
  target_include_directories(hello-embedded-python
    PRIVATE
      ${PYTHON_INCLUDE_DIRS}
    )
  target_link_libraries(hello-embedded-python
    PRIVATE
      ${PYTHON_LIBRARIES}
    )
  ```

### 3.3  Detecting Python modules and packages

+ We can use `execute_process` to execute commands in child processes, and get the return value, output and error info if failure.

## Chapter 4  Creating and Running Tests

### 4.1  Creating a simple unit test

+ Use `enable_testing` and `add_test` to create tests with built-in CMake functionality:

  ```cmake
  add_executable(cpp_test test.cpp)
  
  enable_testing()
  add_test(
    NAME cpp_test
    COMMAND $<TARGET_FILE:cpp_test>
  )
  ```

  After the process of build, the tests can be run with a simple `ctest` or `make test`

+ Interestingly, the test cases can be written in any language supported by the system and the return value is used to indicate whether the test passes: zero for success and non-zero for failure.

### 4.3  Defining a unit test and linking against Google Test

+ Use `FetchContent` to add GTest as dependency in configure time, and then link its library with our application.
+ It seems that there is a `GoogleTest` CMake module specifically for Google Test application since v3.9.

### 4.6  Testing expected failures

+ We can use `set_tests_properties` to pass the test cases when it returns non-zero:

  ```cmake
  enable_testing()
  add_test(example ${PYTHON_EXECUTABLE} ${CMAKE_CURRENT_SOURCE_DIR}/test.py)
  set_tests_properties(example PROPERTIES WILL_FAIL true)
  ```

### 4.7  Using timeouts for long tests

+ We can also use `set_tests_properties` to set a timeout for test cases:

  ```cmake
  set_tests_properties(example PROPERTIES TIMEOUT 10)
  ```

  The test case will terminate and be marked as failure as long as it goes past the timeout.

### 4.8  Running tests in parallel

+ Use `ctest --parallel N` to run test cases in parallel. One thing worth noting is that CMake execute test cases that take long time first in each core and then the shorter cases, in which strategy, the total time costed will be the least. Of course, in the very beginning even before the first execution, CMake is  also not aware the time cost of each test case. However, we can specify explicitly the time cost of each test case with like `set_tests_properties(example PROPERTIES COST 4.5)`.

### 4.9  Running a subset of tests

+ CMake provides several ways to run a subset of all test cases:

  + by test case names (using regular expressions): `ctest -R <reExp>`
  + by test case indexes: `ctest -I <begin>,<end>`
  + by test case labels: `ctest -L <label>`

+ Yep, we can attach labels to test cases, still using `set_tests_properties`:

  ```cmake
  set_tests_properties(
    feature-a feature-b feature-c
    PROPERTIES
      LABELS "quick"
  )
  ```

### 4.10  Using test fixtures

+ Usually some test cases require setup actions before running and cleanup actions after completing, we call this test fixtures. Of course create a test fixture is typically the task of testing framework like GTest, while we could also do that in CMake level.

+ To create test fixture with CMake, use `add_test` to create test cases for setup and cleanup actions first, and then mark them as setup and cleanup with `set_tests_properties`:

  ```cmake
  set_tests_properties(
    setup
    PROPERTIES
      FIXTURES_SETUP my-fixture
  )
  
  set_tests_properties(
    cleanup
    PROPERTIES
      FIXTURES_CLEANUP my-fixture
  )
  ```

  For test cases included in the fixture, mark them as `FIXTURES_REQUIRED`:

  ```cmake
  set_tests_properties(
    feature-a
    feature-b
    PROPERTIES
      FIXTURES_REQUIRED my-fixture
  )
  ```

## Chapter 5  Configure-time and Build-time operations

There are three important periods during the whole process of building with CMake:

+ configure time: processing the `CMakeLists.txt`
+ generation time: generating files for the native build tool
+ build time: invoking the native build tool to create the targets (executables and libraries)

### 5.1  Using platform-independent file operations

+ When we need to execute some commands during the build, we can use `add_custom_target` to bundle those commands as a target and then use `add_dependencies` to make it a dependency of our application target:

  ```cmake
  add_custom_target(unpack-eigen
    ALL
    COMMAND
      ${CMAKE_COMMAND} -E tar xzf ${CMAKE_CURRENT_SOURCE_DIR}/eigen-eigen-5a0156e40feb.tar.gz
    COMMAND
      ${CMAKE_COMMAND} -E rename eigen-eigen-5a0156e40feb eigen-3.3.4
    WORKING_DIRECTORY
      ${CMAKE_CURRENT_BINARY_DIR}
    COMMENT
      "Unpacking Eigen3 in ${CMAKE_CURRENT_BINARY_DIR}/eigen-3.3.4"
  )
  
  add_dependencies(linear-algebra unpack-eigen)
  ```

+ Note the argument following `COMMAND` here:

  ```cmake
  ${CMAKE_COMMAND} -E rename eigen-eigen-5a0156e40feb eigen-3.3.4
  ```

  Actually `-E` indicates the command mode of CMake, which is followed by the platform-independent command applied in CMake.

+ Use `cmake -E` to list all those platform-independent commands.

### 5.2  Running a custom command at configure time

+ Note that when using `execute_process`, we can specify many commands to execute, and the output of the last command will be passed as the input of the next one, which means only the last output can be picked out.
+ Remember commands in `execute_process` are executed in configure time.

### 5.3  Running a custom command at build time: I. Using add_custom_command

+ If we use `add_custom_command` with `OUTPUT` as the first argument, it will generate some files when any target in the same `CMakeLists.txt` or which uses any file generated by the custom command is built:

  ```cmake
  add_custom_command(
    OUTPUT
      ${wrap_BLAS_LAPACK_sources}
    COMMAND
      ${CMAKE_COMMAND} -E tar xzf ${CMAKE_CURRENT_SOURCE_DIR}/wrap_BLAS_LAPACK.tar.gz
    COMMAND
      ${CMAKE_COMMAND} -E touch ${wrap_BLAS_LAPACK_sources}
    WORKING_DIRECTORY
      ${CMAKE_CURRENT_BINARY_DIR}
    DEPENDS
      ${CMAKE_CURRENT_SOURCE_DIR}/wrap_BLAS_LAPACK.tar.gz
    COMMENT
      "Unpacking C++ wrappers for BLAS/LAPACK"
    VERBATIM
  )
  ```

### 5.4  Running a custom command at build time: I. Using add_custom_target

+ To circumvent the limitation that only targets in the same `CMakeLists.txt` with the `add_custom_command` could trigger it, we can combine both `add_custom_command` and `add_custom_target`:

  ```cmake
  add_custom_target(BLAS_LAPACK_wrappers
    WORKING_DIRECTORY
      ${CMAKE_CURRENT_BINARY_DIR}
    DEPENDS
      ${MATH_SRCS}
    COMMENT
      "Intermediate BLAS_LAPACK_wrappers target"
    VERBATIM
  )
  
  add_custom_command(
    OUTPUT
      ${MATH_SRCS}
    COMMAND
      ${CMAKE_COMMAND} -E tar xzf ${CMAKE_CURRENT_SOURCE_DIR}/wrap_BLAS_LAPACK.tar.gz
    WORKING_DIRECTORY
      ${CMAKE_CURRENT_BINARY_DIR}
    DEPENDS
      ${CMAKE_CURRENT_SOURCE_DIR}/wrap_BLAS_LAPACK.tar.gz
    COMMENT
      "Unpacking C++ wrappers for BLAS/LAPACK"
  )
  ```

  so that the custom commands will always be executed.

### 5.5  Running custom commands for specific targets at build time

+ If we use `add_custom_command` with `TARGET` as the first argument, it will register some, like, hooks to a target:

  ```cmake
  add_custom_command(
    TARGET
      example
    POST_BUILD
    COMMAND
      ${PYTHON_EXECUTABLE}
        ${CMAKE_CURRENT_SOURCE_DIR}/static-size.py
        $<TARGET_FILE:example>
    COMMENT
      "static size of executable:"
    VERBATIM
  )
  ```

+ We can register the hook to two times: `PRE_LINK` (for Linux, `PRE_BUILD` has the same effect with `PRE_LINK`) and `POST_BUILD`, whose names are self-explanatory.

##### Last-modified date: 2020.10.17, 5 p.m.