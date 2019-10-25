# CTP notes

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

##### Last-modified date: 2019.10.25, 11 a.m.