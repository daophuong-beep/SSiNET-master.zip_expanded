package events;

import config.Constant;
import infrastructure.element.Element;
import infrastructure.event.Event;
import infrastructure.state.Type;
import network.elements.SourceQueue;
import network.elements.Packet;
import network.states.sourcequeue.Sq1;
import network.states.sourcequeue.Sq2;
import simulator.DiscreteEventSimulator;

public class AGenerationEvent extends Event {
	//Event present for event type (A): packet is created
	public AGenerationEvent(DiscreteEventSimulator sim, long startTime, long endTime, IEventGenerator elem)
	{
		super(sim, endTime);
		
		this.element = elem;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	@Override

	// todo start from event A
	public void actions() 
	{
		DiscreteEventSimulator sim = DiscreteEventSimulator.getInstance();
		//if(getElement() instanceof SourceQueue)
		{
			SourceQueue sourceQueue = (SourceQueue)getElement();
	
			Packet newPacket = sourceQueue.generatePacket(this.getStartTime());
			if(newPacket == null) { return; }
			newPacket.setId(sim.numSent++);
			this.setPacket(newPacket);
			
			newPacket.setType(Type.P1);
			
			// update source queue state
			if(sourceQueue.getState() instanceof Sq1)//it means that elem is an instance of SourceQueue
			{
				sourceQueue.setState(new Sq2(sourceQueue));
			}

			long time = (long)sim.time();
			Event event = new BLeavingSourceQueueEvent(sim, time, time, sourceQueue, newPacket);
			//sourceQueue.insertEvents(event); //add new event
					
			sim.addEvent(event);

			time = (long)sourceQueue.getNextPacketTime();
			
			Event ev = new AGenerationEvent(sim, time, time, sourceQueue);
			
			sim.addEvent(ev);
		}
		
	}
}
