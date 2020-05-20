# Effective Modern C++ Notes

## Introduction

+ **argument** 是形参，可以是左值也可以是右值； 

  **parameter** 是实参，一定是左值。

+ basic exception safety guarantee 是指发生异常后程序的不变量仍然得到保证以及没有资源被泄露；

  strong exception safety guarantee 是指发生异常后程序的状态要和调用这个抛出异常的函数之前一样。

## Chapter 1  Deducing Types

+ C++ 98 中类型推导只用于函数模板的类型参数，C++ 11 中新引入了两个使用类型推导的场景：`auto` 和 `decltype`。

### Item 1  Understand template type deduction

考虑这样的场景：

```c++
template<typename T>
void f(ParamType param);  // ParamType 是 T 或者 T 和一些修饰符（比如 const，&，*）的组合

f(expr);
```

类型推断大致分为三类：

+ **`ParamType` 是指针或引用（但不是 universal reference）**：如果 `expr` 是引用的话，去掉 `&`，然后将 `expr` 的类型和 `ParamType` 相比，得到 `T`（`ParamType` 包含 `T`）。
+ **`ParamType` 是 universal reference**：即 `ParamType` 是 `T&&`。如果 `expr` 是左值，`T` 被推断为左值引用，然后根据 reference collapsing 将 `ParamType` 也折叠成左值引用；如果 `expr` 是右值，`T` 被推断为正常类型（不含 `&`），`expr` 为右值引用。
+ **`ParamType` 不是指针也不是引用**：忽略 `expr` 的 `&`, `const` 以及 `volatile`，然后和 `ParamType` 相比得到 `T`。需要注意的是，对于 `expr` 是 `const int *const` 这种情况，只能忽略后面那个 `const`。

对于 `expr` 是数组的情况（例如 `int[5]`）：

+ 如果 `ParamType` 不是引用，`T` 被推断为 `int *`（typeid 为 `Pi`）。
+ 如果 `ParamType` 是引用，`T` 被推断为 `int(&)[5]`（typeid 为 `A5_i`）。

对于 `expr` 是函数的情况（例如 `int(char)`）：

+ 如果 `ParamType` 不是引用，`T` 被推断为 `int (*)(char)`（typeid 为 `PFicE`）。
+ 如果 `ParamType` 是引用，`T` 被推断为 `int (&)(char)`（typeid 为 `FicE`）。

### Item 2  Understand auto type deduction

auto type deduction 和 template type deduction 类似，只有一点不同。

+ 先说相同点，考虑这样的场景：

  ```c++
  auto x = 27;
  const auto cx = x;
  const auto& rx = x;
  ```

  定义变量时的 type specifier（该处的 `auto`, `const auto`, `const auto&`）即相当于 template type deduction 中的 `ParamType`，而 `auto` 本身则相当于 `T`。

  也就是说，`const auto& rx = x;` 所做的推断和以下 template type deduction 所做的推断是一样的：

	```c++
	template<typename T>
	void func_for_rx(const T& param);

	func_for_rx(x); 
	```

+ 相同点说完了，然后是不同点：在定义变量时如果用花括号进行初始化，`auto` 会被推断为 initializer_list；而对于模板函数来说，不允许对花括号括起来的一组值进行类型推断。

  此外，C++14 中还引入了 `auto` 作为函数返回值和 `auto` 作为 lambda 表达式入参这两种机制。在这两种情形中，`auto` 实际使用 template type deduction 的原则进行类型推断而非 auto type deduction，即返回值和 lambda 表达式的入参不能是花括号括起来的一组值。

### Item 3  Understand decltype

+ `decltype` 是一个关键字，它接受一个变量名或表达式作为参数，返回其类型。这种不做任何修改，直接返回参数类型的类型推断方式也叫 decltype type deduction。

  值得一提的是 `decltype ((x))` 这种用法（`x` 是变量名），推断出的类型为左值引用，因为参数是左值表达式而非变量名，但这不能算特例，只是反直觉而已。

+ 把函数的参数传递给 `decltype`，可以将 `decltype` 推断出的类型作为返回值写在函数参数列表之后，这种用法叫 trailing return type（写在参数列表之后是因为 `decltype` 需要用到函数的参数，而这些参数在参数列表中定义）：

  ```c++
  template <typename Container, typename Index>
  auto authAndAccess(Container& c, Index i) -> decltype(c[i])
  {
      authenticateUser();
      return c[i];
  }
  ```

  而在 C++14 中，可以省略 trailing return type，用之前提到过的 template type deduction （`auto` 作为返回值的情况）来推断返回值：

  ```c++
  template <typename Container, typename Index>
  auto authAndAccess(Container& c, Index i)
  {
      authenticateUser();
      return c[i];
  }
  ```

  但是这就有一个问题，`c[i]` 返回的一般都是左值引用 `T&`，而 template type deduction 中会忽略掉 `&`，所以函数的返回值就变成了 `T`，这与 `decltype(c[i])` 不一致。为了解决这个问题，C++14 引入了 `decltype(auto)`，即使用 decltype type deduction 来推断返回值类型：

  ```c++
  template<typename Container, typename Index>
  decltype(auto) authAndAccess(Container& c, Index i)
  {
   	authenticateUser();
   	return c[i];
  }
  ```

+ `decltype(auto)` 还可以像 `auto` 那样用于定义变量，区别是前者使用 decltype type deduction，而后者使用 auto type deduction。

### Item 4  Know how to view deduced types

作者提供了三种查看类型推断结果的方法：分别在开发时刻（编辑代码时），编译时刻以及运行时刻。

+ 开发时刻：利用 IDE 提供的功能，通常将光标悬停在变量名上就能看到其类型。

+ 编译时刻：利用未定义的模板，通过编译器的报错信息获得变量的类型：

  ```c++
  template<typename T>
  class TD;
  
  const int theAnswer = 42;
  auto x = &theAnswer;
  
  // error: aggregate 'TD<const int *> xType' has incomplete type and cannot be defined
  TD<decltype(x)> xType;  
  ```

  注意这里类型推断使用的原则是 auto type deduction 的 case 3（即 `ParamType` 不是指针也不是引用），`expr` 是 `const int *`，虽然含有 `const`，但是是在 `*` 之前，所以不能忽略（即新定义的指针本身的常量性由 `ParamType` 中是否含有 `const` 决定，而被指资源的常量性则由 `expr` 中是否含有 `const` 决定）。

+ 运行时刻：利用 `typeid` 和 `type_info` 的 `name` 方法获得变量的类型：

  ```c++
  template<typename T>
  void f(const T& param)
  {
   	cout << "T =     " << typeid(T).name() << '\n';
   	cout << "param = " << typeid(param).name() << '\n';
  } 
  
  std::vector<Widget> createVec();
  const auto vw = createVec();
  
  if (!vw.empty()) {
   	f(&vw[0]);
  }
  ```

  输出的结果是 compiler-dependent 的，在 GNU 和 Clang 中是：

  ```
  T =     PK6Widget
  param = PK6Widget
  ```

  也就是 `const Widget *`。

  这里可能有几处误解：

  + `param` （即 `&vw[0]`）类型是 `const Widget *`，按照 template type deduction 的 case 1，`T` 应该是 `Widget *` 才对？

    注意 template type deduction 的 case 1 中所说的 “将 `expr` 的类型和 `ParamType` 相比，得到 `T`”，并不是简单的模式匹配：

    ```
    expr:      const Widget *
    ParamType: const T &
    ```

    看上去 `T` 好像是 `Widget *`，但是需要考虑到涉及指针时，常量性有两重含义：指针本身和被指资源。`ParamType` 中的 `const` 是说 `param` 本身（也就是指针本身）是 const，而 `expr` 中的 `const` 根据它的位置可以得知其含义是被指资源是 const，两者含义并不相同。如果一定要用模式匹配的概念的话，更确切的比对应该是这样：

    |             | 类型             | 常量性 | 引用 |
    | ----------- | ---------------- | ------ | ---- |
    | `expr`      | `const Widget *` | ×      | -    |
    | `ParamType` | `T`              | √      | &    |

    可见 `expr` 有而 `ParamType` 没有的部分就是 `const Widget *`，所以 `T` 就是 `const Widget *`。

    说白了导致这个误解的根本原因是 `const int` 和 `const int *` 两处的 `const` 虽然位置相似但是语义不同。

  + 刚刚解释了 `T` 是 `const Widget *` 没错，但是 `param` 难道不应该是 `const Widget *const &` 吗？

    这就涉及 `type_info::name` 方法的机制问题了，该方法实现上要求返回的类型好像经过了一次 pass-by-value 一样（即应用一次 template type deduction 的 case 3），所以 cvr（`const`，`volatile`，`reference`）三个性质全部被去掉了（仍然如之前讨论的一样，这里的 `const` 是变量本身的 `const`，如果变量是指针，那么被指资源的 `const` 不在此列），`const Widget *const &` 也就变成了 `const Widget *`。

## Chapter 2  auto

### Item 5  Prefer auto to explicit type declarations

在定义变量时，相较于显式地指定其类型，使用 `auto` 有以下优点：

+ 强制初始化
+ 避免用于初始化的表达式类型和声明的类型不一致，从而导致可移植性或执行效率的问题
+ 更改一处类型会自动更新和该类型有关的 `auto`，便于重构
+ 少打很多字

### Item 6  Use the explicitly typed initializer idiom when auto deduces undesired types

+ 在使用 `auto` 来定义变量时，如果用于初始化的表达式类型为 invisible proxy class，则类型推断的结果有可能和预想的不一致：

  ```c++
  std::vector<bool> features() {
    	return std::vector<bool>(5, true);
  }
  
  cout << features()[3] << endl;			// 1
  auto a = features()[3];
  cout << a << endl;			// 0
  bool b = features()[3];
  cout << b << endl;			// 1
  ```

  此处 `std::vector<bool>::operator[]` 的返回值类型是 `std::vector<bool>::reference` 而非 `bool&`，前者即为一种 invisible proxy class。

  所谓 proxy class，是为了模拟或强化别的类型而设计的。有的 proxy class 对用户来说很明显，例如智能指针；而有的则设计得很隐晦，试图对用户封装这一细节，这类 proxy class 就称作 invisible proxy class。`std::vector<bool>::reference` 就是其中之一，使用它是因为 `std::vector` 对于 `bool` 类型元素的存储方式是一个元素占一个 bit，而 C++ 不允许对 bit 的引用，那么 `operator[]` 自然无法返回 `bool&`，只好返回一个表现得像 `bool&` 的 `std::vector<bool>::reference`。

  正常情况下，用 `std::vector<bool>::reference` 初始化一个 `bool` 类型的变量，会触发一次隐式转型，将转型后的值赋给该变量。而初始化 `auto` 变量时，类型推断为 `std::vector<bool>::reference`，该变量的实际值依赖于具体实现细节，比如有可能是指向存储该 bit 的字节的指针以及一个字节内的偏移量，但是该函数的返回值是个右值，这条语句过后指针就悬空了，导致未定义的行为。

+ 对于如何判断函数的返回值是不是 invisible proxy class，没有什么太好的方法，书中给的建议是阅读文档和源代码。

  需要注意的一点是，这并不是 `auto` 的问题，只是用户预期的类型和 `auto` 推断出的类型不一致（`auto` 仍然遵守着它的规则），所以解决这一问题并不需要放弃 `auto`，而可以使用 explicitly typed initializer idiom，也就是将用于初始化的表达式的类型转换成用户想要的类型：

  ```c++
  auto a = static_cast<bool>(features(w)[5]);
  ```

  此外，explicitly typed initializer idiom 并不是只有在遇到 invisible proxy class 才可以使用，当想要强调转型这一事实时也可以使用（毕竟 explicitly typed initializer idiom 本质上就是 `static_cast`）

## Chapter 3  Moving to Modern C++

### Item 7  Distinguish between () and {} when creating objects

+ C++11 引入了 uniform initialization （即用花括号括起来的一组值）来解决原先圆括号初始化的一些问题。相较于后者，前者有以下优点：

  + 可以用于初始化 non-static 成员变量：

    ```c++
    class Widget {
    private:
     	int x{0}; 	// fine, x's default value is 0
     	int y = 0; 	// also fine
     	int z(0); 	// error!
    };
    ```

  + 禁止 narrowing conversion：

    ```c++
    long a;
    int b{a};  // warning
    ```

  + 显式调用空参构造（解决 C++’s most vexing parse）：

    ```c++
    Widget w1();	// 声明函数而非调用空参构造
    Widget w2{};	// 调用空参构造
    ```

  + 初始化列表（initializer_list）：

    ```c++
    std::vector<int> v{ 1, 3, 5 };
    ```

+ 初始化列表的机制也存在一些小瑕疵：当一个类的所有构造函数中出现了接受 initializer_list 作为参数的版本后，以 uniform initialization 形式的构造都会被其捕获，除非花括号中值的类型无法转换到 initializer_list 的类型。

### Item 8  Prefer nullptr to 0 and NULL

+ 0 和 `NULL` 首先是整型，在必要的情况下才会被解释成 null pointer，而 `nullptr` 可以隐式地转换成任何一种指针类型。

  所以 C++98 建议尽量不要同时重载接受整型和指针作为参数的函数版本。

+  考虑这样一个场景：

  ```c++
  class A {};
  int f(std::shared_ptr<A> sp) {}
  
  f(0);  // ok，0 被解释成 null pointer
  
  auto a = 0;  // 除非必要，0 被解释成 int
  f(a);  // error，a 是 int，不能转换到指针
  ```

##### Last-modified date: 2020.5.20, 5 p.m.