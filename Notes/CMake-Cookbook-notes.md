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

##### Last-modified date: 2020.10.14, 12 a.m.