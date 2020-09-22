# Linux learning notes

## I/O Multiplexing

### select

```c++
int select(int nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout);
```

+ `n` is upper limit of file descriptors that you are interested in, which is the maximum number among those fds, plus 1.
+ `readfds` and `writefds` respectively represent the fd set that you care about when they are readable and writable. After invocation of select, both will be cleared except for those which are ready to read and write (**ready** means that i/o operation will not block. e.g. for normal files, operations will not block seemingly anyway, which means you can always read or write a normal file; for sockets and stdin, it seems that you need to wait until something you can read gets inputted)
+ `exceptfds` represent the fd set that you care about for some exceptional conditions (which are out of my scope currently)
+ use `FD_` macro to manipulate the fd sets. Specifically, use `FD_ZERO` and `FD_SET` to initialize a fd set before select and `FD_ISSET` to check which fds are ready after select (it seems that the positive return value of select indicates the number of ready fds)
+ `timeout` is to say if there is still no fd getting ready after some time, return 0, indicating timeout. It can be set to NULL if you don't want a time limit.

### poll

```c++
int poll(struct pollfd *fds, nfds_t nfds, int timeout);
```

+ `fds` is a pointer to `struct pollfd` and `nfds` indicates the number of those structs

+ compared to `select`, `poll` has some edges listed below:

  + `poll` won't modify the input side of fd set (because there are both `event` and `revent` fields in the `struct pollfd`) while `select` will clear the fd set except for those which are ready (the root cause is that input and output side are not separated)
  + the number of fds that you care about in the `poll` is specified by `nfds` while the `fd_set` in `select` is implemented as an array essentially, which implies that it has fixed size (default 1024). so if you want to listen to more fds with `select`, it may need to recompile the `select.h`. 
  + `poll` can listen to more event types while there are just three in `select`.

  however, `poll` has less portability than `select`. almost all the systems support latter. and the timeout precision of `poll` is rougher (milliseconds vs. microseconds)

##### Last-modified date: 2020.9.20, 6 p.m.

