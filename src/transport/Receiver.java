 package transport;

public class Receiver extends NetworkHost {
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
     *          creates a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and a payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and an empty payload
     *    Methods:
     *      void setSeqnum(int seqnum)
     *          sets the Packet's sequence field to seqnum
     *      void setAcknum(int acknum)
     *          sets the Packet's ack field to acknum
     *      void setChecksum(int checksum)
     *          sets the Packet's checksum to checksum
     *      void setPayload(String payload) 
     *          sets the Packet's payload to payload
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */
    
    // Add any necessary class variables here. They can hold expectedSequenceNumber information for the receiver.
    Packet sndpkt;
    int expectedSequenceNumber;
    // Also add any necessary methods (e.g. checksum of a String)
    
    
    // This is the constructor.  Don't touch!
    public Receiver(int entityName) {
        super(entityName);
    }
    //method for creating and checking checksums, for no payload input empty string
    public int checkSum(String payload, int ack, int sequenceNumber){
        int checkSum = 0;
        byte[] bytes = payload.getBytes();
        for(byte b:bytes){
            checkSum += b;
        }   
        checkSum += sequenceNumber;
        checkSum += ack;
        return checkSum;
    }

    // This method will be called once, before any of your other receiver-side methods are called. 
    // It can be used to do any required initialisation (e.g. of member variables you add to control the expectedSequenceNumber of the receiver).
    @Override
    public void init() {
        //sndpkt is the current ack that needs to be sent
        sndpkt = null;
        //the expected sequence number
        expectedSequenceNumber = 0;
        
    }

    // This method will be called whenever a packet sent from the sender(i.e. as a result of a udtSend() being called by the Sender )
    // arrives at the receiver. 
    // The argument "packet" is the (possibly corrupted) packet sent from the sender.
    @Override
    public void input(Packet packet) {
        //if the packet is not corrupt and has the expected sequence number
        if(packet.getChecksum() == checkSum(packet.getPayload(), packet.getAcknum(), packet.getSeqnum()) && expectedSequenceNumber == packet.getSeqnum()){
            //deliver the data to the applciation layer and create an ack to send back
            deliverData(packet.getPayload());
            //has the sequence and acknumber of the currently received packet
            sndpkt = new Packet(packet.getSeqnum(), packet.getAcknum(), checkSum("", packet.getAcknum(), packet.getSeqnum())); 
            udtSend(sndpkt);
            //XOR the expected sequence number like in the sender
            expectedSequenceNumber ^= 1;
        }
        else if(packet.getChecksum() != checkSum(packet.getPayload(), packet.getAcknum(), packet.getSeqnum()) || expectedSequenceNumber != packet.getSeqnum()){
            //if the first packet was not received properly, do not send anything, wait for a timeout at the sender side
            //send back the last ack if the current one was a duplicate or corrupted
            if(sndpkt != null){
                udtSend(sndpkt);
            }
        }
                
    }
}
