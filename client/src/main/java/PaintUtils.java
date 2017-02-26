import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by universallp on 25.02.2017.
 * This file is part of jopp which is licenced
 * under the MOZILLA PUBLIC LICENCE 2.0 - mozilla.org/en-US/MPL/2.0/
 * github.com/UniversalLP/jopp
 */
class PaintUtils {
    static int last_width = 0;
    static int last_height = 0;
    static int offset = 0;
    static int max_offset = 0;
    static int max_nodes = 0;
    static double xStep = 0.0;
    static double yStep = 0.0;
    static int lastDotX = -1;
    static int lastDotY = 0;
    static int scrollbarWidth = 0;
    static int scrollbarProgress = 0;
    static boolean drawRuler = false;
    static Rectangle r;
    static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    static void drawGrid(Graphics g) {
        g.setColor(Color.BLACK);
        int ms;

        for (int h = 0; h < r.height; h += yStep) {
            g.drawLine(r.x, r.y + r.height - h, r.x + r.width, r.y + r.height - h);
            ms = (int) (((float) h / r.height) * JOpp.db.max_ping);
            g.drawString(ms + "ms", 5, r.y + r.height - h);
        }

        for (int w = 0; w < r.width; w += xStep) {
            g.drawLine(r.x + w, r.y, r.x + w, r.y + r.height);
        }
    }

    static void drawEntries(Graphics g, ArrayList<Database.Data> l) {
        int x = r.x;
        int y;
        boolean up = true;

        g.setColor(Color.RED);

        for (int i = offset; i < max_nodes + offset; i++) {
            if (i >= l.size())
                break;
            Database.Data d = l.get(i);
            y = (int) (r.y + r.height - (r.height * ((float) d.latency / JOpp.db.max_ping)));
            g.fillRect(x - 2, y - 2, 4, 4);

            if (d.begin_time == d.end_time) {
                if (lastDotX != -1) // If there's a previous value, connect them
                    g.drawLine(lastDotX, lastDotY, x, y);

                drawEntry(g, up, d.begin_time, x, d.has_packet_loss);
            } else { // This entry spans over a longer time -> draw a point for the beginning and the end
                if (lastDotX != -1) // If there's a previous value, connect them
                    g.drawLine(lastDotX, lastDotY, x, y);
                lastDotX = x;

                drawEntry(g, up, d.begin_time, x, d.has_packet_loss); // Beginning point
                up = !up;
                g.setColor(Color.RED);
                x += xStep * 2;

                if (x > r.width + r.x)
                    break;

                g.fillRect(x - 2, y - 2, 4, 4);
                g.drawLine(lastDotX, y, x, y);
                drawEntry(g, up, d.end_time, x, d.has_packet_loss); // Ending point
            }

            g.setColor(Color.RED);
            lastDotX = x;
            lastDotY = y;
            x += xStep;
            up = !up;
            if (x > r.width + r.x)
                break;
        }
        PaintUtils.lastDotX = -1;
    }

    static void drawEntry(Graphics g, boolean up, long time, int x, boolean packetloss) {
        if (!packetloss)
            g.setColor(Color.BLACK);

        if (!up) {
            g.drawString(sdf.format(new Date(time)), x + 4, r.y + r.height + 15);
            g.drawLine(x, r.y + r.height, x, r.y + r.height + 10);
            g.drawLine(x, r.y + r.height + 10, x + 3, r.y + r.height + 10);

        } else {
            g.drawString(sdf.format(new Date(time)), x + 4, r.y - 5);
            g.drawLine(x, r.y, x, r.y - 10);
            g.drawLine(x, r.y - 10, x + 3, r.y - 10);
        }
    }

    static Rectangle getBounds(Graphics g) {
        Rectangle r = g.getClipBounds();
        r.height -= 120; r.y += 60;
        r.width -= 120;  r.x += 60;
        return r;
    }

    static void calc_offset(Graphics g) {
        r = getBounds(g);

        if (r.width != last_width || r.height != last_height) {
            last_height = r.height;
            last_width = r.width;

            yStep = r.height * (50.0 / r.height);
            xStep = r.width *  (50.0 / r.width);

            max_nodes = (int) Math.round(r.width / xStep);

            if (max_nodes > JOpp.db.total_nodes)
                max_nodes = JOpp.db.total_nodes;

            max_offset = JOpp.db.data_list.size() - max_nodes;

            scrollbarWidth = (int) (((float) max_nodes / JOpp.db.data_list.size()) * r.width);
        }
        scrollbarProgress = r.x + (int) (((float) offset / max_offset) * (r.width - scrollbarWidth));
    }

    static boolean scroll(int i) {
        int old = offset;
        offset = Math.max(0, Math.min(max_offset, offset + i));
        return old != offset;
    }

    static void drawScrollbar(Graphics g) {
        if (max_nodes >= JOpp.db.total_nodes)
            return;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(scrollbarProgress, 10, scrollbarWidth, 20);
        g.setColor(Color.GRAY);
        g.drawRect(r.x, 10, r.width, 20);
    }

    static void drawRuler(Graphics g) {
        if (drawRuler) {
            Point p = JOpp.pFrame.getContentPane().getMousePosition();
            if (p == null)
                return;
            int y = (int) p.getY();
            int ms = JOpp.db.max_ping - (int) (((float) (y - r.y) / r.height) * JOpp.db.max_ping) - 1;
            if (y >= r.y && y <= r.y + r.height) {
                g.setColor(Color.blue);
                g.drawLine(r.x, y, r.x + r.width, y);
                g.drawString(ms + "ms", r.x + r.width + 5, y + 5);
            }
        }
    }
}
