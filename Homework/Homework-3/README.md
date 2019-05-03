# Homework-3

## Requirements

>+ Split your REST Service of Homework-2 into two microservices:
>  + login
>  + function
>+ Dockerize the two microservices

## Dockerization

The gateway application and two microservices were dockerized in the same way as [Task-3](../../Tasks/Task-3).

You can pull them from DockerHub, with following commands:

```
docker pull gusabary/intplat-hw3-gateway
docker pull gusabary/intplat-hw3-login
docker pull gusabary/intplat-hw3-magicsquare
```

or try them directly at 47.100.126.180, followed by port

+ 8080: gateway
  + 8080/login: username and password should be included in request body
  + 8080/magicSquare: loginInfo (encoded username and password) should be included in request header
+ 8001: login
+ 8002: magicSquare

##### Last-modified date: 2019.5.3, 5 p.m.