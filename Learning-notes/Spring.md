## Spring learning notes

### [Guides](<https://spring.io/guides>) 

+ [Building a RESTful Web Service](<https://spring.io/guides/gs/rest-service/>) 提供服务
+ [Consuming a RESTful Web Service](<https://spring.io/guides/gs/consuming-rest/>) 使用服务
+ [Testing the Web Layer](<https://spring.io/guides/gs/testing-web/>) 测试
+ [Spring Boot Actuator](<https://spring.io/guides/gs/actuator-service/>) Actuator

### Notes

+ No converter found for return value of type 可能的解决方案：

  在类中加上 get 方法使类的属性能被外部以某种方式获得。

+ > spring boot 应用程序的入口点是包含 `@SpringBootApplication` 注释和 `main` 方法的类。

  有了这个类，IDE 右上角的 Configurations 会自动配置好。（Spring 官网教程 initial 版本可能不提供 Application 类，所以一开始会显示没有配置）

+ 在新目录下不能创建 java 类文件可能是由于没有 Mark Directory as 。

+ 在资源文件中可以通过设置 `management.endpoints.web.exposure.include` 来选择暴露出哪些端点。

##### Last-modified date: 2019.4.13, 10 a.m. 

