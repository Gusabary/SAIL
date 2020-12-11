# Spdlog Anatomy Notes

**version: v1.8.1**

+ It's easy to read configuration from CMake to header file (with `add_definition`). Reversely, configuration written in header file (like version number) can be read by CMake also (with `file(READ ...)`, refer to `cmake/utils.cmake`)
+ A logger is corresponding to multiple sinks. (can print to many files)
+ `level` (both log level and flush level) is atomic type.
+ Warning: construct a `shared_ptr` from `this`, this may create potentially multiple control blocks. (`logger-inl.h:160`)
+ `log_msg` can be considered as one-off, so it holds `string_view` as `logger_name` and `payload`. But for `backtracer`, it needs to store the `log_msg` for future dump so it stores them in a `log_msg_buffer`, whose payload is allocated in heap instead of stack. (`details/log_msg_buffer.h:12`)
+ Circular queue's actual capacity should be one larger than it declared for the full marker. (`details/circular_q.h:28`)
+ A common technique to improve performance: make a shortcut with cache to avoid time-consuming work each time. (`pattern_formatter-inl.h:1031`)
+ Class `base_sink` uses [NVI idiom](http://gusabary.cn/2020/11/22/Modern-C++-Programming-Cookbook-Notes/Modern-C++-Programming-Cookbook-Notes-7/#10-4-Separating-interfaces-and-implementations-with-the-non-virtual-interface-idiom). (`sinks/base_sink.h:19`) One of the advantages is that locking is taken care of by parent class, so child class only needs to focus on log logic.
+ I suppose if we use it as compiled lib, the *include dependency chain* is `.cpp` -> `-inl.h` -> `.h`, while if we use it as header-only lib, the chain will be `.h` -> `-inl.h`.
+ Append another extra enumerator to the enum to indicate the number of enumerators automatically. (`common.h:148` and `ansicolor_sink.h:87`)

##### Last-modified date: 2020.12.11, 9 p.m.

