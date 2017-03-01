import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by universallp on 25.02.2017.
 * This file is part of jopp which is licenced
 * under the MOZILLA PUBLIC LICENCE 2.0 - mozilla.org/en-US/MPL/2.0/
 * github.com/UniversalLP/opp
 */
class PaintUtils {
    static GraphSettings gS = new GraphSettings();
    private static int offset = 0;
    static Rectangle r;
    static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    static void drawGrid(Graphics g) {
        g.setColor(Color.BLACK);
        int ms;

        for (int h = 0; h < r.height; h += gS.yStep) {
            g.drawLine(r.x, r.y + r.height - h, r.x + r.width, r.y + r.height - h);
            ms = (int) (((float) h / r.height) * JOpp.db.max_ping);
            g.drawString(ms + "ms", 5, r.y + r.height - h);
        }

        for (int w = 0; w < r.width; w += gS.xStep) {
            g.drawLine(r.x + w, r.y, r.x + w, r.y + r.height);
        }
    }

    static void drawEntries(Graphics g, ArrayList<Database.Data> l) {
        int x = r.x;
        int y;
        boolean up = true;

        g.setColor(Color.RED);

        for (int i = offset; i < gS.nodeSpace + offset; i++) {
            if (i >= l.size())
                break;
            Database.Data d = l.get(i);
            y = (int) (r.y + r.height - (r.height * ((float) d.latency / JOpp.db.max_ping)));
            g.fillRect(x - 2, y - 2, 4, 4);

            if (d.begin_time == d.end_time) {
                if (gS.lastNodeX != -1) // If there's a previous value, connect them
                    g.drawLine(gS.lastNodeX, gS.lastNodeY, x, y);

                drawEntry(g, up, d.begin_time, x, d.has_packet_loss);
            } else { // This entry spans over a longer time -> draw a point for the beginning and the end
                if (gS.lastNodeX != -1) // If there's a previous value, connect them
                    g.drawLine(gS.lastNodeX, gS.lastNodeY, x, y);
                gS.lastNodeX = x;

                drawEntry(g, up, d.begin_time, x, d.has_packet_loss); // Beginning point
                up = !up;
                g.setColor(Color.RED);
                x += gS.xStep;

                if (x > r.width + r.x)
                    break;

                g.fillRect(x - 2, y - 2, 4, 4);
                g.drawLine(gS.lastNodeX, y, x, y);
                drawEntry(g, up, d.end_time, x, d.has_packet_loss); // Ending point
            }



            g.setColor(Color.RED);
            gS.lastNodeX = x;
            gS.lastNodeY = y;
            x += gS.xStep;
            up = !up;
            if (x > r.width + r.x)
                break;
        }
        gS.lastNodeX = -1;
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

        if (r.width != gS.lastWinWidth || r.height != gS.lastWinHeight) {
            gS.lastWinHeight = r.height;
            gS.lastWinWidth = r.width;

            gS.yStep = r.height * (50.0 / r.height);
            gS.xStep = r.width *  (50.0 / r.width);

            gS.nodeSpace = (int) Math.round(r.width / gS.xStep);

            if (gS.nodeSpace > JOpp.db.total_nodes)
                gS.nodeSpace = JOpp.db.total_nodes;

            gS.maxScrollOffset = JOpp.db.data_list.size() - gS.nodeSpace;

            gS.scrollbarWidth = (int) (((float) gS.nodeSpace / JOpp.db.total_nodes) * r.width);
        }
        gS.scrollbarOffset = r.x + (int) (((float) offset / gS.maxScrollOffset) * (r.width - gS.scrollbarWidth));
    }

    static boolean scroll(int i) {
        int old = offset;
        offset = Math.max(0, Math.min(gS.nodeSpace, offset + i));
        return old != offset;
    }

    static void drawScrollbar(Graphics g) {
        if (gS.nodeSpace >= JOpp.db.total_nodes)
            return;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(gS.scrollbarOffset, 10, gS.scrollbarWidth, 20);
        g.setColor(Color.GRAY);
        g.drawRect(r.x, 10, r.width, 20);
    }

    static void drawRuler(Graphics g) {
        if (gS.drawRuler) {
            Point p = JOpp.pFrame.getContentPane().getMousePosition();
            if (p == null)
                return;
            int y = (int) p.getY();
            int x = (int) p.getX();
            int ms = JOpp.db.max_ping - (int) (((float) (y - r.y) / r.height) * JOpp.db.max_ping) - 1;

            if (y >= r.y && y <= r.y + r.height && x >= r.x && x <= r.x + r.width) {
                g.setColor(Color.blue);
                g.drawLine(r.x, y, r.x + r.width, y);
                g.drawLine(x, r.y, x, r.y + r.height);
                g.drawString(ms + "ms", r.x + r.width + 5, y + 5);
            }
        }
    }
}
