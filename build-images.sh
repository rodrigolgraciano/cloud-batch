mvn clean verify -DskipTests

docker build -t eldermoraes/cloud-batch-worker -f Dockerfile-worker .
docker build -t eldermoraes/cloud-batch-manager -f Dockerfile-manager .

docker push eldermoraes/cloud-batch-worker
docker push eldermoraes/cloud-batch-manager