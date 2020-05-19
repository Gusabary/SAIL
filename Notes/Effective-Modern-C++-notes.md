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
+ **`ParamType` 不是指针也不是引用**：忽略 `expr` 的 `&`, `const` 以及 `volatile`，然后和 `ParamType` 相比得到 `T`。

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

##### Last-modified date: 2020.5.19, 6 p.m.