#!/bin/bash

# Number of nodes
num_nodes=$1

# Start of the docker-compose file
cat <<EOF > docker-compose.yml
version: '3'
services:

  bootstrap:
    build: ./Bootstrapping
    ports:
      - "8080:8080"
      - "2222:2222"
    environment:
      - broadcasting-listener-port=2222
      - server.port=8080
    

  demo:
    build: ./Demo
    ports:
      - "7045:7045"
    environment:
      - server.port=7045
      - bootstrapping-node-server=bootstrap
      - bootstrapping-node-port=8080
    depends_on:
      - bootstrap
      - node1

    
EOF

# Loop to generate each node
for (( i=1; i<=$num_nodes; i++ ))
do
  cat <<EOF >> docker-compose.yml
  node$i:
    build: ./Node
    ports:
      - "808$i:808$i"
      - "222$(($i+2)):222$(($i+2))"
    environment:
      - broadcasting-listener-port=222$(($i+2))
      - server.port=808$i
      - server.hostname=node$i
      - bootstrapping-server=bootstrap
      - bootstrapping-port=2222
      - DBSIM_ROOT_DIRECTORY=/Node$i
    depends_on:
      - bootstrap
    volumes:
      - ./Node$i:/Node$i


EOF
done