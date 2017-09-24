import com.sun.org.apache.xpath.internal.SourceTree;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.TimeZone;

/**
 * Created by universallp on 25.02.2017.
 * This file is part of jopp which is licenced
 * under the MOZILLA PUBLIC LICENCE 2.0 - mozilla.org/en-US/MPL/2.0/s
 * github.com/UniversalLP/opp
 */
public class JOpp {

    static PaintFrame pFrame;
    static Database db;

    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        String path = null;
        if (args.length == 0) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFileChooser jfc = new JFileChooser();
            if (showAnalyzePrompt()) {

                jfc.setFileFilter(new FileNameExtensionFilter("Opp Database", "csv"));
                jfc.setDialogTitle("Choose a database to analyze");
                jfc.showOpenDialog(null);

                if (jfc.getSelectedFile() == null) {
                    System.out.println("Error: no database selected");
                    System.exit(-1);
                }

                path = jfc.getSelectedFile().getAbsolutePath();
            } else {
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.showOpenDialog(null);
                jfc.setDialogTitle("Choose a folder containing databases");
                path = jfc.getSelectedFile().getPath();
            }


        } else {
            path = args[0];
        }

        if (new File(path).isDirectory()) {
            System.out.println("This is a directory! Analysing databases in the directory");
            startAnalyzer(path);
        } else {
            startPlotter(path);
        }

    }

    static void startPlotter(String path) {
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

    static void startAnalyzer(String path) {
        Analyzer a = new Analyzer(path);
        a.log.writeLine("Analyzing " + path);
        long begin = System.currentTimeMillis();

        a.analyze();

        long duration = System.currentTimeMillis() - begin;

        a.log.writeLine("Done! Analyzing took " + duration + "ms");
    }

    static boolean showAnalyzePrompt() {
        Object[] buttons = { "Open graphical plotter", "Open automatic analyzer" };

        JPanel panel = new JPanel();
        panel.add(new JLabel("What do you want to do?"));

        int result = JOptionPane.showOptionDialog(null, panel, "jOpp",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, buttons, null);

        return result == JOptionPane.YES_OPTION;
    }
}
