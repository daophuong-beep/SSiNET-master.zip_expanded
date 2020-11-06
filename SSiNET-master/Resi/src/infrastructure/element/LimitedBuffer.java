package infrastructure.element;

import common.Queue;
import config.Constant;
import infrastructure.entity.Node;
import network.elements.Packet;

public abstract class LimitedBuffer extends Buffer {
	//todo should set to protected
	protected Node node; //co the bo di
	protected Node connectNode; // ko the bo, vi tu buffer ko the biet duoc no connect voi node khac nao
	protected int size;


	public Node getConnectNode() {
		return connectNode;
	}

	public Node getNode() {
		return node;
	}

	/**
	 *  insertPacket method will insert packet p 
	 * in its buffer
	 * @param p is the packet need to be inserted
	 * @return true if insert sucessfully
	 *         false if not (meaning the buffer was full)
	 */

	//tobe override
	public void checkStateChange(){}

	public void insertPacket(Packet p)
	{
		if(allPackets.size() > size)
			System.out.println("ERROR: Buffer: " + this.toString() + " oversized");
		allPackets.enqueue(p);
	}
	public Packet removePacket()
	{
		if(allPackets.isEmpty()) return null;
		return allPackets.dequeue();
	}
	public boolean isFull()
	{
		if(allPackets.size() > size)
			System.out.println("ERROR: Buffer: " + this.toString() + " oversized");
		return allPackets.size() == size;
	}

	public int getNumOfPacket()
	{
		if(allPackets.size() > size)
			System.out.println("ERROR: Buffer: " + this.toString() + " oversized");
		return allPackets.size();
	}

	public boolean canAddPacket(){
		return allPackets.size() < size ;
	}
}
