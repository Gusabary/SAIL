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

### 2.8  Creating a library of string helpers

+ One thing worth noting is that return value of `remove()` algorithm is the first iterator after the new range, so an extra `erase()` is needed:

  ```c++
  std::string str = "Text with some   spaces";
  str.erase(std::remove(str.begin(), str.end(), ' '), str.end());
  ```

### 2.9  Verifying the format of a string using regular expressions

+ Use a regular expression to match against a string:

  ```c++
  auto pattern {R"(^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$)"s};
  auto rx = std::regex{pattern};
  auto valid = std::regex_match("marius@domain.com"s, rx);
  ```

+ When constructing the `std::regex`, we can specify some extra options. e.g. to ignore letter case:

  ```c++
  auto rx = std::regex{pattern, std::regex_constants::icase}; 
  ```

+ Actually `std::regex_match()` has several overloads, among which there is one to return the matched subexpressions:

  ```c++
  auto rx = std::regex{R"(^([A-Z0-9._%+-]+)@([A-Z0-9.-]+)\.([A-Z]{2,})$)"s};
  auto result = std::smatch{}; 
  auto success = std::regex_match(email, result, rx); 
  ```

  Note that three pairs of parentheses in the regular expressions, which indicates the subexpression needed to match. After calling `std::regex_match()`, the matching results can be queried from the `std::smatch`:

  ```c++
  cout << result[0].str() << endl;  // the entire expression
  cout << result[1].str() << endl;  // subexpression 1
  cout << result[2].str() << endl;  // subexpression 2 
  cout << result[3].str() << endl;  // subexpression 3
  ```

### 2.10  Parsing the content of a string using regular expressions

+ Just like `std::regex_match()`, we can use `std::regex_search()` to parse the content of a string:

  ```c++
  auto match = std::smatch{}; 
  if (std::regex_search(text, match, rx)) { 
      std::cout << match[1] << '=' << match[2] << std::endl;
  }
  ```

+ However, `std::regex_search()` just performs a one-time search, i.e. it won't iterate over the string to find all substrings that match. To solve this, we could use `std::sregex_iterator` or `std::sregex_token_iterator`:

  ```c++
  auto end = std::sregex_iterator{}; 
  for (auto it = std::sregex_iterator{ std::begin(text), std::end(text), rx }; 
      it != end; ++it) { 
      std::cout << (*it)[1] << '=' << (*it)[2] << std::endl; 
  }
  ```

### 2.11  Replacing the content of a string using regular expressions

+ Use `std::regex_replace()` to replace the content of a string. The parameters of it are as follows:

  + the input string on which the replacement will be performed,
  + a `std::basic_regex` that is used to match against,
  + the string format that is used to replace,
  + and some flags.

  ```c++
  auto text{ "bancila, marius"s }; 
  auto rx = std::regex{ R"((\w+),\s*(\w+))"s }; 
  auto newtext = std::regex_replace(text, rx, "$2 $1"s);
  ```

+ The last two parameters are worth mentioning. The string format can use a match identifier to indicate a substring. e.g. `$1` means the first subexpression matched, `$&` means the entire match, `$'` means the substring after the last match and so on.

  And as the last parameter, the flags can be something like `std::regex_constants::format_first_only`, which means *just replace once*.

### 2.12  Using string_view instead of constant string references

+ C++17 introduces `std::string_view`, which is a **non-owning** (doesn't manage lifetime of the data) **constant** (cannot modify) reference to a string, to solve the problem of performance cost due to temporary string objects.

  `std::string_view` provides interfaces which are almost the same with `std::string` so typically we can almost always replace `const std::string &` with `std::string_view` unless a `std::string` is indeed needed.

+ Essentially `std::string_view` just holds a **pointer** to the start position of the character sequence and a **length** of it. 

  It provides `remove_prefix()` and `remove_suffix()` methods to resize the range.

+ `std::string_view` can be constructed from a `std::string` and vice versa.

## Chapter 3  Exploring Functions

### 3.1  Defaulted and deleted functions

+ `=default;` can be applied to special class member functions while `=delete;` can be applied to any function.

  ```c++
  template <typename T> 
  void run(T val) = delete; 
  
  void run(long val) {}  // can only be called with long integers
  ```

+ About the rules of auto generation of special class member functions, see *[Item 17, Effective Modern C++](http://gusabary.cn/2020/05/22/Effective-Modern-C++-Notes/Effective-Modern-C++-Notes(3)-Moving-to-Modern-C++/#Item-17-Understand-special-member-function-generation)*.

### 3.2  Using lambdas with standard algorithms

+ Essentially lambdas are syntactic sugar of unnamed function objects, whose copy and move constructor and destructor are defaulted and assign operators are deleted.

+ Note that lambda cannot (but I had a try, it seems not the case?) capture variables with static storage duration (i.e. variables defined in namespace scope or with `static` specifier).

  And also, lambda cannot capture `this` by reference.

+ Lambdas have several kinds of specifier such as `mutable`, `constexpr` and so on.

### 3.3  Using generic lambdas

+ Generic lambdas have `auto` as its parameters, which is essentially a function object with a template as `operator()`:

  ```c++
  [](auto const s, auto const n) { return s + n; };
  
  // is syntactic sugar to
  struct __lambda_name__ 
  { 
      template<typename T1, typename T2> 
      auto operator()(T1 const s, T2 const n) const { return s + n; } 
  
      __lambda_name__(const __lambda_name__&) = default; 
      __lambda_name__(__lambda_name__&&) = default; 
      __lambda_name__& operator=(const __lambda_name__&) = delete; 
      ~__lambda_name__() = default; 
  };
  ```

### 3.4  Writing a recursive lambda

+ Lambdas can also be recursive (even though it's rarely used):

  ```c++
  std::function<int(int const)> lfib = [&lfib](int const n) { 
      return n <= 2 ? 1 : lfib(n - 1) + lfib(n - 2); 
  }; 
  auto f10 = lfib(10); 
  ```

+ There are some points to mention since the lambda itself is captured by reference:

  + `std::function` needs to be used here instead of `auto` because it's required that compiler knows the type of the lambda when capturing it;
  + the lambda itself cannot be captured by value, because at the time of capture, the lambda is an incomplete type yet, whose `operator()` will throw a `std::bad_function_call` when invoked.

### 3.5  Writing a function template with a variable number of arguments

+ Before C++11, we can only write some functions accepting a variable number of arguments with **variadic macros** (something related to `va_list`, `va_begin`, `va_end`, etc.) and there is no way to create classes with variable number of members.

  While after C++11, both can be done with **variadic templates**.

+ One thing worth noting is that we can use `std::cout << __PRETTY_FUNCTION__ << std::endl` to get the signature of a function with its substituted template parameters:

  ```c++
  template<typename T>
  void f(T t) {
      std::cout << __PRETTY_FUNCTION__ << std::endl;
      // void f(T) [with T = int]
  }
  ```

### 3.6  Using fold expressions to simplify variadic function templates

+ Fold expressions, which is introduced since C++17, essentially applies a binary function to a range of values to produce a single result. Basically it has four forms:

  + `(... op pack)`
  + `(init op ... op pack)`
  + `(pack op ...)`
  + `(pack op ... op init)`

  If the ellipses appears in the left of `pack`, it means a left-folding (like `((1+2)+3)`) and with `init`, it's allowed to provide an initial value.

### 3.7  Implementing higher-order functions map and fold

+ A higher function is one that takes other functions as parameters and applies them to a range of values. Some common examples of higher-order functions include **map** and **fold**.

+ Map is to say apply a transform function to a range and produce a new range of data, which can be implemented with `std::transform()`.

  Fold is to say apply a combining function to a range and produce a single result, which can be implemented with `std::accumulate()` or variadic templates.

### 3.8  Composing functions into a higher-order function

+ A fancy application of variadic template is to create **composed** functions:

  ```c++
  template <typename F, typename G> 
  auto compose(F&& f, G&& g) {  
      return [=](auto x) { return f(g(x)); }; 
  }
  
  template <typename F, typename... R> 
  auto compose(F&& f, R&&... r) { 
      return [=](auto x) { return f(compose(r...)(x)); }; 
  }
  ```

  Now we could use `compose()` to compose functions into a single one:

  ```c++
  auto n = compose( 
      [](int const n) {return std::to_string(n); }, 
      [](int const n) {return n * n; }, 
      [](int const n) {return n + n; }, 
      [](int const n) {return std::abs(n); }
  )(-3);  // n = "36"
  ```

  And we can even overload the `operator*` to write like `f * g` instead of  `compose(f, g)`.

### 3.9  Uniformly invoking anything callable

+ Callable has many forms such as function pointer, functor and lambda. It's convenient for library writers to invoke them in a uniformed way, and `std::invoke()` comes in C++17.
+ Actually, `std::invoke()` can not only invoke a callable, but also get the value of a data member. To be more precise,  suppose `std::invoke()` has form of `std::invoke(f, arg1, arg2...)`,
  + if `f` is a pointer to a member function, then `arg1` is treated like `this` pointer;
  + if `f` is a pointer to a data member, then there should be only a single `arg1` (no more `argN`) and it's treated like `this`, whose `f` member is the return value;
  + if `f` is other callable, then this call to `std::invoke()` is equivalent to `f(arg1, arg2...)`

## Chapter 4  Preprocessor and Compilation

### 4.1  Conditionally compiling your source code

+ Use `#if` (`#ifdef`, `#ifndef`), `#elif`, `#else` and `#endif` to perform conditional compilation. Note that identifies checked by `#ifdef` can not only defined by `#define`, but also `-D` command line option when compiling.

+ Compilers predefine some macros indicating the compiler and platform like `__GNUG__`, `__amd64__` and so on.

+ `defined` operator can be used in `#if` and `#elif` directives: 

  ```c++
  #if defined(a)  // equivalent to #ifdef a
  #if !defined(a)  // equivalent to #ifndef a
  ```

### 4.2  Using the indirection pattern for preprocessor stringification and concatenation

+ The **stringification** is to turn the argument of a function-like macro into string with `operator#`. The **concatenation** is to concatenate two arguments of a function-like macro together with `operator##`.

  The **indirection pattern** is to say that we'd better not just define a single function-like macro. Instead we should wrap it with another macro:

  ```c++
  // stringification
  #define _MAKE_STR(x) #x
  #define MAKE_STR(x) _MAKE_STR(x)
  
  // concatenation
  #define _MERGE(x, y)  x##y
  #define MERGE(x, y) _MERGE(x, y)
  ```

+ So why? Here is the point: arguments of function-like macro are expanded (if they are also macros) first before they are substituted into the macro body, **except that they are preceded by `#` or `##`, or followed by `##`**. Consider this example:

  ```c++
  #define _MAKE_STR(x) #x
  #define MAKE_STR(x) _MAKE_STR(x)
  
  #define NUMBER 42 
  std::string s3 { MAKE_STR(NUMBER) };    // s3 = "42"
  std::string s4 { _MAKE_STR(NUMBER) };   // s4 = "NUMBER" 
  ```

  For `MAKE_STR(NUMBER)`, the `NUMBER` is expanded to `42` first before substituted because in macro `MAKE_STR`, `x` is not preceded by `#` or `##`, or followed by `##`, and then the argument for `_MAKE_STR` is already `42`. But for `_MAKE_STR(NUMBER)`, it's not the case. `x` in `_MAKE_STR` is preceded by `#` so the argument won't be expanded and the result is `"NUMBER"`.

### 4.3  Performing compile-time assertion checks with static_assert

+ Use `static_assert`, which is introduced in C++11, to perform compile-time assertion check:

  ```c++
  static_assert(condition, message)
  ```

  in which `condition` should be convertible to a bool value at compile time and `message` should be a string literal.

+ We can use `static_assert` in **namespace**, **class** and **block** (function) scope to check like whether the template arguments meet some requirements or whether a type has expected size:

  ```c++
  struct alignas(8) item { 
      int    id; 
      bool   active; 
      double value;
  }; 
  static_assert(sizeof(item) == 16, "size of item must be 16 bytes");
  
  template <typename T> 
  class pod_wrapper { 
      static_assert(std::is_pod<T>::value, "POD type expected!"); 
      T value; 
  };
  ```

### 4.4  Conditionally compiling classes and functions with enable_if

+ When talking about `std::enable_if`, we need to know what *SFINAE* is (aha, I know). Generally speaking, when compilers encounter a function call, it will build a set of possible overloads and then select the best match, in which process the *SFINAE* principle takes effect. To be more precise, when an overload is found inappropriate to be a candidate (substitution failure), the compiler will just count it out of the overload set instead of throwing an error. (What really leads to an error is an empty overload set)

+ Actually the implementation of `std::enable_if` is extremely simple:

  ```c++
  template<bool Test, class T = void> 
  struct enable_if {}; 
  
  template<class T> 
  struct enable_if<true, T> { 
      typedef T type; 
  };
  ```

  By contrast the usage of `std::enable_if` is a little bit verbose:

  ```c++
  template<typename T, 
      typename = typename std::enable_if<std::is_integral<T>::value, T>::type
  > 
  auto mul(T const a, T const b) { 
      return a * b; 
  }
  ```

+ One thing worth noting is that we can use **dummy template parameter** to create more than one overloads with type preconditions and avoid redefinition error:

  ```c++
  template <typename T, 
      typename = typename std::enable_if<std::is_integral<T>::value, T>::type
  >
  auto compute(T const a, T const b) { 
      return a * b; 
  } 
  
  template <typename T, 
      typename = typename std::enable_if<!std::is_integral<T>::value, T>::type, 
      typename = void
  > 
  auto compute(T const a, T const b) { 
      return a + b; 
  }
  ```

### 4.5  Selecting branches at compile time with constexpr if

+ C++17 introduces constexpr if `if constrexpr`, which can select branches at compile time. It can be used to simplify the code written with `std::enable_if`:

  ```c++
  template <typename T> 
  auto compute(T const a, T const b) { 
      if constexpr (std::is_integral<T>::value) 
          return a * b; 
      else  
          return a + b; 
  }
  ```

  *recall the problem dgy encountered in 2020.7.20: how to "specialize" a method of a class template*

+ Return statements in discarded constexpr if branches won't contribute to the deduction of return type.

### 4.6  Providing metadata to the compiler with attributes

+ C++11 provides a standard way (instead of something like `__attribute__((...))` in gcc or `__declspec()` in vc++) to provide metadata to the compiler: **attributes**.
+ Attributes are wrapped by two pairs of brackets like `[[attr]]`. They can work on almost everything: type, function, variable, or even translation unit.
+ Here are some useful attributes:
  + `[[noreturn]]` indicates a function does not return;
  + `[[nodiscard]]` indicates return value of the function shouldn't be discarded (i.e. should be assigned to a variable)
  + `[[deprecated("reason")]]` indicates that the entity is deprecated and shouldn't be used any longer.

## Chapter 5  Standard Library Containers, Algorithms and Iterators

### 5.1  Using vector as a default container

+ Use `std::vector` as the default container unless there is a good reason to use another one. That's because `std::vector` has many benefits, among which it's compatible with C-like arrays (just invoking `data()` method to get the address of the first element)
+ One thing worth noting is that the capacity expansion sounds time-consuming, but actually each element can be regarded as moved just once due to the exponential expansion policy. Clearly speaking, you have four elements, here comes the fifth one and it needs to find a larger space, which can contain eight elements. So we can say space for another four elements is acquired after a move of four elements, which is a one-on-one relationship.

### 5.2  Using bitset for fixed-size sequences of bits

+ `std::bitset` can be used for a fixed-size sequence of bits, whose template parameter is the size.

+ `std::bitset` can be converted from integral type and string with its constructors and converted to them with its methods:

  ```c++
  std::bitset<8> b2{ 10 };  // [0,0,0,0,1,0,1,0]
  std::bitset<8> b3{ "1010"s };  // [0,0,0,0,1,0,1,0]
  
  b2.to_ulong();  // 10
  b3.to_string();  // "00001010"
  ```

+ Bits in `std::bitset` can be tested by some methods like `any()`, `all()`, `none()`, `test()` and so on, and can be modified by something like `flip()`, `set()` and `reset()`. Also, `std::bitset` supports operation like `|`, `&` and `^`.

### 5.3  Using `vector<bool>` for variable-size sequences of bits

+ `std::vector<bool>` is essentially a specialization of `std::vector<T>` with speed and space optimization: **it store a single bit instead of a bool value (1 byte) for each element**.

  However, it also brings some drawbacks like the element cannot be referenced, and its iterator cannot be used as a forward iterator:

  ```c++
  std::vector<bool> bv;
  bv.resize(10);
  auto& bit = bv[0];      // error
  ```

+ If you don't want such optimization, `std::deque<bool>` might be a good choice.

### 5.4  Finding elements in a range

+ [STL Algorithms](http://gusabary.cn/2020/03/14/C++STL%E4%B8%AD%E7%9A%84Algorithm/) provides many generic functions to find elements in a range, most of which has return value of the end iterator to indicate the not found result.

### 5.5  Sorting a range

+ When using sorting function provided by STL Algorithms, pass in a functor to customize your own compare function:

  ```c++
  std::vector<int> v{3, 13, 5, 8, 1, 2, 1};
  std::sort(v.begin(), v.end());  // v = {1, 1, 2, 3, 5, 8, 13}
  std::sort(v.begin(), v.end(), std::greater<>());  // v = {13, 8, 5, 3, 2, 1 ,1}
  ```

+ `std::is_sort()` can be used to check whether a range of elements is already sorted.

+ Some containers like `std::list` provide `sort()` as their methods, which should be preferred to the `std::sort()`.

### 5.6  Initializing a range

+ STL Algorithms also provide a series of functions to fill (initialize) elements in a range. Most basically, we can use `std::fill` to fill a range with a given value and `std::generate` to fill a range with the return value of a given callable. (The callable accepts no parameter. If you want to use the original value in the container, resort to `std::transform`) 

  And also, we can use `std::iota` to fill a range with sequentially increasing values (actually it's the `operator++` that gets invoked).

### 5.7  Using set operations on a range

+ Note that set operations like `std::set_union`, `std::set_intersection`, `std::set_difference` and so on should be used on sorted containers (not necessarily a set, vector is ok as long as it's sorted)

### 5.8  Using iterators to insert new elements in a container

+ When using some STL Algorithms like `std::fill_n` to insert a range of elements into a container, we couldn't use some *common* iterators. For example, we couldn't use the iterator returned by `std::begin()`, because the following fill operation will overwrite the existing elements rather than insert. And we also couldn't use the iterator returned by `std::end()`, because the compiler will complain something about out of range error.

  Yep, we can resort to the *insert iterators*, i.e. iterators returned by `std::back_inserter()`, `std::front_inserter()` and `std::inserter()`.

  ```c++
  std::vector<int> v{ 1, 2, 3, 4, 5 };
  std::fill_n(std::inserter(v, std::next(v.begin(), 2)), 3, 0);
  // 1, 2, 0, 0, 0, 3, 4, 5
  ```

+ They are all **output iterators** and increasing or dereferencing these iterators actually do nothing. However, upon assignment, they will invoke some methods of the container separately:

  + `push_back()` for `std::back_insert_iterator`,
  + `push_front()` for `std::front_insert_iterator`,
  + `insert()` for `std::insert_iterator`.

  So this places some limits to containers that insert iterators can work on. e.g. `std::back_insert_iterator` doesn't apply to `std::forward_list` because it doesn't have `push_bakc()` method. For the same reason. `std::front_insert_iterator` doesn't apply to `std::vector`.

+ Note that these insert iterators are preferred when inserting a **range** of elements instead of a single one. For the latter situation, `push_back()`, `push_front()` and `insert()` methods are certainly the first choice.

### 5.9  Writing your own random access iterator

+ From input iterators to random-access iterators, more functionalities are supported:
  + Common requirements of all iterators: copy-constructible, copy-assignable, destructible and can be incremented.
  + Input iterators: support equality comparison and can be dereferenced as rvalue.
  + Output iterators: can be dereferenced as lvalue.
  + Forward iterators: can be default constructed and support multi-pass iteration.
  + Bidirectional iterators: can be decremented.
  + Random-access iterator: support arithmetic operation, offset dereference operator (`[]`) and inequality comparison (`<`, `>=` and so on).
+ For more details, refer to *[the iterator doc](http://www.cplusplus.com/reference/iterator/)*.

### 5.10  Container access with non-member fucntions

+ Non-member functions about the STL container access don't only include `std::begin()` and `std::end()`, but also `std::data()`, `std::size()` and `std::empty()` as of C++17.

+ The implement logic is simple: if the container has methods of `begin()` and `end()`, just invoke them. If it's not the case, implement a specialization for it (such as the C-style array)

  ```c++
  template<class C>
  constexpr auto inline begin(C& c) -> decltype(c.begin()) {
      return c.begin();
  }
  
  template<class T, std::size_t N>
  constexpr T* inline begin(T (&array)[N]) {
      return array;
  }
  ```

## Chapter 6  General Purpose Utilities

### 6.1  Expressing time intervals with chrono::duration

+ C++11 provides a `chrono` library to deal with data and time. It mainly consists of three components: **durations** which represents a time interval, **time pointes** which represents a period of time since the epoch of a clock and **clock** which defines an epoch (start of time) and a tick rate.

+ **Duration** is essentially a class template, whose template parameters are the underlying type of the tick and the kind of the tick represented by `std::ratio` (ratio to the unit of second). For example, the standard library has defined some types for us:

  ```c++
  namespace std {
      namespace chrono {
          typedef duration<long long, ratio<1, 1000000000>> nanoseconds;
          typedef duration<long long, ratio<1, 1000000>> microseconds;
          typedef duration<long long, ratio<1, 1000>> milliseconds;
          typedef duration<long long> seconds;
          typedef duration<int, ratio<60>> minutes;
          typedef duration<int, ratio<3600>> hours;
      }
  }
  ```

+ Remember C++14 brings us some user-defined literals? Yep, `std::chrono_literals` are included. With the help of them, we can define duration like below:

  ```c++
  using namespace std::chrono_literals;
  auto half_day         = 12h;
  auto half_hour        = 30min;
  auto half_minute      = 30s;
  auto half_second      = 500ms;
  auto half_millisecond = 500us;
  auto half_microsecond = 500ns;
  ```

+ Duration with lower precision can be converted implicitly to one with higher precision while duration with higher precision should use `std::chrono::duration_cast` to convert to one with lower precision.

  Also, we can use `count()` method to retrieve the number of ticks.

### 6.2  Measuring function execution time with a standard clock

+ Use the concept of **clock** and **time points** to measure function execution time:

  ```c++
  auto start = std::chrono::high_resolution_clock::now();
  func();
  auto diff = std::chrono::high_resolution_clock::now() - start;
  ```

+ `std::chrono::time_point` is essentially a class template, whose template parameters are clock and duration (you can consider that *time_point = clock + duration*)

  And a clock defines two things: the epoch and tick rate.

+ There are three kinds of clocks: `system_clock`, `high_resolution_clock` and `steady_clock`. They are different in terms of precision and steady attribute.

  If a clock is **steady**, it means it's never adjusted, i.e. the difference between two time pointes is always positive as time passes. When measuring the function execution time, we should always use steady clocks.

### 6.3  Generating hash values for custom types

+ We know that `std::unordered_*` containers use hash table to store values, which requires the underlying type has corresponding hash function.

  To be more precise, only types which have specialization of `std::hash` class can be used as template argument of `std::unordered_*`. The standard has specialized it for all basic types and some common used types like `std::string`. When we want to use custom types, it needs to specialize for it manually:

  ```c++
  namespace std {
      template<>
      struct hash<Item> {
          typedef Item argument_type;
          typedef size_t result_type;
  
          result_type operator()(argument_type const & item) const {
              result_type hashValue = 17;
              hashValue = 31 * hashValue + std::hash<int>{}(item.id);
              hashValue = 31 * hashValue + std::hash<std::string>{}(item.name);
              hashValue = 31 * hashValue + std::hash<double>{}(item.value);
              return hashValue;
          }
      };
  }
  ```

+ Note that `std::hash` is essentially a functor template, whose `operator()` returns the same result for the same arguments and has a very small chance to return the same value for non-equal arguments.

  Here the **non-equal** doesn't necessarily means comparing all fields because some fields don't play a role in the `operator==`. So when calculating hash values, we just need to consider those fields that will determine whether two objects are equal.

+ One thing worth noting is that, we choose `31` as the multiplier. That is because `31*x` can be replaced with `(x<<5)-x`, which is advantageous for performance optimization. Other choices can be `127` and `8191`.

### 6.4  Using std::any to store any value

+ C++17 introduces `std::any` to store a value of any type, just like `Object` in JavaScript.

+ To store a value into `std::any`, use its constructor or assign operator:

  ```c++
  std::any value(42); // integer 12
  value = 42.0;       // double 12.0
  value = "42"s;      // std::string "12"
  ```

+ To read a value from `std::any`, use `std::any_cast`. Note that it can throw `std::bad_any_cast`:

  ```c++
  std::any value = 42.0;
  try {
      auto d = std::any_cast<double>(value);
      std::cout << d << std::endl;
  }
  catch (std::bad_any_cast const & e) {
      std::cout << e.what() << std::endl;
  }
  ```

+ Use `type()` method to check type of the stored value (if no, return `void`) and use `has_value()` method to check whether there is a stored value:

  ```c++
  inline bool is_integer(std::any const & a) {
      return a.type() == typeid(int);
  }
  ```

### 6.5  Using std::optional to store optional values

+ C++17 introduces `std::optional` to store an optional value that may exist or not.

+ To store a value into `std::optional`, use its constructor and assign operator:

  ```c++
  std::optional<int> v1;      // v1 is empty
  std::optional<int> v2(42);  // v2 contains 42
  v1 = 42;                    // v1 contains 42
  std::optional<int> v3 = v2; // v3 contains 42
  ```

+ To read a value from `std::optional`, use `operator*` (`operator->`) or `value()` (`value_or()`) method. The difference is that when the optional is empty, behavior of the former is undefined while the latter will throw a `std::bad_option_access`.

+ Use `has_value()` method to check if the optional is empty.

+ The common use case of `std::optional` is to be the return value indicating failure when empty.

### 6.6  Using std::variant as a type-safe union

+ C++17 introduces `std::variant` to store a value whose type is in a type set (a little bit like union)

+ To store a value into `std::variant`, use its constructor, assign operator or `emplace()` method:

  ```c++
  struct foo {
      int value;
      explicit foo(int const i) : value(i) {}
  };
  
  std::variant<int, std::string, foo> v = 42; // holds int
  v.emplace<foo>(42);                         // holds foo
  ```

+ To read a value from `std::variant`, use `std::get()` with template argument of index or type (if unique). If type of the stored value is not the specified one, a `std::bad_variant_access` will be thrown:

  ```c++
  std::variant<int, double, std::string> v = 42;
  auto i1 = std::get<int>(v);
  auto i2 = std::get<0>(v);
  
  try {
      auto f = std::get<double>(v);
  }
  catch (std::bad_variant_access const & e) {
      std::cout << e.what() << std::endl;  // Unexpected index
  }
  ```

+ Use `index()` method to retrieve index of the stored value and use `std::holds_alternatives()` to check whether the `std::variant` holds the specified type.

+ Note that `std::variant` is default initialized with its first alternative, so it needs to use `std::monostate`, which is an empty type intended to make variants default constructible, if otherwise the first type isn't default constructible.

### 6.7  Visiting a std::variant

+ Use `std::visit()` to visit a `std::variant`, which is to do some operation potentially according to the alternative type.

+ The first parameter of `std::visit()` is a callable which takes a single parameter (so-called **visitor**). The callable is required to be able to accept any alternative type of the variants, so we can use `auto&&` or functor that overloads `operator()` for all alternative types.

  And the following parameters are variants to be visited:

  ```c++
  // lambda
  for (auto const & d : dvds) {
      std::visit([](auto&& arg) {
          using T = std::decay_t<decltype(arg)>;
          if constexpr (std::is_same_v<T, Movie>) {}
          else if constexpr (std::is_same_v<T, Music>) {}
          else if constexpr (std::is_same_v<T, Software>) {}
      }, d);
  }
  
  // functor
  struct visitor_functor {
      void operator()(Movie const & arg) const {}
      void operator()(Music const & arg) const {}
      void operator()(Software const & arg) const {}
  };
  for (auto const & d : dvds) {
      std::visit(visitor_functor(), d);
  }
  ```

  When visiting a variant, the visitor will be invoked with the currently stored value.

+ A visitor isn't necessarily a non-return callable. Instead it could return the type of the second parameter of `std::visit` (which is the variant). And it seems that when the visitor has return value, `std::visit` cannot take more than two parameters.

### 6.8  Registering a function to be called when a program exits normally

+ Use `std::atexit()` and `std::at_quick_exit()` to register a function to be called when the program exits normally, which means returning from `main` function, `std::exit()` or `std::quick_exit()`.

  The function registered takes no parameter and has no return value.

+ One thing worth noting is that the order of invocation of functions registered and destruction of static objects are reverse of that they are registered and constructed:

  ```c++
  std::atexit(exit_handler_1);
  static_foo::instance();
  std::atexit(exit_handler_2);
  std::atexit([]() {std::cout << "exit handler 3" << std::endl; });
  std::exit(42);
  
  // output:
  // exit handler 3
  // exit handler 2
  // static foo destroyed!
  // exit handler 1
  ```

### 6.9  Using type traits to query properties of types

+ There are two categories of type traits:

  + query information about types like `std::is_void`, `std::is_same`, they have a static member called `value`;
  + transform properties of types like `std::add_const`, `std::remove_pointer`, they have a `typedef` called `type`.

+ Typically type traits for querying type information are implemented with full or partial specialization mechanism. To be more precise, return false in primary template and return true in specialization:

  ```c++
  template <typename T> 
  struct is_pointer 
  { static const bool value = false; };
  
  template <typename T> 
  struct is_pointer<T*> 
  { static const bool value = true; };
  ```

+ Type traits for querying can be used in many occasions such as `std::enable_if`, `static_assert`, `std::conditional` and constexpr if.

### 6.10  Writing your own type traits

+ When writing your own type traits for querying, define the `value` field in type traits as `static constexpr`

### 6.11  Using std::conditional to choose between types

+ Use `std::conditional` to choose between types at compile time. It takes three template parameters, the first one is a const bool expression at compile time and the following two are types to be chosen.

+ It can be implemented like this:

  ```c++
  template<bool _Cond, typename _Iftrue, typename _Iffalse>
  struct conditional
  { typedef _Iftrue type; };
  
  template<typename _Iftrue, typename _Iffalse>
  struct conditional<false, _Iftrue, _Iffalse>
  { typedef _Iffalse type; };
  ```

## Chapter 7  Working with files and streams

### 7.4  Using I/O manipulators to control the output of a stream

+ Standard provides many helper functions called **manipulators** to control the input and output of a stream.
+ Listed here are some common used manipulators:
  + `std::boolalpha` and `std::noboolalpha` to toggle the display of bool value between `true ` (`false`) and 1(0).
  + `std::left` and `std::right` to affect the alignment of the output (usually cooperate with `std::setw`).
  + `std::fixed`, `std::setprecision` and `std::defaultfloat` to affect the output of floating-point types.
  + `std::dec`, `std::oct` and `std::hex` to change the base used for integral types.
  + `std::setw` and `std::setfill` to format the output with some custom styles.
+ Note that all the manipulators except `std::setw` has an effect on the stream and all the following I/O operations until encountering another manipulator. (`std::setw` just affects the next single operation)

## Chapter 8  Leveraging Threading and Concurrency

## Chapter 9  Robustness and Performance

### 9.1  Using exceptions for error handling

+ There are some recommended practice when dealing with exceptions:

  + throw exceptions by value and catch it by reference (in most cases, const reference).
  + arrange multiple catch statements in the order from most derived to the base, and `catch(...)` as the last one potentially.
  + use `throw;` directly in the catch statement to rethrow the exception.

+ When we combine the concept of both exception and OOP, there is a key point to mention: Use exceptions to indicate errors in **constructors** and don't let exceptions leave **destructors**.

  The reason is simple: when an exception occurs, **the stack is unwound from the point where the exception was thrown to the point where it will be handled**. This unwinding process involves destruction of all local objects in these stack frames. If the destructor of one object throws an exception, another unwinding process should begin, which conflicts the one already under way.

  And one thing worth noting is that after an exception is thrown from the constructor, the destructor won't get invoked.

+ Standard library provides some exceptions like `std::logic_error`, `std::runtime_error` and `std::bad_*`, which are derived from `std::exception`.

### 9.2  Using noexcept for functions that do not throw

+ C++11 introduces `noexcept` keyword, which can be used as either a **specifier** and an **operator**.

  As a specifier, it applies to a function, indicating that function doesn't throw any exception. Optionally, `noexcept` can take a parameter which can be evaluated to a bool value. To be precise, `noexcept(true)` is equivalent to `noexcept` while `noexcept(false)` means no exception specification.

+ As an operator, it can be used to check whether a function is `noexcept` at compile time:

  ```c++
  void f() noexcept;
  std::cout << noexcept(f()) << std::endl;  // 1
  ```

  Note that the expression used for check is **not evaluated**.

+ Be careful to specify a function as `noexcept`. On the one hand, if exceptions leave a function that is specified as `noexcept`, the program will exit immediately with a call to `std::terminate()`. On the other hand, a `noexcept` function can bring some performance optimizations.

  For example, `push_back()` method of `std::vector` will use move constructor if it's `noexcept` since the method has a **strong exception guarantee** (which means the state of program will stay the same after an exception is thrown, i.e. commit-or-rollback semantic). And actually indicating that move constructors don't throw is an important scenario of `noexcept`.

### 9.4  Creating compile-time constant expressions

+ `constexpr` can be used to create compile-time constant expressions. It can apply to variables, functions, constructors and so on.
+ When applying to variables, they should be [*literal type*](http://www.cplusplus.com/reference/type_traits/is_literal_type/) (e.g. `std::string` cannot be declared as `constexpr`).
+ When applying to functions, they are implicitly `inline` but not `const` since C++14.
+ When applying to constructors, we can potentially create `constexpr` objects of that class.

## Chapter 10  Implementing Patterns and Idioms

### 10.1  Avoiding repetitive if...else statements in factory methods

+ To avoid repetitive if...else statements in factory methods, we can certainly use polymorphism. However, there is another trick which uses a map:

  ```c++
  std::shared_ptr<Image> Create(std::string_view type) {
      static std::map<std::string, std::function<std::shared_ptr<Image>()>> mapping {
          { "bmp", []() {return std::make_shared<BitmapImage>(); } },
          { "png", []() {return std::make_shared<PngImage>(); } },
          { "jpg", []() {return std::make_shared<JpgImage>(); } }
      };
  
      auto it = mapping.find(type.data());
      if (it != mapping.end())
          return it->second();
      return nullptr;
  }
  ```

+ What's more, we can replace the hardcoded string indicating the type with `type_info`:

  ```c++
  std::shared_ptr<Image> Create(std::type_info const & type) {
      static std::map<std::type_info const *, std::function<std::shared_ptr<Image>()>> mapping {
          {&typeid(BitmapImage),[](){return std::make_shared<BitmapImage>();}},
          {&typeid(PngImage),   [](){return std::make_shared<PngImage>();}},
          {&typeid(JpgImage),   [](){return std::make_shared<JpgImage>();}}
      };
      
      auto it = mapping.find(&type);
      if (it != mapping.end())
          return it->second();
      return nullptr;
  }
  
  factory.Create(typeid(PngImage));
  ```

### 10.2  Implementing the pimpl idiom

+ Pimpl idiom, which stands for **pointer to implementation**, is a technique for better separation between interface and implementation, to my understanding.

  Typically, putting interfaces in header files and implementations in source files is already a practice of separation of them. However, when we change some internal state (like private members), the header still needs to recompile although the change is transparent to the client. Now with pimpl idiom, we can move *almost* all these internal state to another class called **pimpl class** and in the original **public class**, just leave interfaces to avoid recompilation when modifying such internal state.

  Practically, public class has all the interfaced defined and a pointer to pimpl class. All the implementations of public class method forward to pimpl class.

+ However, the benefits of pimpl idiom doesn't come for free. It's more difficult to read of course. And also, it cannot apply to protected members and private virtual functions.

### 10.3  Implementing the named parameter idiom

+ C++ supports only positional parameters (some other language also supports named parameter like Python but C++ is still the best programming language!!). However we can implement named parameter technique on our own.

+ To implement that, we need to create a class dedicated for the parameter passing, whose methods share the same name with parameter name and, the most important, return value is the reference to itself (`return *this`). So we can construct the argument list like a chain:

  ```c++
  control c(control_properties(1044).visible(true).height(20).width(100));
  ```

### 10.4  Separating interfaces and implementations with the non-virtual interface idiom

+ Non-virtual interface (NVI) idiom is to make all public interfaces non-virtual and private implementations virtual. Essentially it adds an indirection for better separation and control of interfaces:

  ```c++
  class Base {
  protected:
      virtual void initialize_impl() {}
  public:
      void initialize() {
          initialize_impl();
      }
  };
  
  class Derived : public Base {
  protected:
      virtual void initialize_impl() {}
  };
  ```

### 10.5  Handling friendship with the attorney-client idiom

+ If a function or class is declared as **friend** of another class, all private members of it will be exposed to its friend, which is not always what we want. This can be handled by attorney-client idiom, which make it possible to expose part of private members to friends. The principle is very simple: another indirection, which is called **attorney**:

  ```c++
  class Client {
      int data_1;
      int data_2;
      void action1() {}
      void action2() {}
      friend class Attorney;
  };
  
  class Attorney {
      static inline void run_action1(Client& c) {
          c.action1();
      }
      static inline int get_data1(Client& c) {
          return c.data_1;
      }
      friend class Friend;
  };
  
  class Friend {
  public:
      void access_client_data(Client& c) {
          Attorney::run_action1(c);
          auto d1 = Attorney::get_data1(c);
      }
  };
  ```

### 10.6  Static polymorphism with the curiously recursive template pattern

+ Polymorphism based on virtual functions are called runtime polymorphism or late binding. We can simulate it at compile time with static polymorphism (or early binding), using the mechanism **curiously recursive template pattern (CRTP)**:

  ```c++
  template <class T>
  class Base {
  public:
      void draw() {
          static_cast<T*>(this)->paint();
      }
  };
  
  class Derived : public Base<Derived> {
  public:
      void paint() {}
  }
  ```

+ Note that there is a pitfall when using CRTP: different derived classes with the same base (e.g. `Base<A>` and `Base<B>`) have different types (unlike the runtime polymorphism) so we cannot store them in a homogeneous container such as vector or list. Of course there is a workaround: inherit the base class from another class and use that *base-base* class as the template argument of the container.

### 10.7  Implementing a thread-safe singleton

+ After C++11, the compiler guarantees that objects with static storage will be initialized only once. Remember [the threes ways to initialize objects thread-safely in *Section 2.2, Concurrency with Modern C++*](http://gusabary.cn/2020/10/30/Concurrency-with-Modern-C++-Notes/Concurrency-with-Modern-C++-Notes-2/#2-2-Shared-Data)?

  So implementing a thread-safe singleton is quite easy. We can even combine it with CRTP introduced in last section to create a generic singleton base class:

  ```c++
  template <class T>
  class SingletonBase {
  protected:
      SingletonBase() {}
  public:
      SingletonBase(SingletonBase const &) = delete;
      SingletonBase& operator=(SingletonBase const&) = delete;
      static T& instance() {
          static T single;
          return single;
      }
  };
  
  class Single : public SingletonBase<Single> {
      Single() {}
      friend class SingletonBase<Single>;
  };
  ```

##### Last-modified date: 2020.11.22, 3 p.m.