# SecureMe Development Notes

+ `waitpid(pid, &status, 0)` 的第二个参数用来标识导致 waitpid 返回的子进程的状态，一般而言这是一个 32 位的量（也有例外，比如 ptrace event 会用到第五个字节来标识 event），有以下四种情况：

  + 子进程正常退出，例如 exit，此时 status 的结构是 `xxxx xxxx 1000 0000`，前八位表示 exit code

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

##### Last-modified date: 2020.2.11, 6 p.m.