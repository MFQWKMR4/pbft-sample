# PBFT
- a sample implementation for simulation

### usage

- build image
```bash
sbt Docker/publishLocal
```

- run a container
```bash
export IS_LEADER_NODE=true; docker run -p 9000:9000 --detach --name leader pbft1:0.1.0-SNAPSHOT
```

- run cluster
```bash
cd ./docker
docker-compose up -d leader rep1 rep2 rep3
```

- REST API
```bash
curl -X POST http://localhost:9000/addPeer -d "akka.tcp://pbft-distributed-system@172.21.0.2:2552"
curl -X POST http://localhost:9000/propose -d "aaaa"
curl http://localhost:9000/result
curl http://localhost:9000/info
```

- when port is already in use on Windows
```cmd
netstat -ano | find ":9000"
# tasklist /fi "PID eq 34752"
taskkill /F /PID 34752
```
### example



add peer
```bash
curl -X POST http://localhost:9000/addPeer -d "akka.tcp://pbft-distributed-system@172.21.0.2:2552"
```

### reference
- https://github.com/NightWhistler/naivechain-scala
- https://github.com/MFQWKMR4/actor-model
- https://github.com/kashishkhullar/pbft
- https://qiita.com/4245Ryomt/items/395129513c655a67cdf2
- https://www.scala-sbt.org/1.x/docs/ja/Combined+Pages.html
- https://pmg.csail.mit.edu/papers/osdi99.pdf
- https://hazm.at/mox/distributed-system/algorithm/transaction/pbft/index.html
- https://jp.coursera.org/lecture/scala2-akka-reactive/lecture-3-3-persistent-actor-state-pu6Dw
- https://github.com/x3ro/scala-sbt-logback-example/blob/master/src/main/resources/logback.xml
