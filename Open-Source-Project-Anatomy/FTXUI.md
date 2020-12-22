# FTXUI

**commit: 3a3ec1 2020.12.21**

+ Use `git rev-list --count HEAD` to get number of commits, which is used by FTXUI as patch number.

+ Use this to get dimension of terminal:

  ```c++
  winsize w;
  ioctl(STDOUT_FILENO, TIOCGWINSZ, &w);
  cout << w.ws_col << endl << w.ws_row << endl;
  ```

  `screen/terminal.cpp:32`

+ **A bug!** `std::string::compare` returns 0 if equal and that should be `truecolor` instead of `trueColor`. `screen/terminal.cpp:48`

+ Add prefix `U` to form a `wchar_t` literal and add prefix `L` to form a `wstring` literal.

##### Last-modified date: 2020.12.22, 9 p.m.