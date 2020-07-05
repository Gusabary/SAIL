# Intern Notes

## XML & XSD

+ XML 表示数据，XSD 规范 XML 数据的格式，例如 tag name 叫什么，数据类型是什么。

+ Schema is the root element of XSD and it is always required.

  ```xml
  <xs:schema xmlns:xs = "http://www.w3.org/2001/XMLSchema">
  </xs:schema>
  ```

### XSD Simple Types

+ XSD 中的 `<xs:element>` 对应了 XML 中的一个简单 tag，即不能有任何 attribute。`<xs:element>` 可以有 name 和 type 等等 attribute：

  ```xml
  <!-- xsd: -->
  <xs:element name = "firstname" type = "xs:string"/> 
  
  <!-- xml: -->
  <firstname>Dinkar</firstname>
  ```

+ XSD 中的 `<xs:attribute>` 对应了 XML 中 tag 的一个 atribute。`<xs:attribute>` 也可以有 name 和 type 等等 attribute：

  ```xml
  <!-- xsd: -->
  <xs:attribute name = "rollno" type = "xs:integer"/>  
  
  <!-- xml: -->
  <student rollno = "393" />
  ```

### XSD Complex Types

Complex Types 有两种使用方式：

+ Define a complex type and then create an element using the type attribute：

  ```xml
  <xs:complexType name = "StudentType">
     <xs:sequence>
        <xs:element name = "firstname" type = "xs:string"/>
        <xs:element name = "lastname" type = "xs:string"/>
        <xs:element name = "nickname" type = "xs:string"/>
        <xs:element name = "marks" type = "xs:positiveInteger"/>
     </xs:sequence>
     <xs:attribute name = 'rollno' type = 'xs:positiveInteger'/>
  </xs:complexType>
  
  <xs:element name = 'student' type = 'StudentType' />
  ```

+ Define a complex type directly by naming：

  ```xml
  <xs:element name = "student">
     <xs:complexType>   
        <xs:sequence>
           <xs:element name = "firstname" type = "xs:string"/>
           <xs:element name = "lastname" type = "xs:string"/>
           <xs:element name = "nickname" type = "xs:string"/>
           <xs:element name = "marks" type = "xs:positiveInteger"/>
        </xs:sequence>
        <xs:attribute name = 'rollno' type = 'xs:positiveInteger'/>
     </xs:complexType>
  <xs:element>
  ```

## I/O Multiplexing

### select

```c++
int select(int nfds, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout);
```

+ 对于 select 的调用会阻塞，直到指定的 fd 中有 fd 准备好了读写，或出现异常，或超时。
+ `fd_set` 类型的实现类似于一个 bitmap，默认有 1024 个 bit，所以默认只能监视 1024 个 fd。这样每当需要监视一个 fd 时，就置上对应的 bit。需要注意的是，select 执行后会修改 bitmap，只保留准备好的 fd 对应的 bit 为 1，其余全部 reset，所以每次 select 前需要重新 set bitmap。
+ select 的第一个参数 `nfds` 指定了最大的 fd number，这样可以不用遍历整个 bitmap。
+ select 的优点在于支持所有 Unix 系统，并且 `timeval` 类型可以将 timeout 精确到微秒。

### poll

```c++
int poll(struct pollfd *fds, unsigned int nfds, int timeout);

struct pollfd {
      int fd;
      short events; 
      short revents;
};
```

+ poll 的作用和 select 类似，但是使用的是一个 `pollfd` 结构体，而非 `fd_set` 的 bitmap。
+ 使用 `pollfd`，需要监视多少 fd 就将 poll 的第二个参数 `nfds` 设为多少，不需要像 select 那样计算出一个监视 fd 中的最大值；并且在 fd 很稀疏的情况下，poll 的效率会更高一些。
+ 由于 `pollfd` 中输入（`events`）和输出（`revents`）的字段是分开的，所以调用 poll 不会修改 fd set，也就不需要在每次调用 poll 前重新初始化 fd set。此外，`events` 字段可以监控比读、写、异常更多的事件。相比之下，select 的 fd set 用 bitmap 实现，一个 fd 仅对应一个 bit，只能监控一种事件（监控多个事件需要多个 bitmap），输入和输出也共用一个 bit。
+ 但是部分 Unix 系统不支持 poll。

### epoll

+ 和 select 以及 poll 不同，epoll 将 fd set 信息维护在内核态以提高性能（减少每次调用都需要从用户态拷贝到内核态的开销），具体做法是将已注册的 fd 维护在一棵红黑树上，然后将准备好的 fd 加入一个链表。
+ 内核暴露一些接口给用户态使用，通常的使用方法是先用 `epoll_create` 在内核中创建一个 context，然后使用 `epoll_ctl` 注册或注销 fd，最后使用 `epoll_wait` 等待事件发生。
+ 只有 Linux 系统支持 epoll。

##### Last-modified date: 2020.7.5, 5 p.m.