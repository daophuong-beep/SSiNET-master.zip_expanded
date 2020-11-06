package network.states.sourcequeue;

import infrastructure.event.Event;
import events.AGenerationEvent;
import network.elements.SourceQueue;
import infrastructure.state.State;

public class Sq1 extends State {
	//�	State Sq1: source queue is empty.
	public Sq1(SourceQueue e)
	{
		this.element = e;
	}
	
	/**
	 *  act method is used to call a element that changes state
	 * Here, Source queue elemen that at state Sq1 will
	 * check the list of event (about to happen) whether or not have
	 * the event that create the next packet?
	 * if not , it will create that event. the time that event happen will be 
	 * in the future (another Constant.HOST_DELAY )
	 */
	@Override
	public void act()
	{
		SourceQueue sourceQueue = (SourceQueue) element;
		//if(notYetAddGenerationEvent(sourceQueue))//checking whether or not Source Queue have event that create new packet?
		{
			long time = (long)sourceQueue.getNextPacketTime();
			Event event = new AGenerationEvent(
					sourceQueue.physicalLayer.simulator,
					time, time, element);
			event.register();
		}
	}
	

}
