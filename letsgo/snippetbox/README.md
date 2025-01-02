 go version
 cd .\snippetbox\
 go mod init snippetbox.alexedwards.net
 go run .


 curl http://localhost:4000/snippet/view
 curl http://localhost:4000/snippet/create
 curl http://localhost:4000/missing


curl -i -X POST http://localhost:4000/snippet/create
-- Create a new snippet...
curl -i -X Put http://localhost:4000/snippet/create
-- Method Not Allowed