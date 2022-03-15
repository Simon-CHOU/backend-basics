curl -X POST \
 http://localhost:8080/birthday/dayOfWeek \
 -H 'Content-Type: text/plain' \
 -H 'accept: text/plain' \
 -d 2005-03-09

curl -X POST \
  http://localhost:8080/birthday/chineseZodiac \
  -H 'Content-Type: text/plain' \
  -H 'accept: text/plain' \
  -d 2005-03-09

curl -X POST \
  http://localhost:8080/birthday/starSign \
  -H 'Content-Type: text/plain' \
  -H 'accept: text/plain' \
  -d 2005-03-09