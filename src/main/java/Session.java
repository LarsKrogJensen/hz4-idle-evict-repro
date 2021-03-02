import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

class Session implements DataSerializable {
    private String sessionId;
    private long userId;

    @SuppressWarnings("unused")
    public Session() {
    }

    public Session(String sessionId, long userId) {
        this.sessionId = sessionId;
        this.userId = userId;
    }

    public String sessionId() {
        return sessionId;
    }

    public long userId() {
        return userId;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(sessionId);
        out.writeLong(userId);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        sessionId = in.readUTF();
        userId = in.readLong();
    }
}
