# C++ Notes

## 智能指针

+ unique_ptr 禁用掉了拷贝构造和赋值重载，也即意味着它不能被赋值、赋值初始化、值传参、值返回（这一点编译器可能存在优化，因为返回后原函数内的 unique_ptr 就被销毁了，所以实现成移动构造的话值返回就是可行的）。

+ weak_ptr 表达临时所有权的概念，不会增加被指向的资源的引用计数。

  weak_ptr 可以通过一个 shared_ptr 或另一个 weak_ptr 进行构造。

  当需要通过 weak_ptr 访问资源时，需要先用 lock 方法将其转换成 shared_ptr 以获取资源的所有权。

  可以通过 expired 方法检查 weak_ptr 指向的资源的引用计数是否为 0（是否有其他 shared_ptr 指向它）

+ weak_ptr 可以用于解决循环引用的问题：

  ```
  +-----+  1  +-----+  2  +-----+  4  +-----+
  |  A  |---->|  a  |---->|  b  |<----|  B  |
  |     |     |     |<----|     |     |     |
  +-----+     +-----+  3  +-----+     +-----+
  ```

  当 A, B 退出作用于被销毁时，指针 1, 4 也被销毁，但是 a, b 并不会被释放，因为还有指针 2, 3 的存在。将指针 2, 3 改成 weak_ptr 可以解决这一问题。

  *[reference](<https://segmentfault.com/a/1190000016055581#item-3-6>)*

+ 对于传参和返回时使用智能指针还是裸指针或是引用，C++ Core Guidelines 中有[一条建议](<https://isocpp.github.io/CppCoreGuidelines/CppCoreGuidelines#r30-take-smart-pointers-as-parameters-only-to-explicitly-express-lifetime-semantics>)是只有当你需要操作智能指针本身时，使用它作为入参或返回值，否则你都应该使用裸指针（`p.get()`）或引用（`*p.get()`）。也就是说没有用到智能指针本身的特性，就不要将入参或返回值限定为智能指针，否则裸指针就传不进去了。

+ 对于 shared_ptr 来说，其 reference count 的读写是线程安全的，但是其所指向的资源的读写不能说是完全线程安全的：

  + 一个 shared_ptr 可以被多个线程同时读
  + 多个 shared_ptr 可以被多个线程同时写，但是一个 shared_ptr 不能被多个线程同时写

  为了支持 shared_ptr 的原子操作，有 `std::atomic_*` 一系列函数；而 C++20 引入 `std::atomic_shared_ptr` 彻底解决 shared_ptr 线程安全这一问题，相比之下后者有如下几个优点：

  + 一致性：shared_ptr 本不是原子的类型，却有原子操作
  + 正确性：对于 atomic_shared_ptr，编译器确保必须使用原子操作（不会因为疏忽导致忘记使用）
  + 性能：atomic_shared_ptr 专为多线程而生，可以有更高效的锁的实现，而 shared_ptr 为了使用场景更多的单线程 workload，势必要放弃一些只对多线程高效的实现。

  *references*:

  + <https://www.modernescpp.com/index.php/atomic-smart-pointers>
  + <https://www.justsoftwaresolutions.co.uk/threading/why-do-we-need-atomic_shared_ptr.html>

+ sizeof 看一个 unique_ptr 为 8 字节，shared_ptr 和 weak_ptr 为 16 字节（64 位下）

## 右值引用和移动语义

+ C++ 11 引入，所谓右值引用，赋予了程序员修改右值的能力。在引入右值引用之前，是没有办法修改这个右值的，也即这个右值所在地址中的值不能被修改，只能一直是这个右值（可能存在一个误区：右值不能取地址，但不代表右值没有地址）：

  ```c++
  int &x = 5;       // Error
  const int &x = 5; // OK
  ```

  可见虽然可以将右值赋值给左值引用，但这个左值引用必须是常量引用，仍然无法修改其值。

  引入右值引用后，就可以将右值赋给右值引用，然后修改：

  ```c++
  int &&x = 5;
  x = 6;
  ```

+ 有了右值引用以后，就可以用来实现移动语义。移动是相对拷贝而言的，例如拷贝构造和拷贝赋值。如果被拷贝的对象是一个右值，实际上就没有必要真的拷贝了，将其内容直接转移给新对象，然后自己变成一个 null （即移动语义）可以节省一次拷贝的开销。

+ 利用移动语义的具体方法是实现移动构造和移动赋值：

  ```c++
  A(const A& a);             // 拷贝构造
  A& operator=(const A& a);  // 拷贝赋值
  A(A&& a);                  // 移动构造
  A& operator=(A&& a);       // 移动赋值
  ```

  对于实现了移动构造的类，编译器会根据用来构造的对象是左值还是右值自动调用拷贝构造或者移动构造。但是如果没有实现移动构造，那么不管用来构造的对象是左值还是右值，都只会调用拷贝构造。（前提是实现了拷贝构造）。总结一下：

  |                                | 用左值构造   | 用右值构造   |
  | ------------------------------ | ------------ | ------------ |
  | 未实现拷贝构造和移动构造       | 默认拷贝构造 | 默认移动构造 |
  | 实现了拷贝构造，未实现移动构造 | 拷贝构造     | 拷贝构造     |
  | 未实现拷贝构造，实现了移动构造 | 非法行为     | 移动构造     |
  | 实现了拷贝构造和移动构造       | 拷贝构造     | 移动构造     |

  所谓默认构造是说如果一个类没有显示声明拷贝构造或者移动构造，而又用到这些函数时，编译器会生成一个默认版本，默认版本的实现是：

  + 对于类中的对象成员，调用它们的拷贝构造或者移动构造（取决于该默认版本是默认拷贝还是默认移动）
  + 对于类中的基础类型数据成员（数组除外，例如指针，int），直接赋值（浅复制）
  + 对于类中的数组成员，将数组元素一个个复制过去（深复制）

+ 利用 `std::move()` 函数可以将一个左值转换成一个右值，这样可以使用移动语义，当构造之后不再需要这个左值时，该函数特别有用。

### 完美转发

+ 传参的时候，左值引用只能接受左值，右值引用只能接受右值，所以有时候需要进行重载：

  ```c++
  void f(int &x) {}
  void f(int &&x) {}
  ```

+ 完美转发利用了 c++ 标准中的两个机制：reference collapsing 和 special type deduction：

  + reference collapsing 是说对于 `T&` 或是 `T&&` 这样的类型，`T` 有可能也是个引用，所以需要有一个规则将引用的引用 collapse 成引用：

    + `T& &` -> `T&`
    + `T& &&` -> `T&`
    + `T&& &` -> `T&`
    + `T&& &&` -> `T&&`

    简言之，只要有 `&`，collapse 的结果就是 `&`

  + special type deduction 是说对于 `T&&` 这样的类型，会进行一次类型推断，如果是左值（例如 int 左值）则将 `T` 推断为 `int&`，所以 `T&&` 就是 `int& &&` 即 `int &`；如果是右值（例如 int 右值）则将 `T` 推断为 `int`，所以 `T&&` 就是 `int&&`。

    所以 `T&&` 又叫 forwarding reference（也叫 universal reference）

    需要注意的是 `int`, `int&`, `int&&` 都是左值（右值引用 ≠ 右值）

  *[reference](<https://eli.thegreenplace.net/2014/perfect-forwarding-and-universal-references-in-c>)*

+ 完美转发利用上述两个机制在不用重载的情况下区分出入参的左右值属性：

  + 实参是左值时，推断为左值引用
  + 实参是右值时，推断为普通类型

  ```c++
  template<typename T>
  void f(T &&x) {
      h(forward<T>(x));
  }
  
  int a = 1;
  f(a);  // void f<int &>(int &x)
  f(1);  // void f<int>(int &&x)
  ```

  再配合上 `std::forward()` 函数，保留 x 的左 / 右值属性，做到完美转发。（forward 保留左 / 右值属性，而 move 强转成右值），事实上保留左右值属性的说法并不准确（因为右值引用仍然是左值），应该是将左值引用变为左值，将右值引用变为右值。

  如果没有 forward，直接写 `h(x)`，那么 x 会被认为是左值，即使有可能是右值引用（事实上右值引用也是左值），所以 forward 的作用在于将右值引用变成右值（左值被作用于一个表达式变成了右值）

## Namespace

+ 匿名 namespace 中定义的变量和函数只能在同一翻译单元（编译单元）中被访问到，也就是同一个 `.cpp` 源文件。这也就是 static 功能之一——内部连接，相较于外部连接，内部连接对链接器不可见。

+ 当某个变量名在匿名 namespace 和全局中都有定义时（例如 `a`），那么直接使用 `a` 是有二义性的，而使用 `::a` 可以访问到全局中定义的那个变量。事实上，`a` 和 `::a` 都可以访问到匿名 namespace 或全局定义的变量（如果只在一处定义的话）：

  ```c++
  int a = 1;
  
  namespace {
  int a = 2;
  }
  
  cout << a << endl;     // ambiguous
  cout << ::a << endl;   // 1
  ```

  尚未找到有什么方法可以在两处都定义的情况下访问到匿名 namespace 中的那一个。

## 从源代码到可执行文件

+ 从源代码到可执行文件大概可分为四步：
  + 预处理，产物为 `.i` 文件（翻译单元）
  + 编译，产物为 `.s` 汇编文件
  + 汇编，产物为 `.o` 目标文件
  + 链接，产物为可执行文件
+ 预处理（`gcc -E`）：`#include` 替换，`#define` 宏展开，`#ifdef` 条件编译
+ 编译（`gcc -S`）：转换成汇编代码
+ 汇编（`gcc -c`）：转换成二进制机器码
+ 链接（`gcc`）：函数库一般分为两种：
  + 静态（`.a`）：运行时不需要链接操作，节省时间
  + 动态（`.so`）：运行时链接，可执行文件本身不包含库文件的代码，节省空间

## using

C++ 中 using 有三种用法：

+ using 声明：将别的命名空间或父类的变量名注入到当前作用域。

  + 注入别的命名空间的变量：

    ```c++
    {
        using std::map;
        map<int, int> a;
    }
    ```

  + 类中还可以用来改变父类成员的可见性：

    ```c++
    class A {
    protected:
    	int a;    
    };
    
    class B : private A {
    public:
        using A::a;
    };
    ```

+ using 指令：引入命名空间，使某个 namespace 中所有变量在当前作用域中可见。

  ```c++
  using namespace std;
  ```

+ 类型别名（type alias）：类似 typedef，但比 typedef 强的地方在于可以定义模板类型的别名：

  ```c++
  template<typename Val>
  using int_map_t = std::map<int, Val>;
  
  int_map_t<int> imap;
  ```

  事实上，typedef 只要用 struct 包一层也可以定义模板类型的别名：

  ```c++
  template<typename Val>
  struct int_map {
      typedef std::map<int, Val> type;
  };
  
  int_map<int>::type imap;
  ```

*[reference](<https://blog.wandoer.com/coding/using-%E5%85%B3%E9%94%AE%E5%AD%97%E5%9C%A8-c-%E4%B8%AD%E7%9A%84%E5%87%A0%E7%A7%8D%E7%94%A8%E6%B3%95.htm>)*

## decltype

+ decltype 关键字用来根据表达式推导出其类型：

  ```c++
  int i = 4;
  decltype(i) a;
  ```

+ 有几个主要的用途：

  + 与 using / typedef 一起用，用于定义类型：

    ```c++
    vector<int> vec;
    typedef decltype(vec.begin()) vectype;
    ```

  + 重用匿名类型：

    ```c++
    struct {
        int a;
    } anon_s;
    
    decltype(anon_s) as;
    ```

  + 泛型编程中结合 auto，用于追踪函数的返回值类型：

    ```c++
    template <typename T>
    auto multiply(T x, T y)->decltype(x*y) {
    	return x*y;
    }
    ```

+ decltype 的判别规则（优先级从上到下）：

  + 如果表达式是没有带括号的变量，则推断结果就是该变量的类型
  + 如果表达式是将亡值，则推断结果为该值类型的右值引用
  + 如果表达式是左值，则推断结果是该值类型的左值引用
  + 如果以上都不是，则推断结果就是表达式的类型

  所谓没有带括号的变量，是指 decltype 求的是变量本身的类型，因为带了括号之后 decltype 求的就是变量外面有个括号所形成的表达式的类型了，两者是不一样的，前者是变量本身（第一条规则），后者是左值表达式（第三条规则）：

  ```c++
  int a;
  decltype(a) b;    // int
  decltype((a)) c;  // int &
  ```

  此处 `(a)` 为左值表达式是因为可以对其取地址，而不是说其可修改（常量左值）。

+ *[reference](<https://github.com/Light-City/CPlusPlusThings/tree/master/basic_content/decltype>)*

## Lambda 表达式

+ 匿名函数，完整的声明格式如下：

  ```
  [capture list] (params list) mutable exception-> return type { function body }
  ```

  其中如果指定了 mutable 关键字，则该匿名函数的值捕获变量是可修改的，exception 尚不知道啥意思

+ capture list 中可以捕获外部变量，有两种捕获方式：值捕获和引用捕获

  + `[x]` 值捕获
  + `[&x]` 引用捕获
  + `[=]` 由编译器推断需要捕获哪些变量，值捕获
  + `[&]` 由编译器推断需要捕获哪些变量，引用捕获

+ 可以这样定义一个 lambda 表达式：

  ```c++
  auto f = []() {};
  ```

## Multi-threading

### std::thread

+ `std::thread` 作为一个类，其构造函数的作用是创建一个新线程并执行，构造函数有一个可变长的参数列表，第一个参数为一个 callable，后面的参数是该 callable 的入参。

+ 有三种 callable：

  + 函数指针：

    ```c++
    void f() {}
    std::thread t(f);
    ```

  + Functor：

    ```c++
    class A {
        void operator() {}
    };
    std::thread t(A());
    ```

  + lambda 表达式：

    ```c++
    auto f = []() {};
    std::thread t(f);
    ```

+ `std::thread` 类有一个 `join` 方法，阻塞当前线程直到目标线程执行完成（别忘了调用 `join` 方法以回收线程）。

+ *[reference](<https://www.geeksforgeeks.org/multithreading-in-cpp/>)*

### std::promise & std::future

+ `std::promise` 和 `std::future` 是两个类模板，它们提供了一种线程之间**同步数据**的方法，即线程 a 等待线程 b set 了某个值以后才继续执行。

+ 一种常见的使用方法：

  ```c++
  #include <iostream>
  #include <future>
  
  void f(std::promise<int> &&p) {
      p.set_value(5);
  }
  
  int main() {
      std::promise<int> p;
      std::future<int> fu = p.get_future();
      
      std::thread t(f, std::move(p));
      std::cout << fu.get() << std::endl;
      
      t.join();
      return 0;
  }
  ```

  `promise` 和 `future` 成对使用，通过 `promise` 的 `get_future()` 方法可以获得其对应的 `future`。当主线程调用 `fu.get()` 时会阻塞住，直到线程 t 中 `p.set_value()` 被执行。也就是说将要阻塞的线程要持有一个 `future` 对象（**将来**某个时刻该处的值会被设置），而负责设置该对象值的线程要持有一个 `promise` 对象（**承诺**会设置该处的值）。

+ 需要注意的是 `promise` 的拷贝构造是被禁用掉的，所以创建线程 t 时不能写成 `std::thread t(f, p);`，因为 `thread` 的构造函数经过一连串的函数调用后会在 `tuple` 文件的某处完美转发到一个构造 `promise` 的地方。所以如果创建线程时直接传入 `p` 的话会调用到 `promise` 的拷贝构造而报错。正确的做法是传入 `move(p)`，这样最终会调到 `promise` 的移动构造。（事实上 `future` 的拷贝构造也是被禁掉的）

  当然也可以传入指向 `p` 的指针。

+ 当需要有多个 `future` 对应一个 `promise` 时，可以使用 `std::shared_future`，`shared_future` 允许拷贝构造，也允许多次 `get`。

+ *references*:

  + [promise / future](https://thispointer.com/c11-multithreading-part-8-stdfuture-stdpromise-and-returning-values-from-thread/)
  + [shared future](<https://zh.cppreference.com/w/cpp/thread/shared_future>)

### std::async

+ `std::async` 是一个函数模板，提供了一种**异步执行**某个函数的方法，第一个参数为 callable，之后的可变长参数列表为该 callable 的入参。（并非所有情况都会异步，和 launch policy 有关）

  `std::async` 还有一个重载版本，第一个参数为 launch policy，第二个参数为 callable，之后为 callable 的入参。

+ `std::async` 的返回值类型是 `std::future`，其模板参数类型为 `async` 入参 callable 的返回值类型。

+ 一种常见的使用方法：

  ```c++
  #include <iostream>
  #include <future>
  
  int f() {
      return 2;
  }
  
  int main() {
      std::future<int> fu = std::async(f);
      std::cout << fu.get() << std::endl;
      return 0;
  }
  ```

+ launch policy 有两种：

  + `async`：保证**异步**行为，即创建一个新线程来执行 callable。
  + `deferred`：在调用 `async` 返回的 `future` 的 `get()` 方法时，**同步**地执行 callable。

  如果不指定 policy，那么采用 `async` 还是 `deferred` 将取决于系统的负载，因为 `async` 异步、快，但是需要创建新线程，而 `deferred` 同步、慢，但是不需要创建新线程。

+ *references*:

  + <https://blog.csdn.net/lijinqi1987/article/details/78909479>
  + <https://stackoverflow.com/questions/12620186/futures-vs-promises>

## template

+ 模板的定义要和声明放在一起（不能分别放在头文件和源文件中），因为编译器在处理模板变量时需要知道模板是怎么定义的。

## exception



##### Last-modified date: 2020.5.12, 7 p.m.