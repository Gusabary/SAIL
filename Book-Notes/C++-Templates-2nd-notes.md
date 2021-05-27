# C++ Templates 2nd Notes

## Chapter 1  Function Templates

+ 在自己定义了一个 `max` 函数模板后，调用时可以加上 `::` 表示在 global namespace 中找这个函数，以避免和 `std` namespace 中的 `max` 产生冲突，尤其是当参数为 `std` namespace 下的类型时：

  ```c++
  std::string s1 = "mathematics";
  std::string s2 = "math";
  std::cout << "max(s1, s2): " << ::max(s1, s2) << '\n';
  ```

+ 模板在编译时分两个阶段（two-phase translation）：定义时刻和实例化时刻。前者检查一些和模板参数无关的条件，而后者则检查一些和模板参数相关的条件。

+ 即使类型为模板参数（template parameter）的函数参数（call parameter）有默认值，模板参数的类型也不会自动推导出来，但是可以指定模板参数的默认类型：

  ```c++
  template<typename T = std::string>
  void f(T = "");
  
  f();
  ```

+ 当有多个模板参数而返回值类型是其中之一时，有以下几种方法来处理：

  + 将返回值类型也作为模板参数之一，然后在调用该函数时显式指定；
  + 使用 auto 作为返回值类型；
  + 使用 `common_type` type trait 作为返回值类型。

+ 当重载版本中同时出现函数模板和普通函数时，按照如下规则进行选择：

  + 如果显式指定了模板参数（或仅仅是指定了空参列表 `<>`），选择函数模板；
  + 如果普通函数（非模板）能完美匹配参数，选择普通函数；
  + 如果不需要转型，函数模板也能实例化一个完美匹配参数的模板函数，选择函数模板；
  + 需要转型的话，选择普通函数。

+ 在重载函数模板时，不同版本之间的区别应该尽可能小，比如只是参数个数不同，或者只是显式指定参数类型（而非声明为模板参数的类型）。

  此外，当别的函数需要用到重载函数时，应尽可能地将其声明在所有重载版本之后。

## Chapter 2  Class Templates

+ 对于类模板实例化一个模板类的情况，并非会实例化类中的所有方法，只会实例化被用到的方法。所以即使某个模板参数不满足类中某个方法的要求（例如没有重载指定的运算符），只要用该模板参数实例化的模板类对象没有调用这个方法，那就没有问题。

+ 为了更方便地标识对模板参数的要求，可以使用 concept，但是 concept 作为 language feature 直到 C++20 才引入，在此之前可以使用 static_assert 达到类似的效果。

+ 类模板可以被特化（specialization）：

  ```c++
  template<>
  class Stack<std::string> {};
  ```

  当特化一个类模板时，同时需要特化其所有方法。此外，也可以只特化某个方法，但这种情况下就不能特化整个类模板。

+ 类模板也可以被偏特化（partial specialization）：

  ```c++
  template<typename T>
  class Stack<T*> {};
  
  template<typename T>
  class MyClass<T,T> {};
  ```

  所谓偏特化是指，部分特化一个模板，程序员仍能指定一些模板参数。

+ 模板只能在 global / namespace 作用域或类声明内部声明和定义。

## Chapter 3  Nontype Template Parameters

+ 类模板中不同的 nontype 模板参数会导致实例化不同的模板类，它们是不同的类型，彼此之间默认情况下不能转换。

+ 在函数模板的模板参数列表中，后面的参数可以使用前面参数的信息：

  ```c++
  template<auto Val, typename T = decltype(Val)>
  T foo();
  ```

+ 浮点类型和对象不能作为 nontype 模板参数。

+ C++17 之后可以使用 auto（甚至也可以使用 `decltype(auto)`）来推断 nontype 模板参数的类型。

## Chapter 4  Variadic Templates

+ 一个使用 variadic templates 的例子（经典做法，递归处理）：

  ```c++
  #include <iostream>
  
  void print () {}
  
  template<typename T, typename... Types>
  void print (T firstArg, Types... args)
  {
      std::cout << firstArg << std::endl;  // print first argument
      print(args...);  // call print() for remaining arguments
  }
  ```

  其中 `Types` 称为 template parameter packs，`args` 称为 function parameter packs。

+ C++11 引入 `sizeof...` 来获取 parameter packs 中元素的个数。这或许会使你联想到用 `sizeof...` 来判断 parameter packs 中元素个数是否大于 0，只有在大于 0 的情况下才调用 `print`，这样就可以不用定义空参的 `print` 函数了：

  ```c++
  template<typename T, typename... Types>
  void print (T firstArg, Types... args)
  {
      std::cout << firstArg << std::endl;
      if (sizeof...(args) > 0) {
      	print(args...);
      }
  }
  ```

  但这样其实是不行的，因为在实例化模板时，if 语句的两个分支都会被实例化，这是编译时刻的行为（相对地，if 语句选择分支是运行时刻的行为），所以仍然会实例化空参 `print`，而如果不定义它的话就会报错。

+ C++17 引入 fold expressions 用于将某个二元运算符依次作用于 parameter pack 中的每个元素和上一次的计算结果：

  ```c++
  template<typename... T>
  auto foldSum (T... s) {
  	return (... + s);  // ((s1 + s2) + s3) ...
  }
  ```

  也可以加上一个初始值：

  ```c++
  template<typename... Types>
  void print (Types const&... args) {
  	(std::cout << ... << args) << std::endl;
  }
  ```

+ 作为 template parameter packs，也有 *Effective Modern C++* 类型推断一章中所说的三种形式：引用传递、universal reference 以及值传递：

  ```c++
  template<typename... Args> void f1 (Args const&... args);
  template<typename... Args> void f2 (Args&&... args);
  template<typename... Args> void f3 (Args... args);
  ```

+ fold expressions 是将 parameter pack 中的每个元素和上一次的计算结果做处理，最终结果是一个值；而 varadic expressions 则是将 parameter pack 中的每个元素单独做处理再将其合并，最终结果仍是一个 pack：

  ```c++
  template<typename... T>
  void addOne (T const&... args) {
  	print ((args + 1)...);	// 将 args 中每个元素 +1
  }
  
  template<typename C, typename... Idx>
  void printElems (C const& coll, Idx... idx) {
  	print (coll[idx]...);	// 将 idx 中每个元素作为 index 作用于 coll，最后将结果打包成 pack
  }
  ```

  甚至对每个元素单独处理时也可以使用 pack：

  ```c++
  template<typename... T>
  void printDoubled (T const&... args) {
  	print (args + args...);  // 将 args 中每个元素翻倍
  }
  ```

## Chapter 5  Tricky Basics

+ 当一个依赖于模板参数的名字是类型名时，需要用 `typename` 显式指定：

  ```c++
  template<class T>
  class MyClass {
  	void foo() {
  		typename T::SubType* ptr;
  	}
  };
  ```

  如果此处没有 `typename`，`*` 会被解析成乘号。

+ 对于一些内置类型的 local 变量，是不会默认初始化的，所以对于模板参数类型的变量，最好能显式地初始化一下：

  ```c++
  template<typename T>
  void foo()
  {
  	T x{}; // x is zero (or false) if T is a built-in type
  }
  ```

+ 在类模板中，由于模板参数不同而实例化出的不同模板类属于不同的类型，彼此之间不能直接访问私有成员，除非使用 friend：

  ```c++
  template<typename T>
  class Stack {
      template<typename> friend class Stack;
  };
  ```

+ 类中的成员函数模板偏特化时需要定义在头文件中（类外作用域），要显式地声明成 inline，否则多个编译单元引用这个头文件时会出现重复定义的问题：

  ```c++
  class A {
  public:
      template<typename T>
      T f() {}
  };
  
  template<>
  inline bool A::f<bool>()  {}
  ```

+ 有的时候左尖括号可能被解析成模板参数列表的开始，也可能被解析成小于号，此时可以用 `template` 关键字显式指定为前者（这种情况一般是左尖括号跟在一个依赖于模板参数的名字之后，就像显式指定 `typename` 一样）：

  ```c++
  template<unsigned long N>
  void printBitset (std::bitset<N> const& bs) {
  	std::cout << bs.template to_string<char, std::char_traits<char>,
  		std::allocator<char>>();
  }
  ```

+ C++14 引入变量模板（variable template）：

  ```c++
  template<typename T>
  constexpr T pi{3.1415926535897932385};
  ```

  变量模板有一个用处是简化代码（类似 using 和 typedef 在使用模板时的区别）：

  ```c++
  template<typename T>
  constexpr bool isSigned = std::numeric_limits<T>::is_signed;
  
  // isSigned<char>
  // vs.
  // std::numeric_limits<char>::is_signed
  ```

+ 模板参数本身可以是一个类模板（不可以是函数模板或变量模板），这种用法称为 template template parameter：

  ```c++
  template<typename T, template<typename> class Cont = std::deque>
  class Stack {};
  ```

  类模板作为模板参数之一需要用 `class` 关键字标识，C++17 以后也可以用 `typename`。

  注意，当类模板作为模板参数之一时，实参中的类模板和形参中的类模板的模板参数列表需要完全匹配：

  ```c++
  // 形参中类模板的模板参数列表是 <typename, typename>
  template<typename T, template<typename, typename> class>
  class Stack {};
  
  // 实参中类模板的模板参数列表也是 <typename, typename>
  template<typename T, typename>
  using V = vector<T>;
  
  Stack<int, V> s;
  ```

## Chapter 6  Move Semantics and enable_if<>

+ 如同 *Effective Modern C++* Item 27 中讲的那样，当重载函数中有使用 universal reference 作为参数的版本时，其往往会捕获到比我们预想中更多的调用。解决方法之一是使用 `enable_if<>`。
+ `enable_if<>` 的第一个参数是一个编译期表达式，第二个参数是一个可选的类型。如果表达式为真，那 `enable_if<>` 的结果就是其第二个参数（如果没有第二个参数就是 void）；如果表达式为假，那结果为未定义（所谓 `enable_if<>` 的结果是指其中的 `type` 类型别名），这将导致使用 `enable_if<>` 的模板被忽略。
+ 但毕竟使用 `enable_if<>` 来选择性地忽略模板只是一种 workaround，语义更加清楚的做法是使用 C++20 引入的 concepts。

## Chapter 7  By Value or by Reference?

+ 对于 pass by value，并非每次都是真的 copy，可以分为以下几种情况：

  + 实参为 lvalue（左值），调用拷贝构造；
  + 实参为 prvalue（纯右值），先尝试不调任何构造，如果不行的话调用移动构造；
  + 实参为 xvalue（将亡值），调用移动构造。

  还需要注意的一点是，pass by value 会使参数 decay：去掉 cvr 的限定符，数组和函数都变成指针。

+ pass by reference 不会使参数 decay。

+ 当形参为 `T&` 时，看似不能接受右值作为实参，但如果是个 `const` 右值的话（例如 `const int`），`T&` 就会被推断为 `const int&`，就可以接受右值了（即先做类型推断，再做 value category 的检查）

+ 只有在完美转发的情形中，`T` 才可能被推断为 reference（当实参为左值时）

+ 使用 `std::ref()` 或 `std::cref()` 可以在 caller side 决定是 pass by value 还是 pass by reference。实际上 `std::ref()` 以及 `std::cref()` 的作用在于创建一个 `std::reference_wrapper<>` 类型的对象，指向原始对象，然后 pass by value（有点像传进去一个指针，但要更加优雅一些）。而`std::reference_wrapper<>` 类型支持到原始对象类型的隐式转换，所以可以当成原始对象使用（而不用像指针那样解引用），但是需要注意的是，当成原始对象使用仍需要经过一次隐式转换。

+ decay 有利有弊：好处在于忽略数组长度信息，将长度不同的数组判定为相同类型，便于处理；坏处也同样在于丢失了长度信息，无法区分数组和指针。

## Chapter 8  Compile-Time Programming

+ 对于要求返回值为编译时常量的场合（包括在全局或 namespace scope 中定义常量），`constexpr` 函数会在编译时调用（只要入参都是编译时常量）；否则（例如在 block scope 中定义常量），`constexpr` 函数理论上可以推迟到运行时调用（即使入参均为编译时常量）

+ 利用偏特化可以进行 execution path selection 的操作：

  ```c++
  template<int SZ, bool = isPrime(SZ)>
  struct Helper;
  
  template<int SZ>
  struct Helper<SZ, false> {};
  
  template<int SZ>
  struct Helper<SZ, true> {};
  ```

  但是函数模板并不支持偏特化，workaround 之一是声明为类中的 static 方法，然后去偏特化这个类。

+ 当编译器在决定选择函数的哪个重载版本时，会先尝试替换函数模板中的类型参数（在参数列表以及返回值中），如果替换后的类型会导致错误，那么编译器会将此重载版本忽略，而非报错，即”替换失败并非错误“。

  此机制可以用来对模板的类型参数做一些限制，常用的方法比如 `enable_if`：直接判断类型参数是否符合某条件，如果不符合将该模板禁用：

  ```c++
  template<typename T, typename = std::enable_if_t<std::is_same_v<T, int>>>
  void f(T x) {}
  ```

  `enable_if` 本质上是一个 struct 模板，该模板接受两个类型参数，第一个为编译期表达式，第二个可选（默认 void），为 `enable_if::type` 的类型。巧妙的地方在于该模板偏特化了一个第一个参数为 true 的版本：

  ```c++
  template<bool, typename _Tp = void>
  struct enable_if
  { };
  
  template<typename _Tp>
  struct enable_if<true, _Tp>
  { typedef _Tp type; };
  ```

  只有当第一个参数为 true 时，`enable_if` 才有 type 字段；也就是说当第一个参数为 false 时，如果进行类型参数替换，`enable_if_t` 会导致一个错误，而又由于 SFINAE 的原则，该模板会被忽略。

+ 除了 `enable_if`，还有一个利用 SFINAE 对模板参数进行限制的方法：`decltype`

  需要注意的一个事实是所谓的“替换”，指的只是替换参数列表和返回值中的类型参数（函数体内的代码并不适用 SFINAE，替换后的错误不会被忽略，而是会报错）。

  具体的做法是将需要检查的表达式以 trailing return type 的形式置于函数签名之后：

  ```c++
  template<typename T>
  auto len(T const& t) -> decltype((void)(t.size()), typename T::size_type())
  {
  	return t.size();
  }
  ```

  有几点需要解释：

  + `decltype` 可以接受多个参数（注意 `decltype` 的参数是表达式而非类型，所以在 `T::size_type` 后需要加上一对括号，大概是默认构造的意思），推断出的类型为最后一个表达式的类型。
  + 将 `t.size` 转型成 `void` 是为了防止 `T` 类型重载了 `,` 运算符
  + 虽然 `decltype` 的参数中只有最后一个会决定其推断出的类型（其余的表达式都是 dummy objects，甚至不会调构造），但是需要所有的表达式都合法才能进行正确的替换（即不被 SFINAE out）

+ 编译时 if：`if constexpr` 的判断条件需要是一个编译时定值，条件为否的分支会在编译期被 discard，但是其中仍然会进行一些编译时刻的检查（例如类型检查等等），除非该 `if constexpr` 是在函数模板中且语句与模板的类型参数相关（此时相关检查推迟到实例化时刻而非定义时刻）

## Chapter 9  Using Templates in Practice

+ 一般来讲会将模板的定义也放在头文件中，这种做法称为 Inclusion Model。

  如果将模板的定义单独放在源文件中，当另一个编译单元使用该模板时，由于暂时未能找到模板的定义，所以不会进行实例化；而在定义模板的编译单元中也不会进行实例化（不知道别的编译单元使用了这个模板），所以就会有链接时未定义的错误。

  但是 Inclusion Model 会带来目标文件过大的问题，这也正是 C++20 引入的 Modules 所要解决的。

+ inline 只是一个 hint，建议编译器内联某个函数，所以 inline 并不保证一定内联，但是能保证函数可以出现在多个编译单元中而不报链接时错误。

  同样地，实例化出来的签名完全相同的模板函数也可以出现在多个编译单元中而不报错。但是需要注意，如果是程序员自己完全偏特化出来的签名相同的模板函数，是不可以出现在多个编译单元中的（当然如果这个完全偏特化出来的实例被声明成 inline，那就又可以了）。

+ 目前有一种方法可以降低 include 太多头文件带来的性能开销：Precompiled Headers。即如果多个源文件以一些相同的 include 起始，那么就可以先将这些相同的 include 单独编译，然后在编译这些源文件时直接 reload 即可。也就是说相同的部分只需要编译一次。

## Chapter 10  Basic Template Terminology

+ 将模板形参替换为实参的过程称为 substitution，substitution 可以是尝试性的（SFINAE），通过 substitution 创建模板实例的过程称为 instantiation，由 instantiation 创建出来的实体称为 specialization。

  specialization 有 explicit specialization 和 generated specialization 之分。前者是程序员显式指定特化版本，而后者是由编译器进行的实例化。

  此外，仍然持有模板参数的 specialization 称为 partial specialization，当讨论 explicit specialization 以及 partial specialization 时，原模板一般称为 primary template。

## Chapter 11  Generic Libraries

+ 函数指针作为 callable 参数传递时，实参可以是函数指针，也可以直接是函数名，因为后者会 decay 成函数指针类型。

  当接受 callable 作为入参时，需要处理 callable 为类方法的情况：类方法的调用和一般 callable 的调用形式不一样（`obj.f()` vs. `f()`），这给通用库的编写带来了麻烦。C++17 引入的 `std::invoke()` 解决了这一问题：`invoke` 的第一个参数为 callable，当其为类方法时，将 `invoke` 的第二个参数作为 `this`，第三个参数及其以后的参数作为实参进行调用；当其为一般 callable 时，将第二个参数及其以后的参数作为实参调用。以此统一了写法，方便了通用库的编写。

  `std::invoke()` 还有一个用途是用来实现 wrapping functions，比如需要在调用函数前或后记录 log 等等。

+ type traits 可以对类型进行检查和修改，需要注意一些特殊情况，比如 `std::remove_const_t<const int &>` 的结果仍然是 `const int &`，因为引用并不是 const，所以在 remove const 以及 reference 的时候需要注意次序：`std::remove_const_t<std::remove_reference_t<const int &>>`， 当然也可以直接用 `std::decay_t<const int &>`。

+ `std::addressof` 可以获取到命名实体的地址，多适用于重载了 `&` 运算符的情况。

+ `std::declval` 可以在不创建对象的情况下获取对象的右值引用，但是只能用于 unevaluated operands（比如 `decltype`，`sizeof`），也就是说 unevaluated operands 需要对象作为参数但不求值，所以就可以用 `std::declval` 作为一个占位符。原理大概是 `std::declval` 只有声明而没有实现，所以当试图求 `std::declval` 的值时便会报错。

+ perfect forwarding 不仅可以 forward parametes，也可以 forward local value：

  ```c++
  template<typename T>
  void foo(T x) {
  	auto&& val = get(x);
  	set(std::forward<decltype(val)>(val));
  }
  ```

+ 在定义类模板时，有时会因为对模板的类型参数应用了某些 type traits 而使得其只能是 complete type，这减少了该类模板的应用场景（比如 struct 中有字段为指向该 struct 的指针时，该 struct 就是一个 incomplete type）。为了解决这一问题，可以将应用 type traits 的方法重写为函数模板，这样求值的过程被推迟到了类型参数成为 complete type 以后。

## Chapter 12  Fundamentals in Depth

### declarations

+ C++ currently supports four kinds of templates:

  | namespace scope   | class scope                     |
  | ----------------- | ------------------------------- |
  | class template    | nested class template           |
  | function template | member function template        |
  | variable template | **static** data member template |
  | alias template    | member alias template           |

  now templates are not allowed to be defined in function scope or local class scope.

+ member function templates cannot be declared virtual.

+ unlike ordinary class types, class templates cannot shared the name with a different kind of entity:

  ```c++
  template <typename T>
  struct S {};
  
  int S; // error
  ```

+ templates cannot have C linkage:

  ```c++
  extern "C" template <typename T>
  void f(); // error
  ```

  I think that's because there is no function overload in C (due to no name mangling)

+ Normal declarations of templates declare **primary templates**. such templates has no type attached after the template name:

  ```c++
  template <typename T>
  class A;  // primary template
  
  template <typename T>
  class A<T*>;  // for partial speicialization
  ```

  function templates must always be primary templates. I think that's because partial speicialization can be achieved by overload:

  ```c++
  template <typename T>
  void f(T);
  
  template <typename T>
  void f(T*);
  ```

###  template parameters

+ there are three basic kinds of template parameters: type parameters, nontype parameters and template template parameters, among which type parameters are the most common.

  nontype parameters can be integers, enums, pointers and so on. when they are arrays and function pointers, they will decay. What's interesting is that non-reference nontype parameters are regarded as **prvalue** while a nontype parameter of lvalue reference can be used to denote an **lvalue**:

  ```c++
  template <int& T>
  struct S {
      S() { T = T + 1; }
  };
  
  int a = 1;
  int main()
  {
      std::cout << a << std::endl;  // 1
      S<a>{};  // the template argument here must have 'linkage'
      std::cout << a << std::endl;  // 2
  }
  ```

  I have an explanation about the nontype parameter of non-reference and reference:

  + for non-reference, like an `int`, you need to guarantee the value of the `int` is known at compile time;
  + for reference, like an `int&`, you need to guarantee the address (reference mechanism leverages pointer) of the template argument is known at compile time. that's why the argument should have **linkage**, even if it's not `constexpr`.

  so cool

+ template template parameters are those who themselves are templates:

  ```c++
  template <
      template <typename> typename Container
  >
  struct S {
      Container<int> a;
  };
  
  S<std::vector> s;
  ```

  the cool thing is that it can be nested.

+ template parameter pack can be formed by all three basic kinds of template parameters above. In common case, the pack should appear as the last parameter in the list and only once. however there is some special case that pack can appear multiple times and not at the last position. for example, function templates and partial specializations for class or variable templates:

  ```c++
  template<unsigned...> struct Tensor;
  template<unsigned... Dims1, unsigned... Dims2>
  auto compose(Tensor<Dims1...>, Tensor<Dims2...>);
  ```

+ paremeter pack and parameters for partial specialization cannot have default template argument

## Appendix B  Value Categories

+ **表达式**有类型（type），也有值类别（value category）。在 C++11 之前，值类别只有 lvalue（左值）和 rvalue（右值）两种。考虑这样一个场景：

  ```c++
  int b = 3;
  int a = b;
  ```

  + 第一条语句中，`3` 是表达式，类型为 int，值类别为 rvalue；
  + 第二条语句中，`b` 是表达式，类型为 int，值类别为 lvalue。
  + 需要注意的是第一条语句中的 `b` 和第二条语句中的 `a` 它们都是 “命名实体”，而非表达式，所以没有值类别的概念。

  但是说第二条语句中的 `b` 是 lvalue 也不完全准确，事实上当 lvalue 出现在赋值号右边时，通常会有一个 lvalue-to-rvalue conversion，以及一条 load 指令。

+ C++11 之后，值类别的概念得到扩充，一共有三个基础类别（lvalue，左值；xvalue，将亡值；prvalue，纯右值）和两个复合类别（glvalue，泛左值；rvalue，右值）：

  ```
              expression
              /        \
          glvalue     rvalue
          /    \      /    \
       lvalue   xvalue   prvalue
  ```

  + lvalue（左值）：
    + 表示命名实体的表达式（例如变量名、函数名）
    + 被 `*` 运算符作用的表达式（解引用）
    + 字符串字面量（常量左值）
    + 返回值是左值引用（或函数类型的右值引用）的函数调用
  + prvalue（纯右值）：
    + 除了字符串字面量以外的字面量（对于自定义字面量，it depends）
    + 被 `&` 运算符作用的表达式（取地址）
    + 算术运算表达式
    + 返回值是非引用类型的函数调用
    + lambda 表达式
  + xvalue（将亡值）：
    + 返回值是对象类型的右值引用的函数调用
    + 转换到右值引用的类型转换表达式

+ 用 `decltype((x))` 可以获取表达式 `x` 的值类别：

  + 结果为 `type`，则 `x` 是 prvalue；
  + 结果为 `type&`，则 `x` 是 lvalue；
  + 结果为 `type&&`，则 `x` 是 xvalue。

  `decltype` 参数加上了双括号是因为对于 `x` 是一个命名实体的情况，`decltype` 将返回 `x` 的声明时类型，而非 `x` 作为表达式时的类型：

  ```c++
  int x;
  // decltype(x) -> int
  // decltype((x)) -> int& -> lvalue
  ```

+ 引用类型和值类别有两个重要的交互方式：

  + 每种引用类型都对初始化表达式的值类别有要求：
    + 非常量左值引用只能用左值表达式（lvalue）来初始化；
    + 右值引用只能用右值表达式（xvalue，prvalue）来初始化；
    + 常量左值引用可以用所有值类别的表达式来初始化。
  + 函数的返回值类型决定了函数调用表达式的值类别：
    + 返回值是左值引用（`type&`）或函数类型的右值引用的函数调用是 lvalue；
    + 返回值是对象类型的右值引用（`type&&`）的函数调用是 xvalue；
    + 返回值是非引用类型（`type`）的函数调用是 prvalue。

## Appendix C  Overload Resolution

+ 站在一个 high-level 的角度，处理一个函数调用往往包含以下几步：

  + 进行 function name lookup 以确定一个 initial overload set
  + 调整这个 overload set，比如模板参数推断和替换
  + 剔除任何参数列表不匹配的 overload（即使考虑了隐式转换和默认参数之后也不匹配），这一步的产物称为 viable function candidates
  + 进行 overload resolution，如果结果只有一个，就调用它；如果有多个结果，则 ambiguous
  + 检查这个 selected candidate，例如是不是 deleted，或者是不是 private

+ 在进行 overload resolution 的时候，如果一个 candidate 比另一个 candidate 要好，那么对于其每一个形参而言，都要比另一个 candidate 的每一个形参更能匹配实参。至于如何衡量参数的匹配程度，有一系列规则（从上到下匹配程度递减）：

  + perfect match：类型完全一致，或者仅有 cvr qualifier 的区别
  + match with minor adjustments：例如 decay，或者 `int *` 和 `const int *`
  + match with promotion：所谓 promotion 可以理解成小类型转到大类型（比如 `short` 转 `int`，`float` 转 `double`）
  + match with standard conversions only：包含 standard conversion（例如 `int` 转 `float`）以及子类转父类
  + match with user-defined conversions：例如类中定义的 conversion operator
  + match with ellipsis：形参为省略号，以匹配任意类型

  需要注意的是 overload resolution 这一步发生在模板参数推断和替换之前，考虑这样一个例子：

  ```c++
  template<typename T>
  class MyString {
  public:
    MyString(T const*);  // converting constructor
  };
  
  template<typename T>
  MyString<T> truncate(MyString<T> const&, int);
  
  MyString<char> str1, str2;
  str1 = truncate<char>("Hello World", 5);  // OK
  str2 = truncate("Hello World", 5);        // ERROR
  ```

  模板参数的推导发生在选择隐式转换匹配的 candidate 之前。

+ 当调用类（比如类 `A`）的方法时，一般第一个形参为 `A&`，如果该方法是 const，则第一个形参为 `const A&`，如果该方法有 `&&` 后缀，则第一个形参为 `A&&`。

+ 当两个 candidate 匹配程度相同时，优先选择 non-template function 而非 instance of a function template，如果两个 candidate 都是模板实例，那么选择更加 specialized 的那个。

+ implicit conversion 可以是一串 conversion 的组合，但是其中 user-defined conversion 只能出现一次，而且这一串组合中，converison 的次数越少，认为匹配程度越高。

+ 在转换指针类型的时候，优先级最低的是 `bool`，其次是 `void *`，当转型发生在继承链上时，转到越 derived 的类优先级越高（准确的说是，在继承链上待转型的类的运行时刻类型上方，最靠下的类优先级最高）

##### Last-modified date: 2020.12.8, 9 p.m.