## Hazelcast 4.1.1 eviction issue reproducer

Scaled down reproducer to show that running an aggregate query when an entry is evicted makes the eviction behave slow and weird.
           
Issue is weird and subtle and was not properly caught while upgrading hazelcast 3.12 -> 4.1.1, once in production we noticed that session count kept increasing at an alarming rate and not being evicted as with hazelcast 3.12

Note that issue is reproduced with a single node, i.e. no need for several nodes although problem is the same

### Scenario
Stores user sessions in a hazelcast map with idle timeout of 30 seconds, every user might have several sessions and when a session (entry in map) is expired we run a query to see if user has any remaining sessions. In real app we do consider that as final logout. 
      
After adding sessions to map, they are not 'touched'.

In test scenario we run an aggregation every 3:e second to log number of sessions and users that are 'alive' in map.

#### Output

Notice the jumps in the stats (users, sessions) while entries are very, very slowly being evicted.

```
10:19:55.705 [main] INFO  Main - Added 5000 sessions
10:19:55.706 [main] INFO  Main - Watching stats every 3 seconds, expects that entries starts to expiry after 30 seconds
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000 
Main - Repo stats, size: 4505 sessions: 1560 users: 411  <-- eviction starts, expected 0/0
Main - Repo stats, size: 4505 sessions: 1560 users: 411
Main - Repo stats, size: 4199 sessions: 2539 users: 668  <-- stats 'jump'
Main - Repo stats, size: 4199 sessions: 2539 users: 668
Main - Repo stats, size: 4017 sessions: 3132 users: 823
Main - Repo stats, size: 3907 sessions: 3472 users: 913
Main - Repo stats, size: 3907 sessions: 3472 users: 913
Main - Repo stats, size: 3846 sessions: 3646 users: 960
Main - Repo stats, size: 3846 sessions: 3646 users: 960
Main - Repo stats, size: 3812 sessions: 3732 users: 984   
Main - Repo stats, size: 3791 sessions: 3766 users: 995
Main - Repo stats, size: 3791 sessions: 3766 users: 995
Main - Repo stats, size: 3785 sessions: 3785 users: 1000
Main - Repo stats, size: 3785 sessions: 3785 users: 1000
Main - Repo stats, size: 3785 sessions: 3785 users: 1000
Main - Repo stats, size: 3785 sessions: 3785 users: 1000
Main - Repo stats, size: 3785 sessions: 3785 users: 1000
Main - Repo stats, size: 3785 sessions: 3785 users: 1000
Main - Repo stats, size: 3785 sessions: 3785 users: 1000
Main - Repo stats, size: 3785 sessions: 3785 users: 1000
Main - Repo stats, size: 3612 sessions: 2614 users: 738
Main - Repo stats, size: 3612 sessions: 2614 users: 738
Main - Repo stats, size: 3388 sessions: 2147 users: 671
Main - Repo stats, size: 3388 sessions: 2147 users: 671
Main - Repo stats, size: 3143 sessions: 2141 users: 734
Main - Repo stats, size: 2956 sessions: 2234 users: 805
Main - Repo stats, size: 2956 sessions: 2234 users: 805
Main - Repo stats, size: 2796 sessions: 2391 users: 888
Main - Repo stats, size: 2796 sessions: 2391 users: 888
Main - Repo stats, size: 2685 sessions: 2499 users: 943
Main - Repo stats, size: 2613 sessions: 2556 users: 978
Main - Repo stats, size: 2613 sessions: 2556 users: 978
Main - Repo stats, size: 2604 sessions: 2548 users: 978
Main - Repo stats, size: 2604 sessions: 2548 users: 978
Main - Repo stats, size: 2599 sessions: 2552 users: 981
Main - Repo stats, size: 2591 sessions: 2559 users: 985
... ongoing for 5-10 minutes until eventually
Main - Repo stats, size: 167 sessions: 22 users: 22
Main - Repo stats, size: 131 sessions: 12 users: 12
Main - Repo stats, size: 101 sessions: 10 users: 10
Main - Repo stats, size: 101 sessions: 10 users: 10
Main - Repo stats, size: 58 sessions: 11 users: 11
Main - Repo stats, size: 58 sessions: 11 users: 11
Main - Repo stats, size: 25 sessions: 9 users: 9
Main - Repo stats, size: 10 sessions: 9 users: 9
Main - Repo stats, size: 10 sessions: 9 users: 9
Main - Repo stats, size: 4 sessions: 4 users: 4
Main - Repo stats, size: 4 sessions: 4 users: 4
Main - Repo stats, size: 2 sessions: 2 users: 2
Main - Repo stats, size: 2 sessions: 1 users: 1
Main - Repo stats, size: 2 sessions: 1 users: 1
Main - Repo stats, size: 1 sessions: 1 users: 1
Main - Repo stats, size: 1 sessions: 1 users: 1
Main - Repo stats, size: 1 sessions: 1 users: 1
Main - Repo stats, size: 1 sessions: 1 users: 1
Main - Repo stats, size: 1 sessions: 1 users: 1
Main - Repo stats, size: 1 sessions: 1 users: 1
Main - Repo stats, size: 1 sessions: 1 users: 1
Main - Repo stats, size: 1 sessions: 1 users: 1
Main - Repo stats, size: 1 sessions: 1 users: 1
Main - Repo stats, size: 1 sessions: 1 users: 1
Main - Repo stats, size: 0 sessions: 0 users: 0
Main - Repo stats, size: 0 sessions: 0 users: 0
```

#### Output if disable aggregate during eviction callback
                                                    
If we instead comment out, SessionRepository:55:
//                int sessionCount = sessionCountFor(event.getOldValue().userId());

and run again entries are evicted much more prompty without and weird stats 'jumps'

```
Main - Added 5000 sessions
Main - Watching stats every 3 seconds, expects that entries starts to expiry after 30 seconds
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 5000 sessions: 5000 users: 1000
Main - Repo stats, size: 4513 sessions: 0 users: 0   <-- eviction starts
Main - Repo stats, size: 4513 sessions: 0 users: 0       no jumps, very clean
Main - Repo stats, size: 4022 sessions: 0 users: 0
Main - Repo stats, size: 4022 sessions: 0 users: 0
Main - Repo stats, size: 3482 sessions: 0 users: 0
Main - Repo stats, size: 2952 sessions: 0 users: 0
Main - Repo stats, size: 2952 sessions: 0 users: 0
Main - Repo stats, size: 2452 sessions: 0 users: 0
Main - Repo stats, size: 2452 sessions: 0 users: 0
Main - Repo stats, size: 1950 sessions: 0 users: 0
Main - Repo stats, size: 1462 sessions: 0 users: 0
Main - Repo stats, size: 1462 sessions: 0 users: 0
Main - Repo stats, size: 1016 sessions: 0 users: 0
Main - Repo stats, size: 1016 sessions: 0 users: 0
Main - Repo stats, size: 505 sessions: 0 users: 0
Main - Repo stats, size: 16 sessions: 0 users: 0
Main - Repo stats, size: 16 sessions: 0 users: 0
Main - Repo stats, size: 0 sessions: 0 users: 0
``` 

### Hazelcast 4.2 beta

With Hazelcast 4.2 beta, an exception is thrown                      

```
12:43:18.948 [ForkJoinPool.commonPool-worker-9] ERROR c.h.map.impl.query.QueryOperation - [10.192.35.218]:5701 [hz4-repro] [4.2-BETA-1] null
java.lang.NullPointerException: null
	at com.hazelcast.map.impl.recordstore.AbstractEvictableRecordStore.accessRecord(AbstractEvictableRecordStore.java:184)
	at com.hazelcast.map.impl.recordstore.AbstractEvictableRecordStore.expireOrAccess(AbstractEvictableRecordStore.java:154)
	at com.hazelcast.map.impl.MapContainer.hasNotExpired(MapContainer.java:185)
	at com.hazelcast.internal.util.IterableUtil$2.hasNext(IterableUtil.java:93)
	at com.hazelcast.map.impl.query.ParallelAccumulationExecutor.accumulateParallel(ParallelAccumulationExecutor.java:80)
	at com.hazelcast.map.impl.query.ParallelAccumulationExecutor.execute(ParallelAccumulationExecutor.java:60)
	at com.hazelcast.map.impl.query.AggregationResultProcessor.populateResult(AggregationResultProcessor.java:40)
	at com.hazelcast.map.impl.query.AggregationResultProcessor.populateResult(AggregationResultProcessor.java:27)
	at com.hazelcast.map.impl.query.QueryRunner.populateNonEmptyResult(QueryRunner.java:241)
	at com.hazelcast.map.impl.query.QueryRunner.runIndexOrPartitionScanQueryOnOwnedPartitions(QueryRunner.java:150)
	at com.hazelcast.map.impl.query.QueryRunner.runIndexOrPartitionScanQueryOnOwnedPartitions(QueryRunner.java:107)
	at com.hazelcast.map.impl.query.QueryOperation.callInternal(QueryOperation.java:89)
	at com.hazelcast.map.impl.query.QueryOperation.call(QueryOperation.java:78)
	at com.hazelcast.spi.impl.operationservice.impl.OperationRunnerImpl.call(OperationRunnerImpl.java:272)
	at com.hazelcast.spi.impl.operationservice.impl.OperationRunnerImpl.run(OperationRunnerImpl.java:248)
	at com.hazelcast.spi.impl.operationservice.impl.OperationRunnerImpl.run(OperationRunnerImpl.java:213)
	at com.hazelcast.spi.impl.operationexecutor.impl.OperationExecutorImpl.run(OperationExecutorImpl.java:411)
	at com.hazelcast.spi.impl.operationexecutor.impl.OperationExecutorImpl.runOrExecute(OperationExecutorImpl.java:438)
	at com.hazelcast.spi.impl.operationservice.impl.Invocation.doInvokeLocal(Invocation.java:600)
	at com.hazelcast.spi.impl.operationservice.impl.Invocation.doInvoke(Invocation.java:579)
	at com.hazelcast.spi.impl.operationservice.impl.Invocation.invoke0(Invocation.java:540)
	at com.hazelcast.spi.impl.operationservice.impl.Invocation.invoke(Invocation.java:240)
	at com.hazelcast.spi.impl.operationservice.impl.OperationServiceImpl.invokeOnTarget(OperationServiceImpl.java:359)
	at com.hazelcast.map.impl.query.QueryEngineImpl.dispatchFullQueryOnAllMembersOnQueryThread(QueryEngineImpl.java:291)
	at com.hazelcast.map.impl.query.QueryEngineImpl.dispatchFullQueryOnQueryThread(QueryEngineImpl.java:267)
	at com.hazelcast.map.impl.query.QueryEngineImpl.dispatchOnQueryThreads(QueryEngineImpl.java:152)
	at com.hazelcast.map.impl.query.QueryEngineImpl.doRunOnQueryThreads(QueryEngineImpl.java:145)
	at com.hazelcast.map.impl.query.QueryEngineImpl.runOnGivenPartitions(QueryEngineImpl.java:124)
	at com.hazelcast.map.impl.query.QueryEngineImpl.execute(QueryEngineImpl.java:94)
	at com.hazelcast.map.impl.proxy.MapProxySupport.executeQueryInternal(MapProxySupport.java:1378)
	at com.hazelcast.map.impl.proxy.MapProxyImpl.aggregate(MapProxyImpl.java:854)
	at SessionRepository.sessionCountFor(SessionRepository.java:33)
```
                                  




   
