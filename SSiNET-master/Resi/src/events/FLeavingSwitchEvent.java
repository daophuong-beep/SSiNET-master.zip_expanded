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
import network.entities.TypeOfHost;
/*import network.states.packet.StateP2;
import network.states.packet.SStateP3;
import network.states.packet.StateP5;*/
import network.states.unidirectionalway.W0;
import network.states.unidirectionalway.W1;
import simulator.DiscreteEventSimulator;

public class FLeavingSwitchEvent extends Event {
	//Event present for event type (F): packet leave EXB of Switch to go to LINK

    public FLeavingSwitchEvent(
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
    	DiscreteEventSimulator sim = DiscreteEventSimulator.getInstance();
        ExitBuffer exitBuffer = (ExitBuffer)element;
        UnidirectionalWay unidirectionalWay = exitBuffer.physicalLayer.links.
        get(exitBuffer.getConnectNode().getId()).getWayToOtherNode(exitBuffer.physicalLayer.node);
        if(exitBuffer.isPeekPacket(packet) && unidirectionalWay.getState() instanceof W0
                && ((exitBuffer.getState().type == Type.X11) || (exitBuffer.getState().type == Type.X01))
        ){
            unidirectionalWay.addPacket(exitBuffer.removePacket());
            if(packet.getState().type == Type.P5)
            {
                packet.setType(Type.P3);
            }
           this.settingbuffer(unidirectionalWay, exitBuffer);

            Node nextNode = exitBuffer.getConnectNode();
            exitBuffer.physicalLayer.node.getNetworkLayer().routingAlgorithm.update(packet, nextNode);
            if(nextNode instanceof Host){
            	Host h = (Host)nextNode;
            	if(h.type == TypeOfHost.Destination || h.type == TypeOfHost.Mix)
            	{
                	this.AddEventG(unidirectionalWay, exitBuffer, sim);
            	}
            }
            else if(nextNode instanceof Switch) {
            	this.AddEventG(unidirectionalWay, exitBuffer, sim);
            }
        }
        
    }

private void settingbuffer( UnidirectionalWay unidirectionalWay,ExitBuffer exitBuffer) {
	 exitBuffer.setType(Type.X00);
     exitBuffer.getState().act();

     unidirectionalWay.setState(new W1(unidirectionalWay));
     unidirectionalWay.getState().act();

}

private void AddEventG(UnidirectionalWay unidirectionalWay,ExitBuffer exitBuffer,DiscreteEventSimulator sim) {
	long time = (long)exitBuffer.physicalLayer.simulator.time();
    Event event = new GReachingDestinationEvent(
    		sim
    		, time
            , time + unidirectionalWay.getLink().getTotalLatency(packet.getSize())
            , unidirectionalWay, packet);
    event.register(); //insert new event
}
private void AddEventD(UnidirectionalWay unidirectionalWay,ExitBuffer exitBuffer,DiscreteEventSimulator sim) {
	long time = (long)exitBuffer.physicalLayer.simulator.time();
    Event event = new DReachingENBEvent(
    		sim,
    		time
            , time + unidirectionalWay.getLink().getTotalLatency(packet.getSize())
            , unidirectionalWay, packet);
    event.register(); //insert new event
}
}