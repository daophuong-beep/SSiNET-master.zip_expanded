package network;

import config.Constant;
import custom.jellyfish.KShortestPathsRoutingAlgorithm;
import network.host.Host;
import network.port.InputPort;
import network.port.IntegratedPort;
import network.port.OutputPort;
import routing.RoutingAlgorithm;
import simulator.Event;

import java.util.*;

/**
 * Created by Dandoh on 6/27/17.
 */
public class Switch extends Node {

    private RoutingAlgorithm ra;
    public Map<Integer, IntegratedPort> ports = new HashMap<>();

    public Switch(int id, RoutingAlgorithm ra) {
        super(id);
        this.ra = ra;
    }
    //Tinh toan xem voi du lieu trong Input port thi cong outport tiep theo la cong nao
    public void computeNextOutPort(InputPort inPort){
        Switch self = this;
        if (!inPort.isBufferEmpty() && (inPort.getNextOutPort() == null)){
            Packet p = inPort.getTopPacket();
            int nextNode = getNextNode(p);
            OutputPort nextOutPort = ports.get(nextNode).getOutPort();

            double finishTime = sim.getTime() + Constant.SWITCH_CYCLE;//tao ra event moi, thoi gian co cong them do tre la SWITCH_CYCLE
            sim.getEventList().add(new Event(sim, finishTime, 2.0) {
                @Override
                public void actions() {
                    //Gui du lieu tu input port cua SWITCH den outport cua SWITCH
                    inPort.setNextOutPort(nextOutPort);
                    nextOutPort.addRequest(inPort.id);

                    // Schedule for next cycle, boi vi co the van con packet trong
                    //input port cua SWITCH, o day co them EVENT
                    self.computeNextOutPort(inPort);

                    // Trigger next step, tao event de gui du lieu tu inport sang outport
                    if (!nextOutPort.swFlag) {
                        nextOutPort.swFlag = true;
                        self.switchPacket(nextOutPort);//co ve nhu gui packet tu INPUT port sang OUTPUT port
                    }
                }
            });
        }
        else {
            inPort.rcFlag = false;
        }
    }

    public void switchPacket(OutputPort outPort){
        Switch self = this;//gui du lieu tu input port sang output port
        if (outPort.canReceive() && !outPort.getRequestList().isEmpty()) {
            //Select an input port in request list that has latest packet time. Chon phuc vu input Port nao
            int selectedId = -1; //ma cai packet dau tien duoc sinh ra som nhat
            double latestStartTime = Double.MAX_VALUE;
            for (int inPortId : outPort.getRequestList()) {
                if (!ports.containsKey(inPortId)) {
                    System.exit(1);
                }
                InputPort inPort = ports.get(inPortId).getInPort();
                Packet p = inPort.getTopPacket();
                if (p.getStartTime() < latestStartTime) {
                    latestStartTime = p.getStartTime();
                    selectedId = inPortId;
                }
            }
            InputPort selectedInPort = ports.get(selectedId).getInPort();
            Packet p = selectedInPort.getTopPacket();

            double finishTime = sim.getTime() + Constant.SWITCH_CYCLE;//Tang bien thoi gian len
            sim.getEventList().add(new Event(sim, finishTime, 1.0) {
                @Override
                public void actions() {//gui du lieu tu input port sang outport thanh cong
                    outPort.addPacketToBuffer(p);
                    outPort.removeRequest(selectedInPort.id);//request gui den outport da duoc phuc vu
                    selectedInPort.setNextOutPort(null);
                    selectedInPort.removeTopPacket();
                    //PT duoi day thuc hien viec truyen tin hieu CREDIT_DELAY xuong IntegratedPort khac
                    self.sendCreditSignal(ports.get(selectedInPort.id).getLink());

                    // Next cycle, tao ra event moi, kiem tra xem neu van con packet nao trong inputport khong, neu con thi
                    self.switchPacket(outPort);//tiep tuc chon ra packet roi gui tu inport sang outport

                    // Trigger next step, tao ra event moi
                    if (!outPort.stFlag) {//outPort khong bi ban
                        outPort.stFlag = true;
                        self.forwardPacket(outPort);//chuan bi gui tu outport den link
                    }

                    //Wake up previous step, cai inputPort da duoc phuc vu gui 01 packet roi, tiep tuc cap nhat xem
                    InputPort previousInPort = selectedInPort;//no se gui tiep packet cua no di dau
                    if (!selectedInPort.rcFlag && !selectedInPort.isBufferEmpty()) {
                        selectedInPort.rcFlag = true;
                        self.computeNextOutPort(previousInPort);
                    }
                }
            });
        }
        else {
            outPort.swFlag = false;
        }
    }

    private void sendCreditSignal(Link linkToPreviousPort){
        Switch self = this;//PT nay chuan bi gui tin hieu CREDIT_DELAY den IntegratedPort khac
        IntegratedPort previousPort = linkToPreviousPort.getOtherPort(this);
        OutputPort previousOutputPort = previousPort.getOutPort();
        sim.getEventList().add(new Event(sim, sim.getTime() + Constant.CREDIT_DELAY) {//CREDIT_DELAY la thoi gian truyen tin hieu tu
            @Override//input port (cua IntegratedPort) nay ve nguoc lai outport (cua IntegratedPort khac)
            public void actions() {
                previousOutputPort.increaseCreditCount();//tang gia tri creditCount nghia la them cho trong o buffer
                if (!previousOutputPort.stFlag && (previousOutputPort.getCreditCount() == 1)){//=1 nghia la con packet chua duoc gui di
                    Node previousNode = linkToPreviousPort.getOtherPort(self).getNode();
                    if (previousNode instanceof Host){
                        ((Host) previousNode).forwardToLink();
                    }
                    else if (previousNode instanceof Switch){
                        previousOutputPort.stFlag = true;
                        ((Switch) previousNode).forwardPacket(previousOutputPort);
                    }
                }
            }
        });
    }

    public void forwardPacket(OutputPort outPort){//gui du lieu tu outport den link
        Switch self = this;
        IntegratedPort integratedPort = ports.get(outPort.id);
        Link outLink = integratedPort.getLink();

        if (!outPort.isBufferEmpty() && (outPort.getCreditCount() > 0) && !outLink.isBusy(self)){
            Packet p = outPort.removeTopPacket();

            double finishTime = sim.getTime() + Constant. SWITCH_CYCLE;
            sim.getEventList().add(new Event(sim, finishTime, 0.0) {//tao event moi de gui du lieu tu outport den link
                @Override
                public void actions() {//gui du lieu tu outport qua link
                    outLink.transferPacket(integratedPort, p);//outlink truyen du lieu sang HOST hoac Input cua Switch
                    outPort.decreaseCreditCount();//outport giam gia tri creaditCount, do la so o trong o Input tiep theo

                    //Wake up previous step
                    if (!outPort.swFlag && (outPort.getBuffer().size() == Constant.QUEUE_SIZE-1)){
                        outPort.swFlag = true;
                        self.switchPacket(outPort);
                    }

                    //Next cycle
                    self.forwardPacket(outPort);//chuan bi tao event de gui du lieu tu output sang LINK
                }
            });
        }
        else {
            outPort.stFlag = false;
        }
    }

    @Override
    public void clear() {
        for (Map.Entry<Integer, IntegratedPort> port: this.ports.entrySet()) {
            port.getValue().getLink().clear();
        }

    }

    public int getNextNode(Packet p){
        int nextNode;
        if (ra instanceof KShortestPathsRoutingAlgorithm)
            nextNode = ((KShortestPathsRoutingAlgorithm) ra).next(id, p);
        else
            nextNode = ra.next(p.getSource(), id, p.getDestination());
        return nextNode;
    }
}
