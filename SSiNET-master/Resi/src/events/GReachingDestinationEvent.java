package events;

import config.Constant;
import infrastructure.element.Element;
import infrastructure.entity.Node;
import infrastructure.event.Event;
import infrastructure.state.Type;
import network.elements.ExitBuffer;
import network.elements.Packet;
import network.elements.UnidirectionalWay;

import network.entities.Host;
import network.entities.Switch;
import network.states.enb.N0;


import network.states.unidirectionalway.W0;
import network.states.unidirectionalway.W1;
import network.states.unidirectionalway.W2;
import simulator.DiscreteEventSimulator;

public class GReachingDestinationEvent extends Event {
	//Event present for event type (G): packet reach destination node

    public GReachingDestinationEvent(
    		DiscreteEventSimulator sim,
    		long startTime, long endTime, Element elem, Packet p)
    {
    
    	super(sim, endTime);
        this.startTime = startTime;
        this.endTime = endTime;
        this.element = elem;
        this.packet = p;
    }
    @Override
    public void actions()
    {
        
        {
            UnidirectionalWay unidirectionalWay = (UnidirectionalWay)element;
            
            Node nextNode = unidirectionalWay.getToNode();

            if(
            	packet.getState().type == Type.P3
            		&& unidirectionalWay.getState() instanceof W1
                    && nextNode.isDestinationNode()
                    && unidirectionalWay.getPacket() == packet)
            {
                unidirectionalWay.removePacket();
                Host destinationNode = (Host)nextNode;
                destinationNode.receivePacket(packet);
                packet.setType(Type.P6);
                unidirectionalWay.setState(new W0(unidirectionalWay));
                unidirectionalWay.getState().act();
              settingbufferG(unidirectionalWay);
            }
        }
       
    }

	
	private void settingbufferG (UnidirectionalWay unidirectionalWay) {
	    	ExitBuffer sendExitBuffer = unidirectionalWay.getFromNode().physicalLayer
	                .exitBuffers.get(unidirectionalWay.getToNode().getId());
	        if (sendExitBuffer.getState().type == Type.X00) {
	        	sendExitBuffer.setType(Type.X01);
	            sendExitBuffer.getState().act();
	        }
	        if (sendExitBuffer.getState().type == Type.X10) {
	        	sendExitBuffer.setType(Type.X11);
	            sendExitBuffer.getState().act();
	        }
	    }
}
