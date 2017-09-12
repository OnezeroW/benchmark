# MongoDB
export PATH=$PATH:/home/benchmark/opt/mongodb/bin
mongod --fork --logpath /tmp/mongodb.log -dbpath ./opt/mongodb/data/

# GridDB
export PATH=$PATH:/home/benchmark/opt/griddb/bin
export GS_HOME=/home/benchmark/opt/griddb
export GS_LOG=/home/benchmark/opt/griddb/log
export no_proxy=127.0.0.1
gs_startnode
sleep 5
gs_joincluster -c cluster1 -u admin/admin

# AsterixDB
/home/benchmark/opt/asterixdb/opt/local/bin/start-sample-cluster.sh

# PostgreSQL
service postgresql start

# Cassandra
service cassandra start

# CrateDB
service crate start