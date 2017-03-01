import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Created by universallp on 25.02.2017.
 * This file is part of jopp which is licenced
 * under the MOZILLA PUBLIC LICENCE 2.0 - mozilla.org/en-US/MPL/2.0/
 * github.com/UniversalLP/opp
 */
class PaintFrame extends JFrame {

    PaintFrame(String title) {
        super(title);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setContentPane(new PaintPane());
        setVisible(true);

    }

    class PaintPane extends JPanel {

        PaintPane() {
            addMouseWheelListener(new PaneMouseWheelListener());
            addMouseListener(new PaneMouseHandler());
            addMouseMotionListener(new PaneMouseMotionListener());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            PaintUtils.calc_offset(g);
            PaintUtils.drawGrid(g);
            PaintUtils.drawEntries(g, JOpp.db.data_list);
            PaintUtils.drawScrollbar(g);
            PaintUtils.drawRuler(g);
        }
    }

    class PaneMouseWheelListener implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (PaintUtils.scroll(e.getWheelRotation()))
                JOpp.pFrame.repaint();
        }
    }

    class PaneMouseHandler implements MouseInputListener {

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            PaintUtils.gS.drawRuler = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            PaintUtils.gS.drawRuler = false;
            JOpp.pFrame.repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) { }

        @Override
        public void mouseExited(MouseEvent e) { }

        @Override
        public void mouseDragged(MouseEvent e) { }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }

    class PaneMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {
            if (PaintUtils.gS.drawRuler)
                JOpp.pFrame.repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }
}
