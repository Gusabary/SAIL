# Task-1

## Requirements

>How to use **F12**.
>
>Analyze the home page loading of SJTU.
>
>+ Compare with other website home page.
>+ Carry out your optimization solution.

## Findings

+ **上海交通大学**网站主页的加载信息：

![](./上交.png)

+ **南京大学**网站主页的加载信息：

![](./NJU.png)

两个网站都是先发出 GET 请求，返回页面主体的框架，然后再请求样式和脚本进行渲染。

交大网站的第一次请求花在**建立连接**上的时间比较多，而且好多资源并没有在第一次请求时得到，后续的请求延长了页面加载的时间。

南大网站的第一次请求花在**接受资源**上的时间比较多，而且第一次请求占据了页面加载的大部分时间。

##### Last-modified date: 2019.3.30, 8 p.m.