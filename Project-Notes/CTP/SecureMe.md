# SecureMe Development Notes

+ `waitpid(pid, &status, 0)` 的第二个参数用来标识导致 waitpid 返回的子进程的状态，一般而言这是一个 32 位的量（也有例外，比如 ptrace event 会用到第五个字节来标识 event），有以下四种情况：

  + 子进程正常退出，例如 exit，此时 status 的结构是 `xxxx xxxx y000 0000`，前八位表示 exit code

    `WIFEXITED` 宏用来判断是否为该情况，`WEXITSTATUS` 宏用来获取 exit code

  + 子进程由于收到信号而终止，此时 status 的结构是 `0000 0000 xyyy yyyy`，后七位不能全为 0 或全为 1，且后七位表示导致终止的信号值，此外 x 为 1 的话表示 core dumped

    `WIFSIGNALED` 宏用来判断是否为该情况，`WTERMSIG` 宏用来获取信号值，`WIFSIGNALED` 宏的实现很精彩：

    ```c
    #define __WIFSIGNALED(status) (((signed char) (((status) & 0x7f) + 1) >> 1) > 0)
    ```

    如果后七位全为 0，与 `0x7f` 取与后再加 1，就变成 `0x01`，再右移一位就是 0，返回 false

    如果后七位全为 1，与 `0x7f` 取与后再加 1，就变成 `0x80`，重点在于类型转成了 signed char，右移的话 `1000 0000` 就成了 `1100 0000`（如果是 unsigned 就是 `0100 0000`），符号位是 1，为负，返回 false

  + 子进程由于收到信号而暂停，此时 status 结构是 `xxxx xxxx 0111 1111`，前八位表示收到的信号值

    `WIFSTOPPED` 宏用来判断是否为该情况，`WSTOPSIG` 宏用来获取信号值。

    一般出现这种情况是因为使用了 ptrace 或者 waitpid 的第三个参数指定了 `WUNTRACED`

  + 子进程由于收到 SIGCONT 信号而即将继续，此时 status 结构是 `1111 1111 1111 1111`

    `WIFCONTINUED` 宏用来判断是否为该情况

+ 调用 `execve` 实质上会加载目标程序，覆写内存中的 text，data，bss，stack，所以原先注册好的 signal handler 会失效。因为 signal handler 本质上就是说当接收到某个信号时调用位于某个地址处的函数，但是当整个 text 段都被刷了一遍后原先的地址也就没有意义了。

  不仅会重置所有 signal handler，也会清除所有 pending signal，可以理解为原先的 pending signal 可能是有某个 handler 来处理的，但是 handler 被重置了，该 pending signal 的处理方式将和预期不一致，所以就被清除了。

+ `sigaction` 作为另一种注册 signal handler 的方式，提供比 `signal` 更强大的功能：

  ```c++
  int sigaction(int signum, const struct sigaction *act, struct sigaction *oldact);
  ```

  重点在于 `sigaction` 这个结构体，其中有两个字段比较关键：`sa_flags` 和 `sa_sigaction`，当 `sa_flags` 字段包含 `SA_SIGINFO` 时，会使用 `sa_sigaction` 所指向的函数作为 signal handler，为什么说这个比较关键呢，因为 `sa_sigaction` 比 `sa_handler` 多两个入参：

  ```c++
  void (*sa_sigaction)(int, siginfo_t *, void *);
  ```

  + 第二个参数是一个 `siginfo_t` 类型的结构体，提供更多关于导致调用 handler 的 signal 的信息（普通的 `sa_handler` 只能提供 signal number 这一信息，即第一个参数）
  + 第三个参数是一个 `ucontext_t` 类型的结构体（被转换成了 `void *`），提供一些调用 handler 时刻的上下文信息，例如寄存器的值

+ 使用 ptrace 时，每当被 trace 的子进程**收到一个信号**时，都会 trap 进父进程。

+ 如果没有在 setoptions 中指定 traceexec，那么在子进程 exec 时会自动收到一个 SIGTRAP 信号来让父进程有机会做 exec 前最后的一些设置（所以子进程中不需要手动给自己发一个信号）

  当然如果指定了 traceexec，那么子进程 exec 时显然是会收到一个带有 exec event 信息的 SIGTRAP 信号的

+ `openat` syscall 额外接受一个 fd 作为入参，当 filepath 参数为相对路径时，可以打开相对于 fd 所指的目录的文件，特别地，当 fd 为 `AT_FDCWD` 时，所指的目录为当前目录。（可以用 `opendir` + `dirfd` 来获取到目录的 fd，`opendir` 本质上是带 O_DIRECTORY 选项的 `open`，但是只建议在 `opendir` 中使用）

  设计以 at 为后缀的 syscall 大多有两个目的：

  + 避免竞争，当 open 一个多级目录下的文件时，某一级的目录名可能会被更改，如果先 open 那个目录得到 fd 再 openat，可以避免这一竞争
  + 实现线程粒度的“当前工作目录”，cwd 的概念是对应进程而言的，要实现线程级别的 cwd，可以让每个线程对应一个目录的 fd 即可

+ 一些和文件系统相关的 syscall：

  + `lseek` 调整文件指针
  + `stat` 获取文件 metadata（追随 symlink），`lstat` 不追随 symlink，`fstat` 接受 fd 作为入参
  + `access` 检查文件 mode
  + `umask` 设置创建文件的 mode 掩码，但是仍然可以通过 `chmod` 强行更改
  + `utimes` 获取文件的 atime 和 mtime

+ c++ 中派生类不能直接初始化基类的成员，而应该调用基类的构造函数。

+ 在成员函数后加上 const 可以保证该函数不会修改成员变量，可以理解为该函数中 `this` 不仅是一个指针常量（本身不能被修改），也是一个常量指针（指向的东西不能被修改）。

+ 条件完全一样（syscall 一样，参数要求也一样）的 seccomp filter，以**先定义**的为准，如果在 BasicRule 模块中定义了不允许所有 open（没有对参数作要求），那么 FileWhitelist 模块就相当于整个都被屏蔽了。

+ 测试中 mock 的功能主要是将调用链在此处截断，不让测试的代码和较多组件关联（比如数据库、网络），GMock 中 `EXPECT_CALL` 意为期望某个函数被调用（而不是期望某个函数被调用时返回值是多少），`ON_CALL` 意为手动设置某个函数的返回值。

+ 为什么 mock 类要继承实际的类，考虑这样一个场景：A class 是要测试的那个类，这个类中持有一个 B class 对象，测试的过程中不希望真的调用 B class 的方法，于是就 mock 出一个 C class 继承 B class，那么在初始化 A 的时候，它实际上持有的就是 C，而不是 B，如果 C 不是继承的 B，这种做法就不可行了。自然地，mock 用到的那些方法需要为虚函数，否则 A 调用的还是 B 的方法而不是 C 的方法。

  当然，还有另外一种方法不用继承也不用虚函数，可以用模板，此时 mock 出的 C 和 B 没有关系，需要注意的是 A 是一个类模板，根据模板类型的不同可以实例化成持有 B 的模板类或是持有 C 的模板类，这样在生存环境中定义 A 时，传入 B 的模板类型，在测试环境中传入 C 的模板类型就可以做到 mock 的效果，不过想想就很麻烦，还是虚函数方便一些。

+ `sockaddr` 及 `sockaddr_in` 的定义：

  ```c++
  struct sockaddr {
      unsigned short sa_family;   // 2 bytes
      char sa_data[14];           // 14 bytes
  };
  
  struct sockaddr_in {
      unsigned short sin_family;  // 2 bytes
      unsigned short sin_port;    // 2 bytes
      struct in_addr sin_addr;    // 4 bytes
      unsigned char sin_zero[8];  // 8 bytes
  };
  
  struct in_addr {
      unsigned int s_addr;
  };
  ```

  以 `localhost:1234` 为例，`sockaddr_in` 结构体中的内容为 `02 00 04 d2 7f 00 00 01 00 00 00 00 00 00 00 00`（顺序为低地址到高地址，如果将前八个字节以 long 的类型读出的话，就是 `100007fd2040002`）

  + 1, 2 两个字节 `02 00`，小端法存储，实际为 `0x2`，也就是 `AF_INET`
  + 3, 4 两个字节 `04 d2`，大端法存储，实际为 `0x4d2`，也就是端口 `1234`
  + 5~8 四个字节 `7f 00 00 01`，大端法存储，实际为 `0x7f000001`，也就是 ip `127.0.0.1`

  需要注意的是直接用 `->sin_addr.s_addr` 读取 ip 的话读出来是 `0x1000007f`，因为 long 是用小端法存的，所以要经过 ntop 的转换才能得到 ip 字符串。

+ GMock 中 `SetArgReferee` 可以直接对引用实参赋值，而使用 `SetArgPointee` 对指针实参赋值时需要注意几点：

  + 被赋值的对象要有拷贝构造和拷贝赋值

  + 如果 mock 函数的返回值不为 void 的话，需要再加一个 action 标识返回值：

    ```c++
    EXPECT_CALL(mock, mockMethod(_))
        .Times(1)
        .WillOnce(DoAll(SetArgPointee<0>(x), Return(0)));
    ```

##### Last-modified date: 2020.3.14, 10 a.m.