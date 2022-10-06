docker rm -f cloud-batch-worker1
docker rm -f cloud-batch-worker2
docker rm -f cloud-batch-manager

docker run -d --name cloud-batch-worker1 --link mysql:mysql eldermoraes/cloud-batch-worker
docker run -d --name cloud-batch-worker2 --link mysql:mysql eldermoraes/cloud-batch-worker

sleep 5

docker run -d --name cloud-batch-manager --link mysql:mysql eldermoraes/cloud-batch-manager