# Concurrency with Modern C++ Notes

## Chapter 1  Memory Model

### 1.1  Basics of the Memory Model

+ A **memory location** is:

  + an object of an scalar type (arithmetic type, pointer, enumeration type or `std::nullptr_t`)
  + or the largest contiguous sequence of bit fields not including zero length.

  the second case seems to be hard to understand, ok, first we should know about what bit fields are.

+ Typically bit fields are structs with members declared to hold bit-wise size, e.g.

  ```c++
  struct S {
      unsigned char b1 : 3;
      unsigned char    : 0;
      unsigned char b2 : 6;
      unsigned char b3 : 2;
  };
  ```

  so what does the `:0` of the second fields mean? It means the next field should start from a new byte and `:0` can only appear as a nameless field. So the memory of the struct would look like below:

  ```
  byte0    byte1
  |--------|--------|
   <->      <----><>
   b1       b2    b3
  ```

+ Ok let's go back to the definition of memory location, "*the largest contiguous sequence of bit fields not including zero length*" means the bit fields segment that separated by the zero-length field, which is to say, in the above example, `b1` is a memory location while `b2` and `b3` holds another memory location.

  Also, it's safe to say that adjacent bit fields share the same memory location.

+ With the understanding of memory location, we can say that if two threads access the same memory location simultaneously, there will be a race condition. Of course we have two approaches to solve this: **all-or-nothing** (atomic operation) and **before-or-after** (lock).

### 1.2  The Contract

+ There are three levels of contracts / rules in C++, from strong to weak: single threading, multi-threading and atomic.
+ Inside the Atomic rules, there are three memory models, from strong to weak: sequential consistency, acquire-release semantic and relaxed semantic.
+ The *strong* or *weak* is to say as programmers, we are *sure* or *uncertain* about the control flow, execution sequence or something like that.

### 1.3  Atomics

+ Sequential consistency guarantees that 

  + instructions are executed in the order they are written down,
  + and there is a global order of all operations on all threads (imagine that there is a global clock, whenever it ticks, an operation is executed and seen by all threads).

  Acquire-release sematic guarantees that threads are synchronized only at some specific points.

  Relaxed semantic makes few guarantees.

+ We can specify which memory order to adopt when using atomic operations. Sequential consistency is the default behavior.

+ `std::atomic_flag` is an atomic boolean, which has two states: **set** and **clear**. It provides two methods: `clear` and `test_and_set`.

  `std::atomic_flag` is the only [lock-free](https://stackoverflow.com/questions/14011849/what-is-lock-free-multithreaded-programming) atomic. It can be used in some `std::atomic`s, whose `is_lock_free` method will return true.

  Compared to mutex (switch to another thread, passive waiting), `std::atomic_flag` can be used to implement spinlock (waste CPU cycles, active waiting).

+ In contrast with `std::atomic_flag`, `std::atomic` provides `load` and `store` methods. `std::atomic<bool>` and `std::atomic<user-defined type>` have the most limited methods, `std::atomic<T*>` extends from that and `std::atomic<arithmetic type>` extend from `std::atomic<T*>`.

  Also, `std::atomic` provides CAS operations: `compare_exchange_strong` and `compare_exchange_weak`

+ `std::atomic<user-defined type>` could be applied for classes that satisfy some requirements, such as no user-defined copy assignment operator, no virtual methods, etc.

  `std::atomic<T*>` and `std::atomic<arithmetic type>` extend more methods such as `++`, `fetch_*`, etc.

+ `std::atomic` has no copy constructor and copy assignment operator, but they support an assignment from and an implicit conversion to the underlying type

+ For some reasons such as compatibility with C, there are also some free atomic functions, which accept a pointer to `std::atomic` as the first argument. However, there is an exception: `std::shared_ptr`.

  `std::shared_ptr` needs atomic operations because it by definition cannot guarantee thread safety when different threads **write** to the same `std::shared_ptr` at the same time.

  ```c++
  std::shared_ptr<int> ptr = std::make_shared<int>(2011);
  
  for (auto i = 0; i < 10; i++) {
     std::thread([&ptr] {
       // ptr = std::make_shared<int>(2014);  // not thread-safe
       auto localPtr = std::make_shared<int>(2014);
       std::atomic_store(&ptr, localPtr);     // thread-safe       
     }).detach(); 
  }
  ```

  Moreover, `std::atomic` can also apply for `std::shared_ptr` since C++20.

##### Last-modified date: 2020.10.21, 11 p.m.