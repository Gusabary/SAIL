# Linux learning notes

+ 使用 kill 命令批量终止进程时，`grep abc` 可以过滤出想要终止的进程，但是同时也会过滤出 `grep` 本身，可以再加上 `grep -v grep` 反选过滤掉，还可以用 `awk '{print $2}'` 只打印 pid

  但是这项工作可以用 `killall` 命令更方便地完成。

  *[了解更多](<https://blog.csdn.net/lu_embedded/article/details/53590815>)*

##### Last-modified date: 2019.11.3, 10 a.m.