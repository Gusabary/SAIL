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

### 1.6  Fences

+ Fences are memory barriers, which prevent that operations in both side cannot be reordered through it.

  There are three kinds of `std::atomic_thread_fence`: full fence, acquire fence and release fence. To be more specific, full fence can prevent reorder of all combinations of load and store operations, except store-load; acquire fence cannot prevent reorder of store-* while release fence cannot prevent reorder of load-*.

+ So how about the relation between atomics and fences. Generally speaking, fences need no atomics and they are more heavyweight.

  My understanding of the *heavyweight* is that fences prevent reorder of more operation combinations than atomics with the same memory ordering. Take `std::memory_order_acquire` for an example, atomic with this memory order guarantees that read and write operations cannot be reordered before an atomic load while fences also guarantee that load operations cannot be reordered after, which is a bidirectional limit.

  With the fences, atomic load and store operation with acquire/release memory model can be replaced by ones with relaxed semantic:

  ```c++
  // atomics
  ptr.store(p, memory_order_release);
  
  // fences
  atomic_thread_fence(memory_order_release);
  ptr.store(p, memory_order_relaxed);
  ```

+ For synchronization between signal handler and the code running in the **same** thread, `std::atomic_signal_fence` should be used. It seems that `std::atomic_signal_fence` emits fewer hardware fence instructions than `std::atomic_thread_fence` since it synchronizes instructions in the same thread.

## Chapter 2  Multithreading

### 2.1  Threads

+ `std::thread` has no copy operations. It accepts a callable as work package, whose return value is ignored.

  The creator of `std::thread` should manage its lifecycle, i.e. it should invoke `join()` to wait the thread ends or `detach()` to detach itself from the thread. Actually, before `join()` or `detach()` is called, the thread is *joinable*, and the destructor of a joinable thread throws a `std::terminate` exception.

  One thing worth noting is that detached threads will terminate with the executable binary, which means when the main thread exits, all detached threads will also exit even if their work package hasn't fully done. Take below for an example:

  ```c++
  int main() {
      std::thread t([] { std::cout << "hello" << std::endl; });
      t.detach();
      // if this line is commented, "hello" may not be printed
      // std::this_thread::sleep_for(std::chrono::milliseconds(1));
      return 0;
  }
  ```

+ `std::thread`'s constructor is a variadic template. So if you want to pass argument by reference, it needs to use `std::ref` even if the parameter of the callable as work package is reference.

+ We can use `swap()` method to swap (in a move way) two threads.

+ We can use `std::thread::native_handle()` to get information about system-specific implementation of `std::thread`.

### 2.2  Shared Data

+ Insertion to and extracting from global stream objects (like `std::cin`, `std::cout`) are thread-safe, although the output statements can interleave. In another word, writing to `std::cout` is not a data race but a race condition (of output statements).

+ There are many kinds of mutex. Most basically, there is a `std::mutex`, which supports `lock()`, `try_lock()` and `unlock()`. Then it's `std::recursive_mutex`, which can lock many times and stay locked until unlock as many times as it has locked. There also `std::timed_mutex` and `std::recursive_timed_mutex` which support `try_lock_for()` and `try_lock_until()`.

  `std::shared_timed_mutex` (since C++14) and `std::shared_mutex` (since C++17) also provide a series of methods of `*_lock_shared_*`, which can be used to implement read-write lock (introduced later).

+ Cool, right? Since we have mutex we can write some code like this:

  ```c++
  std::mutex m;
  m.lock();
  sharedVariable = getVar();
  m.unlock();
  ```

  However, it's quite prone to deadlock due to the `getVar()`: what if it throws an exception? what if it also acquire the mutex `m`? what if it's a library function and someday gets upgraded with some code you never know?

  So apparently, it's better to avoid calling functions while holding a lock.

+ To solve deadlocks, we can use *locks*: `std::lock_guard`, `std::unique_lock`, `std::shared_lock `(since C++14) and `std::scoped_lock` (since C++17).

  First let's look at `std::lock_guard`. Maybe you've heard about *RAII*. Yep, that's the mechanism `std::lock_guard` uses to solve the deadlock which happens when you forget to release the lock (maybe because an exception is thrown):

  ```c++
  {
    std::mutex m;
    std::lock_guard<std::mutex> lockGuard(m);
    /* critical section */
  }
  ```

+ Then it's `std::unique_lock`, which is stronger but more expensive than `std::lock_guard`. For example it enables you to create a lock without locking the mutex immediately, recursively lock a mutex and so on.

  One thing worth noting is that we can use `std::lock()`, which is a variadic template, to lock multiple mutexes in an atomic step:

  ```c++
  std::mutex a, b;
  std::unique_lock<std::mutex> guard1(a, std::defer_lock);
  std::unique_lock<std::mutex> guard2(b, std::defer_lock);
  std::lock(guard1, guard2);
  ```

+ Here comes `std::shared_lock`, which behaves like `std::unique_lock`, except in the condition that it's used with `std::shared_mutex` or `std::shared_timed_mutex` (which are introduced before). It can be used to implement a read-write lock. To be more precise, `std::lock_guard<std::shared_mutex>` or `std::unique_lock<std::shared_mutex>` is used for write lock while `std::shared_lock<std::shared_mutex>` is used for read lock. This is essentially because `std::shared_mutex` supports both `*_lock_*` and `*_lock_shared_*` methods which invoked separately by `std::unique_lock` and `std::shared_lock`.

+ Finally it's `std::scoped_lock`. Still remember the `std::lock()` function? Yep, they are very similar. Actually, `std::scoped_lock`'s constructor is a variadic template, which 1) behaves like a `std::lock_guard` when there is just one mutex argument, 2) invokes `std::lock()` when there are multiple mutex arguments.

  In another word, `std::scoped_lock` can lock many mutexes in an atomic step.

+ Sometimes we need to ensure that objects are initialized in a thread-safe way (imagine the singleton design pattern), typically there are three ways to do that (ok, if you count in initializing objects in main thread before creation of child threads, there are four).

  The first is use `constexpr` to initialize objects as constant expressions in compile time. Note that an object can be annotated as `constexpr` only if its class satisfies some restrictions. For example, it cannot have virtual base class and virtual methods; it's constructor must be empty (except for the initialization list) and const expression; its base classes and non-static members should all be initialized (in the initialization list) and so on.

+ The second is to use `std::call_once` and `std::once_flag`. The semantic is easy to understand: `std::call_once` is a function, which accepts two parameters, the first one is a `std::once_flag` and the second one is a callable. We can invoke `std::call_once` many times with the same `std::once_flag`, and exactly one callable of them will be executed exactly once.

  Use this to implement singleton:

  ```c++
  class MySingleton {
  private:
      static std::once_flag initInstanceFlag;
      static MySingleton* instance;
      MySingleton() = default;
      ~MySingleton() = default;
  
  public:
      MySingleton(const MySingleton&) = delete;
      MySingleton& operator=(const MySingleton&) = delete;
  
      static MySingleton* getInstance(){
          std::call_once(initInstanceFlag, MySingleton::initSingleton);
          return instance;
      }
  
      static void initSingleton(){
          instance = new MySingleton();
      }
  };
  
  MySingleton* MySingleton::instance = nullptr;
  std::once_flag MySingleton::initInstanceFlag;
  ```

+ The third is static variables with block scope. Those static variables are created exactly once and lazily, which means they won't get created until used. And since C++11, there is another guarantee: static variables with block scope are created in a thread-safe way (but it seems to be dependent on compiler implementations). So we can write a singleton class like this:

  ```c++
  class MySingleton {
  public:
      static MySingleton& getInstance() {
          static MySingleton instance;
          return instance;
      }
  
  private:
      MySingleton() = default;
      ~MySingleton() = default;
      MySingleton(const MySingleton&) = delete;
      MySingleton& operator=(const MySingleton&) = delete;
  };
  ```

### 2.3  Thread-Local Data

+ Actually I've never heard about the `thread_local` keyword in C++ before. This keyword acts like `static`:

  + if it qualifies a variable in namespace scope or as static class member, the variable will be created before its first usage:

    ```c++
    class A {
    public:
        thread_local static int x;
    };
    
    thread_local int A::x;
    ```

  + if it qualifies a variable in a function, the variable will be created at its first usage:

    ```c++
    void f() {
        thread_local int a;
    }
    ```

+ The difference between `thread_local` and `static` is that variables qualified by the former have lifecycle bound to the thread which created them while ones qualified by the latter have lifecycle bound to the main thread.

### 2.4  Condition Variables

+ `std::condition_variable` is literally a condition variable, which provides methods like `notify_one()`, `notify_all()`, `wait()` and so on.

  The `wait()` method usually accepts two parameters, the first one is a `std::unique_lock` and the second one is a callable called *Predict*. Let's take a closer look.

+ Why does the lock need to be a `std::unique_lock` instead of `std::lock_guard`? Be aware that when `wait()` is invoked, the lock gets released and actually we will see later that the lock gets acquired and released repeatedly, so we need a `std::unique_lock` instead of a one-time `std::lock_guard`.

+ Then what's the role of *Predict*? When talking about condition variables, we should be clear about these two phenomena: *lost wakeup* and *spurious wakeup*. Lost wakeup is to say the notify could come before the wait while spurious wakeup is to say the thread in waiting state could wake up itself even if there is no notification. Predict is to solve these problems:

  ```c++
  std::unique_lock<std::mutex> lck(mutex_);
  condVar.wait(lck, []{ return dataReady; });
  
  // equivalent to
  std::unique_lock<std::mutex> lck(mutex_);
  while ( ![]{ return dataReady; }() ) {
      condVar.wait(lck);
  }
  ```

+ The `dataReady` in the above example is a flag used to synchronize the notification. It doesn't need to be an atomic, but it must be protected by a mutex (we can use `std::lock_guard` here):

  ```c++
  {
  	std::lock_guard<std::mutex> lck(mutex_);
  	dataReady = true;
  }
  condVar.notify_one();
  ```

  If not protected by a mutex, it may happen that the modification to `dataReady` and notification are executed **rightly after the Predict check and before the condition variable wait**, which will cause the thread to wait forever.

### 2.5  Tasks

+ Task is also a mechanism to perform work package asynchronously. Different from threads, tasks are not necessarily in another thread. Actually, the workflow of tasks is to perform the work package and produce the promise, and the result can be synchronized through a future.

+ `std::async` is a simple way to create a task, and its return value is the future of the task. Other than the work package, we can pass in a policy when invoking `std::async`, which can be `std::launch::deferred` for lazy evaluation or `std::launch::async` for eager evaluation.

  Also, it's not necessary to assign the return value of `std::async` to a variable. In another word, we can just invoke it and dismiss its return value, in which case the future is called *fire and forget future*:

  ```c++
  std::async(std::launch::async, []{ std::cout << "fire and forget" << std::endl; });
  ```

  Note that we need `std::launch::async` to make sure a eager evaluation because we have no future to wait on.

  However, there is an inconspicuous drawback here: future waits on its destructor until its promise is done. In the case of fire and forget futures, the futures are temporary, whose destructor gets invoked immediately after the `std::async` creating them. So the async is actually, umm, a fake one:

  ```c++
  std::async(std::launch::async, [] {
      std::this_thread::sleep_for(std::chrono::seconds(5));
      std::cout << "first thread" << std::endl;
  });
  /* waiting for the promise done */
  
  std::async(std::launch::async, [] {
      std::this_thread::sleep_for(std::chrono::seconds(1));  
      // get printed after 6 seconds instead of 1
      std::cout << "second thread" << std::endl;
  });
  ```

+ `std::package_task` is another way to create a task which is not executed immediately. Actually its usage typically consists of four steps:

  ```c++
  // 1. create the task
  std::packaged_task<int(int, int)> sumTask([](int a, int b){ return a + b; });
  
  // 2. assign to a future
  std::future<int> sumResult = sumTask.get_future();
  
  // 3. do the execution
  sumTask(2000, 11);
  
  // 4. wait on the future
  sumResult.get();
  ```

  To my understanding, `std::async` combines the first three steps together and the task created by it cannot accept parameters.

  If we want to execute the task and wait on the future multiple times, it needs to invoke the `reset()` method of `std::packaged_task`.

+ `std::promise` can set not only a value but also an exception with `set_exception()` method. If that's the case, the corresponding future will encounter the exception when invoking the `get()` method.

  `std::future` can use `valid()` method to check if the shared state is available and use `wait_for()` or `wait_until()` to wait with a timeout. The latter returns a `std::future_status`, which is a scoped enum class with enumerations of `deferred`, `ready` and `timeout` (to be frank, I don't know what `deferred` means)

+ Different from `std::future`, `std::shared_future` is copyable and can be queried multiple times.

  We have two ways to get a `std::shared_future`: `get_future()` method of `std::promise` and `share()` method of `std::future`. Note that after invocation of `share()`, the `valid()` method of `std::future` shall return false.

+ I think it needs a clarification about *available shared state* here. We know `valid()` method of `std::future` or `std::shared_future` indicates whether an available shared state exists. In another word, if it returns true, `wait()` method can be called without exception; if it returns false, `wait()` will result in an exception.

  For initialized `std::future`, before the first `get()`, `wait()` or `share()`, the `valid()` will return true; while after that, `valid()` shall return false. And for initialized `std::shared_future`, `valid()` shall always return true, which means you can always query on a `std::shared_future`.

+ If the callable used in `std::async` and `std::packaged_task` throws an exception, it will be stored in the shared state (just like what `set_exception()` method of `std::promise` does), and rethrown when queried by future. One thing worth noting is that `std::current_exception()` can be used to get the caught exception in the catch block.

+ `void` as the template argument, `std::promise` and `std::future` could be used for notification and synchronization. Compared to condition variables, the task-based notification mechanism could not perform synchronization multiple times (since `std::promise` could only set its value once and `std::future` could only query once) but needn't a shared variable or mutex and isn't prone to lost wakeup or spurious wakeup.

  So the conclusion is that if multiple synchronization is not needed, task-based notification mechanism is preferred.

##### Last-modified date: 2020.10.30, 7 p.m.