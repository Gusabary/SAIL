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

+ **A bug!** `std::string::compare` returns 0 if equal and that should be `truecolor` instead of `trueColor`. `screen/terminal.cpp:48` (Already fixed now)

+ Add prefix `U` to form a `wchar_t` literal and add prefix `L` to form a `wstring` literal.

+ `Receiver` is essentially a data sink while `Sender` is essentially a data source.

+ `TabContainer` uses external `selected` (e.g. a `Toggle` component) to indicate which child is active.

  So if you don't want that arrow keys can change focus between components inside a container, use `TabContainer`.

  And the most important, `TabContainer` only renders the active child component.

+ what's the meaning of `SELECTED` in `Requirement`? 

  It seems that the relationship of `Focus` and `Select` is similar to the one of `Focused` and `Active`. In another word, `Select` or `Active` means an active child of its parent (the parent may not be active) while `Focus` means all of its ancestors are active.

+ `Component` renders to an `Element` while `Element` renders to `Screen`.

+ `Node` provides three virtual methods that could be overriden: `ComputeRequirement`, `SetBox` and `Render`.

+ TODO: read code of hbox's `ComputerRequirement` and `SetBox`

+ `Container` manages inheritance of components for us. If we don't put buttons in a container, they won't have parent component (because default constructor for `Button` doesn't set parent), which means they are all *focused* and that's not what we want.

+ Choose a component as the top component and pass it to `ScreenInteractive`. Whenever there is an event, the top component's `OnEvent` will be invoked and such invocation will propagate along the inheritance chain.

##### Last-modified date: 2020.12.22, 9 p.m.