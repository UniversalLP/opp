import javax.swing.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by universallp on 01.03.2017.
 * This file is part of jopp which is licenced
 * under the MOZILLA PUBLIC LICENCE 2.0 - mozilla.org/en-US/MPL/2.0/
 * github.com/UniversalLP/opp
 */
public class Analyzer {
    static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private final File db_folder;
    private final boolean canAnalyze;
    private Database db_instance;

    Analyzer(String pathToAnalyze) {
        this.db_folder = new File(pathToAnalyze);
        canAnalyze = db_folder.isDirectory() || db_folder.exists();
    }

    void analyze() {
        if (!canAnalyze) {
            JOptionPane.showMessageDialog(null, "Cannot analyze Databases since the target folder is invalid!", JOptionPane.ICON_PROPERTY, JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (File f : db_folder.listFiles()) {
            if (f.isFile() && f.canRead() && f.getName().endsWith(".odb")) {
                analyzeFile(f);
            }
        }
    }

    private void analyzeFile(File f) {
        db_instance = Database.create_from_file(f.getAbsolutePath());
        int total_latency = 0;
        int ping_diff;
        int max_ping_jump = 0;
        long time_of_jump = 0;
        long time_of_maxping = 0;
        int last_ping = 0;
        double avg = 0;

        for (Database.Data d : db_instance.data_list) {
            ping_diff = d.latency - last_ping;
            last_ping = d.latency;
            if (ping_diff > max_ping_jump) {
                max_ping_jump = ping_diff;
                time_of_jump = d.begin_time;
            }

            if (d.latency == db_instance.max_ping)
                time_of_maxping = d.begin_time;
            total_latency += d.latency;
        }

        avg = total_latency / db_instance.data_list.size();
        System.out.println("---------");
        System.out.println(f.getName() + ":");
        System.out.println(String.format("Max ping of      %sms at %s", db_instance.max_ping, sdf.format(new Date(time_of_maxping))));
        System.out.println(String.format("Max ping jump of %sms at %s", max_ping_jump, sdf.format(new Date(time_of_jump))));
        System.out.println(String.format("Average ping of  %sms", avg));

    }
}
