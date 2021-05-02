```
docker run --rm \
 -p 5432:5432 \
 -e POSTGRES_DB=translation \
 -e POSTGRES_USER=translation \
 -e POSTGRES_PASSWORD=translation \
 bitnami/postgresql:11.11.0-debian-10-r59
 ```