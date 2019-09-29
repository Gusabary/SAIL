# Task-2

## Requirements

>Find the resources consumption of your REST service.
>
>+ Get the CPU, memory consumption of it when there is no request.
>+ With the increasing requests, find out the trend of each resource utilization.

## Findings

没有 request 时 CPU 和 memory 的使用情况：

![](./no-request.png)

发送一次 request 时：

![](./one-request.png)

连续发送多次 request 时：

![](./many-requests.png)

对于 **IntelliJ IDEA** 和 **Postman** 两个应用，随着 request 数量增多，前者对于 CPU 和内存的使用变化不大，后者对于 CPU 的使用明显增多，内存的占用率也有提升。

前后端之间的通信，主要是前端使用了更多资源。

##### Last-modified date: 2019.4.20, 3 p.m.