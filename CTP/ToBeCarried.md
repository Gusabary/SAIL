# ToBeCarried Development Notes

## linux

+ 文件类型

  | 文件类型   | 字符表示 | 备注                                               |
  | ---------- | -------- | -------------------------------------------------- |
  | 普通文件   | -        | 纯文本、二进制、数据格式文件、压缩文件             |
  | 目录文件   | d        | 目录                                               |
  | 块设备     | b        | 存储数据的接口设备（硬盘）                         |
  | 字符设备   | c        | 串行端口的接口设备（键盘、鼠标）                   |
  | 套接字文件 | s        | 用于网络通信                                       |
  | 管道文件   | p        | FIFO，用于解决多个程序同时存取一个文件所造成的错误 |
  | 链接文件   | l        | 软链接                                             |

+ 不同进程之间的 fd 是不共享的，在一个进程中将 fd=1 重定向到某个文件，在另一个进程中 cout 仍然会打印到标准输出。

+ 使用 POSIX thread 函数时，不仅要 `#include <pthread.h>`，还要在 gcc 链接时加上 `-pthread` （可能还要下载 pthread），POSIX 线程的概念是基于用户态的，gettid systemcall 这类线程是基于内核态的。[了解更多](<https://www.cnblogs.com/luntai/p/6184156.html>)

+ `/proc` 文件系统中 `/proc/[pid]/fd` 目录下的 `socket:[inode]` 这种软链接，方括号中是 inode number，在 `/proc/[pid]/net` 目录下可以看到三个文件 `raw`, `tcp` 以及 `udp`，分别为三种不同类型的网络连接，以 `tcp` 为例：

  ```bash
  $ cat tcp
  sl  local_address  rem_address  .....  inode
  20: 1000000A:AFB6  BB92A8B4:277E ...  54138358
  ```

  其中 `local_address` 和 `rem_address` 字段分别表示本地和远端套接字的地址（ip + 端口），要注意的是 ip 部分是**反序**（BB92A8B4 表示 B4.A8.92.BB 即 180.168.146.187）而端口部分是**正序**（277E 表示 10110）。

  所以 `fd` 目录下出现的 socket 软链接可以在 `net` 目录下查看到连接的地址。

## ptrace / syscall

+ 子进程因为设置了 `PTRACE_TRACEME` 而在执行系统调用时被系统停止 （状态设置为 TASK_TRACED）

+ clone 和 fork 类似，也是调用一次返回两次，父进程返回子进程 id，子进程返回 0

  > A process has five fundamental parts: code ("text"), data (VM), stack, file I/O, and signal tables.

  fork 出来的进程只共享 code，而利用 clone 可以对创建出来的进程的资源共享有更精细的控制。

+ 网络相关 system call:

  ```c++
  #include <sys/types.h>
  #include <sys/socket.h>
  
  int socket(int domain, int type, int protocol);  // @return: fd
  int connect(int sockfd, const struct sockaddr *addr, 
              socklen_t addrlen);  // @return: 0-success, -1-error
  int accept(int sockfd, struct sockaddr *addr, 
              socklen_t *addrlen);  // @return: fd
  ssize_t sendto(int sockfd, const void *buf, size_t len, 
              int flags, const struct sockaddr *dest_addr, 
              socklen_t addrlen);  // @return: the number of bytes sent, -1-error
  ssize_t recvfrom(int sockfd, void *buf, size_t len, 
              int flags, struct sockaddr *src_addr, 
              socklen_t *addrlen);  // @return: the number of bytes received, -1-error
  ssize_t sendmsg(int sockfd, const struct msghdr *msg, int flags);  // @return: =sendto
  ssize_t recvmsg(int sockfd, struct msghdr *msg, int flags);  // @return: =recvfrom
  int shutdown(int sockfd, int how);  // @return: 0-success, -1-error
  int bind(int sockfd, const struct sockaddr *addr, 
           	socklen_t addrlen);  // @return: 0-success, -1-error
  int listen(int sockfd, int backlog);  // @return: 0-success, -1-error
  int getsockname(int sockfd, struct sockaddr *addr, 
              socklen_t *addrlen);  // @return: 0-success, -1-error
  int getpeername(int sockfd, struct sockaddr *addr, 
              socklen_t *addrlen);  // @return: 0-success, -1-error
  int socketpair(int domain, int type, int protocol, 
              int sv[2]);  // @return: 0-success, -1-error
  int setsockopt(int sockfd, int level, int optname,
              const void *optval, socklen_t optlen);  // @return: 0-success, -1-error
  int getsockopt(int sockfd, int level, int optname,
              void *optval, socklen_t *optlen);  // @return: 0-success, -1-error
  ```

## lsof

+ 当你给它传递选项时，默认行为是对结果进行 “或” 运算。
+ 因为 lsof 需要访问核心内存和各种文件，所以必须以 root 用户的身份运行它才能够充分地发挥其功能。
+ lsof 的输出中 FD 字段表示打开文件的 fd，格式为一个数字（fd）加上一个字母，字母表示该文件的读写模式（例如 u 表示可读可写）；TYPE 字段表示打开的文件类型，REG 为文件，DIR 为目录，CHR 为字符设备（fd = 0, 1, 2），BLK 为块设备。

## C++

+ char vs. unsigned char

  ```c++
  char c = 0x80;
  unsigned char uc = 0x80;
  
  printf("%x\n", c);
  printf("%x\n", uc);
  if (c == 0x80)
      printf("yes\n");
  else
      printf("no\n");
  
  // output:
  // ffffff80
  // 80
  // no
  ```

  对于第一位为 1 的 byte，char 和 unsigned char 会由于符号位的缘故产生神秘的错误，所以存储 byte 的话最好还是用 unsigned char。

+ ```c++
  #include <a>
  #include <b>
  ```

  上面的 include 会影响下面的，如果在 a 中定义了一个全局宏，b 也会受到影响（宏是直接替换）

+ 堆 vs. 栈

  ```c++
  char *p;
  {
      char tmp[size];  // 此时 tmp 分配在栈上，地址差不多是 0x7ffe54525f70 这样
      strcmp("123", tmp);
      p = tmp;  
  	printf("%s\n", p);  // 可以正确打印出 tmp 的内容
  }
  printf("%s\n", p);  // 离开 tmp 的作用域后，tmp 被释放，p 变成了悬空指针
  ```

  ```c++
  // tmp 是类的成员变量，分配在堆上，地址差不多是 0x207e4c0 这样
  // 或者 tmp 是 static 变量，也分配在堆上，地址差不多是 0x9e47e0 这样
  char *p;
  {
      strcmp("123", tmp);
      p = tmp;
      printf("%s\n", p);  // 可以正确打印出 tmp 的内容
  }
  printf("%s\n", p);  // 仍然可以正确打印出 tmp 的内容
  ```

## Git

+ github 判断用户是谁是通过邮箱来判断的，ssh keys 只负责判断用户有没有权限读写某个仓库，可以用 `git config --global user.email ${email}` 来修改使用哪个邮箱，然后用 `git commit --amend --reset-author` 来修改最近一次提交。

##### Last-modified date: 2020.2.16, 12 p.m.