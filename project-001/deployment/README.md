# PLEASE NOTE THAT THIS TWO FILES MUST BE IN THE BASE DIRECTORY.

## Say you clone all the repos in this project and place everything in a single file example of a base folder name **"banking-app"** the docker-compose.yml and nignix.conf must be place in the **"banking-app"** not in **"deployement folder"**.


## This is all you need to run this project.

```bash tree
project-root/
├── frontend/
├── deposit-service/
├── withdraw-service/
├── maintenance-service/
├── escrow-service/
├── blacklist-service/
├── beneficiary-service/
├── bank-collection-service/
├── authentication-service/
├── history-service/
├── revenue-service/
├── wallet-service/
├── notification-service/
├── nginx.conf
└── docker-compose.ymlç

```

### Make sure you bring out docker-compose.yml and ngnix.conf from "deployment folder" to base folder.

- Then you can play around with any of these command below:

```bash

docker-compose up -d
docker-compose up --build -d
docker-compose up --build -d --remove-orphans
docker-compose build --no-cache

docker-compose down -v
docker-compose down -v --rmi all
docker system prune -a --volumes
docker system prune -a 

docker-compose down && docker-compose up -d --build

sudo systemctl restart  apache2

docker-compose up -d db

docker-compose ps

docker-compose build --no-cache

docker-compose restart web

docker logs httpdocs-web-1

docker logs httpdocs-web-1 2>&1 | grep -i error

docker network prune
docker volume ls
docker exec -it nginx nginx -t



=====Redis

docker run -d --name redis -p 6379:6379 redis

docker exec -it 02f588ccde37f67e71a47e2643534bf2a4285ae18bf8cdb868d8e518ca639370 redis-cli FLUSHDB

docker run -it --network my-network redis redis-cli -h redis-container-name FLUSHALL

docker stop redis

docker rm redis

docker run -d -p 5080:5080 --name antmedia antmedia/enterprise

docker run --rm -it --network host kurento/kurento-media-serve

docker stop antmedia

docker  stop "/antmedia"

docker rm antmedia

docker stop antmedia/enterprise

redis-cli --scan --pattern "user:*" | xargs redis-cli DEL

```
