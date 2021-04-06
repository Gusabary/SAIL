# wasm-micro-runtime

**version: WAMR-03-25-2021**

+ wonderful approach to checking endian (`wasm_loader.c:3066`):

  ```c
  static union {
      int a;
      char b;
  } __ue = { .a = 1 };
  
  #define is_little_endian() (__ue.b == 1)
  ```

+ `SECTION_TYPE_DATACOUNT`'s id should be always 12 if any, shouldn't it? (`wasm_loader.c:2971`)

+ load -> instantiate -> interpret

+ what is **heap** for during memory instantiation? 

+ how to terminate a wasm instance

