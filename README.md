### 操作说明

backend目录中是一个为mongodb设计的通用的CRUD服务；

stocksdktestgateway目录是一个gateway服务，用来接受前端发来的请求，并处理请求格式 使其适应backend的接口，再将处理过的请求转发给backend。

运行需要docker容器，先在backend目录下`docker-compose up -d`,然后分别启动两个服务。
