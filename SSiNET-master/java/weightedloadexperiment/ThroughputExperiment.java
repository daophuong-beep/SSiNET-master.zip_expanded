package weightedloadexperiment;

import common.Knuth;
import common.StdOut;
import config.Constant;
import custom.fattree.FatTreeGraph;
import custom.fattree.FatTreeRoutingAlgorithm;
import custom.jellyfish.KShortestPathsRoutingAlgorithm;
import custom.jellyfish.RandomRegularGraph;
import graph.Coordination;
import network.Network;
import network.NetworkObject;
import network.host.Host;
import routing.RoutingAlgorithm;
import routing.ShortestPathRoutingAlgorithm;
import simulator.DiscreteEventSimulator;
import son.ReadFile;
import son.WriteFile;
import umontreal.ssj.charts.XYLineChart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Dandoh on 6/27/17.
 */
public class ThroughputExperiment {

    private Network network;
    private boolean writeFile = false;

    public ThroughputExperiment(Network network) {
        this.network = network;
    }

    public double[][] calThroughput(Map<Integer, Integer> trafficPattern, boolean verbose) {
        DiscreteEventSimulator simulator =
                new DiscreteEventSimulator(true, Constant.MAX_TIME, verbose);
        NetworkObject.setSim(simulator);
        network.clear(); // clear all the data, queue, ... in switches, hosts

        int count = 0;
        for (Integer source : trafficPattern.keySet()) {
            Integer destination = trafficPattern.get(source);
            count++;
            network.getHostById(source).generatePacket(destination);
            //if(count == 1) break;
        }
        if(count == Host.COUNT)
        {
            StdOut.println("HOST.COUNT: " + Host.COUNT);
        }
        else{
            StdOut.println("count: " + count + " HOST.COUNT: " + Host.COUNT);
        }

        simulator.start();
        if(network.checkDeadlock())
            StdOut.println("Deadlock: " + true);
        //System.out.println("Num packets sent: " + simulator.numSent);
        //System.out.println("Num packets received: " + simulator.numReceived);
        //System.out.println("Num packets lost: " + simulator.numLoss);
        //StdOut.printf("Loss percentage = %.2f\n",
        //        1.0 * simulator.numLoss / (simulator.numReceived + simulator.numLoss) * 100);
        double averageTime = simulator.totalPacketTime / simulator.numReceived;
        //StdOut.printf("Average packet time = %.2fms\n", averageTime / 1e6);

        //Coordination C = new Coordination(network.getGraph());
        //StdOut.printf("Total cable length = %.2f\n", C.totalCableLength());
        //StdOut.printf("Avg hop = %.2f\n", 1.0 * simulator.totalHop / simulator.numReceived);

        double interval = 1e7;
        int nPoint = (int) (simulator.getTimeLimit() / interval + 1);
        double[][] points = new double[2][nPoint];
        for (int i = 0; i < nPoint; i++) {
            // convert to ms
            points[0][i] = i * interval;
            points[1][i] = simulator.receivedPacket[i];
        }

        double throughput = 0;
        for (int i = 0; i < nPoint; i++) {
            points[1][i] = 100 * points[1][i] * Constant.PACKET_SIZE /
                (trafficPattern.size() * Constant.LINK_BANDWIDTH * interval / 1e9);
        }
        throughput = points[1][nPoint-1];

        StdOut.printf("Throughput : %.2f\n", throughput);

        double rawThroughput = throughput* Constant.LINK_BANDWIDTH / 100 / 1e9;
        //StdOut.printf("RAW Throughput : %.2f GBit/s\n", rawThroughput);

        double alternativeRawThroughput = simulator.numReceived *  Constant.PACKET_SIZE / (trafficPattern.size());
        //StdOut.printf("b1: %f\n", alternativeRawThroughput);
        alternativeRawThroughput = alternativeRawThroughput / (nPoint * interval);
        //StdOut.printf("#Flows: %d, Time Limit = %f %f\n", trafficPattern.size(), simulator.getTimeLimit(), nPoint*interval);
        //StdOut.printf("Alternative Raw throughput = %f Gb/s", alternativeRawThroughput);

        if(writeFile || true) {
            XYLineChart chart = new XYLineChart(null, "Time (ns)", "Throughput (%)", points);
            chart.setAutoRange00(true, true);      // Axes pass through (0,0)
            chart.toLatexFile("./results/chart.tex", 12, 8);
            chart.view(800, 500);

            // Export to filfp
            try {
                String fileName = "./results/throughput.txt";
                File file = new File(fileName);
                // creates the file
                file.createNewFile();

                FileWriter writer = new FileWriter(file);

                // Writes the content to the file
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return points;
    }

    public static void main(String[] args) {

        //for(int timeOfRun = 0; timeOfRun < 100-3; timeOfRun++)
        {
            FatTreeGraph G = new FatTreeGraph(4);
            FatTreeRoutingAlgorithm ra = new FatTreeRoutingAlgorithm(G, false);
//        FullGraph G = new FullGraph(256);
//        RoutingAlgorithm ra = new FullRoutingAlgorithm(G);
//        SmallWorldGraph G = new SmallWorldGraph(16, 16,
//                "torus", new double[]{1.6, 1.6});
//        RoutingAlgorithm ra = new SmallWorldRoutingAlgorithm(G);
//        CORRAKGraph G = new CORRAKGraph(32, 32, "grid", 1);
//        RoutingAlgorithm ra = new CORRARoutingAlgorithm(G);
            //RoutingAlgorithm ra = new ShortestPathRoutingAlgorithm(G);
            //N = so switch; k = so port cua switch; r = so port cua switch ma ket noi den switch khac
            //RandomRegularGraph G = new RandomRegularGraph(256, 16, 12);
            //RoutingAlgorithm ra = new ShortestPathRoutingAlgorithm(G);

            //RandomRegularGraph G = new RandomRegularGraph(20, 16, 4);


            String pathInLinux = "/home/tienthanh/Public/NS3repo/ns-3-allinone/ns-3-dev/scratch/subdir/matrixTopo/";

            //RandomRegularGraph G = readFile.getGraph();
            //RoutingAlgorithm ra = new KShortestPathsRoutingAlgorithm(G, 1);

            Network network = new Network(G, ra);

            //WriteFile.WriteConnection(G, pathInLinux);
            //WriteFile.WriteCoordinate(network, pathInLinux);

            ThroughputExperiment experiment = new ThroughputExperiment(network);
            Integer[] hosts = G.hosts().toArray(new Integer[0]);

            Knuth.shuffle(hosts);
            List<Integer> sources = new ArrayList<>();
            List<Integer> destination = new ArrayList<>();
            sources.addAll(Arrays.asList(hosts).subList(0, hosts.length / 2));


            destination.addAll(Arrays.asList(hosts).subList(hosts.length / 2, hosts.length));

            Map<Integer, Integer> traffic = new HashMap<>();
            int sizeOfFlow = 1; //
                                //sources.size();
            for (int i = 0; i < sizeOfFlow; i++) {
                traffic.put(sources.get(i), destination.get(i));
                //traffic.put(destination.get(i), sources.get(i));
                //StdOut.printf("From source: %d To destination: %d\n", sources.get(i), destination.get(i));
            }

            experiment.calThroughput(traffic, false);

            //ThanhNT
            int rxPacket = 0;
            double thp = 0, privateThp = 0;
            for (int i = 0; i < network.getHosts().size(); i++) {
                Host host = network.getHosts().get(i);
                if (host.receivedPacketInHost != 0) {
                    //System.out.println("Host " + host.id + " receives: " + host.receivedPacketInHost + " packets "
                    //        + "from " + host.firstTx + " to " + (host.lastRx / 1e9)
                    //);
                    rxPacket += host.receivedPacketInHost;
                    privateThp = host.receivedPacketInHost * Constant.PACKET_SIZE / (host.lastRx - host.firstTx);
                    thp += privateThp;
                    //System.out.println("\t Private Throughput = " + privateThp);
                }
            }

            System.out.println("Rx Packet: " + rxPacket + " " + (thp / traffic.size()));
            //Endof ThanhNT
        }
    }
}
