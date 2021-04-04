# GoogleTest

**version: release-1.10.0**

+ root source directory and root build directory of **subproject** can be referred to as `${subproj_SOURCE_DIR}` and `${subproj_BINARY_DIR}`.

+ TODO: use `GTEST_API_` to qualify symbols that need to be exported (`internal/gtest-port.h:739`)

+ TODO: *Koenig loopkup* (`gtest-message.h:113`)

+ to avoid user misspelling `SetUp` as `Setup`, GTest takes an interesting mechanism to discover that problem in compile time. (`gtest.h:512`)

+ ? what's *shared*?

+ ? what's *type parameter* and *value parameter*?

  it seems that a test suite can have *type parameter* and *value parameter* so different tests can be generated according to different parameter. but seemingly support just single parameter.

+ classes defined in `gtest.h`:

  + `AssertionResult`
  + `Test`
  + `TestProperty`
  + `TestResult`
  + `TestInfo`
  + `TestSuite`
  + `Environment`
  + `TestEventListener`
  + `TestEventListeners`
  + `UnitTest`: singleton, pimpl
  + `EqHelper`
  + `AssertHelper`
  + `WithParamInterface`
  + `ScopedTrace`

+ classes in `gtest.h` uses virtual and non-virtual destructor to indicate whether the class can be inherited, maybe better to use `final` specifier?

+ BUG: forgot to use indirection pattern of stringification? (`internal/gtest-internal.h:1370`)

+ use `__FILE__` and `__LINE__` to get code location

+ whenever adding a test through `GTEST_TEST_` (`internal/gtest-internal.h:1350`), its `TestInfo` will be created and registered to the `UnitTest` singleton (`gtest.cc:2581`), for `RUN_ALL_TESTS()` in the future.

+ ? what's *death tests*?


