## Hazelcast 4.1.1 eviction issue reproducer

Scaled down reproducer to show that running an aggregate query when an entry is evicted makes the eviction behave slow and weird.
           
Issue is weird and subtle and was not properly caught while upgrading hazelcast 3.12 -> 4.1.1, once in production we noticed that session count kept increasing at an alarming rate and not being evicted as with hazelcast 3.12

Note that issue is reproduced with a single node, i.e. no need for several nodes although problem is the same

### Scenario
Stores user sessions in a hazelcast map with idle timeout of 30 seconds, every user might have several sessions and when a session (entry in map) is expired we run a query to see if user has any remaining sessions. In real app we do consider that as final logout. 
      
After adding sessions to map, they are not 'touched'.

In test scenario we run an aggregation every 3:e second to log number of sessions and users that are 'alive'.

#### Output

Notice the jumps in the stats (users, sessions) while entries are very, very slowly being evicted.

``
10:19:55.705 [main] INFO  Main - Added 5000 sessions
10:19:55.706 [main] INFO  Main - Watching stats every 3 seconds, expects that entries starts to expiry after 30 seconds
10:19:58.743 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:01.718 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:04.715 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:07.713 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:10.714 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:13.713 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:16.715 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:19.714 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:22.718 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:25.713 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:28.714 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:31.718 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:34.720 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:37.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:40.720 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:43.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:46.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:49.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:52.718 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:20:55.717 [pool-1-thread-1] INFO  Main - Repo stats, size: 4505 sessions: 1560 users: 411 <-- eviction starts, expected 0/0
10:20:58.719 [pool-1-thread-1] INFO  Main - Repo stats, size: 4505 sessions: 1560 users: 411
10:21:01.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 4199 sessions: 2539 users: 668 <-- stats jumps....
10:21:04.715 [pool-1-thread-1] INFO  Main - Repo stats, size: 4199 sessions: 2539 users: 668
10:21:07.713 [pool-1-thread-1] INFO  Main - Repo stats, size: 4017 sessions: 3132 users: 823
10:21:10.714 [pool-1-thread-1] INFO  Main - Repo stats, size: 3907 sessions: 3472 users: 913
10:21:13.714 [pool-1-thread-1] INFO  Main - Repo stats, size: 3907 sessions: 3472 users: 913
10:21:16.712 [pool-1-thread-1] INFO  Main - Repo stats, size: 3846 sessions: 3646 users: 960
10:21:19.715 [pool-1-thread-1] INFO  Main - Repo stats, size: 3846 sessions: 3646 users: 960
10:21:22.715 [pool-1-thread-1] INFO  Main - Repo stats, size: 3812 sessions: 3732 users: 984
10:21:25.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 3791 sessions: 3766 users: 995
10:21:28.714 [pool-1-thread-1] INFO  Main - Repo stats, size: 3791 sessions: 3766 users: 995
10:21:31.717 [pool-1-thread-1] INFO  Main - Repo stats, size: 3785 sessions: 3785 users: 1000
10:21:34.717 [pool-1-thread-1] INFO  Main - Repo stats, size: 3785 sessions: 3785 users: 1000
10:21:37.714 [pool-1-thread-1] INFO  Main - Repo stats, size: 3785 sessions: 3785 users: 1000
10:21:40.715 [pool-1-thread-1] INFO  Main - Repo stats, size: 3785 sessions: 3785 users: 1000
10:21:43.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 3785 sessions: 3785 users: 1000
10:21:46.717 [pool-1-thread-1] INFO  Main - Repo stats, size: 3785 sessions: 3785 users: 1000
10:21:49.728 [pool-1-thread-1] INFO  Main - Repo stats, size: 3785 sessions: 3785 users: 1000
10:21:52.717 [pool-1-thread-1] INFO  Main - Repo stats, size: 3785 sessions: 3785 users: 1000
10:21:55.717 [pool-1-thread-1] INFO  Main - Repo stats, size: 3612 sessions: 2614 users: 738
10:21:58.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 3612 sessions: 2614 users: 738
10:22:01.714 [pool-1-thread-1] INFO  Main - Repo stats, size: 3388 sessions: 2147 users: 671
10:22:04.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 3388 sessions: 2147 users: 671
10:22:07.715 [pool-1-thread-1] INFO  Main - Repo stats, size: 3143 sessions: 2141 users: 734
10:22:10.717 [pool-1-thread-1] INFO  Main - Repo stats, size: 2956 sessions: 2234 users: 805
10:22:13.713 [pool-1-thread-1] INFO  Main - Repo stats, size: 2956 sessions: 2234 users: 805
10:22:16.717 [pool-1-thread-1] INFO  Main - Repo stats, size: 2796 sessions: 2391 users: 888
10:22:19.714 [pool-1-thread-1] INFO  Main - Repo stats, size: 2796 sessions: 2391 users: 888
10:22:22.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 2685 sessions: 2499 users: 943
10:22:25.718 [pool-1-thread-1] INFO  Main - Repo stats, size: 2613 sessions: 2556 users: 978
10:22:28.718 [pool-1-thread-1] INFO  Main - Repo stats, size: 2613 sessions: 2556 users: 978
10:22:31.715 [pool-1-thread-1] INFO  Main - Repo stats, size: 2604 sessions: 2548 users: 978
10:22:34.713 [pool-1-thread-1] INFO  Main - Repo stats, size: 2604 sessions: 2548 users: 978
10:22:37.715 [pool-1-thread-1] INFO  Main - Repo stats, size: 2599 sessions: 2552 users: 981
10:22:40.716 [pool-1-thread-1] INFO  Main - Repo stats, size: 2591 sessions: 2559 users: 985
... ongoing for 5-10 minutes until eventually
10:25:49.708 [pool-1-thread-1] INFO  Main - Repo stats, size: 167 sessions: 22 users: 22
10:25:52.711 [pool-1-thread-1] INFO  Main - Repo stats, size: 131 sessions: 12 users: 12
10:25:55.712 [pool-1-thread-1] INFO  Main - Repo stats, size: 101 sessions: 10 users: 10
10:25:58.710 [pool-1-thread-1] INFO  Main - Repo stats, size: 101 sessions: 10 users: 10
10:26:01.709 [pool-1-thread-1] INFO  Main - Repo stats, size: 58 sessions: 11 users: 11
10:26:04.709 [pool-1-thread-1] INFO  Main - Repo stats, size: 58 sessions: 11 users: 11
10:26:07.709 [pool-1-thread-1] INFO  Main - Repo stats, size: 25 sessions: 9 users: 9
10:26:10.709 [pool-1-thread-1] INFO  Main - Repo stats, size: 10 sessions: 9 users: 9
10:26:13.709 [pool-1-thread-1] INFO  Main - Repo stats, size: 10 sessions: 9 users: 9
10:26:16.711 [pool-1-thread-1] INFO  Main - Repo stats, size: 4 sessions: 4 users: 4
10:26:19.709 [pool-1-thread-1] INFO  Main - Repo stats, size: 4 sessions: 4 users: 4
10:26:22.711 [pool-1-thread-1] INFO  Main - Repo stats, size: 2 sessions: 2 users: 2
10:26:25.709 [pool-1-thread-1] INFO  Main - Repo stats, size: 2 sessions: 1 users: 1
10:26:28.711 [pool-1-thread-1] INFO  Main - Repo stats, size: 2 sessions: 1 users: 1
10:26:31.710 [pool-1-thread-1] INFO  Main - Repo stats, size: 1 sessions: 1 users: 1
10:26:34.712 [pool-1-thread-1] INFO  Main - Repo stats, size: 1 sessions: 1 users: 1
10:26:37.712 [pool-1-thread-1] INFO  Main - Repo stats, size: 1 sessions: 1 users: 1
10:26:40.712 [pool-1-thread-1] INFO  Main - Repo stats, size: 1 sessions: 1 users: 1
10:26:43.711 [pool-1-thread-1] INFO  Main - Repo stats, size: 1 sessions: 1 users: 1
10:26:46.710 [pool-1-thread-1] INFO  Main - Repo stats, size: 1 sessions: 1 users: 1
10:26:49.709 [pool-1-thread-1] INFO  Main - Repo stats, size: 1 sessions: 1 users: 1
10:26:52.709 [pool-1-thread-1] INFO  Main - Repo stats, size: 1 sessions: 1 users: 1
10:26:55.711 [pool-1-thread-1] INFO  Main - Repo stats, size: 1 sessions: 1 users: 1
10:26:58.711 [pool-1-thread-1] INFO  Main - Repo stats, size: 1 sessions: 1 users: 1
10:27:01.713 [pool-1-thread-1] INFO  Main - Repo stats, size: 0 sessions: 0 users: 0
10:27:04.713 [pool-1-thread-1] INFO  Main - Repo stats, size: 0 sessions: 0 users: 0
``


#### Output if disable aggregate during eviction callback
                                                    
If we instead comment out, SessionRepository:55:
//                int sessionCount = sessionCountFor(event.getOldValue().userId());

and run again entries are evicted much more prompty without and weird stats 'jumps'

``
10:05:58.660 [main] INFO  Main - Added 5000 sessions
10:05:58.661 [main] INFO  Main - Watching stats every 3 seconds, expects that entries starts to expiry after 30 seconds
10:06:01.692 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:04.672 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:07.670 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:10.670 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:13.671 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:16.668 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:19.670 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:22.670 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:25.667 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:28.670 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:31.673 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:34.670 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:37.672 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:40.669 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:43.668 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:46.669 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:49.669 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:52.669 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:55.672 [pool-1-thread-1] INFO  Main - Repo stats, size: 5000 sessions: 5000 users: 1000
10:06:58.668 [pool-1-thread-1] INFO  Main - Repo stats, size: 4513 sessions: 0 users: 0   <-- eviction starts
10:07:01.671 [pool-1-thread-1] INFO  Main - Repo stats, size: 4513 sessions: 0 users: 0
10:07:04.668 [pool-1-thread-1] INFO  Main - Repo stats, size: 4022 sessions: 0 users: 0
10:07:07.667 [pool-1-thread-1] INFO  Main - Repo stats, size: 4022 sessions: 0 users: 0
10:07:10.668 [pool-1-thread-1] INFO  Main - Repo stats, size: 3482 sessions: 0 users: 0
10:07:13.670 [pool-1-thread-1] INFO  Main - Repo stats, size: 2952 sessions: 0 users: 0
10:07:16.667 [pool-1-thread-1] INFO  Main - Repo stats, size: 2952 sessions: 0 users: 0
10:07:19.670 [pool-1-thread-1] INFO  Main - Repo stats, size: 2452 sessions: 0 users: 0
10:07:22.667 [pool-1-thread-1] INFO  Main - Repo stats, size: 2452 sessions: 0 users: 0
10:07:25.671 [pool-1-thread-1] INFO  Main - Repo stats, size: 1950 sessions: 0 users: 0
10:07:28.671 [pool-1-thread-1] INFO  Main - Repo stats, size: 1462 sessions: 0 users: 0
10:07:31.671 [pool-1-thread-1] INFO  Main - Repo stats, size: 1462 sessions: 0 users: 0
10:07:34.668 [pool-1-thread-1] INFO  Main - Repo stats, size: 1016 sessions: 0 users: 0
10:07:37.669 [pool-1-thread-1] INFO  Main - Repo stats, size: 1016 sessions: 0 users: 0
10:07:40.667 [pool-1-thread-1] INFO  Main - Repo stats, size: 505 sessions: 0 users: 0
10:07:43.668 [pool-1-thread-1] INFO  Main - Repo stats, size: 16 sessions: 0 users: 0
10:07:46.667 [pool-1-thread-1] INFO  Main - Repo stats, size: 16 sessions: 0 users: 0
10:07:49.667 [pool-1-thread-1] INFO  Main - Repo stats, size: 0 sessions: 0 users: 0
``






   
