package transport;

public class Sender extends NetworkHost {
    
    /*
     * Predefined Constant (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and Packet payload
     *
     *
     * Predefined Member Methods:
     *
     *  void startTimer(double increment):
     *       Starts a timer, which will expire in "increment" time units, causing the interrupt handler to be called.  You should only call this in the Sender class.
     *  void stopTimer():
     *       Stops the timer. You should only call this in the Sender class.
     *  void udtSend(Packet p)
     *       Sends the packet "p" into the network to arrive at other host
     *  void deliverData(String dataSent)
     *       Passes "dataSent" up to app layer. You should only call this in the Receiver class.
     *
     *  Predefined Classes:
     *
     *  NetworkSimulator: Implements the core functionality of the simulator
     *
     *  double getTime()
     *       Returns the current time in the simulator. Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().getTime()
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().printEventList()
     *
     *  Message: Used to encapsulate a message coming from the application layer
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      void setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *      String getData():
     *          returns the data contained in the message
     *
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet, which is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload):
     *          creates a new Packet with a sequence field of "seq", an ack field of "ack", a checkSum field of "check", and a payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an ack field of "ack", a checkSum field of "check", and an empty payload
     *    Methods:
     *      void setSeqnum(int seqnum)
     *          sets the Packet's sequence field to seqnum
     *      void setAcknum(int acknum)
     *          sets the Packet's ack field to acknum
     *      void setChecksum(int checkSum)
     *          sets the Packet's checkSum to checkSum
     *      void setPayload(String payload) 
     *          sets the Packet's payload to payload
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checkSum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */

    // Add any necessary class variables here. They can hold state information for the sender. 
    private int packetNumber;
    private Packet currentPacket;
    private boolean waitingForAck;
    // Also add any necessary methods (e.g. checkSum of a String)
    /**
     * calculates the checksum of the packet for setting and checking
     * @param sequenceNumber the sequence number of the packet
     * @param ackNumber the acknowledgement number
     * @param payload the payload of the packet
     * @return returns the integer the sum of the packets payload, sequence number and acknumber
     */
    public int checkSum(int sequenceNumber, int ackNumber, String payload){
        int checkSum = 0;
        byte[] bytes = payload.getBytes();
        for(byte b:bytes){
            checkSum += b;
        }
        checkSum += ackNumber;
        checkSum += sequenceNumber;
        return checkSum;
    }

    // This is the constructor.  Don't touch!
    public Sender(int entityName) {
        super(entityName);
    }

    // This method will be called once, before any of your other sender-side methods are called. 
    // It can be used to do any required initialisation (e.g. of member variables you add to control the state of the sender).
     @Override
    public void init() {
        packetNumber = 0;
        currentPacket = null;
        waitingForAck = false;
    } 
    
    // This method will be called whenever the app layer at the sender has a message to send.  
    // The job of your protocol is to ensure that the data in such a message is delivered in-order, and correctly, to the receiving application layer.
    @Override
    public void output(Message message) {
        //boolean to check and makes that there are no packets in transit, if there are reject the message from the applciation layer
        if(!waitingForAck){
            //create a new packet with sequence number and packet number, calculate the checksum of it
            currentPacket = new Packet(packetNumber, packetNumber, checkSum(packetNumber, packetNumber, message.getData()), message.getData());
            udtSend(currentPacket);
            //start a timer
            startTimer(40);
            //indicate that a packet has been sent
            waitingForAck = true;
        }
    }
    
    
    // This method will be called whenever a packet sent from the receiver (i.e. as a result of a udtSend() being done by a receiver procedure) arrives
    // at the sender.  
    // "packet" is the (possibly corrupted) packet sent from the receiver.
    @Override
    public void input(Packet packet) {
        //if the packet is not corrupt and has the right ack number
        if(packet.getChecksum() == checkSum(packet.getAcknum(), packet.getSeqnum(), "") && packet.getAcknum() == packetNumber){
            //stop the timer and change the packet number, achieved by XORing the packet number with 1
            stopTimer();
            packetNumber ^= 1;
            //indicate that we are no longer waiting for a response and we are ready to send another packet in to the network
            waitingForAck = false;
        }
    }
    
    
    // This method will be called when the senders's timer expires (thus generating a timer interrupt). 
    // You'll probably want to use this method to control the retransmission of packets. 
    // See startTimer() and stopTimer(), above, for how the timer is started and stopped. 
    @Override
    public void timerInterrupt() {
        udtSend(currentPacket);
        startTimer(40);
    }
}
