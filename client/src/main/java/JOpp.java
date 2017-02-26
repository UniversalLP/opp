import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.TimeZone;

/**
 * Created by universallp on 25.02.2017.
 * This file is part of jopp which is licenced
 * under the MOZILLA PUBLIC LICENCE 2.0 - mozilla.org/en-US/MPL/2.0/s
 * github.com/UniversalLP/jopp
 */
public class JOpp {

    static PaintFrame pFrame;
    static Database db;

    public static void main(String[] args) throws InterruptedException {
        String path;
        if (args.length == 0) {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileFilter(new FileNameExtensionFilter("Opp Database", "odb"));
            jfc.setDialogTitle("Choose a database to analyze");
            jfc.showOpenDialog(null);
            path = jfc.getSelectedFile().getAbsolutePath();
        } else {
            path = args[0];
        }

        db = Database.create_from_file(path);
        PaintUtils.sdf.setTimeZone(TimeZone.getDefault());

        if (db == null) {
            System.out.println("Errors parsing the database! Is this a valid opp database?");
            System.exit(-1);
        }

        System.out.println("Successfully parsed database with " + db.data_list.size() + " entries!");
        System.out.println("Min ping: " + db.min_ping + ", Max ping: " + db.max_ping + ", Total nodes: " + db.total_nodes);

        pFrame = new PaintFrame("jOpp - Grapical Plotter | " + path);

    }

}
