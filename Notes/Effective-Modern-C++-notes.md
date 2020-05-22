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

##### Last-modified date: 2020.5.22, 8 p.m.