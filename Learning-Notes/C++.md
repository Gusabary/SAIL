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
  + **预处理**（`gcc -E`），产物为 `.i` 文件（翻译单元）

    `#include` 替换，`#define` 宏展开，`#ifdef` 条件编译等等

  + **编译**（`gcc -S`），产物为 `.s` 汇编文件

  + **汇编**（`gcc -c`），产物为 `.o` 目标文件（可重定位目标文件）

  + **链接**（`gcc`），产物为可执行文件（可执行目标文件）

    函数库一般分为两种：

    + 静态（`.a`）：运行时不需要链接操作，节省时间（目标文件打包）
    + 动态（`.so`）：运行时链接，可执行文件本身不包含库文件的代码，节省空间（共享目标文件）

+ 汇编器的任务是将汇编文件翻译成目标文件，而不仅仅是将汇编代码翻译成机器码（事实上翻译而成的机器码大多只在 .text 节中，只占了目标文件很少的一部分）

  Linux 中目标文件采用 ELF 格式。该格式的目标文件可分为四个部分：

  + **ELF 头**（ELF Header，`readelf -h`）：其中包含段头部表和节头部表的偏移、大小、条目数等等。
  + **段头部表**（Program Header Table，`readelf -l`）：表中每个条目对应一个段，包含段的偏移、地址、大小等等。
  + **节**（Sections，`readelf -x <name>`）：可以理解成目标文件的 payload，每个节都是一串字节序列。
  + **节头部表**（Section Header Table，`readelf -S`）：表中每个条目对应一个节，包含节的地址、偏移、大小等等。

  段是节的集合，其意义在于将连续的节映射到连续的内存段，以便于加载。例如只读内存段（代码段）包括 ELF Header，Program Header Table，.text 节，.rodata 节等等，而读写内存段（数据段）包括 .data 节，.bss 节。

  段的设计是为了加载方便，所以常常出现在可执行目标文件中，在可重定位目标文件中可以没有 Program Header Table。

+ 静态链接主要分为两步：

  + **符号解析**（symbol resolution）：将对每个符号的**引用**和其**定义**关联起来。

    符号表（`readelf -s`）在目标文件中有对应的节，其中保存了该目标模块对符号引用和定义的信息。

  + **重定位**（relocation）：首先重定位输入目标文件的节，使每个符号定义有确定的运行时地址；然后重定位符号引用，使得它们指向正确的符号定义的位置。

    重定位条目（`readelf -r`）在目标文件中也有对应的节，每个条目即对应一处本目标模块对一个外部符号的引用，其中保存了重定位时需要用到的信息。

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

+ 模板在**编译时刻**展开，每有一个不同的模板实参列表，就会有一个对应的模板实例。

  所以模板的定义要和声明放在一起（不能分别放在头文件和源文件中），因为编译器在实例化模板时需要知道模板是怎么定义的。

  如果函数模板中有 static 变量，那么实例化出来的每个模板函数都有自己独立的 static 变量。*[reference](<https://www.geeksforgeeks.org/templates-and-static-variables-in-c/>)*

+ 一个小例子：

  `a.h`：

  ```c++
  template <class T>
  class A {
  public:
      void f();
      void g();
  };
  
  template <class T>
  void A<T>::f() {}
  ```

  `main.cpp`：

  ```c++
  #include "a.h"
  
  int main() 
  {
      A<int> a;
      a.f();
      a.g();
      return 0;
  }
  ```

  `a.cpp`：

  ```c++
  #include "a.h"
  
  template <class T>
  void A<T>::g() {}
  ```

  然后用 `g++ main.cpp a.cpp` 编译时会报 `undefined reference to A<int>::g()` 的错误。因为正如之前所说，模板在编译时刻展开，所以到了链接的时候，`main.cpp` 的编译单元里面调用的是 `A<int>::f()` 和 `A<int>::g()`。而前者因为 `A<T>::f()` 定义在本单元中，所以会实例化一个 `A<int>::f()`，但是对后者来说 `a.cpp` 的编译单元中只有一个 `A<T>::g()`，并没有实例化 `A<int>::g()`（在编译 `a.cpp` 时没有调用 `A<int>::g()` 所以自然不会实例化它，即编译时刻对别的编译单元内的函数调用是不可见的），所以链接的时候会报上述错误。

  如果 `a.cpp` 中写成这样就没问题（手动实例化）：

  ```c++
  #include "a.h"
  
  template <>
  void A<int>::g() {}
  ```

+ 在类模板中定义函数模板：

  ```c++
  template <class T>
  class A {
  public:
      template <class K>
      void f();
  };
  
  template <class T> 
  template <class K>
  void A<T>::f() {}
  ```

+ *[reference](<https://www.geeksforgeeks.org/templates-cpp/>)*

### template specialization

+ template specialization 是说对于一个模板，我们可以为某个特定的类型指定不同的版本。

  函数模板：

  ```c++
  template <class T>
  void f() {}
  
  template <>
  void f<int>() {}
  ```

  类模板：

  ```c++
  template <class T>
  class A {
  };
  
  template <>
  class A<int> {
  };
  ```

  编译器会选择最 specialized 的一个版本进行展开。

  *[reference](<https://www.geeksforgeeks.org/template-specialization-c/>)*

+ 如果一个函数模板定义在一个类模板中，那么似乎只有当类模板被 specialized 以后里面的函数模板才能 specialized，即这样的写法似乎不行：

  ```c++
  template <class T> 
  template <>
  void A<T>::f<int>() {}
  ```

  *[reference](<https://stackoverflow.com/questions/4994775/c-specialization-of-template-function-inside-template-class>)*

### non-type arguments

+ 模板参数也可以不是类型，但是一定要是常量：

  ```c++
  #include <iostream> 
     
  template <int x> 
  void f() {
      std::cout << x << std::endl;
  }
  
  int main() {
      f<3>();
      return 0;
  } 
  ```

+ non-type arguments 和 enum 一起使用可以用来实现 template metaprogramming，关键在于编译器每看到一种新的模板参数列表，就会实例化一个新的模板实例（这些都是在编译时刻完成的）。

  *[reference](<https://www.geeksforgeeks.org/template-metaprogramming-in-c/>)*

## exception

+ 和 C 相比，C++ 中引入了 exception handling，好处大概有如下几点：

  + 将正常代码和异常处理代码分离，C 中经常用 if 语句去处理异常，这导致代码可读性比较差。
  + 异常处理可以跨函数调用，不用像 C 那样在中间每一层函数调用时加 if 判断。
  + 可以抛出 basic type，也可以抛出对象，这样就允许将多种异常组织成一个更容易理解的结构。

+ try 语句块中出现 throw，抛出异常后由 catch 语句块接住。

+ throw 后面跟着一个**表达式**，交给哪一个 catch 语句块来处理由该表达式的**类型**决定（catch 后面的括号中指明了该 catch 语句块处理何种类型的异常，特殊地，`...` 表示捕获所有类型的异常）

+ 在 catch 语句块中可以写 `throw;` 来重新抛出异常，常用于该 catch 语句块只处理该异常的一部分的场景。

+ 在抛出异常执行流转移到 catch 语句块前，try 语句块中的对象会被析构。

+ 对于一个函数来说，可以将在函数内抛出而该函数没有处理的异常以 exception specification 的形式写在函数签名后面：

  ```c++
  void fun(int *ptr, int x) throw (int *, int)
  { 
      if (ptr == NULL) 
          throw ptr; 
      if (x == 0) 
          throw x; 
      /* Some functionality */
  } 
  ```

+ C++ 也定义了一些继承自 `std::exception` 的标准异常。

+ *references:*

  + <https://www.tutorialspoint.com/cplusplus/cpp_exceptions_handling.htm>
  + <https://www.geeksforgeeks.org/exception-handling-c/>

## friend

+ friend 即友元，提供了一种使类的 protected 或 private 成员在类外也能被访问的方法，有友元类和友元函数两种使用方式。

+ 友元类：使其他类能访问本类中的 protected 和 private 成员。

  ```c++
  class A {
  private:
      int a;
      friend class B;
  }; 
    
  class B { 
  public:
      void showA(A& x) { 
          std::cout << x.a << std::endl; 
      } 
  }; 
  ```

+ 友元函数：使其他函数（可以是别的类的成员函数，也可以是全局函数）能访问本类中的 protected 和 private 成员。

  ```c++
  class B; 
    
  class A { 
  public: 
      void showB(B&); 
  }; 
    
  class B { 
  private: 
      int b; 
      friend void A::showB(B& x);
  }; 
    
  void A::showB(B& x) { 
      std::cout << x.b << std::endl; 
  }  
  ```

+ friend 关系不是相互的，A 类能访问 B 类私有成员，B 不一定能访问 A 的。

+ friend 关系不被继承，这一点需要详细说明一下（假设父类 A 有友元 C，子类 B 继承自 A）：

  + A 中定义的 protected 或 private 成员作为子对象的一部分存在 B 对象中，所以对 C 还是可见的。
  + B 中新定义的 protected 或 private 成员（即 A 中没有的成员）对 C 是不可见的，此即为友元关系不被继承。

  ```c++
  class A {
  private:
      int va;
      friend void f();
  };
  
  class B : public A {
  protected:
      int vb;
  };
  
  void f() {
      B b;
      std::cout << b.va << std::endl;  // ok
      std::cout << b.vb << std::endl;  // error
  }
  ```

+ *[reference](<https://www.geeksforgeeks.org/friend-class-function-cpp/>)*

## Type casting

C++ 提供了四种用于类型转换的关键字：`dynamic_cast`, `static_cast`, `reinterpret_cast` 和 `const_cast`。

*[reference](<http://www.cplusplus.com/doc/oldtutorial/typecasting/>)*

### dynamic_cast

+ `dynamic_cast` 只能用于转换指向对象的指针或者对象的引用（对象所属的类需要有继承关系）。

+ 在有继承关系的多个类之间，向上转型是合法的，向下转型当且仅当有**正确的多态**时是合法的，侧向转型可以理解成先向下转型再向上转型（双重继承）

  这里的合法是指 `dynamic_cast` 的返回值是转型后的对象的指针或引用，而不是 null 或者抛出 `bad_cast` 异常。

+ 考虑这样一种情景：

  ```c++
  class A {};
  class B : public A {};
  class C : public A {};
  class D : public B, public C {};
  
  // 向上转型，合法
  B *b = new B;
  A *a = dynamic_cast<A *>(b);
  
  // 向下转型，当有正确的多态时合法
  A *a = new B;  // 正确的多态
  B *b = dynamic_cast<B *>(a);
  
  // 侧向转型，合法与否的关键在于向下转型是否合法
  B *b = new D;  // 正确的多态
  C *c = dynamic_cast<C *>(b);
  // 这一步理解上可以拆成先向下转型，在向上转型:
  // D *d = dynamic_cast<D *>(b);  // 多态正确的话是可以转的
  // C *c = dynamic_cast<C *>(d);  // 无论如何都是可以转的
  ```

  也可以这么理解，转换后的对象不能比转换前的对象拥有更多信息，所以从子类转换到父类是安全的。

  在向下转型时有可能需要在 A 类中加一个虚析构函数使 A 具有多态性，否则会报 `source type is not polymorphic` 这样的错误。*[reference](<https://stackoverflow.com/questions/15114093/getting-source-type-is-not-polymorphic-when-trying-to-use-dynamic-cast>)*

+ 向下转型需要检查多态正确性这件事情，是需要 RTTI 来完成的，如果在编译时禁掉了 RTTI （`-fno-rtti`）就不允许向下转型了（即使多态正确也不行，因为没有信息来检查多态是否正确）

### static_cast

+ `static_cast` 可以用于转换指向对象的指针或者对象的引用（对象所属的类需要有继承关系）。

+ 在有继承关系的多个类之间，向上转型和向下转型都是合法的，因为 `static_cast` 不会像 `dynamic_cast` 那样做安全检查，而是将保证正确性这一责任交给程序员。

  ```c++
  class A {};
  class B : public A {};
  
  // 向下转型，即使没有正确的多态也是合法的
  A *a = new A;  // 不正确的多态
  B *b = static_cast<B *>(a);
  ```

+ `static_cast` 也可以用来进行 C 风格**隐式转换**下合法的转换：

  ```c++
  double d = 3.1415926;
  
  // 合法，因为 int i = d; 是合法的
  int i = static_cast<int>(d);
  
  // 不合法，因为 int a = &d; 是不合法的（尽管 int a = (int)&d; 是合法的）
  int a = static_cast<int>(&d);
  ```

### reinterpret_cast

+ `reinterpret_cast` 可以用于转换任意两个指针或引用（不必有继承关系），甚至被转换或转换到的变量可以是 int 这样的类型而不必是指针。

+ 它只是对某个地址处的数据用不同的类型解释了一遍：

  ```c++
  class A {};
  class B {};  // 不必有继承关系
  A * a = new A;
  B * b = reinterpret_cast<B*>(a);
  ```

### const_cast

+ `const_cast` 可以将常量转成非常量，将非常量转成常量。

+ 例如将某个常量作为入参传递给一个接受非常量的函数：

  ```c++
  #include <iostream>
  using namespace std;
  
  void print (char * str) {
    	cout << str << endl;
  }
  
  int main () {
      const char * c = "sample text";
      print (const_cast<char *> (c));
      return 0;
  }
  ```

### typeid

+ `typeid` 可以用来获取一个表达式的类型，其返回值为 `type_info` 类型的对象。

  `type_info` 类重载了 `==` 和 `!=` 运算符，可以用来比较两个类型是否一样。该类还提供了 `name()` 方法用于获取字符串形式的类型名称。

+ 但是 `name()` 方法的返回值是 compiler dependent 的，比如我用的 `g++` 编译下面这段程序，`class CBase *` 就会输出成 `P5CBase`，相应地，`class CBase` 是 `5CBase`，`class CDerived` 是 `8CDerived`（类名重整）。

  ```c++
  #include <iostream>
  #include <typeinfo>
  #include <exception>
  using namespace std;
  
  class CBase { virtual void f(){} };
  class CDerived : public CBase {};
  
  int main () {
      try {
          CBase* a = new CBase;
          CBase* b = new CDerived;
          cout << "a is: " << typeid(a).name() << '\n';  		// class CBase *
          cout << "b is: " << typeid(b).name() << '\n';		// class CBase *
          cout << "*a is: " << typeid(*a).name() << '\n';		// class CBase
          cout << "*b is: " << typeid(*b).name() << '\n';		// class CDerived
      } catch (exception& e) { cout << "Exception: " << e.what() << endl; }
      return 0;
  }
  ```

  需要注意的一点是，当 `typeid` 的参数为指针时，返回值为指针的声明时类型；而当 `typeid` 的参数为对象时，返回值为对象的运行时类型（即需要考虑多态性）

### *_pointer_cast

+ `*_pointer_cast` 效果类似 `*_cast`，只是前者作用于 `shared_ptr` 而非裸指针。

+ 使用方法类似：

  ```c++
  std::shared_ptr<A> foo = dynamic_pointer_cast<A>(sp);
  A *foo = dynamic_cast<A *>(sp.get());
  ```

+ `dynamic_pointer_cast`, `static_pointer_cast` 以及 `const_pointer_cast` 在 C++11 就引入了，而 `reinterpret_pointer_cast` 到 C++17 才引入。

## STL Iterators

+ `stack`，`queue` 以及 `priority_queue` 没有迭代器

+ 数组类容器：`vector` 和 `deque` 拥有 random-access 迭代器。当 erase 一个元素时，两者的行为略有不同：前者会将被 erase 的元素之后所有元素往前移一位，所以迭代器会指向后一个元素；而后者可能将被 erase 的元素之后所有元素往前移一位，也可能将被 erase 的元素之前所有元素往后移一位（取决于其在 deque 中的位置）

  ```c++
  deque<int> v{1, 2, 3, 4, 5};
  
  auto it = next(v.begin());
  v.erase(it);
  cout << *it << endl;	// 1
  
  it = prev(v.end(), 2);
  v.erase(it);
  cout << *it << endl;	// 5
  ```

+ 链表类容器：`list`，`map` 以及 `set` 拥有 bidirectional 迭代器。当 erase 一个元素时，迭代器所指元素不会改变，并且迭代器仍能通过自增或自减回到链表上，只是该元素无法再从链表中访问到了。

+ 但是总之，erase 一个迭代器所指元素后，最好不要再对该迭代器进行操作了。

+ *[reference](https://www.softwaretestinghelp.com/iterators-in-stl/)*

##### Last-modified date: 2020.9.19, 12 p.m.

