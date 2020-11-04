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

### 1.4  Understanding the various forms of non-static member initialization

+ There are three ways to initialize a non-static member: in the body of constructor, in the constructor's initializer list and using *default member initialization* (since C++11, remember *uniform initialization*, right?).
+ The principle is easy:
  + If the member is irrelevant to the constructor parameters, use default member initialization.
  + If the member is decided by the constructor parameters, use constructor's initializer list, in which case you can still use default member initialization to provide a default value (the former has higher priority than the latter)
  + If necessary you can initialize members in constructor body, such as throwing an exception potentially before initialization.
+ Note that the order in which members are initialized is the one they are declared in the class definition, instead of they appeared in the constructor's initializer list.

### 1.5  Controlling and querying object alignment

+ C++11 provides standardized ways to specify and query the alignment requirements of a type: `alignas` and `alignof`.

+ We can use `alignas` to specify the alignment of a variable, class member or even class:

  ```c++
  struct alignas(4) foo { 
  	char a; 
  	char b; 
  }; 
  struct bar { 
  	alignas(2) char a; 
  	alignas(8) int  b; 
  }; 
  alignas(8)   int a; 
  alignas(256) long b[4];
  ```

  `alignas` accepts an argument of **power of 2**, and the **stricter** (larger) one between it and the natural alignment size will be the actual alignment.

  To be more precise, the natural alignment of a class is determined by **the largest alignment size** of its data members.

+ Also, we could use `alignof` to retrieve the alignment requirement of a type (including the data member field):

  ```c++
  cout << alignof(long) << endl;
  cout << alignof(bar::b) << endl;
  ```

### 1.6  Using scoped enumerations

+ Enumeration is a collection of named values of integral underlying types and those named values are called enumerators.
+ Prefer scoped enumerations (`enum class`) to unscoped enumerations (`enum`) due to following reasons:
  + unscoped enumerations export their enumerators to the surrounding scope, which is prone to name collision;
  + unscoped enumerations' underlying types cannot be manually specified so it's impossible for forward declaration.
  + unscoped enumerators could be converted implicitly to its underlying type.
+ *[reference](http://gusabary.cn/2020/05/22/Effective-Modern-C++-Notes/Effective-Modern-C++-Notes(3)-Moving-to-Modern-C++/#Item-10-Prefer-scoped-enums-to-unscoped-enums)*

### 1.7  Using override and final for virtual methods

+ Use `override` to ensure the method in derived class indeed overrides a virtual method in base class.
+ Use `final` to prevent a method to be overriden or a class to be inherited.
+ *[reference](http://gusabary.cn/2020/05/22/Effective-Modern-C++-Notes/Effective-Modern-C++-Notes(3)-Moving-to-Modern-C++/#Item-12-Declare-overriding-functions-override)*

### 1.8  Using range-based for loops to iterate on a range

+ Range-based loop is actually a syntactic sugar with form like:

  ```c++
  for ( range_declaration : range_expression ) loop_statement
  ```

  The `range_declaration` can be something beyond your expectation like perfect forwarding or structured binding:

  ```c++
  for (auto&& [rate, flag] : getRates2()) 
      std::cout << rate << std::endl;
  ```

### 1.9  Enabling range-based for loops for custom types

+ Roughly speaking, custom types that have `begin()` and `end()` methods which return a iterator or pointer type could be used in range-based loops. It's also ok if there are free `begin()` and `end()` functions accepting the custom types as parameter.

### 1.10  Using explicit constructors and conversion operators to avoid implicit conversion

+ Converting constructors and conversion operators provide ways to implicitly convert the custom type from and to another type:

  ```c++
  class A {
  public:
      A(int a, char c) {}
      operator char() { return 'a'; };
  };
  
  void f(A a) {
      cout << a << endl;  // implicit conversion from A to char
  }
  
  f({3, 'c'});  // implicit conversion from {int, char} to A 
  ```

+ Use `explicit` to prevent the implicit conversion:

  ```c++
  class A {
  public:
      explicit A(int a, char c) {}
      explicit operator char() { return 'a'; };
  };
  
  void f(A a) {
      cout << static_cast<char>(a) << endl;
  }
  
  f(A{3, 'c'});
  ```

### 1.11  Using unnamed namespaces instead of static globals

+ To solve name collision in different translation unit in a large project, the typical C solution is to declare those symbols static to limit their linkage to internal. The recommended C++ solution is to wrap those symbols in an unnamed namespace.

  Essentially, unnamed namespace is equivalent to below:

  ```c++
  namespace _unique_name_ {} 
  using namespace _unique_name_; 
  namespace _unique_name_ 
  {
      // symbols in unnamed namespace
      void print(std::string message) { 
          std::cout << "[file1] " << message << std::endl; 
      } 
  } 
  ```

  So symbols with the same name are actually in different namespaces, which solves name collision.

+ One thing worth noting is that the constant expression as template argument could not be a static value, so only C++ solution works:

  ```c++
  template <int const& Size> 
  class test {}; 
  
  static int Size1 = 10; 
  
  namespace 
  { 
      int Size2 = 10; 
  } 
  
  test<Size1> t1;  // error
  test<Size2> t2;  // ok
  ```

### 1.12  Using inline namespaces for symbol versioning

+ Symbols defined in an inline namespace is treated as if they are defined in the surrounding namespace:

  ```c++
  namespace A {
      inline namespace B {
          int c;
      }
  }
  
  A::c;
  ```

+ Note that this *inline* property is *transitive* and even the most outside namespace could be declared as inline.

### 1.13  Use structured bindings to handle multi-return values

+ Actually `std::tie` can do the same thing as structured bindings, but just a little bit verbose:

  ```c++
  std::map<int, std::string> m; 
  std::map<int, std::string>::iterator it; 
  bool inserted; 
  
  std::tie(it, inserted) = m.insert({ 1, "one" }); 
  ```

+ Since C++17, it's even possible to declare variables in `if` and `switch` conditional statements, which makes structured bindings more powerful:

  ```c++
  if (auto [it, inserted] = m.insert({ 1, "two" }); inserted) { 
      std::cout << it->second << std::endl; 
  }
  ```

## Chapter 2  Working with Numbers and Strings

### 2.1  Converting between numeric and string types

+ Use `std::to_string()` to convert a numeric (including integral and floating point type) to string.

+ Use `std::stoi()` to convert a string to an integer type. Other than the string, it accepts another two parameters, which are the address of variable to store the number of characters processed and the number indicating the base (default is 10).

  Note that the `0` (and `0x`) prefix in the string is only valid when the base is 0 or 8 (0 or 16).

+ Use `std::stod()` to convert a string to a double type. It doesn't accept the number indicating the base explicitly while the string still has several forms like decimal floating point (containing `e`), binary floating point (containing `0x` and `p`), `inf` and `nan`.

+ The functions converting string to numeric types can throw two exceptions potentially, which are `std::invalid_argument` and `std::out_of_range`.

### 2.2  Limits and other properties of numeric types

+ `std::numeric_limits`, which is a class template, provides some information about numeric types, among which the most common used is `::min()` and `::max()`.
+ Since C++11, all static members of `std::numeric_limits` are `constexpr`, which can be used everywhere including as constant expression, so the C-style macro of numeric properties can be deprecated completely.
+ *[reference](https://en.cppreference.com/w/cpp/types/numeric_limits)*

### 2.3  Generating pseudo-random numbers

+ When talking about random numbers in modern C++, we need to be clear about two concepts: engines and distributions:

  + **Engines** are used to produce random numbers with a uniform distribution.
  + **Distributions** are used to convert the output of engine to a specified distribution.

+ So things are clear: choose an engine to produce a random number and use a distribution to convert it to, say, a range we want:

  ```c++
  std::random_device rd{};
  auto mtgen = std::mt19937{ rd() };
  auto ud = std::uniform_int_distribution<>{ 1, 6 };
  for (auto i = 0; i < 20; ++i) {
      auto number = ud(mtgen);
  }
  ```

  First, we use `std::random_device` engine to produce a random number as seed. Then use it to seed another engine `std::mt19937`, which will be used by distributions later. And then define a uniform distribution to limit the range to between 1 and 6. Finally invoke the distribution with the chosen engine to produce random numbers in the range we want.

### 2.5  Creating cooked user-defined literals

+ Since C++11, we can create cooked user-defined literals with `operator""`:

  ```c++
  constexpr size_t operator"" _KB(const unsigned long long size) { 
      return static_cast<size_t>(size * 1024); 
  } 
  
  auto size{ 4_KB };  // size_t size = 4096;
  auto buffer = std::array<byte, 1_KB>{};
  ```

+ There are some points to mention:

  + For integral type, the argument needs to be `unsigned long long` and for floating-point type, it needs to be `long double`, i.e. literals should handle the largest possible values.
  + It's recommended to define the literal operator in a separate namespace and then `using` it to avoid name collision.
  + It's also recommended to prefix the user-defined suffix with an underscore (`_`) to avoid conflict with standard literal suffix introduced in C++14 (such as `s`, `min` and so on).

### 2.6  Creating raw user-defined literals

+ Raw literal operators, as **fallbacks** of cooked literal operators, accept a string of char as parameter:

  ```c++
  T operator "" _suffix(const char*); 
  template<char...> T operator "" _suffix();
  ```

### 2.7  Using raw string literals to avoid escaping characters

+ Raw string literals has two forms:

  ```c++
  R"( literal )"
  R"delimiter( literal )delimiter"
  ```

  The principle is what you see is what you get, e.g.:

  ```c++
  auto sqlselect { 
            R"(SELECT * 
            FROM Books 
            WHERE Publisher='Paktpub' 
            ORDER BY PubDate DESC)"s
  };
  ```

  even the `\n` will be included in the string.

##### Last-modified date: 2020.11.4, 9 p.m.