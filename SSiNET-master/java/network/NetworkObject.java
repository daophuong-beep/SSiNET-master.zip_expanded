package network;

import simulator.DiscreteEventSimulator;

public abstract class NetworkObject {
    public int id;

    protected static DiscreteEventSimulator sim;

    public NetworkObject(int id) {
        this.id = id;
    }

    public abstract void clear();

    public static void setSim(DiscreteEventSimulator sim) {
        NetworkObject.sim = sim;
    }
}
