+ 后端运行命令：`mvn spring-boot:run` 

+ 本地登录服务器命令：`ssh root@IP`

+ `docker push` 的镜像名需要符合 `用户名/软件名` 的格式。

+ 端口映射 `0.0.0.0:8888->8080/tcp` 中，`8080` 为 docker 的端口，`0.0.0.0` 为本机（**Ubuntu**）IP。

  即 Ubuntu 0.0.0.0 = Ubuntu localhost = Windows 47.100.126.180.

  但是在 Windows 中通过 47.100.126.180 访问 8888 端口需要将其加入服务器的安全组中。

+ [教程](<https://blog.csdn.net/caox_nazi/article/details/78366584#commentBox>)中 Dockerfile 第一行的镜像也可以换成 `frolvlad/alpine-oraclejre8` 。

+ 在服务器中访问服务器自己的 ip，也要将访问的端口加入安全组。

##### Last-modified date: 2019.5.3, 3 p.m.

