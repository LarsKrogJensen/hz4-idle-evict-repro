import com.hazelcast.aggregation.Aggregator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SessionStatsAggregator implements Aggregator<Map.Entry<String, Session>, SessionStats> {

    // userId -> {sessionId}
    private final Map<Long, Set<String>> sessionsPerUser = new HashMap<>();

    @Override
    public void accumulate(Map.Entry<String, Session> input) {
        Session session = input.getValue();

        sessionsPerUser.compute(session.userId(), (userId, sessionIds) -> {
            Set<String> set = sessionIds != null ? sessionIds : new HashSet<>();
            set.add(session.sessionId());
            return set;
        });
    }

    @Override
    public void combine(Aggregator aggregator) {
        SessionStatsAggregator other = (SessionStatsAggregator) aggregator;
        other.sessionsPerUser.forEach((userId, sessionIds) ->
                sessionsPerUser.merge(userId, sessionIds, (s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                })
        );
    }

    @Override
    public SessionStats aggregate() {
        int sessions = sessionsPerUser.values().stream().mapToInt(Set::size).sum();

        return new SessionStats(sessions, sessionsPerUser.size());
    }
}
