# C++17 STL Cookbook Notes

## Chapter 1  The New C++17 Features

### 1.1  structured bindings

+ Structured bindings can be used to unpack pair, tuple, **struct** and even **array of fixed size**.
+ Before C++17, there is a `std::tie` which has similar functionality. What's more, combined with `std::tie`, we can use `std::ignore` to represent a placeholder for uninterested field.

### 1.2  if and switch with initializers

+ Define variables which will only be used in if scope in the if-initializer:

  ```c++
  if (auto it = m.find(c); it != m.end()) {
      // ...
  }
  // it is not available now
  ```

  the same with switch statement.

+ Essentially, it's just a syntactic sugar of

  ```c++
  {
      auto it = m.find(c);
      if (it != m.end()) {
          // ...
      }
  }
  ```

### 1.3  bracket initializer with auto

+ When using bracket initializer to initialize an auto object, here comes the rule:
  + `auto var {arg}` deduces `var` to be the same type with `arg`
  + `auto var {arg1, arg2, ...}` is invalid and doesn't compile
  + `auto var = {arg1, arg2, ...}` deduces `var` to be `std::initializer_list<T>` if all `args` have the type `T`.

### 1.4  class template argument deduction (CTAD)

+ With CTAD, we can define a class template without specifying template arguments if the constructor arguments can help deduce them:

  ```c++
  std::pair my_pair (123, "abc");       // std::pair<int, const char*>
  ```

+ Actually compiler has implicit deduction guides, which sometimes cannot satisfy our requirement:

  ```c++
  template <typename T>
  struct sum {
      T value;
  
      template <typename ... Ts>
      sum(Ts&& ... values) : value{(values + ...)} {}
  };
  
  sum s {1, 2, 3, 4};
  ```

  Compiler cannot deduce `T` from argument list `Ts: int, int, int, int`, so we need a **explicit deduction guide**:

  ```c++
  template <typename ... Ts>
  sum(Ts&& ... ts) -> sum<std::common_type_t<Ts...>>;
  ```

  to help compiler perform the deduction.

+ *(reference)[https://en.cppreference.com/w/cpp/language/class_template_argument_deduction]*

### 1.5  constexpr if

+ Different from `#if`, all branches in constexpr if have to be **syntactically well-formed** (but not necessarily **semantically valid**).

### 1.6  inline variables

+ Declare variables as inline to avoid multiple definition error during link stage:

  ```c++
  class process_monitor { 
  public: 
      static const inline std::string standard_string 
          {"some static globally available string"}; 
  };
  
  inline process_monitor global_process_monitor;
  ```

  which makes it easier to develop header-only library.

### 1.7  fold expressions

+ Use fold expressions to calculate on arguments of variadic template in a single line of code.

+ Note that fold expressions can not only work on arguments pack itself, but also expression including the pack:

  ```c++
  template <typename T, typename ... Ts>
  bool insert_all(T &set, Ts ... ts)
  {
      return (set.insert(ts).second && ...);
  }
  ```

  In another word, it can be considered that because `ts` is a pack, the whole `set.insert(ts).second` also becomes a pack.

## Chapter 2  STL Containers

+ **erase-remove idiom**: `std::remove` just move around elements in the container and return the **new end iterator** so we need to invoke `erase` method again to erase in fact those elements after the new end iterator:

  ```c++
  v.erase(remove_if(begin(v), end(v), odd), end(v));
  ```

+ if the order of a vector doesn't matter, when deleting an item by index or iterator, we can overwrite it with the last item and then erase the last one, so the deletion is O(1)

+ compared to `operator[]`, we can use `at` method to perform bound check, and the price is a negligible performance loss.

+ use `lower_bound` to get the right position for a new item in a sorted container and then perform insertion to keep it always sorted

+ after C++17, we can use `try_emplace` method of map to **try** inserting an entry into a map, that's to say, if the key exists, the inserted object won't be constructed, which saves really much time for us, compared to old `insert` and `emplace`.

+ `insert` method of map provides an overload which takes an iterator as a hint to denote the position where the inserted item should be. If the hint is correct (this can be easily checked through whether the newly inserted item and the hint is direct neighbor), insertion performance can be improved.

+ it's a fact that type of key in a map is const, so we cannot directly change it. However, after C++17, map supports `extract` method to extract (remove and get) a node from map, whose `key` method returns a non-const reference to key of the node. so we can modify that and then reinsert it to map without any performance penalty (especially heap allocation)

+ actually `std::unordered_map`'s complete definition is:

  ```c++
  template<
      class Key,
      class T,
      class Hash      = std::hash<Key>,
      class KeyEqual  = std::equal_to<Key>,
      class Allocator = std::allocator< std::pair<const Key, T> >
  > class unordered_map;
  ```

  if `Key` is a custom type, we can provide `Hash` in two ways:

  + specialize `std::hash` for our type
  + specify `Hash` template parameter explicitly

+ `std::istream_iterator` takes a template parameter as token type and a `std::istream` as the source stream. it supports two operations: 1) `*it` to get the current token from the stream (equivalent to `cin >>`) and 2) `++it` to jump to next token.

  `std::insert_iterator` performs kinda like iterator but when assigning value to the element it points to, it *inserts* a new value.  use `std::inserter` to get a `std::insert_iterator`, it takes two arguments: the container and the iterator pointing to where the new element should be inserted.

  ```c++
  set<string> s;
  istream_iterator<string> it {cin};
  istream_iterator<string> end;
  copy(it, end, inserter(s, s.end()));
  ```

+ move entries in a map into a vector for sorting by value instead of key:

  ```c++
  map<string, size_t> words;
  vector<pair<string, size_t>> word_counts;
  word_counts.reserve(words.size());
  move(begin(words), end(words), back_inserter(word_counts));
  ```

  use `back_inserter` here because `std::vector` has only `push_back` but no `push_front`.

+ `std::priority_queue` is also an container adapter, which wraps on `std::vector` by default. and priority queue is logically implemented by heap.

## Chapter 3  Iterators

+ it's recommended to declare constructor which create a type from another type as explicit to avoid implicit type conversion.

+ to make our own iterators compatible with STL algorithm, we need to activate **iterator trait** functionality for it, i.e. specialize `std::iterator_traits` for our own iterator class and populate some type definitions like `iterator_category`, `value_type` and so on.

+ something like `std::insert_iterator` and `std::istream_iterator` is called **iterator adapter**, it can wrap an object into an iterator which can perform some special operation on the object when dereferenced, assigned, increased or whatever.

+ use `std::make_reverse_iterator` to get `rbegin` from `end`.

+ since C++17, there is no constraint that `begin` and `end` iterator should be the same type in range-based loop syntactic sugar, so we can use a **iterator sentinel** as end iterator when it's not that easy to determine a real end iterator.

+ gcc (and clang) provides some sanitizers to check whether STL iterators are correctly used. enable these sanitizers by passing some flags during compilation.

+ `std::valarray` (since C++98) supports element-wise mathematical operations.

  range in C++20 can make cpp code more function-programming like.

## Chapter 4  Lambda Expressions

+ since C++14, we can initialize new variables in the capture list like this:

  ```c++
  [count = 0] () mutable { return ++count; }
  ```

+ lambda without capturing any variable has the same (more precisely, not the complete same, just convertible) type with corresponding function pointer, but those which has non-empty capture list cannot be represented by function pointer:

  ```c++
  int a;
  std::vector<void (*)(int)> v;
  v.push_back([](int) {});    // ok
  v.push_back([a](int) {});   // error
  ```

  so `std::function` comes into play.

+ std library has provided some logical conjunction functor for us like `std::logical_and<>`.

## Chapter 5  STL Algorithm Basics

+ by default, `std::is_sorted` will return false for vector with descending elements like 3, 2, 1.

+ we can provide comparison function whose signature has form like `bool(const T&, const T&)` as the third argument of `sort` algorithm. note that this function shouldn't have any side effect.

+ `std::remove` and `std::replace` algorithms have their `_copy` counterpart which leave the source container unaltered.

+ `std::copy` and `std::transform` could have source iterator and destination one of different types. e.g. copy elements to stdout:

  ```c++
  copy(begin(vs), end(vs), ostream_iterator<string>{cout, "\n"});
  ```

+ `std::binary_search` and `std::equal_range` (also `std::lower_bound` and `std::upper_bound`) use binary search on the container so the elements are required to be sorted.

+ `std::minmax_element` and `std::clamp` can be useful.

+ since C++17, we can provide **searcher** for `std::search` algorithm, which might bring some performance improvement.

+ C++17 provides a `std::sample` to sample limited number of elements from a long container.

##### Last-modified date: 2021.3.18, 9 p.m.