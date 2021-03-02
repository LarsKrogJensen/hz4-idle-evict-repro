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






   
