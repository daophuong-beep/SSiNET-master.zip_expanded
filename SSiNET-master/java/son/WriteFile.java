package son;

import custom.corra.CORRAGraph;
import custom.jellyfish.RandomRegularGraph;
import network.Link;
import network.Network;
import network.Switch;
import network.host.Host;
import network.port.IntegratedPort;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class WriteFile {
    String type;

    public WriteFile(RandomRegularGraph graph, String path, Network network) {
        try {
            FileWriter fw = new FileWriter(path);
            String s1 = "constructor:" + " " + graph.getK() + " " + graph.getK() + " " + graph.getR() + "\n";
            String s2 = "nHost&nSwitch:" + " " + graph.getnSwitch() + " " + graph.getnHost() + "\n";
            fw.write(s1);
            fw.write(s2);
            for (int i = 0; i < graph.V(); i++) {
                for (int j : graph.adj(i)) {

                    if (graph.isSwitchVertex(i)) {
                        List<Switch> switches = network.getSwitches();
                        Switch switchI = switches.get(i);
                        IntegratedPort portJ = switchI.ports.get(j);
                        Link linkIJ = portJ.getLink();
                        double lengthIJ = linkIJ.getLength();
                        String temp = "edge:" + " " + i + " " + j + " " + lengthIJ + "\n";
                        fw.write(temp);
                    } else if (graph.isHostVertex(i) && i < graph.getnHost()) {
                        List<Host> hosts = network.getHosts();
                        Host hostI = hosts.get(i);
                        IntegratedPort portJ = hostI.portToSwitch;
                        Link linkIJ = portJ.getLink();
                        double lengthIJ = linkIJ.getLength();
                        String temp = "edge:" + " " + i + " " + j + " " + lengthIJ + "\n";
                        fw.write(temp);
                    }

                }

            }
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("success.....");
    }


    public static void WriteConnection(RandomRegularGraph graph, String path) {
        try {
            int nextIndex = getNextIndex(path, "RSN_adjacency_matrix_");
            path = path + "RSN_adjacency_matrix_" + nextIndex + ".txt";
            FileWriter fw = new FileWriter(path);
            for (int i = 0; i < graph.V(); i++) {
                for(int j = 0; j < graph.V();j++)
                {
                    if(i >= j)
                    {
                        fw.write("0");
                    }
                    else
                    {
                        if(graph.adj(i).contains(j))
                        {
                            fw.write("1");
                        }
                        else{
                            fw.write("0");
                        }
                    }
                    if(j == graph.V() - 1)
                    {
                        fw.write("\n");
                    }
                    else {
                        fw.write("\t");
                    }

                }
            }
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static int getNextIndex(String path, String pattern)
    {
        final File folder = new File(path);

        List<String> result = new ArrayList<>();

        search(".*\\.txt", folder, result);

        int max = -1;
        for (String s : result) {
            if(s.contains(pattern)) {
                String[] elems = s.split("_");
                if (elems.length > 1) {
                    String index = elems[elems.length - 1].replaceAll(".txt", "");
                    try {
                        int x = Integer.parseInt(index);
                        if (max < x)
                            max = x;
                    } catch (Exception e) {

                    }
                }
            }
        }
        max++;
        return max;
    }

    public static void search(final String pattern, final File folder, List<String> result) {
        for (final File f : folder.listFiles()) {

            if (f.isDirectory()) {
                search(pattern, f, result);
            }

            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                    result.add(f.getAbsolutePath());
                }
            }

        }
    }

    public static void WriteCoordinate(Network network, String path)
    {
        int nextIndex = getNextIndex(path, "RSN_node_coordinates_");
        path = path + "RSN_node_coordinates_" + nextIndex + ".txt";
        try {
            FileWriter fw = new FileWriter(path);
            for (int i = 0; i < network.cordOfNodes.size(); i++) {
                fw.write(network.cordOfNodes.get(i));
                fw.write("\n");
            }
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /*public WriteFile(CORRAGraph graph, String path) {
        try {
            FileWriter fw = new FileWriter(path);
            int size = graph.getnCol() * graph.getnRow();
            String s = size + " " + (graph.getE() - graph.hosts().size()) + " " + graph.getTotalRandomLink() + "\n";
            fw.write(s);
            for (int i = 0; i < graph.switches().size(); i++) {
                for (int j : graph.adj(i)) {
                    if (graph.isSwitchVertex(j)) {
                        String temp = i + " " + j + "\n";
                        fw.write(temp);
                    }
                }
            }
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("success.....");
    }*/

    public WriteFile(RandomRegularGraph graph, String path) {
        try {
            FileWriter fw = new FileWriter(path);
            String s = graph.getN() + " " + graph.getK() + " " + graph.getR() + "\n";
            fw.write(s);
            for (int i = 0; i < graph.switches().size(); i++) {
                for (int j : graph.adj(i)) {
                    if (graph.isSwitchVertex(j)) {
                        String temp = i + " " + j + "\n";
                        fw.write(temp);
                    }
                }
            }
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("success.....");
    }

//    public WriteFile(RandomRegularGraph graph, String path, Network network){
//        try{
//            FileOutputStream fos = new FileOutputStream("data/son/testout1.txt");
//            DataOutputStream dos = new DataOutputStream(fos);
//
//            String s1 = graph.getK() + " " + graph.getK() + " " + graph.getR() + "\n";
//            dos.writeUTF(s1);
//            String s2 = graph.getnHost() + " " + graph.getnHost() + "\n";
//            dos.writeUTF(s2);
//            for (int i = 0; i < graph.V(); i++) {
//                for (int j : graph.adj(i)) {
//                    List<Switch> switches = network.getSwitches();
//                    Switch switchI = switches.get(i);
//                    IntegratedPort portJ = switchI.ports.get(j);
//                    Link linkIJ = portJ.getLink();
//                    double lengthIJ = linkIJ.getLength();
//                    String temp = i + " " + j + " " + lengthIJ + "\n";
////                    String temp = i + " " + j;
//                    dos.writeUTF(temp);
//                }
//            }
//            dos.close();
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//
//    }

}