### 操作说明
docker-compose.yml中是一个mongodb，`docker-compose up -d`来运行这个容器；

backend目录中是一个为mongodb设计的通用的CRUD服务；可以直接启动backend.jar来运行。

stocksdktestgateway目录是一个gateway服务，用来接受前端发来的请求，并处理请求格式 使其适应backend的接口，再将处理过的请求转发给backend。
里面以`sdkVersion`为例，实现了CURD方法，需要参考其实现，对应地更改函数中的`collectionName`.

