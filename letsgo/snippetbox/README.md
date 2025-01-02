 go version
 cd .\snippetbox\
 go mod init snippetbox.alexedwards.net
 go run .


 curl http://localhost:4000/snippet/view
 curl http://localhost:4000/snippet/create
 curl http://localhost:4000/missing