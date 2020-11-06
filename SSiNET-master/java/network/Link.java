package network;


import config.Constant;
import network.host.Host;
import network.port.InputPort;
import network.port.IntegratedPort;
import simulator.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dandoh on 6/27/17.
 */
public class Link extends NetworkObject {
    Map<Integer, IntegratedPort> ports;
    Map<Integer, Boolean> isBusy;
    private long bandwidth;
    private double length;

    public Link(Node u, Node v) {
        super(0);
        IntegratedPort portU = new IntegratedPort(u, this, v.id);
        IntegratedPort portV = new IntegratedPort(v, this, u.id);
        ports = new HashMap<>();
        ports.put(u.id, portU);
        ports.put(v.id, portV);
        isBusy = new HashMap<>();
        isBusy.put(u.id, false);
        isBusy.put(v.id, false);

        this.bandwidth = Constant.LINK_BANDWIDTH;
        this.length = Constant.DEFAULT_LINK_LENGTH;
    }

    public Link(Node u, Node v, double length) {
        this(u, v);
        this.length = length;
        //System.out.println("Link from: " + u.id + " to: " + v.id + " Length = " + length);
    }

    public long serialLatency(int packetSize) {
        if(packetSize != 100000 && this.bandwidth != 1e9)
            System.out.println(packetSize + " " + this.bandwidth);
        return (long) (1e9 * packetSize / this.bandwidth);
    }
    public long propagationLatency() {
        //if(length != Constant.DEFAULT_LINK_LENGTH && length != Constant.HOST_TO_SWITCH_LENGTH)
        //    System.out.println("!!!!!!!!!Length = " + length);
        return (long) (length / Constant.PROPAGATION_VELOCITY);
    }
    public long getTotalLatency(int packetSize) {
        return serialLatency(packetSize) + propagationLatency();
    }

    public double transferPacket(IntegratedPort sendPort, Packet p) {//gui du lieu tu outport qua link
        Link self = this;
        double currentTime = sim.time();
        IntegratedPort receivePort = getOtherPort(sendPort.getNode());
        Node receiveNode = receivePort.getNode();
        InputPort receiveInPort = receivePort.getInPort();
        warnBusy(sendPort.getNode());

        long latency = getTotalLatency(p.getSize());

        sim.getEventList().add(new Event(sim, currentTime + propagationLatency()) {
            @Override//tao ra su kien khi header cua packet den duoc inport cua SWITCH tiep theo (1)
            public void actions() { //gui du lieu tu Link den Input port cua SWITCH
                receiveInPort.addPacketToBuffer(p);
                p.nHop++;
                //Trigger next step
                if (receiveNode instanceof Switch && !receiveInPort.rcFlag && (receiveInPort.getBuffer().size() == 1)) {
                    receiveInPort.rcFlag = true;//(2) khi ay phai tinh toan luon nextOutPort
                    ((Switch) receiveNode).computeNextOutPort(receiveInPort);//tao event de chuan bi cho hanh dong tiep theo
                }
            }
        });

        sim.getEventList().add(new Event(sim, currentTime + latency) {//tao event khi packet da duoc chuyen het den
            @Override//cho inputPort cua SWITCH tiep theo (hoac HOST dich)
            public void actions() { //Gui tu link den inputPort tiep theo (chi tang do tre)
                //receiveInPort.addPacketToBuffer(p);
                freeLink(sendPort.getNode());//giai phong Link, cho phep gui tiep
                if (!sendPort.getOutPort().stFlag){
                    sendPort.getOutPort().stFlag = true;
                    Node sendNode = sendPort.getNode();
                    if (sendNode instanceof Host)//neu nut nguon la HOST
                        ((Host) sendNode).forwardToLink();//thi HOST co kha nang phai gui lai
                    else//neu nut nguon la SWITCH thi SWITCH se gui lai
                        ((Switch) sendNode).forwardPacket(sendPort.getOutPort());
                }

                if (receiveNode instanceof Host) {//neu nut nhan la HOST, tinh toan throughput?
                    ((Host) receiveNode).receivePacket(p);
                }
            }
        });
        return latency;
    }

    public IntegratedPort getPort(Node node){
        return ports.get(node.id);
    }

    public IntegratedPort getOtherPort(Node node){
        for (int id : ports.keySet()){
            if (id != node.id) return ports.get(id);
        }
        return null;
    }

    public boolean isBusy(Node srcNode){ return isBusy.get(srcNode.id); }

    private void warnBusy(Node srcNode){
        isBusy.put(srcNode.id, true);
    }

    private void freeLink(Node srcNode){
        isBusy.put(srcNode.id, false);
    }

    @Override
    public void clear() {
    }

    public double getLength()
    {
        return this.length;
    }
}
