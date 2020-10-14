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

##### Last-modified date: 2020.10.14, 11 p.m.