package events;

import config.Constant;
import infrastructure.element.Element;
import infrastructure.entity.Node;
import infrastructure.event.Event;
import infrastructure.state.State;
import infrastructure.state.Type;
import network.elements.ExitBuffer;
import network.elements.Packet;
import network.elements.SourceQueue;
import network.elements.UnidirectionalWay;
import network.entities.Host;
import network.entities.Switch;
import network.entities.TypeOfHost;
//import network.states.packet.StateP2;
//import network.states.packet.SStateP3;
import network.states.unidirectionalway.W0;
import network.states.unidirectionalway.W1;
import simulator.DiscreteEventSimulator;

public class CLeavingEXBEvent extends Event {
	//Event present for event type (C): packet leave EXB

    public CLeavingEXBEvent(DiscreteEventSimulator sim, 
    		long startTime, long endTime, Element elem, Packet p)
    {
    	super(sim, endTime);
    	//countSubEvent++;
        this.startTime = startTime;
        this.endTime = endTime;
        this.element = elem;
        this.packet = p;
    }

    @Override
    public void actions()
    {
    	DiscreteEventSimulator sim = DiscreteEventSimulator.getInstance();
        {
            ExitBuffer exitBuffer = (ExitBuffer)element;
            
            UnidirectionalWay unidirectionalWay = exitBuffer.physicalLayer.links.get(exitBuffer.physicalLayer.node.getId())// vi la link cua host nen hoi khac
                    .getWayToOtherNode(exitBuffer.physicalLayer.node);

            if(unidirectionalWay.getState() instanceof W0 && exitBuffer.isPeekPacket(packet)
                    && ((exitBuffer.getState().type == Type.X11) || (exitBuffer.getState().type == Type.X01))) {
                unidirectionalWay.addPacket(exitBuffer.removePacket());

                //change Packet state
                
                {
                   
                	packet.setType(Type.P3);
                }
                //change EXB state
               
               this.settingstate(exitBuffer, unidirectionalWay);

                Node nextNode = unidirectionalWay.getToNode();
                // next node is switch so add event D
                if(nextNode instanceof Switch) {
                    // add event D
                	long time = (long)exitBuffer.physicalLayer.simulator.time();
                    Event event = new DReachingENBEvent(
                    		sim,
                    		time,
                            time + unidirectionalWay.getLink().getTotalLatency(packet.getSize()),
                            unidirectionalWay, packet);
                    event.register(); //insert new event
                }
                else if(nextNode instanceof Host){
                	Host h = (Host)nextNode;
                	if(h.type == TypeOfHost.Destination || h.type == TypeOfHost.Mix)
                	{
	                    // add event G
	                	long time = (long)exitBuffer.physicalLayer.simulator.time();
	                    Event event = new GReachingDestinationEvent(
	                    		sim,
	                    		time,
	                            time + unidirectionalWay.getLink().getTotalLatency(packet.getSize()),
	                            unidirectionalWay, packet);
	                    event.register(); //add new event
                	}
                }
            }
        }
		
    }
    
    /**
     * this method is for changing state for exb
     * @param exitBuffer
     * @param unidirectionalWay
     */
    
    private void settingstate(ExitBuffer exitBuffer,UnidirectionalWay unidirectionalWay) {
	 
    exitBuffer.setType(Type.X00);
    exitBuffer.getState().act();
    //change uniWay state
    unidirectionalWay.setState(new W1(unidirectionalWay));
    unidirectionalWay.getState().act();
    }
/**
 * this method is for 
 * @param exitBuffer
 * @param sim
 * @param unidirectionalWay
 */
private void addEventD(ExitBuffer exitBuffer, DiscreteEventSimulator sim, UnidirectionalWay unidirectionalWay	) {
	long time = (long)exitBuffer.physicalLayer.simulator.time();
    Event event = new DReachingENBEvent(
    		sim,
    		time,
            time + unidirectionalWay.getLink().getTotalLatency(packet.getSize()),
            unidirectionalWay, packet);
    event.register(); //insert new event
}

private void addEventG( ExitBuffer exitBuffer, DiscreteEventSimulator sim,UnidirectionalWay unidirectionalWay) {
	long time = (long)exitBuffer.physicalLayer.simulator.time();
    Event event = new GReachingDestinationEvent(
    		sim,
    		time,
            time + unidirectionalWay.getLink().getTotalLatency(packet.getSize()),
            unidirectionalWay, packet);
    event.register(); 
}
}