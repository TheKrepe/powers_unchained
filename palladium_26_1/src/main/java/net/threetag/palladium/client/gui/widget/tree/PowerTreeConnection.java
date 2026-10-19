package net.threetag.palladium.client.gui.widget.tree;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.threetag.palladium.client.gui.widget.PowerTreeWidget;

import java.util.ArrayList;
import java.util.List;

public class PowerTreeConnection {

    private final PowerTreeWidget.Element from;
    private final PowerTreeWidget.Element to;
    private final PowerTreeConnectionStyle.Resolved style;
    private final PowerTreeConnectionPath path;
    private final List<int[]> via;
    private final PowerTreeState state;
    private int dockingOffset = 0;
    private List<int[]> points = List.of();

    public PowerTreeConnection(PowerTreeWidget.Element from, PowerTreeWidget.Element to,
                               PowerTreeConnectionStyle.Resolved style, PowerTreeConnectionPath path, List<int[]> via) {
        this.from = from;
        this.to = to;
        this.style = style;
        this.path = path;
        this.via = via;
        this.state = PowerTreeState.connection(from.getState(), to.getState());
    }

    public PowerTreeWidget.Element getFrom() {
        return this.from;
    }

    public PowerTreeWidget.Element getTo() {
        return this.to;
    }

    public PowerTreeState getState() {
        return this.state;
    }

    public PowerTreeConnectionPath getPath() {
        return this.path;
    }

    public boolean canDock() {
        return this.via.isEmpty() && this.path != PowerTreeConnectionPath.STRAIGHT;
    }

    public int getDockingSpacing() {
        return this.style.getWidth() + this.style.getOutlineWidth() * 2 + 1;
    }

    public void setDockingOffset(int dockingOffset) {
        this.dockingOffset = dockingOffset;
    }

    public void buildPoints() {
        List<int[]> anchors = new ArrayList<>();
        anchors.add(new int[]{this.from.getX(), this.from.getY()});
        anchors.addAll(this.via);

        if (this.path == PowerTreeConnectionPath.ELBOW_VERTICAL) {
            anchors.add(new int[]{this.to.getX() + this.dockingOffset, this.to.getY()});
        } else if (this.path == PowerTreeConnectionPath.ELBOW_HORIZONTAL) {
            anchors.add(new int[]{this.to.getX(), this.to.getY() + this.dockingOffset});
        } else {
            anchors.add(new int[]{this.to.getX(), this.to.getY()});
        }

        List<int[]> points = new ArrayList<>();
        points.add(anchors.getFirst());

        for (int i = 1; i < anchors.size(); i++) {
            int[] a = anchors.get(i - 1);
            int[] b = anchors.get(i);

            if (a[0] != b[0] && a[1] != b[1]) {
                if (this.path == PowerTreeConnectionPath.ELBOW_HORIZONTAL) {
                    int midX = a[0] + (b[0] - a[0]) / 2;
                    points.add(new int[]{midX, a[1]});
                    points.add(new int[]{midX, b[1]});
                } else if (this.path == PowerTreeConnectionPath.ELBOW_VERTICAL) {
                    int midY = a[1] + (b[1] - a[1]) / 2;
                    points.add(new int[]{a[0], midY});
                    points.add(new int[]{b[0], midY});
                }
            }

            points.add(b);
        }

        this.points = points;
    }

    public void draw(GuiGraphicsExtractor gui, int x, int y, boolean outline) {
        int extra = outline ? this.style.getOutlineWidth() : 0;

        if (outline && extra <= 0) {
            return;
        }

        int width = this.style.getWidth();
        int color = outline ? this.style.getOutlineColor(this.state) : this.style.getColor(this.state);
        var dash = this.style.getDash();
        float phase = 0F;

        for (int i = 1; i < this.points.size(); i++) {
            float ax = x + this.points.get(i - 1)[0];
            float ay = y + this.points.get(i - 1)[1];
            float bx = x + this.points.get(i)[0];
            float by = y + this.points.get(i)[1];
            float length = Mth.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay));

            if (length <= 0F) {
                continue;
            }

            if (dash.isEmpty()) {
                drawLine(gui, ax, ay, bx, by, width, extra, color);
                continue;
            }

            int on = dash.get().getFirst();
            int period = on + dash.get().get(1);
            float t = 0F;

            while (t < length) {
                float pos = (phase + t) % period;

                if (pos < on) {
                    float end = Math.min(length, t + (on - pos));
                    drawLine(gui, Mth.lerp(t / length, ax, bx), Mth.lerp(t / length, ay, by),
                            Mth.lerp(end / length, ax, bx), Mth.lerp(end / length, ay, by), width, extra, color);
                    t = end;
                } else {
                    t = Math.min(length, t + (period - pos));
                }
            }

            phase = (phase + length) % period;
        }
    }

    private static void drawLine(GuiGraphicsExtractor gui, float ax, float ay, float bx, float by, int width, int extra, int color) {
        int lo = width / 2 + extra;
        int hi = width - width / 2 + extra;

        if (Math.round(ay) == Math.round(by)) {
            int y = Math.round(ay);
            gui.fill(Math.round(Math.min(ax, bx)) - lo, y - lo, Math.round(Math.max(ax, bx)) + hi, y + hi, color);
        } else if (Math.round(ax) == Math.round(bx)) {
            int x = Math.round(ax);
            gui.fill(x - lo, Math.round(Math.min(ay, by)) - lo, x + hi, Math.round(Math.max(ay, by)) + hi, color);
        } else {
            float length = Mth.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay));
            gui.pose().pushMatrix();
            gui.pose().translate(ax, ay);
            gui.pose().rotate((float) Math.atan2(by - ay, bx - ax));
            gui.fill(-lo, -lo, Math.round(length) + hi, hi, color);
            gui.pose().popMatrix();
        }
    }
}
