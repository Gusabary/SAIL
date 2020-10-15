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

##### Last-modified date: 2020.10.15, 10 p.m.