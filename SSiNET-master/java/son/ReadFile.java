package son;

import custom.jellyfish.RandomRegularGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ReadFile {
    private int nSwitch;
    private int nHost;
    RandomRegularGraph graph;

    public ReadFile(String path) {
//        readFirstLine(path);
//        readRestFile(path);
//        read1(path);
//        read2(path);
        read(path);
    }

    public RandomRegularGraph getGraph() {
        return graph;
    }

//    public void read1(String path) {
//        try {
//            //Bước 1: Tạo đối tượng luồng và liên kết nguồn dữ liệu
//            FileInputStream fis = new FileInputStream("data/son/testout.txt");
//            DataInputStream dis = new DataInputStream(fis);
//            //Bước 2: Đọc dữ liệu
//            String s = dis.readLine();
//
//            dis.skip(1);
//            this.nSwitch = Integer.parseInt(s.split(" ")[0]);
//            this.nHost = Integer.parseInt(s.split(" ")[1]);
//            this.graph = new RandomRegularGraph(nSwitch, nHost);
//            StdOut.printf("nSwitch: %d\nnHost: %d\n", this.nSwitch, this.nHost);
//            return;
//            //Bước 3: Đóng luồng
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public void read2(String path) {
//        try {
//            //Bước 1: Tạo đối tượng luồng và liên kết nguồn dữ liệu
//            FileInputStream fis = new FileInputStream("data/son/testout.txt");
//            DataInputStream dis = new DataInputStream(fis);
//            //Bước 2: Đọc dữ liệu
//            String s = dis.readLine();
//
//            dis.skip(2);
//            int u = Integer.parseInt(s.split(" ")[0]);
//            int v = Integer.parseInt(s.split(" ")[1]);
//
//            if (!graph.hasEdge(u, v)) {
//                graph.addEdge(u, v);
//            }
//            //Bước 3: Đóng luồng
//            fis.close();
//            dis.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }


//    private void readFirstLine(String path) {
//        try (Stream<String> stream = Files.lines(Paths.get(path))) {
//            stream.skip(1).forEach(line -> {
//                this.nSwitch = Integer.parseInt(line.split(" ")[0]);
//                this.nHost = Integer.parseInt(line.split(" ")[1]);
//                this.graph = new RandomRegularGraph(nSwitch, nHost);
//                StdOut.printf("nSwitch: %d\nnHost: %d\n", this.nSwitch, this.nHost);
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void readRestFile(String path) {
//
//        try (Stream<String> stream = Files.lines(Paths.get(path))) {
//            stream.skip(2).forEach(line -> {
////                if (line.split(" ").length > 2) return;
//
//                int u = Integer.parseInt(line.split(" ")[0]);
//                int v = Integer.parseInt(line.split(" ")[1]);
//
//                if (!graph.hasEdge(u, v)) {
//                    graph.addEdge(u, v);
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void read(String path) {
        try (Stream<String> stream = Files.lines(Paths.get(path))) {

            stream.forEach(line -> {
                if (line.contains("nHost&nSwitch")) {

                    this.nSwitch = Integer.parseInt(line.split(" ")[1]);
                    this.nHost = Integer.parseInt(line.split(" ")[2]);
                    this.graph = new RandomRegularGraph(nSwitch, nHost);
                } else if (line.contains("edge")) {
                    int u = Integer.parseInt(line.split(" ")[1]);
                    int v = Integer.parseInt(line.split(" ")[2]);

                    if (!graph.hasEdge(u, v)) {
                        graph.addEdge(u, v);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}