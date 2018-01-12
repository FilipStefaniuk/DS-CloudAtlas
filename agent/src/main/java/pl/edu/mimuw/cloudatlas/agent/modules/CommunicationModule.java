package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.commons.lang.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.FromNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.NetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.TimerMessage;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Module(value = "CommunicationModule", unique = true)
public class CommunicationModule extends ModuleBase {

    private static Logger LOGGER = LogManager.getLogger(CommunicationModule.class);

    private static final Integer HEADER_SIZE = 32;
    private static final Integer PACKET_SIZE = 500;

    public static final int SEND = 1;
    public static final int CLEAR = 2;

    private Integer port;
    private PathName agentId;
    private Long clearDelay;
    private Long retryConnectDelay;

    private Long nextId = 0L;
    private ConcurrentHashMap<PartialMessageId, PartialMessage> partialMessages = new ConcurrentHashMap<>();
    private BlockingQueue<NetworkMessage<?>> toSendQueue = new LinkedBlockingQueue<>();

    @Handler(SEND)
    private final MessageHandler<?> h1 = new MessageHandler<NetworkMessage>() {
        @Override
        protected void handle(NetworkMessage message) {
            try {

                LOGGER.debug("SEND: IN: " + message.toString());

                toSendQueue.put(message);

            } catch (Exception e) {
                LOGGER.error("SEND: " + e.getMessage(), e);
            }
        }
    };

    @Handler(CLEAR)
    private final MessageHandler<?> h2 = new MessageHandler<GenericMessage<PartialMessageId>>() {
        @Override
        protected void handle(GenericMessage<PartialMessageId> message) {
            try {

                LOGGER.debug("CLEAR: IN: " + message.toString());

                partialMessages.remove(message.getData());

            } catch (Exception e) {
                LOGGER.error("CLEAR: " + e.getMessage(), e);
            }
        }
    };

    private final Thread sender = new Thread(new Runnable() {

        @Override
        public void run() {
            while (true) {

                try {
                    NetworkMessage msg = toSendQueue.take();
                    InetSocketAddress address = new InetSocketAddress(msg.getTarget(), msg.getPort());

                    DatagramChannel channel = DatagramChannel.open();

                    FromNetworkMessage<?> toSendMessage =
                            new FromNetworkMessage<>(msg.getMessage().getData(), InetAddress.getLocalHost(), port, agentId);
                    toSendMessage.setAddress(msg.getMessage().getAddress());

                    byte [] bytes = SerializationUtils.serialize(toSendMessage);
                    int count = (int) Math.ceil((double) bytes.length / (double) PACKET_SIZE);
                    long messageId = nextId++;

                    for (int i = 0; i < count; ++i) {

                        int offset = i*PACKET_SIZE;
                        int length = Math.min(PACKET_SIZE, bytes.length - offset);
                        long timestamp = System.currentTimeMillis();

                        ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE + HEADER_SIZE)
                                .putLong(getEventQueueId().getMostSignificantBits())
                                .putLong(messageId)
                                .putLong(timestamp)
                                .putInt(i)
                                .putInt(count)
                                .put(ByteBuffer.wrap(bytes, offset, length));

                        buffer.flip();
                        channel.send(buffer, address);
                    }

                } catch (Exception e) {
                    LOGGER.error("SENDER: " + e.getMessage(), e);
                }
            }
        }
    });

    private final Thread receiver = new Thread(new Runnable() {


        @Override
        public void run() {
            while(true) {
                try {

                    LOGGER.debug("RECEIVER: Binding new socket.");
                    DatagramChannel channel = DatagramChannel.open();
                    channel.socket().bind(new InetSocketAddress(port));

                    while (true) {

                        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + PACKET_SIZE);

                        channel.receive(buffer);
                        buffer.flip();

                        long timestamp_rcv = System.currentTimeMillis();

                        long eqId = buffer.getLong();
                        long msgId = buffer.getLong();
                        long timestamp_snd = buffer.getLong();
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
                                Long timerId = nextId++;
                                partialMessage = new PartialMessage(count, timerId, timestamp_snd, timestamp_rcv);
                                partialMessages.put(messageId, partialMessage);

                                Message timerMsg = new GenericMessage<>(messageId);
                                timerMsg.setAddress(new Address(CommunicationModule.class, CLEAR));
                                Message msg = new TimerMessage(timerId, getName(),
                                        System.currentTimeMillis(), clearDelay, timerMsg);
                                Address address = new Address(TimerModule.class, TimerModule.SCHEDULE);
                                sendMessage(address, msg);
                                LOGGER.debug("RECEIVER: OUT: " + msg.toString());
                            }

                            partialMessage.addPartialData(new PartialData(nr, bytes));

                            if (partialMessage.isReady()) {
                                partialMessages.remove(messageId);
                                messageBytes = partialMessage.buildMessage();
                                Long timerId = partialMessage.getTimerId();
                                timestamp_snd = partialMessage.timestamp_snd;
                                timestamp_rcv = partialMessage.timestamp_rcv;

                                Message msg = new TimerMessage(timerId, getName(), null, null, null);
                                Address address = new Address(TimerModule.class, TimerModule.CANCEL);
                                sendMessage(address, msg);
                                LOGGER.debug("RECEIVER: OUT: " + msg.toString());
                            }
                        }

                        if (messageBytes != null) {
                            FromNetworkMessage msg = (FromNetworkMessage) SerializationUtils.deserialize(messageBytes);
                            msg.setTimestampSnd(timestamp_snd);
                            msg.setTimestampRcv(timestamp_rcv);
                            sendMessage(msg.getAddress(), msg);
                            LOGGER.debug("RECEIVER: OUT: " + msg.toString());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("RECEIVER: " + e.getMessage(), e);
                }

                try {
                    Thread.sleep(retryConnectDelay);
                } catch (Exception e) {
                    LOGGER.error("RECEIVER: " + e.getMessage(), e);
                }
            }
        }
    });

    @Override
    public void initialize(ConfigurationProvider configurationProvider) {

        port = configurationProvider.getProperty("Agent.port", Integer.class);
        clearDelay = configurationProvider.getProperty("Agent.CommunicationModule.clearDelay", Long.class);
        retryConnectDelay = configurationProvider.getProperty("Agent.CommunicationModule.retryBindSocketDelay", Long.class);
        agentId = new PathName(configurationProvider.getProperty("Agent.agentId", String.class));

        sender.start();
        receiver.start();
    }

    /*********************************************************************************************
                                            Subclasses
     ********************************************************************************************/

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

    public static class PartialMessageId implements Serializable {
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
        private Long timerId;
        private Long timestamp_snd;
        private Long timestamp_rcv;

        public PartialMessage(Integer totalPackets, Long timerId, Long time_snd, Long time_rcv) {
            this.requiredPackets = totalPackets;
            this.timerId = timerId;
            this.timestamp_snd = time_snd;
            this.timestamp_rcv = time_rcv;
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

        public Long getTimerId() {
            return timerId;
        }

    }
}
