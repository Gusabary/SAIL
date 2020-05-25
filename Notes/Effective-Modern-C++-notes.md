# Effective Modern C++ Notes

## Introduction

+ **argument** 是实参，可以是左值也可以是右值； 

  **parameter** 是形参，一定是左值。

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

### Item 9  Prefer alias declarations to typedefs

+ C++11 引入了 alias declarations，和 `typedef` 相比，它支持模板别名：

  ```c++
  template<typename T>
  using MyAllocList = std::list<T, MyAlloc<T>>; 
  
  MyAllocList<Widget> lw;
  ```

  而 `typedef` 想使用模板的话需要在外面包一层 struct：

  ```c++
  template<typename T>
  struct MyAllocList {
   	typedef std::list<T, MyAlloc<T>> type;
  };
  
  MyAllocList<Widget>::type lw;
  ```

+ `typedef` 使用模板时不仅定义变量时要多写一个 `::type`，在别的类中使用这个别名时还要加上 `typename`：

  ```c++
  template<typename T>
  class A {
  private:
   	typename MyAllocList<T>::type list;
  };
  ```

  因为对于编译器来说，它能够确保通过 alias declarations 声明的类型别名一定是类型，而不能肯定在模板中通过 `typedef` 声明的别名也一定是类型，因为有可能在某个地方模板的一个特化实例中该别名就不是类型：

  ```c++
  template<>
  class MyAllocList<Wine> {
  private:
   	enum class WineType { White, Red, Rose };
   	WineType type;
  };
  ```

  所以需要程序员通过加上 `typename` 向编译器保证这一点。

### Item 10  Prefer scoped enums to unscoped enums

+ C++11 引入 scoped enums，相较于 C++98 中的 unscoped enums，有以下优点：

  + enum name 只有在 enum class 中可见，即使用 name 要加上 class 前缀，不会造成命名污染的问题；
  + strongly typed，禁止隐式转型成其他类型，可以通过 `static_cast` 转型；
  + underlying type 默认是 int，不用指定即可进行 forward declaration（unscoped enums 没有默认的 underlying type，编译器会根据不同情况选择最合适的，所以要进行 forward declaration 的话需要手动指定，此外，C++98 中不能手动指定 underlying type）。

+ 但是 unscoped enums 并非一点用也没有，例如在获取 tuple 中的某个字段时，可以利用 unscoped enums 允许隐式转型的机制少些很多代码：

  ```c++
  using UserInfo = std::tuple<std::string,   // name
   			   				std::string,   // email
   			   				std::size_t>;  // reputation 
  
  enum UserInfoFields { uiName, uiEmail, uiReputation };
  UserInfo uInfo;
  auto val = std::get<uiEmail>(uInfo); 
  ```

### Item 11  Prefer deleted functions to private undefined ones

+ 如果想要让某个函数禁止被调用，相较于 C++98 中将函数声明为 private 然后不实现的做法，C++11 提供了更好的机制：在函数签名后加上 `=delete;`，它有以下优点：

  + 编译报错更明显。C++98 中的做法对于调用这类函数，报错是“不能调用 private 方法”，而本质原因是这类函数被设置为禁止调用，只是实现机制是通过 private 来隐藏它们。相比之下，C++11 的报错信息就是“不能调用 deleted 的函数”，更加本质。

  + 在编译时刻报错。在 C++98 的做法中，尽管方法被声明成 private，但是仍然有可能被调用到（其他成员函数、友元等等），这种情况下到了链接时刻才会报错（因为只有声明没有实现）。而 C++11 中在编译时刻就能发现这一行为。

  + 禁用重载版本。C++98 中禁用函数的实现机制就决定了只能禁用类的方法，而不能禁用非成员函数。C++11 中则可以，利用这一点可以过滤掉某些重载版本：

    ```c++
    bool isLucky(int number);
    bool isLucky(double) = delete; 
    ```

  + 禁用模板特化版本。对于类中定义的模板，想要禁用某些特化版本，C++98 的做法是将被禁的特化版本声明成 private，但这是不行的，因为模板特化只能写在 namespace 的 scope 中，而不能写在 class 的 scope 中。C++11 则可以在类外禁用它：

    ```c++
    class Widget {
    public:
        template<typename T>
     	void processPointer(T* ptr) {}
    // private:           // C++98, error
     	// template<>
     	// void processPointer<void>(void*);
    };
    
    template<>			  // C++11, ok
    void Widget::processPointer<void>(void*) = delete; 
    ```

+ 说白了就是 C++98 中的做法只是禁用函数效果的一个模拟，而 C++11 中是真的禁用掉了。

### Item 12  Declare overriding functions override

+ 子类的方法想要 override 父类的方法需要满足很多条件，例如函数名相同，父类方法为虚函数，除此以外对参数类型、返回值类型、异常声明类型、方法的常量性、引用限定符（reference qualifiers）等等都有要求。

  所以一不小心就会导致没有真的 override，而为了保证这一点，可以在子类方法后加上 `override` 来确保有一个对应的父类方法真的被 override 了。

+ 所谓方法的 reference qualifiers，是用于使不同左右值属性的 `*this` 能调到不同的重载版本：

  ```c++
  class Widget {
  public:
   	using DataType = std::vector<double>;
   	DataType& data() & 
   	{ return values; } 
   	DataType data() && 
   	{ return std::move(values); } 
  private:
   	DataType values;
  };
  
  auto vals1 = w.data();			   // lvalue
  auto vals2 = makeWidget().data();  // rvalue
  ```

  同理，如果某个方法后加上了 `const` 限定符，则该方法只能被 const `*this` 调用。

### Item 13  Prefer const_iterators to iterators

+ 尽量使用 const 的概念在 C++11 之前就出现了，但是 C++98 对于 `const_iterator` 的支持不是很实用。在 C++11 中，能用 `const_iterator` 的地方还是要尽量去用。
+ 泛型编程中，应当尽可能地使用非成员版本的 `begin`, `end`, `cbegin` 等等函数，而在 C++11 中只有非成员版本的 `begin` 和 `end`，没有 `cbegin`，`rbegin` 等等（虽然可以自己实现），后者在 C++14 中引入。

### Item 14  Declare functions noexcept if they won't emit exceptions

+ 对于某个函数可能抛出且未捕获的异常，C++98 的建议是在函数签名后写 `throw()`，里面填上这些异常类型（如果没有未捕获的异常就空着）；而 C++11 觉得真正重要的是一个函数是否抛异常，而不是抛哪些异常，所以建议的做法是如果不抛异常，就在函数签名后加上 `noexcept`。

+ 声明一个函数为 noexcept 具有优化的作用。例如如果 move 操作是 noexcept 的，`push_back` 导致 `vector` 扩容时就可以选择 move 而非 copy 来保证异常安全。

+ noexcept 带来的优化很重要，但是正确性更加重要：一旦声明了一个函数为 noexcept，就不要轻易地反悔，因为这有可能影响客户端的代码。

  此外，强行将函数实现成 noexcept 也是没有必要的，因为 noexcept 带来的性能优化可能远远比不上强行实现带来的性能开销大。

+ 对于析构函数来说，它们都是隐式声明成 noexcept 的，除非类中某个成员的析构函数显式地声明会抛出异常（`noexcept(false)`）

+ 某些库函数的设计者将函数分为 wide contracts 和 narrow contracts，区别在于是否有使用条件上的限制。对于不会抛出异常的 wide contracts 函数来说，可以放心地将其声明为 noexcept。而对于不会抛出异常的 narrow contracts 函数，尽管是否违反使用限制应该由调用者来检查，但是有时由该函数来检查也是可以的，也就是说该函数还是有可能会因为不满足使用限制而抛出异常，所以不能声明为 noexcept。

### Item 15  Use constexpr whenever possible

+ `constexpr` 可以用于限定一个对象或函数。

  当 `constexpr` 作用于一个对象时，它表明该对象是一个编译时常量，即该对象的值在编译时刻就已确定（所有 constexpr 对象都是 const，但不是所有 const 对象都是 constexpr）

  当 `constexpr` 作用于一个函数时，如果该函数的所有入参都是编译时常量，那么该函数会在编译时刻执行并返回一个编译时常量作为返回值；如果并非所有入参都是编译时常量，那么该函数就是一个普通的函数，在运行时刻被调用。

+ C++11 中的 constexpr 函数只能有一条 `return` 语句（不过仍然可以使用三目运算符实现条件语句，使用递归实现循环语句），C++14 则将该限制放宽了许多，只是不能调用 non-constexpr 的函数。

+ 编译时常量（字面量）不仅可以是内置类型（`void` 除外），也可以是用户定义的类，因为构造函数也可以声明成 `constexpr`。

+ 尽可能地使用 `constexpr`，因为 `constexpr` 对象和函数有更大的使用范围，将运算转移到编译时刻也能提高运行时刻的效率。

  事实上 C++ 有很多地方只能使用编译时常量，例如数组大小、non-type 模板参数、enumerator 的值以及 alignment specifiers 等等。

+ 和 noexcept 类似，一旦将函数声明成 `constexpt` 就不要轻易反悔，因为这有可能会影响客户端的代码。

### Item 16  Make const member functions thread safe

+ const 成员函数不会修改成员变量，所以是线程安全的。但是 C++11 引入的 `mutable` 改变了这一点：被 `mutable` 修饰的成员变量即使在 const 成员函数中也可以被修改。这提供了更多的灵活性，但是同时也增加了程序员的负担：现在需要程序员来保证 cosnt 成员函数的线程安全了。

+ 保证线程安全有两种方法：互斥锁和原子操作。它们的区别在于锁的粒度（临界区大小）不同，如果临界区只是一条语句、一个变量、一次访存操作这种很小的粒度，就可以使用原子操作：

  ```c++
  mutable std::atomic<unsigned> callCount{ 0 };
  
  ++callCount;  // atomic increment
  ```

  如果临界区比较大，就需要用互斥锁来保护：

  ```c++
  mutable std::mutex m;
  
  {
   	std::lock_guard<std::mutex> g(m); // lock mutex
   	/* critical section */
  } 	// unlock mutex
  ```

  需要注意的是原子变量和互斥锁都声明成了 `mutable`，因为在 const 成员变量中需要修改它们。

  此外，`std::atomic` 和 `std::mutex` 都是 move-only 的类，这就导致包含它们的类不能被默认拷贝。

### Item 17  Understand special member function generation

+ 所谓 special member function，是指默认构造、拷贝构造、拷贝赋值、析构、移动构造、移动赋值这六个函数，它们在没有声明的情况下被使用时，编译器**通常**会生成一个默认的版本。

  这里说**通常**，是因为有些特殊的规则限制编译器在某些情况下不会生成默认版本：

  + 当声明了任意形式的构造函数（包括拷贝构造和移动构造）时，不会自动生成默认构造。

  + 当声明了移动操作（包括移动构造和移动赋值）时，不会自动生成拷贝操作和另一个移动操作。

    这是因为声明移动操作意味着编译器生成的移动操作不能满足需求，那么很有可能生成的拷贝操作也不能满足需求。

  + 当声明了拷贝操作时，不会自动生成移动操作。

    原因同上，至于为什么声明拷贝操作不会阻止另一个拷贝操作的生成，应该是考虑和 C++98 的兼容性。

  + 当声明了析构函数时，不会自动生成移动操作。

    根据 Rule of Three（拷贝操作、析构函数这三个函数中只要声明了一个，其他两个也应该声明），声明了析构函数时就不应该自动生成拷贝操作了，但是制定 C++98 标准的时候 Rule of Three 的重要性尚未被人们意识到，后来又为了兼容性，就导致即使声明了析构函数，拷贝操作仍然可以自动生成。但是对于 C++11 新引入的移动操作，在声明了析构函数的情况下就不会自动生成了。

+ 编译器自动生成的默认版本是 public，inline 且 non-virtual 的（除了父类有虚析构情况下子类析构是虚函数以外其他都是非虚的）

  自动生成的拷贝操作使用 memberwise copy，即对于所有 non-static 成员变量，调用它们的拷贝操作，如果某个成员的拷贝操作没有声明并且也无法生成，则拷贝失败。

  自动生成的移动操作使用 memberwise move，即对于所有 non-static 成员变量，调用它们的移动操作，如果某个成员的移动操作没有声明并且也无法生成，则转而调用拷贝操作，如果拷贝操作也没有，那移动失败。

+ 对于 special member function，可以指定 `=default;` 来声明一个和编译器自动生成的默认版本一样的函数。

+ 此外，成员函数模板不会影响编译器对 special member function 的自动生成。

## Chapter 4  Smart Pointers

使用裸指针有以下几个问题：

+ 从裸指针的声明中不能看出其指向的是一个对象还是一个数组。
+ 从裸指针的声明中不能看出是否需要释放它，以及怎样释放。
+ 很难保证裸指针释放且仅被释放一次。
+ 没有办法得知裸指针是否悬空了。

智能指针解决了这些问题。

### Item 18  Use std::unique_ptr for exclusive-ownership resource management

+ `std::unique_ptr` 禁用拷贝，默认情况下大小和执行效率和裸指针一样。可以自定义 deleter，但是使用函数指针或者有状态的 function object 作为 deleter 时 `unique_ptr` 的大小就不再是一个指针长了；而使用 lambda 表达式或者无状态的 function object 则不会带来空间上的开销。

+ `unique_ptr` 的一个使用场景是作为工厂函数的返回值类型，它可以很方便地转换成 `shared_ptr`：

  ```c++
  std::unique_ptr<Investment> makeInvestment();
  
  auto pInvestment = makeInvestment();
  std::shared_ptr<Investment> sp = makeInvestment();
  ```

### Item 19  Use std::shared_ptr for shared-ownership resource management

+ `std::shared_ptr` 大小为双指针长，一个指向被管理的对象，另一个指向 control block。control block 中存储着 reference count, weak count, custom deleter, allocator 等等。

  而且对 reference count 的操作需要是原子的，所以 `shared_ptr` 在空间和时间上的开销都会比 `unique_ptr` 大一些。

+ 由于 custom deleter 存储在 control block 中，所以使用函数指针或者有状态的 function object 也不会再增加 `shared_ptr` 的大小。此外，相同类型的 `shared_ptr` 可以有不同类型的 custom deleter，这点和 `unique_ptr` 不同，后者将 custom deleter 的类型作为其模板参数之一。

+ 需要注意的是，以下情况会创建 control block：

  + 调用 `std::make_shared`；
  + 从 `unique_ptr` 或 `auto_ptr` 创建一个 `shared_ptr`；
  + 以裸指针为参数构造 `shared_ptr`。

  所以用某一个裸指针多次构造 `shared_ptr` 将会导致一个对象和多个 control block 关联，而当 control block 中的 reference count 降为 0 时会释放关联的对象，也就是说该对象会被释放多次从而导致未定义的行为（正确的做法应该是用一个 `shared_ptr` 去拷贝构造另一个）。

  看上去很好避免，但其实有一个场景就很容易导致这个问题：使用 `this` 指针构造 `shared_ptr`，因为无法保证在类外指向该对象的指针是不是已经用于构造了一个 `shared_ptr`。解决此问题可以让类继承自一个工具类：

  ```c++
  class Widget: public std::enable_shared_from_this<Widget>
  ```

  然后在需要用 `this` 指针构造 `shared_ptr` 的时候，可以使用 `shared_from_this` 方法：

  ```c++
  std::vector<std::shared_ptr<Widget>> processedWidgets;
  
  processedWidgets.emplace_back(shared_from_this());
  ```

  需要注意的是由于 `shared_from_this` 的实现机制，需要事先有一个 `shared_ptr` 和 `this` 关联，这可以通过将类的构造声明为私有，对外暴露一个返回 `shared_ptr` 的工厂方法来实现。

### Item 20  Use std::weak_ptr for std::shared_ptr-like pointers that can dangle

+ `std::weak_ptr` 可以由 `shared_ptr` 构造而来，它不能解引用，想要通过 `weak_ptr` 访问被指向的对象需要先转换成 `shared_ptr`，有两种转换方法：`weak_ptr::lock()` 以及用 `weak_ptr` 构造一个 `shared_ptr`，区别在于当 `weak_ptr` expired 时，前者会返回 `nullptr`，而后者抛出异常。
+ `weak_ptr` 的空间和时间开销本质上和 `shared_ptr` 一样，都是双指针大小，都需要原子操作。
+ `weak_ptr` 的设计理念决定了其应用场景：弱引用。所谓弱引用，即不增加 reference count，可以得知对象是否 expired 而不管理其生命周期（就像一个旁观者，能看到对象是否还在，但是自己不参与进去）。具体的应用例如 cache，观察者模式，解决循环引用等等。

### Item 21  Prefer std::make_unique and std::make_shared to direct use of new

+ 所谓 make 函数是指接收一组参数，将它们完美转发到某个类的构造，动态创建一个对象然后返回指向该对象的智能指针。一共有三个 make 函数：`make_unique`，`make_shared` 以及 `allocate_shared`。后两个类似，但是 `allocate_shared` 可以指定使用的 allocator。

+ 和直接使用 new 相比，使用 make 函数有以下优点：

  + 减少重复。利用 `auto` 可以少写一次类名：

    ```c++
    auto upw1(std::make_unique<Widget>());
    std::unique_ptr<Widget> upw2(new Widget);
    ```

  + 异常安全。因为编译器不保证参数计算的顺序，所以 `g` 有可能在 new 之后，`shared_ptr` 构造之前抛出异常造成内存泄露：

    ```c++
    f(std::shared_ptr<Widget>(new Widget), g());
    ```

  + 更紧凑的内存布局和更高的执行效率。使用 make 函数可以将对象和 control block 分配在一块内存中，这样只用申请一次。

+ 但是也有些情况下 make 函数并不如 new 好用（甚至是只能用 new）：

  + 需要使用自定义的 deleter 时：

    ```c++
    auto widgetDeleter = [](Widget* pw) { … };
    
    std::unique_ptr<Widget, decltype(widgetDeleter)> upw(new Widget, widgetDeleter);
    std::shared_ptr<Widget> spw(new Widget, widgetDeleter);
    ```

  + 需要使用 braced initializer 时。之前提到 make 函数将参数完美转发到类的构造，而 braced initializer 不允许完美转发。但也不是完全没有办法，可以先用 braced initializer 初始化一个 `initializer_list`，将可以完美转发了：

    ```c++
    auto initList = { 10, 20 };
    // auto spv = std::make_shared<std::vector<int>>({ 10, 20 });  // error
    auto spv = std::make_shared<std::vector<int>>(initList);	 // ok
    ```

  + 有些类定义了自己的 `operator new` 和 `operator delete`，一次只申请 / 释放正好一个对象大小的空间，而 make 函数需要将对象和 control block 申请在一起。

  + 将对象和 control block 申请在一起意味着它们也只能同时被释放。但是本来只要所有指向该对象的 `shared_ptr` 都析构了（reference count 降为 0），对象的内存就可以释放了，而现在不行，因为还要等 `weak_ptr` 都析构了（weak count 降为 0），control block 和对象的内存才能同时释放。而在这两个时刻之间，对象的内存是白白占用着的。

### Item 22  When using the Pimpl Idiom, define special member functions in the implementation file

+ Pimpl Idiom 是说将类实现相关的细节移到类的源文件（implementation file）中，在头文件中只保留一个指向包含了原先私有成员的结构体的指针，这样客户端的代码就可以少依赖很多类实现相关的代码，加快构建速度：

  ```c++
  // widget.h
  class Widget {
  public:
   	Widget();
  private:
   	struct Impl;
   	std::unique_ptr<Impl> pImpl;
  };
  
  // widget.cpp
  #include "widget.h"
  #include "gadget.h"
  #include <string>
  #include <vector>
  struct Widget::Impl {
   	std::string name;
   	std::vector<double> data;
   	Gadget g1, g2, g3;
  };
  Widget::Widget() : pImpl(std::make_unique<Impl>()) {}
  ```

+ 但是仅有这些还不能 work，仔细分析代码：我们没有声明析构，那么编译器会自动生成一个。自动生成的析构会调 `pImpl` 的析构，也就是 `unique_ptr` 的 default deleter，而这个 deleter 在 `delete` 裸指针之前会先 `static_assert` 一下这个裸指针指向的对象不是一个 incomplete type（也就是声明但未实现的类型）。而在编译客户端代码（例如 `main.cpp`）时，实现类的代码（`widget.cpp`）是不可见的，也就是说 `struct Impl` 是 incomplete type，`static_assert` 失败了。

  要解决这个问题也很好办，我们仍然可以使用编译器自动生成的析构，但是在此之前要让编译器看到 `struct Impl` 的定义：

  ```c++
  // widget.h
  ~Widget();
  
  // widget.cpp
  Widget::~Widget() = default;
  ```

  也就是说让编译器在源文件中看到 `struct Impl` 的定义后再自动生成析构，而头文件中加上一个析构声明即可。

+ 因为声明了析构，编译器不会自动生成移动操作了，需要我们自己声明，事实上默认移动已经符合我们的要求了（调用 `pImpl` 的移动操作），所以只需要加上 `=default;` 即可。但是不能加在头文件中，原因和之前类似，默认移动的实现都需要调用析构，就会导致 incomplete type 的问题。具体为什么要调用析构，移动构造和移动赋值有些不同：移动构造中，如果抛出了异常编译器需要析构掉 `pImpl`；移动赋值中编译器需要在赋值前把旧的 `pImpl` 析构掉。

  解决的方法也类似：将移动操作的定义（也就是 `=default;`）放到源文件中，让编译器生成默认代码时看到 `struct Impl` 的定义：

  ```c++
  // widget.h
  Widget(Widget&& rhs);
  Widget& operator=(Widget&& rhs);
  
  // widget.cpp
  Widget::Widget(Widget&& rhs) = default;
  Widget& Widget::operator=(Widget&& rhs) = default;
  ```

+ 此外，我们还需要声明拷贝操作，但是编译器的默认拷贝并不能满足我们的要求（`unique_ptr` 没有拷贝操作），所以这回要自己实现。同样，为了避免 incomplete type 的问题（`make_unique` 需要知道 `struct Impl` 的大小，赋值需要知道 `struct Impl` 的拷贝操作），需要在头文件中声明，在源文件中写上定义：

  ```c++
  // widget.h
  Widget(const Widget& rhs);
  Widget& operator=(const Widget& rhs); 
  
  // widget.cpp
  Widget::Widget(const Widget& rhs) : pImpl(std::make_unique<Impl>(*rhs.pImpl)) {}
  
  Widget& Widget::operator=(const Widget& rhs) {
   	*pImpl = *rhs.pImpl;
   	return *this;
  }
  ```

+ 最后理一下关键的逻辑：之所以要将 special member function 定义在源文件中，是因为在编译它们时，`struct Impl` 不能是 incomplete type。而之所以 `struct Impl` 不能是 incomplete type，是因为 `unique_ptr` 的构造和析构需要用到 `struct Impl` 的信息（例如 sizeof，结构体成员的析构等等）。

  `shared_ptr` 的析构似乎倒不要求 `struct Impl` 不能是 incomplete type，因为 `shared_ptr` 的 deleter 类型并不是自己模板的类型参数之一，而 `unique_ptr` 的 deleter 类型是，这么设计是为了使 `unique_ptr` 有更小的运行时数据结构以及更快的执行效率。

## Chapter 5  Rvalue References, Move Semantics and Perfect Forwarding

### Item 23  Understand std::move and std::forward

+ `std::move` 和 `std::forward` 在运行时刻不做任何事（它们不会生成哪怕一个字节的可执行代码），只是在编译时刻转换参数的左右值属性：

  ```c++
  template<typename T>
  decltype(auto) move(T&& param)
  {
   	using ReturnType = remove_reference_t<T>&&;
   	return static_cast<ReturnType>(param);
  }
  ```

  注意：作为返回值的右值引用是右值！

+ `move` 无条件地将参数转换成右值，而 `forward` 可以根据模板参数来决定是否将参数转换成右值。尽管用 `forward` 也可以实现 `move` 的功能，但是需要多指定一个模板参数，并且语义上也不太合适：

  ```c++
  A a;
  std::move(a);
  std::forward<A>(a);
  ```

### Item 24  Distinguish universal reference from rvalue reference

+ universal reference 需要满足两个条件：有形如 `T&&` 的类型以及发生了类型推断。

  + 仅发生类型推断但没有 `T&&` 类型的情况：

    ```c++
    template<typename T>
    void f(std::vector<T>&& param);		// rvalue reference
    ```

    注意必须是严格的 `T&&`（当然不一定非得是 `T`），多一个 `const` 也不行：

    ```c++
    template<typename T>
    void f(const T&& param);	// rvalue reference
    ```

  + 仅有 `T&&` 类型但没有发生类型推断的情况：

    ```c++
    template<class T, class Allocator = allocator<T>
    class vector {
    public:
     	void push_back(T&& x);		// rvalue reference
    };
    ```

    该情况中，实际调到 `push_back` 前类模板已经被实例化了，所以 `T` 已经是一个具体的类型了，不会发生类型推断。

  `T&&` 也可以是 `auto&&`：

  ```c++
  auto&& var2 = var1;  // universal reference
  ```

  这在 C++14 的 lambda 表达式中尤其有用：

  ```c++
  auto timeFuncInvocation = [](auto&& func, auto&&... params) {  // universal reference
   	// start timer
   	std::forward<decltype(func)>(func)(std::forward<decltype(params)>(params)...);
   	// stop timer and record elapsed time
   };
  ```

+ universal reference 表现成 lvalue reference 还是 rvalue reference 由初始化表达式是左值还是右值来决定：

  ```c++
  template<typename T>
  void f(T&& param); // param is a universal reference
  
  Widget w;
  f(w); 			  // lvalue passed to f; param's type is Widget&  (lvalue reference)
  f(std::move(w));  // rvalue passed to f; param's type is Widget&& (rvalue reference)
  ```

### Item 25  Use std::move on rvalue references, std::forward on universal references

+ 对于函数入参，将 `move` 作用于 rvalue reference，将 `forward` 作用于 universal reference，需要注意的是如果多次使用该入参，则只能将 `move` 或 `forward` 作用在最后一次上：

  ```c++
  template<typename T>
  void setSignText(T&& text) {
   	sign.setText(text);
   	auto now = std::chrono::system_clock::now();
  	signHistory.add(now, std::forward<T>(text));
  }
  ```

  当然，这里说的 “最后一次” 也可以是返回值（如果函数的返回值是 return by value 的话）：

  ```c++
  Matrix operator+(Matrix&& lhs, const Matrix& rhs) {
   	lhs += rhs;
   	return std::move(lhs);
  }
  ```

  这样做的好处在于省去一次 copy 的开销。

+ 但是将 `move` 作用于返回值这一操作对于 local 对象来说，情况有所不同。首先需要明白为什么要将 `move` 作用于返回值？因为想省下一次 copy 操作的开销。那么返回一个 local 对象真的会 copy 吗？其实大部分情况下是不会的，因为编译器做了 RVO（返回值优化），即直接在存放函数返回值的内存位置处构造这个 local 对象：

  ```c++
  Widget makeWidget() {
   	Widget w;  // 构造在存放函数返回值的内存位置，而非存放普通 local 对象的位置
   	return w;
  }
  ```

  所以即使不加 `move`，编译器也不会 copy。相反，如果加了 `move`，编译器将不会进行 RVO，因为 RVO 需要满足两个条件：local 对象类型要和返回值类型一样且 local 对象就是被返回的对象。而被返回的对象（`std::move(w)`）是 `w` 的引用，并非 `w` 本身，所以不满足 RVO 的条件。

  那有的人也许会说，我加了 `move` 虽然放弃了 RVO 的机会（这里说是 “机会” 是因为不是说不加 `move` 就一定会 RVO，仍然有很多其他情况阻止编译器做 RVO，比如有多处返回值返回不同的 local 变量），但至少保证了不会 copy。这个观点也是不对的，因为如果满足 RVO 的条件而编译器由于种种原因没有做 RVO 的话，它也会将返回值作为右值来处理，即看上去像是编译器帮你加了 `move` 一样。

  总结一下就是，当函数满足 RVO 的条件时，不要将 `move` 作用于返回值。因为如果确实做了 RVO，那就白白多了一次 move 操作；而就算没有做 RVO，编译器也会将其当做右值处理，手动加上 `move` 并没有任何优化。

+ 同样地，对于返回值是值传递进来的参数的情况，编译器也会将其当做右值处理（这种情况做不了 RVO），不用程序员加上 `move`：

  ```c++
  Widget makeWidget(Widget w) {
   	return w;
  }
  ```

### Item 26  Avoid overloading on universal references

+ 尽量避免对 universal reference 进行重载：

  ```c++
  template<typename T>
  void logAndAdd(T&& name);
  
  void logAndAdd(int idx);
  ```

  在这个例子中，只要参数类型不是 int，对 `logAndAdd` 的调用都会被有 universal reference 的版本捕获，因为它可以实例化出一个完美匹配的模板函数。在 C++ 重载函数的版本选择中，类型能完美匹配的优于需要转型的，如果类型都能完美匹配，那么非模板函数优于实例化出来的模板函数。

+ 除此以外，当类的构造中含有 universal reference 时，情况会变得更糟。它会捕获所有非常量拷贝构造（编译器自动生成的拷贝构造参数具有 `const`，不能算完美匹配了），还会捕获所有来自子类的拷贝构造（子类的类型被父类的拷贝构造接受需要转型）

### Item 27  Familiarize yourself with alternatives to overloading on universal references

如果不得不利用重载 universal reference 提供的功能，有几种方式可以避免它带来的问题：

+ 不重载，用不同名的函数。
+ 不使用 universal reference，可以使用常量左值引用或值传递。

（以上两种方法都不是很好）

+ Tag Dispatch。重载 universal reference 带来的问题就是选择重载版本时，它往往会捕获到比我们想象的多得多的调用，那只要解决这个问题就可以了。注意到选择重载版本是以参数类型为依据的，universal reference 只是其中一个参数，我们可以使用另一个参数（tag）来决定调用（dispatch）哪个版本：

  ```c++
  template<typename T>
  void logAndAdd(T&& name) {
   	logAndAddImpl(
      	std::forward<T>(name),
   		std::is_integral<typename std::remove_reference<T>::type>()
          // 使用 remove_reference 是因为 int& 不是 integral type
      );
  }
  
  template<typename T>
  void logAndAddImpl(T&& name, std::false_type)；
  
  void logAndAddImpl(int idx, std::true_type)；
  ```

  `std::true_type` 和 `std::false_type` 是编译时的 bool 类型。选择重载版本时它们能 “屏蔽” universal reference 的完美匹配（就好比是在 “完美匹配 + 完美不匹配” 和 “需要转型 + 完美匹配” 中选一个）。

+ 还有一种方法，用到模板可以在某些条件下被禁用的机制，`condition` 不满足时，该模板就好像不存在一样：

  ```c++
  template<typename T, typename = typename std::enable_if<condition>::type>
  ```

  为了不让 universal reference 捕获到和自定义类型仅 cvr 属性不同的参数、是自定义类型派生类的参数或者 integral type，可以这样指定自定义类型的含有 universal reference 的构造函数：

  ```c++
  template<
  	typename T,
   	typename = std::enable_if_t<
   		!std::is_base_of<Person, std::decay_t<T>>::value &&
   		!std::is_integral<std::remove_reference_t<T>>::value
   	>
  >
  explicit Person(T&& n) : name(std::forward<T>(n)) {}
  ```

  这种方法解决了 Tag Dispatch 的一个问题：即使是含有 universal reference 的构造函数也不能捕获所有调用（还存在其他重载版本，比如默认拷贝构造），所以就没办法作为一个 dispatcher。

+ universal reference 的确效率很高，但是往往存在易用性上的问题，比如报错信息太隐晦。

### Item 28  Understand reference collapsing

reference collapsing 是说当编译器生成了一个引用的引用时（程序员是不能定义引用的引用的），会按照以下规则将其折叠成单引用：

```c++
T& &   -> T&
T& &&  -> T&
T&& &  -> T&
T&& && -> T&&
```

有四种情况会发生 reference collapsing：

+ 模板实例化（universal reference），这也是用的最多的一个情形：

  ```c++
  template<typename T>
  void func(T&& param);
  ```

  当实参为左值时，`T` 推断为左值引用 `&`，发生 reference collapsing（当实参为右值时，`T` 被推断为普通类型，没有 reference collapsing）

+ auto 类型推断（universal reference），如 Item 24 中所述，这也是 universal reference：

  ```c++
  auto&& w1 = w;
  ```

+ typedef 和类型别名，上面两种情况是根据参数的左右值属性来推断不同的 `T` 或 `auto`，推断的结果有两种：普通类型和左值引用，只有推断为左值引用时才会发生 reference collapsing，但是这种情况（以及下面那种情况）不同，它们没有发生类型推断：

  ```c++
  template<typename T>
  class Widget {
  public:
   	typedef T&& RvalueRefToT;
  };
  
  Widget<int&> w1;	// int&
  Widget<int&&> w2;	// int&&
  ```

+ decltype，和第三种情况类似：

  ```c++
  decltype(a) &&b;
  ```

### Item 29  Assume that move operations are not present, not cheap and not used

move 操作并非总是优于 copy 操作，例如：

+ 有些类不支持 move 操作；
+ 对于小对象，copy 操作有时也会比 move 操作快（比如 `std::string` 的 SSO，Small String Optimization）；
+ move 操作会抛出异常，而接口要求 noexcept 时。

### Item 30  Familiarize yourself with perfect forwarding failure cases

perfect forwarding 是说有一个转发函数和目标函数，转发函数需要将其接受的参数原封不动地（保留类型，cvr 属性）传递给目标函数。而如果将这些参数传递给转发函数让其转发和直接传递给目标函数得到的行为不一样的话，perferct forwarding 就被认为失败了。

导致 perfect forwarding 失败可能有以下几种情况：

+ 参数为 braced initializers，即花括号括起来的一组值。将 braced initializers 直接传递给目标函数没有问题，但是转发函数的参数是 universal reference，无法从 braced initializers 推断出类型。但是有一个 trick 可以解决这个问题：先用 braced initializers 初始化一个 `auto` 的 initializer_list 然后将其传进来。

+ 参数为希望被当成空指针的 0 或 NULL。当转发函数接受到 0 或 NULL 时，会优先转发给目标函数接受 integral type 的重载版本，而非接受指针的重载版本。

+ 参数为未定义的 static const 成员变量。需要先明确一下，“定义” 成员变量是写在类外的，“声明” 成员变量才是类内的：

  ```c++
  class Widget {
  public:
   	static const std::size_t MinVals = 28;  // declaration
  };
  
  const std::size_t Widget::MinVals;  // definition
  ```

  如果不对未定义的 static const 成员变量取地址，那不定义没有什么问题，但是作为转发函数的参数，需要引用这个成员变量，而对编译器生成的代码来说，引用和指针通常没什么区别。即不定义就不占内存，就没有地址，就没办法引用。

+ 参数为有重载版本的函数名或者函数模板。将函数名或函数模板直接传递给目标函数时，可以通过目标函数的参数类型决定用哪个重载版本或模板实例，但是转发函数的参数类型是 universal reference，没有办法选择重载版本或模板实例。解决的方法是先将函数名赋值给一个新定义的确定类型的变量，然后将新定义的变量传递进来或者直接将函数名转型成指定的类型。

+ 参数是位域。C++ 不允许非常量引用绑定到位域，因为没法直接修改 bit。解决的方法和之前类似，先用位域初始化一个 `auto ` 变量，然后将这个新定义的变量传递进来。

## Chapter 6  Lambda Expressions

首先要搞清楚几个概念：

+ **lambda 表达式**：就是一个表达式，是代码本身：

  ```c++
  [](int val) { return 0 < val && val < 10; }
  ```

+ **closure**：由 lambda 表达式创建出来的运行时对象，closure 包含 capture list 中的数据。

+ **closure class**：closure 对象所属的类。每有一个 lambda 表达式，编译器就会生成一个 closure class。lambda 表达式中的语句就出现在 closure class 的成员函数中。

注意，lambda 表达式和 closure class 存在于编译时刻，closure 存在于运行时刻。

### Item 31  Avoid default capture modes

+ 使用默认的 by-reference capture 容易导致悬空引用的问题，除非能保证被捕获的变量生命周期不短于 lambda。指定 capture 变量至少能让问题变得更容易解决一些。

+ 使用 by-value capture 也会有悬空指针的问题（使用默认的 capture mode 将使这个问题更加隐蔽），尤其是 capture this 指针的时候，一个解决方法是将变量拷贝一份，然后 capture by value：

  ```c++
  void Widget::addFilter() const {
   	auto divisorCopy = divisor;  // divisor is a data member
   	filters.emplace_back(
   		[divisorCopy](int value)  // capture the copy
   		{ return value % divisorCopy == 0; }  // use the copy
   	);
  }
  ```

  C++14 中还可以使用 init capture 使代码更简洁：

  ```c++
  void Widget::addFilter() const {
   	filters.emplace_back(
   		[divisor = divisor](int value) 	 // copy divisor to closure
   		{ return value % divisor == 0; } // use the copy
   	);
  }
  ```

### Item 32  Use init capture to move objects into closures

+ C++14 引入的 init capture 使 lambda 表达式更加强大灵活。使用 init capture，在 `=` 左边指定 closure class 中成员变量的变量名，在 `=` 右边指定由于初始化该成员变量的表达式。

+ init capture 可以用来移动构造 capture 变量，在 C++11 中也有方法来模拟这一点：用对象来移动构造一个 bind object（`std::bind` 生成的对象），然后将该对象引用传参给 lambda：

  ```c++
  auto func = std::bind(
      [](const std::vector<double>& data) {},
      std::move(data)
  );
  ```

  `std::bind` 的第二个参数是右值，所以被用来移动构造 bind object（`func`），当 `func` 被调用时，其中的成员变量被传递给 `std::bind` 的第一个参数，由于是引用传递，所以总的开销也就只有一次 move，和使用 init capture 一样。

### Item 33  Use decltype on auto&& parameters to std::forward them

+ C++14 还引入了 generic lambda，即 labmda 参数类型可以是 `auto`，这个机制可以用来实现 lambda 的完美转发：

  ```c++
  auto fwd = [](auto&& param) {
   	f(std::forward<decltype(param)>(param));
  };
  ```

+ 和函数模板实现的完美转发相比，区别主要在于 `forward` 的类型参数：

  ```c++
  template<typename T>
  void fwd(T&& param) {
   	f(std::forward<T>(param));
  }
  ```

  以 `int` 为例，当入参为左值时，前者 `param` 是 `int&`，后者 `T` 也是 `int&`，`forward` 表现相同。但是当入参为右值时，情况有所不同：前者 `param` 是 `int&&`，后者 `T` 是 `int`，但是仔细看一下 `forward` 的实现就会发现，由于 reference collapsing，两者最终的表现仍然是相同的：

  ```c++
  template<typename T> 
  T&& forward(remove_reference_t<T>& param) {
   	return static_cast<T&&>(param);
  }
  ```

### Item 34  Prefer lambdas to std::bind

+ 和 `std::bind` 相比，lambda 表达式在诸多方面要更加好用，例如延迟求值，很好处理重载函数，内联，可读性更高等等。
+ C++14 中没有理由再使用 `std::bind` 而不用 lambda 表达式了，因为它解决了 C++11 中 lambda 表达式两个小问题：
  + move capture，C++14 引入 init capture 可以移动构造 capture 变量。
  + polymorphic function objects，本质是 `bind` 的第一个参数是 callable，这个 callable 可以是个函数模板，而 C++14 引入 lambda 表达式的 `auto` 参数，从而也支持了这一点。

## Chapter 7  The Concurrency API

### Item 35  Prefer task-based programming to thread-based

+ 异步执行一个函数有两种方法：`std::thread` 和 `std::async`：

  ```c++
  int doAsyncWork();
  
  std::thread t(doAsyncWork);
  
  auto fut = std::async(doAsyncWork);
  ```

+ `async` 相比 `thread` 有如下优点：

  + 能更方便地获取返回值、捕获异常；
  + 使用默认的 policy 可以将线程管理的任务交给系统。

### Item 36  Specify std::launch::async if asynchronicity is essential

+ `std::async` 有两种 policy：`async` 和 `deferred`，前者确保异步执行，后者推迟执行直到调用 `async` 的线程调用了 `get` 或 `wait`。如果不指定 policy，则系统会根据负载自动指定。

+ 让系统来指定的话会有一些问题：

  + 由于没有办法确定函数是不是在一个新的线程中执行，所以当函数读写一些 thread local 的变量时需要小心；
  + 如果系统指定 `deferred`，那么 `wait_for` 的返回值也会是 `deferred`，需要将这一点考虑进来；
  + 如果系统指定 `deferred`，并且没有 `get` 或 `wait`，那么函数不会被执行。

  必要的话需要手动指定 policy 为 `async`。

### Item 37  Make std::threads unjoinable on all paths

+ `std::thread` 有两个状态：joinable 和 unjoinable

  + joinable 是说 `std::thread` 和一个线程相对应，这个线程可以是处于等待执行、正在执行、阻塞或终止的状态；
  + unjoinable 是说 `std::thread` 不和任何一个线程相对应，有以下一些情况会导致一个 `std::thread` unjoinable：
    + 默认构造一个 `std::thread`，没有传进去一个函数。`std::thread` 没有东西执行，自然就不会绑定到任何一个线程；
    + 被移动的 `std::thread`，原先和该 `std::thread` 绑定的线程被移动到了另一个 `std::thread`（`std::thread` 禁用拷贝）；
    + 已经被 `join` 的 `std::thread`。调用 `join` 方法会等待函数执行完成并回收线程；
    + 已经被 `detach` 的 `std::thread`。调用 `detach` 方法强行断开 `std::thread` 和某个线程的关联。

+ 如果一个 joinable `std::thread` 的析构函数被调用，程序将会终止。因为程序无法决定使用隐式的 `join` 还是隐式的 `detach`：

  + 如果使用隐式的 `join`，会带来性能上的问题。因为析构 `std::thread` 意味着其中执行的函数已经不再重要，再花费时间等待其执行完成就没有必要了。
  + 如果使用隐式的 `detach`，则会带来正确性上的问题。因为 `std::thread` 在执行时往往会修改它存在的栈帧中的数据，而直接将其 `detach` 然后继续执行主线程就有可能导致主线程的下一个栈帧和之前 `std::thread` 的栈帧有重合部分，主线程中的数据就好像是莫名其妙被修改了一样。

  好的解决方法是通知 `std::thread` 立刻停止执行其中的函数，但是 C++11 并不支持这一点。

+ 程序员应当确保在任何一条执行路径中，`std::thread` 最终处于 unjoinable 的状态。而手动保证这一点是很难的，但是可以借用 RAII 的概念，在 `std::thread` 上封装一个类，在析构函数中将 `std::thread` 的状态改变成 unjoinable。

### Item 38  Be aware of varing thread handle destructor behavior

+ `async` 的 task 和 `std::thread` 一样，都和一个线程相对应，它们称为 thread handle（线程句柄）
+ 但是它们析构时的行为不太一样，`future` 析构时，大部分情况下就是直接析构，不 `join` 也不 `detach`，只是将 shared state 的 reference count 减一；而当满足以下三个条件时，析构会阻塞住直到 task 执行完（就像被调了 `join` 一样）：
  + 这个 `future` 和 `std::async` 创建的 shared state 相关联；
  + `std::async` 的 policy 是 `async`（不管是指定的还是系统选择的）；
  + 这个 `future` 是和这个 shared state 相关联的最后一个 `future`。

### Item 39  Consider void futures for one-shot event communication

+ 使用 condition variable 和 flag 可以实现同步机制（detect - react，某一线程达到某个条件时，另一线程才能继续执行），但是实现的方式并不优雅（需要互斥锁、需要防止假醒等等）

+ 使用 `promise` 和 `future` 能达到类似的效果，但是缺点是只能做一次同步：

  ```c++
  std::promise<void> p;
  void react(); // func for reacting task
  void detect() // func for detecting task
  {
   	std::thread t([] {
   		p.get_future().wait();	// suspend
   		react(); 
   	});
   	p.set_value();	// awake
      t.join();
  }
  ```

  使用 `shared_future` 可以一次通知多个线程。

### Item 40  Use std::atomic for concurrency, volatile for special memory

+ `std::atomic` 提供一种原子操作的手段，对 `atomic` 变量的 RMW（Read - Modify - Write）操作是原子的；此外它还提供一种类似 barrier 的机制，即在 `atomic` 操作前的语句不会被 reorder 到 `atomic` 操作后。
+ `volatile` 则是用来告诉编译器某块内存是特殊的，比如用于 memory-mapped I/O 的内存（和外围设备通信），这就决定了编译器对这些内存的读写操作不能做像正常内存那样的优化（比如多次读就合并为一次，多次写就只写最后一次）。

## Chapter 8  Tweaks

### Item 41  Consider pass by value for copyable parameters that are cheap to move and always copied

+ 当需要根据参数为左值还是右值做不同的处理时，可以使用重载或者完美转发，但是它们都有各自的缺点。C++11 引入的移动语义使另一种方法成为可能：pass by value：

  ```c++
  void addName(std::string newName)
  { names.push_back(std::move(newName)); }
  ```

+ C++11 中的值传递，对于左值参数调用拷贝构造，而对于右值参数则会调用移动构造。所以整体来看相比于重载和完美转发，值传递性能上只多了一次移动的开销。

  但是注意，值传递仅建议使用于：

  + copyable 的参数。对于禁用拷贝的参数，重载只有右值版本，值传递便没有优势了（本身值传递相对于重载的优势就是后者要维护两个函数）
  + cheap to move 的参数。很好理解，毕竟值传递会多一次移动的开销。
  + always copied 的参数。这一点是说如果参数不总是被 copy（这里的 copy 是 “副本” 的含义，可以通过拷贝也可以通过移动）的话，重载或者完美转发事实上不会有任何开销，而值传递至少还要构造和析构参数。

+ 此外，通过赋值来制作参数副本的情况比通过构造要复杂得多，需要考虑诸如内存分配、优化等等问题。而且如果形参是父类，实参是子类，还容易导致 slicing problem。

### Item 42  Consider emplacement instead of insertion

+ 大部分情况下，emplace 操作是比 insert 高效的（省去了临时对象的构造和析构），但是并非全部情况。有一些启发式的方法可以帮助判断哪些情况下 emplace 确实比 insert 高效：
  + 新对象被构造进容器，而非赋值进去，即添加的操作不会影响容器中原来的元素；
  + 添加操作的参数类型和容器元素的类型不同；
  + 容器允许元素重复。
+ 除了以上这些情况，emplace 操作还有一些小瑕疵：
  + 当 emplace 的参数有 `new` 时，在 `new` 出来的资源被对象接管前，会有一个真空期，如果此时抛出异常会导致内存泄漏；
  + 当 emplace 的参数类型和容器元素类型不一致时，即使容器元素类的构造声明了 `explicit`，emplace 操作仍能执行，但是 insert 不行（因为需要进行一次隐式转型）。

##### Last-modified date: 2020.5.25, 5 p.m.