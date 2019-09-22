# Istio learning notes

## Prepare

1. [Downloading the release](<https://istio.io/docs/setup/>)
2. [Installation](<https://istio.io/docs/setup/install/kubernetes/>)
3. [Bookinfo example](<https://istio.io/docs/examples/bookinfo/>)

在配置 Bookinfo 服务的时候，istio-ingressgateway 服务类型默认是 LoadBalancer，但是集群没有 LoadBalancer，可以通过 `kubectl edit svc istio-ingressgateway -n istio-system` 修改服务类型为 NodePort 以及 http 端口为 30521，再通过浏览器就可以访问了。

## Pilot + Envoy

+ service discovery + load balancing
+ traffic management resources
  + Virtual service
  + Destination rule
  + Gateway
  + Service entry
  + Sidecar
+ network resilience and testing
  + timeouts and retries
  + circuit breakers
  + fault injection
  + fault tolerance

##### Last-modified date: 2019.9.22, 10 a.m.

