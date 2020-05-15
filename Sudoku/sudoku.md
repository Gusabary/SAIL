# Sudoku development notes

+ 函数的入参有默认值时，写在函数声明处就可以了（如果有声明的话），不用写在函数定义的地方。

+ C++ 11 提供 `std::stoi()` 将 string 转成 int

+ 用 using 定义结构体类型别名时，结构体最好不要是匿名的，否则使用该类型的指针或引用时可能会出现 `declared using anonymous type, is used but never defined` 这样的错误。

  ```c++
  using point_t = struct point_t {
      int x;
      int y;
  };
  ```

##### Last-modified date: 2020.5.15, 3 p.m.