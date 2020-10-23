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

### 1.4  The Class Template `std::atomic_ref` (C++20)

+ Before getting close to `std::atomic_ref`, I think we should first know about `std::ref`. `std::ref` is a, umm, free function which returns a `std::reference_wrapper<T>`. That is what really matters.

  `std::reference_wrapper` is essentially a class template. Its instantiated class has `sizeof` of 8, in another word, it holds just a pointer as member (so it seems more reasonable to be pointer wrapper?).

  It can be converted implicitly or with `get` method to reference of the underlying type.

  Different from the raw reference, it can be rebind with `operator=`, so it's by definition copyable and assignable.

  *[reference](https://www.nextptr.com/tutorial/ta1441164581/stdref-and-stdreference_wrapper-common-use-cases)*

+ Now things are clear, `std::atomic_ref` is atomic version of `std::ref`. Similar to `std::atomic`, `std::atomic_ref` also has extended methods for `T*` partial specialization and more extended methods for arithmetic full specialization.

### 1.5  The Synchronization and Ordering Constraints

+ C++ provides six variants of memory ordering, among which the default is `std::memory_order_seq_cst`. One thing worth noting is that what can be configured with those memory ordering variants is atomic **operation** instead of atomic data type itself.

+ Let's introduce the six memory orderings. First we should know the fact that there are two kind of atomic operations: read and write (of course, there is a *read-modify-write*, but we can regard it essentially as a *read* plus a *write*). Each operation has an attribute indicating its memory model (the attribute has five possible values: `SeqCst`, `Acquire`, `Consume`, `Release` and `Relaxed`), to be more precise:

  |                          | read                 | write     |
  | ------------------------ | -------------------- | --------- |
  | sequential consistency   | `SeqCst`             | `SeqCst`  |
  | acquire-release semantic | `Acquire`, `Consume` | `Release` |
  | relaxed semantic         | `Relaxed`            | `Relaxed` |

  From the table, we can say, for example, if an atomic read operation has an attribute of `SeqCst`, it's memory model is sequential consistency; if an atomic write operation has an attribute of `Relaxed`, it's memory model is relaxed semantic.

  Ok that's the mapping from attribute (which is a concept I construct) to memory model, so how about the one from the memory ordering (`std::memory_order_*`) to the attribute?

  The answer is simple, each memory ordering gives corresponding attribute contained in its name:

  |                                         | read      | write     |
  | --------------------------------------- | --------- | --------- |
  | `std::memory_order_seq_cst`             | `SeqCst`  | `SeqCst`  |
  | `std::memory_order_acq_rel`             | `Acquire` | `Release` |
  | `std::memory_order_release` (for write) | `Relaxed` | `Release` |
  | `std::memory_order_acquire` (for read)  | `Acquire` | `Relaxed` |
  | `std::memory_order_consume` (for read)  | `Consume` | `Relaxed` |
  | `std::memory_order_relaxed`             | `Relaxed` | `Relaxed` |

  Note that if you specify `std::memory_order_release` (which is designed for write operation) in an atomic read operation (`load`), the actual memory model applied will be relaxed semantic.

+ Now we have already know the relations between memory ordering and memory model. It's time to get closer to each kind of memory model. Wait, before that, we should tweak our mind about the atomic read and write operations:

  + read is not just a single read, it's actually a potential **sync** plus a read,
  + write is not just a single write, it's actually a write plus a potential **flush**.

  Let's take a deep insight into that by inspecting memory models.

+ Different memory models have different details about the sync and flush:

  + In **sequential consistency** memory model, the sync is to sync from **system** while the flush is also to flush to **system**;
  + In **acquire-release semantic** memory model, the sync is to sync from **what the release thread has flushed to system**, while the flush is to flush to **system**;
  + In **relaxed semantic** memory model, there is no sync or flush.

+ Above is the discussion about the variable visibility, how about the execution order?

  My perspective is that if some variables need flushing, they should be evaluated first so the order is guaranteed; while if they don't need flushing, the evaluation can be delayed and they can be reordered across the boundary built by atomic operation.

  The condition for sync is similar. If sync is to happen, the statements behind the atomic operation should not be reordered up, otherwise the synced variables would get polluted.

+ About the `Consume` attribute, there is something to point out:

  + `Consume` attribute leads to, umm, more precisely, *consume-release semantic* memory model, in which the sync is to sync the **dependent variables** among what the release thread has flushed to system, which is a little bit weaker than acquire-release semantic;
  + However, in the present stage, consume-release semantic just lives in the C++ standard. It seems that compilers map `std::memory_order_consume` to `std::memory_order_acquire`.

+ *[reference](https://gcc.gnu.org/wiki/Atomic/GCCMM/AtomicSync)*

##### Last-modified date: 2020.10.23, 7 p.m.