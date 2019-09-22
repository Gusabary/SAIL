# OSS learning notes

+ 对象由元信息（Object Meta），用户数据（Data）和文件名（Key）组成。对象由存储空间内部唯一的 Key 来标识。

+ OSS 以 HTTP RESTful API 的形式对外提供服务。

+ OSS 通过使用 AccessKeyId 和 AccessKeySecret 对称加密的方法来验证某个请求的发送者身份。AccessKeyId 用于标识用户；AccessKeySecret 是用户用于加密签名字符串和 OSS 用来验证签名字符串的密钥，必须保密。

+ 内网指的是阿里云产品之间的内网通信网络，例如您通过ECS云服务器访问OSS服务。

  同一个Region的ECS和OSS之间内网互通，不同Region的ECS和OSS之间内网不互通。

+ 相对于RAM提供的长效控制机制，STS提供的是一种临时访问授权。

+ 通过STS生成的凭证包括安全令牌（SecurityToken）、临时访问密钥（AccessKeyId，AccessKeySecret）。使用AccessKey方法与您在使用阿里云账户或RAM用户AccessKey发送请求时的方法相同。需要注意的是在每个向OSS发送的请求中必须携带安全令牌。

+ 访问时以URL的形式来表示OSS的资源。OSS的URL构成如下：

  ```
  <Schema>://<Bucket>.<外网Endpoint>/<Object>
  ```

  - Schema：HTTP或者为HTTPS
  - Bucket：OSS存储空间名称
  - Endpoint：Bucket所在数据中心的访问域名，您需要填写外网Endpoint
  - Object：上传到OSS上的文件的访问路径

+ 浏览器中使用`signatureUrl`方法生成可下载的HTTP地址，URL的有效时间默认为半个小时。

+ 通过`list`来列出当前Bucket下的所有文件：

  ```js
  let result = await client.list({
      prefix: dir,    // dir 是当前目录名
      delimiter: '/'  // 目录用 / 分隔
  });
  
  result.prefixes.forEach(function (subDir) {  // prefixes 是当前目录下有'/'的项，即子目录
      console.log('SubDir: %s', subDir);
  });
  result.objects.forEach(function (obj) {      // objects 是当前目录下没有'/'的项，即一般文件
      console.log('Object: %s', obj.name);
  });
  ```

##### Last-modified date: 2019.9.21, 7 p.m.