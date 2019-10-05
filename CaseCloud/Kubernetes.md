# Kubernetes

每个 API 对象都有 3 大类属性：元数据 metadata、规范 spec 和状态 status。元数据是用来标识 API 对象的，每个对象都至少有 3 个元数据：namespace，name 和 uid

## Service

+ 不指定 targetPort 的情况下，默认是和 port 一样的端口

## Secret

secret 有三种不同的类型：

+ `Opaque`：存储 base64 编码的数据，大多以自定义为主
+ `service-account-token`：存储 `ca.crt`, `namespace`, `token` 三个字段的数据（经过 base64 编码）， 创建 sa 时自动创建
+ `dockerconfigjson`：存储 `.dockerconfigjson` 一个字段的数据（经过 base64 编码），解码后是 docker 的配置以及认证信息，比如服务器、用户名、密码。在创建 pod 时可以通过声明 `imagePullSecrets` 字段指定使用该类型的某个 secret

## Service Account

Service account 是为了方便 Pod 里面的进程调用 Kubernetes API 或其他外部服务而设计的。

k8 会在每个 namespace 创建一个名为 `default` 的默认 sa 。

每个 sa 下都会有一个 `service-account-token` 类型的 secret，当创建 pod 时，会将**所使用的 sa** 下的 secret 挂载到容器内。所使用的 sa 是指：

+ 如果在声明 pod 时有指定 `spec.serviceAccountName` 字段，就使用指定的 sa
+ 如果没有指定，就使用默认的 sa，即 `default`

## trouble shooting

### curl 不通

`curl` httpbin 的 ip，报错 `curl: (56) Recv failure: Connection reset by peer`

解决方法：用 curl 镜像起一个 pod，然后再 curl :

```sh
kubectl exec -it curl-pod -- curl httpbin-svc
```

##### Last-modified date: 2019.10.5, 8 p.m.