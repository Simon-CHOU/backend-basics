 go version
 cd .\snippetbox\
 go mod init snippetbox.alexedwards.net
 go run .


 curl http://localhost:4000/snippet/view
 curl http://localhost:4000/snippet/create
 curl http://localhost:4000/missing


curl -i -X POST http://localhost:4000/snippet/create
-- Create a new snippet...
curl -i -X PUT http://localhost:4000/snippet/create
-- Method Not Allowed

```
>curl -i -X PUT http://localhost:4000/snippet/create
HTTP/1.1 405 Method Not Allowed
Date: Thu, 02 Jan 2025 10:26:12 GMT
Content-Length: 18
Content-Type: text/plain; charset=utf-8

Method Not Allowed

vs

>curl -i -X PUT http://localhost:4000/snippet/create
HTTP/1.1 405 Method Not Allowed
Allow: POST
Date: Thu, 02 Jan 2025 10:26:39 GMT
Content-Length: 18
Content-Type: text/plain; charset=utf-8

Method Not Allowed

w.Header().Set("Allow", "POST")    ->   Allow: POST 
```