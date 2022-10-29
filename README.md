# PBFT
- a sample implementation for simulation

### usage

- build image
```bash
sbt Docker/publishLocal
```

- run a container
```bash
# docker run --detach --name node1 pbft1:0.1.0-SNAPSHOT
# docker container prune
export IS_LEADER_NODE=true; docker run -p 9000:9000 --detach --name node1 pbft1:0.1.0-SNAPSHOT
```

- REST API
```bash
curl -X POST http://localhost:9000/propose -d "aaaa"
curl http://localhost:9000/result
```

### example


### reference
- https://github.com/NightWhistler/naivechain-scala
- https://github.com/MFQWKMR4/actor-model
- https://github.com/kashishkhullar/pbft
- https://qiita.com/4245Ryomt/items/395129513c655a67cdf2
- https://www.scala-sbt.org/1.x/docs/ja/Combined+Pages.html
- https://pmg.csail.mit.edu/papers/osdi99.pdf
- https://hazm.at/mox/distributed-system/algorithm/transaction/pbft/index.html
- https://jp.coursera.org/lecture/scala2-akka-reactive/lecture-3-3-persistent-actor-state-pu6Dw
