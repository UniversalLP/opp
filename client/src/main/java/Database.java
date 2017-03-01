import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by universallp on 25.02.2017.
 * This file is part of jopp which is licenced
 * under the MOZILLA PUBLIC LICENCE 2.0 - mozilla.org/en-US/MPL/2.0/
 * github.com/UniversalLP/opp
 */
class Database {

    class Data {
        long begin_time, end_time;
        int latency;
        boolean has_packet_loss;

        private Data(long begin, long end, int lat, boolean pl) {
            this.begin_time = begin;
            this.end_time = end;
            this.latency = lat;
            this.has_packet_loss = pl;
        }
    }

    ArrayList<Data> data_list = new ArrayList<>();
    int max_ping = 0;
    int min_ping = Integer.MAX_VALUE;
    int total_nodes = 0;
    private File database;

    private Database(File f) {
        this.database = f;
        read_from_file();
        init();
    }

    private void read_from_file() {
        try (BufferedReader br = new BufferedReader(new FileReader(database))) {
            String line;
            while ((line = br.readLine()) != null) {
                long start = Long.parseLong(line);
                line = br.readLine();
                long end = Long.parseLong(line);
                line = br.readLine();
                int latency = Integer.parseInt(line);
                line = br.readLine();
                boolean packetLoss = !line.equals("0");

                data_list.add(new Data(start * 1000, end * 1000, latency, packetLoss));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        for (Data d : data_list) {
            if (d.latency > max_ping)
                max_ping = d.latency;

            if (d.latency < min_ping)
                min_ping = d.latency;

            if (d.begin_time != d.end_time) // This entry spans over a longer timespan -> it'll take up two values in the graph
                total_nodes++;
            total_nodes++;
        }
        //ping_diff = max_ping - min_ping;
    }

    public static Database create_from_file(String path) {
        File f = new File(path);
        if (f.exists()) {
            return new Database(f);
        } else {
            System.out.println("Cannot load database! File doesn't exist!");
            return null;
        }
    }
}
