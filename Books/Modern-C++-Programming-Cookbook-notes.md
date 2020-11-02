# Modern C++ Programming Cookbook Notes

## Chapter 1  Learning Modern Core Language Features

### 1.1  Using auto whenever possible

+ In C++11, we can use `auto` to define local variables and specify return type of functions with a trailing return type;

  In C++14, we can also use `auto` for return type of functions without a trailing return type and argument  types of lambda.

+ Actually using `auto` is so recommended that there appears a term called *AAA*, which stands for *almost always auto*. Indeed `auto` has many edges but I still don't completely agree that term.

+ So how to explain the *always*, where could we use `auto`? I want to talk about the local variable scenario:

  + when defining a variable whose type doesn't need specifying explicitly, we could use `auto`:

    ```c++
    auto s = "text";      // char const * 
    auto v = { 1, 2, 3 }; // std::initializer_list<int> 
    ```

  + when defining a variable whose type needs specifying explicitly, we could also use `auto`:

    ```c++
    auto s = std::string {"text"};         // std::string
    auto v = std::vector<int> { 1, 2, 3 }; // std::vector<int>
    ```

+ And now let's compare the pros and cons about `auto`. First there are many advantages, including making it impossible to leave variables uninitialized, avoiding implicit conversion and less typing. However, there also some shortcomings, such as being hard to find the actual type without help of IDE and prone to some problems about type deduction.

  For example, in the case of perfect forwarding, we need to use `decltype(auto)` as return type to return value for value and reference for reference.

  *For more about type deduction, refer to [Chapter 1, Effective Modern C++](http://gusabary.cn/2020/05/20/Effective-Modern-C++-Notes/Effective-Modern-C++-Notes(1)-Intro&Deducing-Types/)*

### 1.2  Creating type aliases and alias templates

+ Use `using` to create type aliases and alias templates instead of `typedef` for better readability:

  ```c++
  // for using
  using array_t = int[10]; 
  using fn = void(byte, double); 
  
  template<typename T>
  using MyAllocList = std::list<T, MyAlloc<T>>;
  
  // for typedef
  typedef int array_t[10]; 
  typedef void(*fn)(byte, double); 
  
  template<typename T>
  struct MyAllocList {
      typedef std::list<T, MyAlloc<T>> type;
  };
  ```

+ Note that alias templates cannot be partially or explicitly specialized.

### 1.3  Understanding uniform initialization

+ Brace-initialization is a uniform method for initialization since C++11 so it's also called *uniform initialization*.

+ Compared to initialization method using parentheses, uniform initialization has some edges, e.g.

  + It can be used to initialize non-static data member;
  + It prevents implicit narrowing conversion;
  + It can be used to invoke the default constructor explicitly;
  + It can be used to initializer aggregate types conveniently.

  *[reference](http://gusabary.cn/2020/05/22/Effective-Modern-C++-Notes/Effective-Modern-C++-Notes(3)-Moving-to-Modern-C++/#Item-7-Distinguish-between-and-when-creating-objects)*

  But uniform initialization also has a pitfall when used: if an overload with `std::initializer_list` as parameter exists, all uniform initialization will be resolved to that version:

  ```c++
  // a vector with a single element of 5
  std::vector<int> v{5};
  
  // a vector with 5 elements
  std::vector<int> v(5);
  ```

+ When it comes to type deduction, *direct initialization* and *copy initialization* differ. Precisely speaking, when direct initialization is used, the variable will be deduced as `std::initializer_list` while when copy initialization is used, it will be deduced according to the **single** element in the braces:

  ```c++
  auto a = {42};   // std::initializer_list<int>
  auto b {42};     // int
  auto c = {4, 2}; // std::initializer_list<int>
  auto d {4, 2};   // error, too many
  ```

##### Last-modified date: 2020.11.2, 8 p.m.