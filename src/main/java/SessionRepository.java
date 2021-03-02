import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.query.Predicates;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class SessionRepository {

    private final IMap<String, Session> sessionMap;

    public SessionRepository(IMap<String, Session> sessionMap) {
        this.sessionMap = sessionMap;
        sessionMap.addLocalEntryListener(new Listener());
    }

    public void store(Session session) {
        sessionMap.set(session.sessionId(), session);
    }

    public int size() {
        return sessionMap.size();
    }

    public SessionStats stats() {
        return sessionMap.aggregate(new SessionStatsAggregator());
    }

    public int sessionCountFor(long userId) {
        return sessionMap.aggregate(Aggregators.count(), Predicates.equal("userId", userId)).intValue();
    }


    private class Listener implements
            EntryExpiredListener<String, Session>,
            EntryRemovedListener<String, Session> {

        private final ExecutorService executor;

        public Listener() {
            executor = ForkJoinPool.commonPool();
        }

        @Override
        public void entryExpired(EntryEvent<String, Session> event) {
            entryRemoved(event);
        }

        @Override
        public void entryRemoved(EntryEvent<String, Session> event) {
            executor.execute(() -> {
                int sessionCount = sessionCountFor(event.getOldValue().userId());
                // enable to verify that entries are evicted and query does work - but
                // polutes the console output
//                System.out.println("Removed/evitect remaining sessions: " + sessionCount);
            });
        }
    }
}
