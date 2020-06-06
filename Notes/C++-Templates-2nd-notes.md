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

##### Last-modified date: 2020.6.6, 6 p.m.