import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.hazelcast.core.Hazelcast.newHazelcastInstance;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        HazelcastInstance hz = initHazelcast();
        SessionRepository repo = new SessionRepository(hz.getMap("sessions"));

        primeMap(repo, 1_000, 5);
        queryStats(repo);
    }

    private static void primeMap(SessionRepository repo, int userCount, int sessionsPerUser) {
        long userIdSeed = ThreadLocalRandom.current().nextLong(0, 1_000_000);

        IntStream.range(0, userCount).forEach(i -> {
            long userId = userIdSeed + i;

            IntStream.range(0, sessionsPerUser).forEach(j -> {
                var session = new Session(
                        UUID.randomUUID().toString(),
                        userId
                );
                repo.store(session);
            });
        });

        log.info("Added {} sessions", sessionsPerUser * userCount);
    }

    private static void queryStats(SessionRepository repo) {
        log.info("Watching stats every 3 seconds, expects that entries starts to expiry after 30 seconds");
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
        service.scheduleAtFixedRate(() -> {
            var stats = repo.stats();
            log.info("Repo stats, size: {} sessions: {} users: {}", repo.size(), stats.sessions, stats.users);
        }, 3, 3, TimeUnit.SECONDS);
    }

    private static HazelcastInstance initHazelcast() {

        Config config = new Config()
                .setClusterName("hz4-repro")
                .setProperty("hazelcast.logging.type", "slf4j")
                .setProperty("hazelcast.index.copy.behavior", "NEVER")
                .setProperty("hazelcast.shutdownhook.enabled", "false")
                .setProperty("hazelcast.shutdownhook.policy", "GRACEFUL")
                .setProperty("hazelcast.phone.home.enabled", "false")
                .setProperty("hazelcast.jmx", "false");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);

        config.addMapConfig(new MapConfig()
                .setName("sessions")
                .setBackupCount(1)
                .setStatisticsEnabled(true)
                .setReadBackupData(false)
                .setMaxIdleSeconds(60)
                .addIndexConfig(new IndexConfig(IndexType.HASH, "userId"))
                .setInMemoryFormat(InMemoryFormat.BINARY));

        return newHazelcastInstance(config);
    }

}
