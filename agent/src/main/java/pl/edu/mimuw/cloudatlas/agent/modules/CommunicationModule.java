package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.commons.lang.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.NetworkMessage;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Module(value = "CommunicationModule", unique = true)
public class CommunicationModule extends ModuleBase {

    private static Logger LOGGER = LogManager.getLogger(CommunicationModule.class);

    private static final Integer PORT = 19911;

    private static final Integer HEADER_SIZE = 24;
    private static final Integer PACKET_SIZE = 10;

    public static final int SEND = 1;

    private Long nextId = 0L;
    private ConcurrentHashMap<PartialMessageId, PartialMessage> partialMessages = new ConcurrentHashMap<>();
    private BlockingQueue<NetworkMessage> toSendQueue = new LinkedBlockingQueue<>();


    @Handler(SEND)
    private final MessageHandler<?> h1 = new MessageHandler<NetworkMessage>() {
        @Override
        protected void handle(NetworkMessage message) {
            try {

//                LOGGER.debug("SEND handler " + ((IdMessage) message.getMessage()).getId());

                toSendQueue.put(message);

            } catch (InterruptedException e) {
                LOGGER.error("Failed to send message.", e);
            }
        }
    };

    private final Thread sender = new Thread(new Runnable() {

        @Override
        public void run() {
            try {

                while (true) {

                    NetworkMessage msg = toSendQueue.take();
                    InetSocketAddress address = new InetSocketAddress(msg.getTarget(), msg.getPort());

                    DatagramChannel channel = DatagramChannel.open();

                    byte [] bytes = SerializationUtils.serialize(msg.getMessage());
                    int count = (int) Math.ceil((double) bytes.length / (double) PACKET_SIZE);
                    long messageId = nextId++;

                    for (int i = 0; i < count; ++i) {

                        int offset = i*PACKET_SIZE;
                        int length = Math.min(PACKET_SIZE, bytes.length - offset);

                        ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE + HEADER_SIZE)
                                .putLong(getEventQueueId().getMostSignificantBits())
                                .putLong(messageId)
                                .putInt(i)
                                .putInt(count)
                                .put(ByteBuffer.wrap(bytes, offset, length));

                        buffer.flip();
                        channel.send(buffer, address);
                    }

//                    NetworkMessage networkMessage = new NetworkMessage(localhost, PORT, new IdMessage(2137));
//                    sendMessage(new Address(CommunicationModule.class, SEND), networkMessage);
                }

            } catch (IOException | InterruptedException e) {
                LOGGER.warn("Failed to send message.", e);
            }
        }
    });

    private final Thread receiver = new Thread(new Runnable() {

        private DatagramChannel channel;

        @Override
        public void run() {
            try {
                channel = DatagramChannel.open();
                channel.socket().bind(new InetSocketAddress(PORT));

                while (true) {

                    ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + PACKET_SIZE);

                    channel.receive(buffer);
                    buffer.flip();

                    long eqId = buffer.getLong();
                    long msgId = buffer.getLong();
                    int nr = buffer.getInt();
                    int count = buffer.getInt();

                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);

                    byte[] messageBytes = null;

                    if (count == 1) {
                        messageBytes = bytes;
                    } else {
                        PartialMessageId messageId = new PartialMessageId(eqId, msgId);
                        PartialMessage partialMessage = partialMessages.get(messageId);

                        if (partialMessage == null) {
                            partialMessage = new PartialMessage(count);
                            partialMessages.put(messageId, partialMessage);
                        }

                        partialMessage.addPartialData(new PartialData(nr, bytes));

                        if (partialMessage.isReady()) {
                            partialMessages.remove(messageId);
                            messageBytes = partialMessage.buildMessage();
                        }
                    }

                    if (messageBytes != null) {
                        Message msg = (Message) SerializationUtils.deserialize(messageBytes);
                        sendMessage(msg.getAddress(), msg);
                    }
                }
            } catch (IOException e) {}
        }
    });

    public CommunicationModule() {

        sender.start();
        receiver.start();
    }

    public static class PartialData implements Comparable<PartialData> {
        private Integer number;
        private byte [] bytes;

        public PartialData(Integer number, byte[] bytes) {
            this.number = number;
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }

        @Override
        public int compareTo(PartialData partialData) {
            return partialData.number >= number ? -1 : 0;
        }
    }

    public static class PartialMessageId {
        Long SenderId;
        Long MessageId;

        public PartialMessageId(Long senderId, Long messageId) {
            SenderId = senderId;
            MessageId = messageId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PartialMessageId that = (PartialMessageId) o;

            if (SenderId != null ? !SenderId.equals(that.SenderId) : that.SenderId != null) return false;
            return MessageId != null ? MessageId.equals(that.MessageId) : that.MessageId == null;
        }

        @Override
        public int hashCode() {
            int result = SenderId != null ? SenderId.hashCode() : 0;
            result = 31 * result + (MessageId != null ? MessageId.hashCode() : 0);
            return result;
        }
    }

    public static class PartialMessage {
        private Integer requiredPackets;
        private List<PartialData> partialDataList = new ArrayList<>();

        public PartialMessage(Integer totalPackets) {
            this.requiredPackets = totalPackets;
        }

        public void addPartialData(PartialData partialData) {
            partialDataList.add(partialData);
        }

        boolean isReady() {
            return partialDataList.size() == requiredPackets;
        }

        public byte[] buildMessage() {
                Collections.sort(partialDataList);

                ByteBuffer bytes = ByteBuffer.allocate(requiredPackets * PACKET_SIZE);
                for (PartialData data : partialDataList) {
                    bytes.put(data.getBytes());
                }
                return bytes.array();
        }
    }
}
