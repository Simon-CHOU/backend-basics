
下载POM文件的指定字节内容

curl http://localhost:8080/file/chunk/download  -H "Range: bytes=0-100"

```shell
# 下载 0-100 字节，合计101字节的文件并保存到 t.mp4
curl http://localhost:8080/file/chunk/download  -H "Range: bytes=0-100" -o "t.mp4"
```

