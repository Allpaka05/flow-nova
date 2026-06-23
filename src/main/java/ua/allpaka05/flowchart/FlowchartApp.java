package ua.allpaka05.flowchart;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.awt.datatransfer.*;

// ================================================================
//  ТЕМА — всі кольори централізовано
// ================================================================
class T {
    static boolean dark = false;

    // Світла тема
    static final Color L_BG      = new Color(244, 245, 249);
    static final Color L_SURF    = new Color(255, 255, 255);
    static final Color L_SIDE    = new Color(250, 251, 254);
    static final Color L_BORDER  = new Color(220, 222, 232);
    static final Color L_TEXT    = new Color(20,  22,  35);
    static final Color L_TEXT2   = new Color(95,  100, 118);
    static final Color L_TEXT3   = new Color(150, 155, 170);
    static final Color L_HOVER   = new Color(234, 236, 245);
    static final Color L_CANVAS  = new Color(247, 248, 252);

    // Темна тема
    static final Color D_BG      = new Color(25,  27,  32);
    static final Color D_SURF    = new Color(32,  35,  42);
    static final Color D_SIDE    = new Color(28,  31,  38);
    static final Color D_BORDER  = new Color(50,  54,  65);
    static final Color D_TEXT    = new Color(220, 224, 235);
    static final Color D_TEXT2   = new Color(140, 146, 165);
    static final Color D_TEXT3   = new Color(85,  90, 110);
    static final Color D_HOVER   = new Color(38,  42,  52);
    static final Color D_CANVAS  = new Color(20,  22,  28);

    // Акцент (однаковий в обох темах)
    static final Color ACCENT       = new Color(79, 127, 255);
    static final Color ACCENT_DARK  = new Color(55, 105, 235);
    static final Color ACCENT_ALPHA = new Color(79, 127, 255, 35);
    static final Color SUCCESS      = new Color(34, 197, 94);
    static final Color DANGER       = new Color(230, 60, 60);

    static Color bg()     { return dark ? D_BG     : L_BG;     }
    static Color surf()   { return dark ? D_SURF   : L_SURF;   }
    static Color side()   { return dark ? D_SIDE   : L_SIDE;   }
    static Color border() { return dark ? D_BORDER : L_BORDER; }
    static Color text()   { return dark ? D_TEXT   : L_TEXT;   }
    static Color text2()  { return dark ? D_TEXT2  : L_TEXT2;  }
    static Color text3()  { return dark ? D_TEXT3  : L_TEXT3;  }
    static Color hover()  { return dark ? D_HOVER  : L_HOVER;  }
    static Color canvas() { return dark ? D_CANVAS : L_CANVAS; }

    static final Font F_UI    = new Font("Segoe UI", Font.PLAIN,  13);
    static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN,  11);
    static final Font F_BOLD  = new Font("Segoe UI", Font.BOLD,   12);
    static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,   13);
}

class DefaultStyles {
    // Стилі фігур за замовчуванням
    static Color  nodeBg          = new Color(218, 232, 255);
    static Color  nodeText        = new Color(18,  22,  40);
    static Color  nodeBorder      = new Color(70,  110, 195);
    static int    nodeBorderW     = 2;
    static String nodeFontName    = "Segoe UI";
    static int    nodeFontSize    = 14;

    // Стилі ліній за замовчуванням
    static Color   edgeColor      = new Color(38, 42, 58);
    static int     edgeStrokeW    = 2;
    static int     edgeLineStyle  = 0;
    static int     edgeArrowStyle = 0;
    static boolean edgeDashed     = false;
    static boolean edgeArrowStart = false;
    static boolean edgeArrowEnd   = true;

    static Color edgeColorForTheme(boolean dark) {
        if (edgeColor.equals(new Color(38,42,58)))
            return dark ? new Color(180,186,205) : new Color(38,42,58);
        return edgeColor;
    }
}

// ================================================================
//  КНОПКА (Flat / Filled / Danger)
// ================================================================
class Btn extends JButton {
    enum V { GHOST, FILLED, DANGER }
    private final V v;
    private boolean hov = false;

    Btn(String text, V v) {
        super(text);
        this.v = v;
        setFocusPainted(false); setBorderPainted(false); setOpaque(false);
        setFont(T.F_UI); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
            public void mouseExited (MouseEvent e) { hov = false; repaint(); }
        });
    }
    Btn(String text) { this(text, V.GHOST); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg = switch (v) {
            case FILLED -> hov ? T.ACCENT_DARK : T.ACCENT;
            case DANGER -> hov ? new Color(200, 45, 45) : T.DANGER;
            default     -> hov ? T.hover() : T.surf();
        };
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
        if (v == V.GHOST) {
            g2.setColor(T.border());
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 7, 7);
        }
        setForeground(v == V.GHOST ? T.text() : Color.WHITE);
        g2.dispose();
        super.paintComponent(g);
    }
}

// ================================================================
//  ТУЛБАР
// ================================================================
class Toolbar extends JPanel {
    JLabel zoomLbl;

    Toolbar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 3, 6));
        setPreferredSize(new Dimension(0, 44));
        setOpaque(false);
    }

    @Override protected void paintComponent(Graphics g) {
        g.setColor(T.surf());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(T.border());
        g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
    }

    // Метод тепер приймає шлях до картинки в ресурсах, наприклад "/icons/open.png"
    JButton addBtn(String iconPath, String tip, Runnable act) {
        JButton b = new JButton() { // Кнопка без тексту, лише для іконки
            boolean h = false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){h=true; repaint();}
                public void mouseExited(MouseEvent e){h=false; repaint();}
            });
            }
            @Override protected void paintComponent(Graphics g) {
                if (h) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(T.hover());
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        // Завантажуємо іконку з ресурсів
        try {
            java.net.URL imgURL = getClass().getResource(iconPath);
            if (imgURL != null) {
                b.setIcon(new ImageIcon(imgURL));
            } else {
                b.setText("?"); // Заглушка, якщо файл не знайдено
            }
        } catch (Exception ex) {
            b.setText("?");
        }

        b.setPreferredSize(new Dimension(36, 32));
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(tip);
        b.addActionListener(e -> act.run());
        add(b);
        return b;
    }

    void addSep() {
        JPanel s = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(T.border());
                g.fillRect(0, 4, 1, getHeight()-8);
            }
        };
        s.setPreferredSize(new Dimension(8, 30));
        s.setOpaque(false);
        add(s);
    }

    void initZoom(Runnable onReset) {
        addSep();
        zoomLbl = new JLabel("100%");
        zoomLbl.setFont(T.F_SMALL); zoomLbl.setForeground(T.text2());
        zoomLbl.setPreferredSize(new Dimension(40, 30)); zoomLbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(zoomLbl);
        addBtn("/icons/mashtab.png", "Скинути масштаб", onReset); // ФІКС: тепер це справжній кольоровий символ скидання
    }

    void setZoom(double sc) {
        if (zoomLbl != null) zoomLbl.setText(Math.round(sc*100)+"%");
    }

    void refreshColors() {
        repaint();
        if(zoomLbl!=null){ zoomLbl.setForeground(T.text2()); }
    }
}

// ================================================================
//  СЕКЦІЯ-АКОРДЕОН (права панель)
// ================================================================
class Sec extends JPanel {
    boolean open = true;
    JPanel body;
    String label;

    Sec(String label) {
        this.label = label;
        setLayout(new BorderLayout()); setOpaque(false);
        setAlignmentX(LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JButton hdr = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(T.surf()); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(T.border()); g2.drawLine(0,0,getWidth(),0);
                g2.setFont(T.F_BOLD); g2.setColor(T.text2()); g2.drawString(label, 10, 17);
                // стрілка
                int ax = getWidth()-18, ay = 7;
                int[] xs, ys;
                if (open) { xs=new int[]{ax,ax+7,ax+4}; ys=new int[]{ay+6,ay+6,ay}; }
                else       { xs=new int[]{ax,ax+7,ax+4}; ys=new int[]{ay,ay,ay+6};   }
                g2.setColor(T.text3()); g2.fillPolygon(xs,ys,3);
                g2.dispose();
            }
        };
        hdr.setPreferredSize(new Dimension(0, 26));
        hdr.setFocusPainted(false); hdr.setBorderPainted(false); hdr.setOpaque(false);
        hdr.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hdr.addActionListener(e -> { open=!open; body.setVisible(open); repaint(); });

        body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(6,10,10,10));

        add(hdr, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    void row(String lbl, JComponent c) {
        JLabel l = new JLabel(lbl); l.setFont(T.F_SMALL); l.setForeground(T.text2());
        l.setAlignmentX(LEFT_ALIGNMENT);
        body.add(l); body.add(Box.createVerticalStrut(2));
        c.setAlignmentX(LEFT_ALIGNMENT); c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        body.add(c); body.add(Box.createVerticalStrut(8));
    }
    void comp(JComponent c) { c.setAlignmentX(LEFT_ALIGNMENT); body.add(c); body.add(Box.createVerticalStrut(5)); }
    void gap(int n) { body.add(Box.createVerticalStrut(n)); }
}

// ================================================================
//  КОЛЬОРОВИЙ СВОТЧ-РЯДОК
// ================================================================
class ColorRow extends JPanel {
    private Color cur;
    private final JPanel swatch;

    ColorRow(String lbl, Color init) {
        cur = init;
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        setOpaque(false); setAlignmentX(LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel l = new JLabel(lbl); l.setFont(T.F_SMALL); l.setForeground(T.text2());
        swatch = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cur); g2.fillRoundRect(1,1,getWidth()-2,getHeight()-2,5,5);
                g2.setColor(T.border()); g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,5,5);
                g2.dispose();
            }
        };
        swatch.setPreferredSize(new Dimension(28, 18)); swatch.setOpaque(false);
        swatch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(l); add(swatch);
    }

    void setPickListener(Runnable r) { swatch.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){r.run();}}); }
    void setColor(Color c) { cur = c; swatch.repaint(); }
    Color getColor() { return cur; }
}

// ================================================================
//  TOAST-ПОВІДОМЛЕННЯ
// ================================================================
class Toast {
    static void show(JFrame parent, String msg, boolean ok) {
        JWindow w = new JWindow(parent);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 7)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ok ? new Color(22,50,28) : new Color(60,18,18));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        JLabel ico = new JLabel(ok ? "✓" : "✗"); ico.setForeground(ok ? T.SUCCESS : T.DANGER); ico.setFont(T.F_TITLE);
        JLabel txt = new JLabel(msg);  txt.setForeground(new Color(215,220,230)); txt.setFont(T.F_UI);
        p.add(ico); p.add(txt); p.setBorder(new EmptyBorder(0,4,0,10));
        w.setContentPane(p); w.pack();
        Point loc = parent.getLocation(); Dimension sz = parent.getSize();
        w.setLocation(loc.x + sz.width - w.getWidth() - 20, loc.y + sz.height - w.getHeight() - 52);
        try { w.setOpacity(0.94f); } catch (Exception ignored) {}
        w.setVisible(true);
        new javax.swing.Timer(2400, e -> w.dispose()).start();
    }
}

// ================================================================
//  КАРТКА ФІГУРИ В ПАЛІТРІ
// ================================================================
class ShapeCard extends JPanel {
    private final ShapeType type;
    private boolean hov = false;

    ShapeCard(ShapeType type, Runnable onAdd) {
        this.type = type;
        setPreferredSize(new Dimension(130, 62));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Додати: " + name() + " (або перетягніть на полотно)");

        // --- ДОДАНО: Механізм віддачі даних при перетягуванні ---
        setTransferHandler(new TransferHandler() {
            @Override
            protected Transferable createTransferable(JComponent c) {
                // 1. Створюємо зображення для прев'ю
                int w = 120, h = 60;
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();

                // 2. Налаштовуємо графіку для красивого рендеру
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 3. Створюємо тимчасовий об'єкт для малювання
                // Використовуємо 0, 0 як координати, бо малюємо всередині буфера
                FlowNode previewNode = new FlowNode(0, 0, type, T.dark);
                // Додайте false як другий аргумент (ви не хочете, щоб прев'ю виглядало вибраним)
                previewNode.draw(g2, false);
                g2.dispose();

                // 4. Встановлюємо прев'ю
                setDragImage(img);
                setDragImageOffset(new Point(w / 2, h / 2)); // Центр фігури під курсором

                return new StringSelection(type.name());
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                // Запускаємо Drag-and-Drop при затисненні і русі миші
                getTransferHandler().exportAsDrag(ShapeCard.this, e, TransferHandler.COPY);
            }
        });
        // --------------------------------------------------------

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hov=true;  repaint(); }
            public void mouseExited (MouseEvent e) { hov=false; repaint(); }
            public void mouseClicked(MouseEvent e) { onAdd.run(); } // Звичайний клік працює як і раніше
        });
    }

    private String name() {
        return switch (type) {
            case PROCESS -> "Процес"; case TERMINATOR -> "Початок/Кінець";
            case DECISION -> "Умова"; case CYCLE -> "Цикл";
            case DATA -> "Дані"; case CONNECTOR -> "З'єднувач"; case COMMENT -> "Коментар";
        };
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // Фон картки
        g2.setColor(hov ? T.hover() : T.surf());
        g2.fillRoundRect(0,0,w-1,h-1,9,9);
        g2.setColor(hov ? T.ACCENT : T.border());
        g2.setStroke(new BasicStroke(hov ? 1.5f : 1f));
        g2.drawRoundRect(0,0,w-2,h-2,9,9);

        // Мініатюра
        Color sb = hov ? new Color(T.ACCENT.getRed(),T.ACCENT.getGreen(),T.ACCENT.getBlue(),40)
                : (T.dark ? new Color(55,65,90) : new Color(215,228,255));
        Color sc = hov ? T.ACCENT : (T.dark ? new Color(90,115,175) : new Color(75,115,200));
        g2.setStroke(new BasicStroke(1.4f));

        int sx=8, sy=8, sw=w-16, sh=h-26;
        switch(type) {
            case PROCESS    -> { g2.setColor(sb);g2.fillRoundRect(sx,sy,sw,sh,4,4); g2.setColor(sc);g2.drawRoundRect(sx,sy,sw,sh,4,4); }
            case TERMINATOR -> { g2.setColor(sb);g2.fillRoundRect(sx,sy,sw,sh,sh,sh); g2.setColor(sc);g2.drawRoundRect(sx,sy,sw,sh,sh,sh); }
            case DECISION   -> { Polygon p=new Polygon(); p.addPoint(sx+sw/2,sy); p.addPoint(sx+sw,sy+sh/2); p.addPoint(sx+sw/2,sy+sh); p.addPoint(sx,sy+sh/2); g2.setColor(sb);g2.fillPolygon(p); g2.setColor(sc);g2.drawPolygon(p); }
            case DATA       -> { Polygon p=new Polygon(); p.addPoint(sx+8,sy); p.addPoint(sx+sw,sy); p.addPoint(sx+sw-8,sy+sh); p.addPoint(sx,sy+sh); g2.setColor(sb);g2.fillPolygon(p); g2.setColor(sc);g2.drawPolygon(p); }
            case CYCLE      -> { Polygon p=new Polygon(); int o=8; p.addPoint(sx+o,sy); p.addPoint(sx+sw-o,sy); p.addPoint(sx+sw,sy+sh/2); p.addPoint(sx+sw-o,sy+sh); p.addPoint(sx+o,sy+sh); p.addPoint(sx,sy+sh/2); g2.setColor(sb);g2.fillPolygon(p); g2.setColor(sc);g2.drawPolygon(p); }
            case CONNECTOR  -> { int d=Math.min(sw,sh); g2.setColor(sb);g2.fillOval(sx+(sw-d)/2,sy,d,d); g2.setColor(sc);g2.drawOval(sx+(sw-d)/2,sy,d,d); }
            case COMMENT    -> { g2.setColor(sc); g2.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{5},0)); g2.drawLine(sx,sy,sx,sy+sh); g2.setStroke(new BasicStroke(1.4f)); g2.drawLine(sx,sy,sx+12,sy); g2.drawLine(sx,sy+sh,sx+12,sy+sh); }
        }

        // Назва
        g2.setFont(T.F_SMALL); g2.setColor(T.text2());
        FontMetrics fm = g2.getFontMetrics();
        String n = name();
        g2.drawString(n, (w - fm.stringWidth(n))/2, h-6);
        g2.dispose();
    }
}

// ================================================================
//  ГОЛОВНИЙ КЛАС
// ================================================================
public class FlowchartApp extends JFrame {

    CanvasPanel canvas;
    JPanel      rightPanel, leftPanel;
    CardLayout  cardLayout;
    JPanel      canvasProps, nodeProps, edgeProps;
    Toolbar     toolbar;

    // Поля правої панелі — фігура
    JTextField   nodeTF, nodeW, nodeH;
    JComboBox<String> fontBox, alignBox;
    JSpinner     fontSzSpin, borderWSpin;
    JCheckBox    boldCB, italicCB;
    ColorRow     bgRow, textRow, borderRow;

    // Поля правої панелі — лінія
    JComboBox<String> lineStyleBox, arrowStyleBox;
    JSpinner     lineWSpin;
    JCheckBox    dashedCB, arrowStartCB, arrowEndCB;
    ColorRow     edgeColorRow;

    boolean updatingUI = false;

    // ── main ────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FlowchartApp().setVisible(true));
    }

    // ── конструктор ─────────────────────────────────────────
    public FlowchartApp() {
        setTitle("FlowNova - Flowchart editor");
        setSize(1460, 880);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        java.net.URL imgURL = getClass().getResource("/icons/app_icon32.png");
        if (imgURL != null) {setIconImage(new ImageIcon(imgURL).getImage());}

        canvas = new CanvasPanel(this::syncProps, T.dark);
        canvas.onZoom = z -> toolbar.setZoom(z);

        buildToolbar();
        buildLeft();
        buildRight();
        buildLayout();
        applyTheme();
    }

    // ================================================================
    //  ТУЛБАР
    // ================================================================
    void buildToolbar() {
        toolbar = new Toolbar();

        // Справжні кольорові емодзі для всіх кнопок
        toolbar.addBtn("/icons/open.png", "Відкрити (Ctrl+O)",        this::loadProject);
        toolbar.addBtn("/icons/save.png", "Зберегти (Ctrl+S)",        this::saveProject);
        toolbar.addSep();
        toolbar.addBtn("/icons/import.png", "Імпорт псевдокоду (Ctrl+I)", this::importTxt);
        toolbar.addBtn("/icons/export.png", "Експорт PNG (Ctrl+E)",     this::exportPNG);
        toolbar.addSep();
        toolbar.addBtn("/icons/undo.png", "Скасувати (Ctrl+Z)",  canvas::undo);
        toolbar.addBtn("/icons/redo.png", "Повторити (Ctrl+Y)",  canvas::redo);
        toolbar.addSep();
        toolbar.addBtn("/icons/copy.png", "Копіювати (Ctrl+C)",  canvas::copySelected); // ФІКС: замінено ⧉ на сторінку
        toolbar.addBtn("/icons/paste.png", "Вставити (Ctrl+V)",  canvas::pasteItems);
        toolbar.addBtn("/icons/delete.png", "Видалити (Delete)",   canvas::deleteSelected);
        toolbar.addSep();
        toolbar.addBtn("/icons/tema.png", "Перемкнути тему", this::toggleTheme);       // ФІКС: замінено ◑ на півмісяць
        toolbar.addSep();
        toolbar.addBtn("/icons/hotkeys.png", "Гарячі клавіші", this::showKeys);           // ФІКС: замінено ? на синій емодзі-знак
        toolbar.initZoom(() -> { canvas.scale=1; canvas.tx=0; canvas.ty=0; canvas.repaint(); toolbar.setZoom(1); });

        // Невидимий JMenuBar для глобальних акселераторів
        JMenuBar mb = new JMenuBar();
        mb.setPreferredSize(new Dimension(0,0));
        JMenu m = new JMenu();
        accel(m, KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, this::saveProject);
        accel(m, KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, this::loadProject);
        accel(m, KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, this::importTxt);
        accel(m, KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK, this::exportPNG);
        mb.add(m);
        setJMenuBar(mb);
    }

    private void accel(JMenu menu, int key, int mod, Runnable r) {
        JMenuItem it = new JMenuItem(); it.setAccelerator(KeyStroke.getKeyStroke(key, mod));
        it.addActionListener(e -> r.run()); menu.add(it);
    }

    // ================================================================
    //  ЛІВА ПАНЕЛЬ
    // ================================================================
    void buildLeft() {
        leftPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(T.side()); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(T.border()); g.drawLine(getWidth()-1,0,getWidth()-1,getHeight());
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(148, 0));
        leftPanel.setBorder(new EmptyBorder(12, 7, 12, 7));
        leftPanel.setOpaque(false);

        JLabel lbl = new JLabel("ЕЛЕМЕНТИ");
        lbl.setFont(T.F_BOLD); lbl.setForeground(T.text3());
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        leftPanel.add(lbl); leftPanel.add(Box.createVerticalStrut(10));

        for (ShapeType t : ShapeType.values()) {
            final ShapeType ft = t;
            ShapeCard card = new ShapeCard(t, () -> {
                canvas.saveState();
                int cx = canvas.snap((int)(((canvas.getWidth()/2.0)-canvas.tx)/canvas.scale)-60);
                int cy = canvas.snap((int)(((canvas.getHeight()/2.0)-canvas.ty)/canvas.scale)-30);
                canvas.nodes.add(new FlowNode(cx, cy, ft, T.dark));
                canvas.repaint();
            });
            card.setAlignmentX(CENTER_ALIGNMENT);
            leftPanel.add(card); leftPanel.add(Box.createVerticalStrut(5));
        }
        leftPanel.add(Box.createVerticalGlue());
    }

    // ================================================================
    //  ПРАВА ПАНЕЛЬ
    // ================================================================
    void buildRight() {
        cardLayout = new CardLayout();
        rightPanel = new JPanel(cardLayout) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(T.side()); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(T.border()); g.drawLine(0,0,0,getHeight());
            }
        };
        rightPanel.setPreferredSize(new Dimension(258, 0));
        rightPanel.setOpaque(false);

        buildCanvasPanel();
        buildNodePanel();
        buildEdgePanel();

        rightPanel.add(scroll(canvasProps), "CANVAS");
        rightPanel.add(scroll(nodeProps),   "NODE");
        rightPanel.add(scroll(edgeProps),   "EDGE");
    }

    private JScrollPane scroll(JPanel p) {
        JScrollPane sp = new JScrollPane(p, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null); sp.setOpaque(false); sp.getViewport().setOpaque(false);
        // Швидкий скрол колісцем: крок 16px (замість дефолтного 1-3px)
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setBlockIncrement(80);
        return sp;
    }

    // ── ПОЛОТНО ────────────────────────────────────────────
    // Поля для дефолтних стилів (зберігаємо щоб можна було оновлювати свотчі при зміні теми)
    ColorRow defNodeBgRow, defNodeTextRow, defNodeBorderRow;
    ColorRow defEdgeColorRow;
    JComboBox<String> defLineStyleBox, defArrowStyleBox;
    JSpinner defNodeBorderWSpin, defNodeFontSzSpin, defEdgeWSpin;
    JComboBox<String> defFontBox;
    JCheckBox defEdgeDashedCB, defEdgeArrowStartCB, defEdgeArrowEndCB;

    void buildCanvasPanel() {
        canvasProps = propRoot();
        canvasProps.add(sectionTitle("ПОЛОТНО"));

        // ── Сітка ──────────────────────────────────────────────
        Sec grid = new Sec("Сітка");
        JCheckBox snapCB = styledCB("Прив'язка до сітки", true);
        snapCB.addActionListener(e -> { canvas.snapToGrid = snapCB.isSelected(); canvas.repaint(); });
        JSpinner gridSpin = mkSpin(20,5,100,5);
        gridSpin.addChangeListener(e -> { canvas.gridSize=(int)gridSpin.getValue(); canvas.repaint(); });
        grid.comp(snapCB); grid.row("Розмір сітки:", gridSpin);
        canvasProps.add(grid);

        // ── Лінії полотна ──────────────────────────────────────
        Sec lines = new Sec("Лінії");
        JCheckBox orthoCB = styledCB("Ортогональні лінії", true);
        orthoCB.addActionListener(e -> { canvas.orthogonal=orthoCB.isSelected(); canvas.repaint(); });
        JCheckBox dstuCB  = styledCB("ДСТУ-стрілки на зворотніх відрізках", false);
        dstuCB.setToolTipText("Стрілки на кожному відрізку що веде вліво або вгору");
        dstuCB.addActionListener(e -> {
            canvas.dstuMode = dstuCB.isSelected();
            if (canvas.dstuMode) canvas.rebuildDstu(); else canvas.clearDstu();
            canvas.repaint();
        });
        lines.comp(orthoCB); lines.comp(dstuCB);
        canvasProps.add(lines);

        // ── Вигляд полотна ─────────────────────────────────────
        Sec view = new Sec("Полотно");
        Btn btnBg    = new Btn("Колір фону полотна");
        Btn btnReset = new Btn("Скинути масштаб");
        Btn btnClear = new Btn("Очистити все", Btn.V.DANGER);
        btnBg.addActionListener(e -> { Color c=JColorChooser.showDialog(this,"Колір фону",canvas.getBackground()); if(c!=null)canvas.setBackground(c); });
        btnReset.addActionListener(e -> { canvas.scale=1; canvas.tx=0; canvas.ty=0; canvas.repaint(); toolbar.setZoom(1); });
        btnClear.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this,"Очистити полотно?","",JOptionPane.YES_NO_OPTION)==0) { canvas.saveState(); canvas.clear(); }
        });
        view.comp(btnBg); view.comp(btnReset); view.gap(4); view.comp(btnClear);
        canvasProps.add(view);

        // ── Стиль фігур за замовчуванням ───────────────────────
        Sec defNode = new Sec("Стиль фігур за замовчуванням");

        defNodeBgRow     = new ColorRow("Фон фігури:",   DefaultStyles.nodeBg);
        defNodeTextRow   = new ColorRow("Текст:",        DefaultStyles.nodeText);
        defNodeBorderRow = new ColorRow("Межа:",         DefaultStyles.nodeBorder);

        defNodeBgRow.setPickListener(() -> {
            Color c = JColorChooser.showDialog(this, "Фон фігури за замовчуванням", DefaultStyles.nodeBg);
            if (c != null) { DefaultStyles.nodeBg = c; defNodeBgRow.setColor(c); }
        });
        defNodeTextRow.setPickListener(() -> {
            Color c = JColorChooser.showDialog(this, "Колір тексту за замовчуванням", DefaultStyles.nodeText);
            if (c != null) { DefaultStyles.nodeText = c; defNodeTextRow.setColor(c); }
        });
        defNodeBorderRow.setPickListener(() -> {
            Color c = JColorChooser.showDialog(this, "Колір межі за замовчуванням", DefaultStyles.nodeBorder);
            if (c != null) { DefaultStyles.nodeBorder = c; defNodeBorderRow.setColor(c); }
        });

        defNodeBorderWSpin = mkSpin(DefaultStyles.nodeBorderW, 1, 10, 1);
        defNodeBorderWSpin.addChangeListener(e -> DefaultStyles.nodeBorderW = (int)defNodeBorderWSpin.getValue());

        String[] fns = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        defFontBox = styledCombo(fns);
        defFontBox.setSelectedItem(DefaultStyles.nodeFontName);
        defFontBox.addActionListener(e -> DefaultStyles.nodeFontName = (String)defFontBox.getSelectedItem());

        defNodeFontSzSpin = mkSpin(DefaultStyles.nodeFontSize, 6, 96, 1);
        defNodeFontSzSpin.addChangeListener(e -> DefaultStyles.nodeFontSize = (int)defNodeFontSzSpin.getValue());

        defNode.comp(defNodeBgRow);
        defNode.comp(defNodeTextRow);
        defNode.comp(defNodeBorderRow);
        defNode.row("Товщина межі:", defNodeBorderWSpin);
        defNode.row("Шрифт:", defFontBox);
        defNode.row("Розмір шрифту:", defNodeFontSzSpin);
        canvasProps.add(defNode);

        // ── Стиль ліній за замовчуванням ───────────────────────
        Sec defEdge = new Sec("Стиль ліній за замовчуванням");

        defEdgeColorRow = new ColorRow("Колір лінії:", DefaultStyles.edgeColor);
        defEdgeColorRow.setPickListener(() -> {
            Color c = JColorChooser.showDialog(this, "Колір лінії за замовчуванням", DefaultStyles.edgeColor);
            if (c != null) { DefaultStyles.edgeColor = c; defEdgeColorRow.setColor(c); }
        });

        defEdgeWSpin = mkSpin(DefaultStyles.edgeStrokeW, 1, 10, 1);
        defEdgeWSpin.addChangeListener(e -> DefaultStyles.edgeStrokeW = (int)defEdgeWSpin.getValue());

        defLineStyleBox = styledCombo(new String[]{"Суцільна","Штрих","Точкова"});
        defLineStyleBox.setSelectedIndex(DefaultStyles.edgeLineStyle);
        defLineStyleBox.addActionListener(e -> DefaultStyles.edgeLineStyle = defLineStyleBox.getSelectedIndex());

        defArrowStyleBox = styledCombo(new String[]{"Заповнена","Відкрита","Кругла"});
        defArrowStyleBox.setSelectedIndex(DefaultStyles.edgeArrowStyle);
        defArrowStyleBox.addActionListener(e -> DefaultStyles.edgeArrowStyle = defArrowStyleBox.getSelectedIndex());

        defEdgeDashedCB    = styledCB("Штрихпунктирна",   DefaultStyles.edgeDashed);
        defEdgeArrowStartCB= styledCB("Стрілка на початку", DefaultStyles.edgeArrowStart);
        defEdgeArrowEndCB  = styledCB("Стрілка в кінці",  DefaultStyles.edgeArrowEnd);
        defEdgeDashedCB    .addActionListener(e -> DefaultStyles.edgeDashed    = defEdgeDashedCB.isSelected());
        defEdgeArrowStartCB.addActionListener(e -> DefaultStyles.edgeArrowStart= defEdgeArrowStartCB.isSelected());
        defEdgeArrowEndCB  .addActionListener(e -> DefaultStyles.edgeArrowEnd  = defEdgeArrowEndCB.isSelected());

        defEdge.comp(defEdgeColorRow);
        defEdge.row("Товщина:", defEdgeWSpin);
        defEdge.row("Тип лінії:", defLineStyleBox);
        defEdge.row("Стиль стрілки:", defArrowStyleBox);
        defEdge.comp(defEdgeDashedCB);
        defEdge.comp(defEdgeArrowStartCB);
        defEdge.comp(defEdgeArrowEndCB);
        canvasProps.add(defEdge);

        canvasProps.add(Box.createVerticalGlue());
    }

    // ── ФІГУРА ─────────────────────────────────────────────
    void buildNodePanel() {
        nodeProps = propRoot();
        nodeProps.add(sectionTitle("ФІГУРА"));

        // Текст
        Sec secTxt = new Sec("Текст");
        nodeTF = styledTF(); nodeTF.getDocument().addDocumentListener(dl(this::updateNode));
        secTxt.row("Вміст:", nodeTF);

        String[] fns = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontBox = styledCombo(fns); fontBox.addActionListener(e -> updateNode());
        secTxt.row("Шрифт:", fontBox);

        fontSzSpin = mkSpin(14,6,96,1); fontSzSpin.addChangeListener(e -> updateNode());
        secTxt.row("Розмір шрифту:", fontSzSpin);

        alignBox = styledCombo(new String[]{"По центру","Зліва","Справа"});
        alignBox.addActionListener(e -> updateNode());
        secTxt.row("Вирівнювання:", alignBox);

        JPanel stylePnl = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); stylePnl.setOpaque(false);
        boldCB   = styledCB("Жирний", false); boldCB.addActionListener(e->updateNode());
        italicCB = styledCB("Курсив",  false); italicCB.addActionListener(e->updateNode());
        stylePnl.add(boldCB); stylePnl.add(italicCB);
        secTxt.comp(stylePnl);
        nodeProps.add(secTxt);

        // Розміри
        Sec secSz = new Sec("Розміри");
        nodeW = styledTF(); nodeW.getDocument().addDocumentListener(dl(this::updateNode));
        nodeH = styledTF(); nodeH.getDocument().addDocumentListener(dl(this::updateNode));
        secSz.row("Ширина:", nodeW); secSz.row("Висота:", nodeH);
        nodeProps.add(secSz);

        // Вигляд
        Sec secSt = new Sec("Вигляд");
        borderWSpin = mkSpin(2,1,10,1); borderWSpin.addChangeListener(e->updateNode());
        secSt.row("Товщина межі:", borderWSpin);

        bgRow     = new ColorRow("Фон фігури:", new Color(218,232,255));
        textRow   = new ColorRow("Текст:",       new Color(20,22,40));
        borderRow = new ColorRow("Межа:",         new Color(70,110,195));
        bgRow    .setPickListener(() -> pickNodeColor("bg"));
        textRow  .setPickListener(() -> pickNodeColor("text"));
        borderRow.setPickListener(() -> pickNodeColor("border"));
        secSt.comp(bgRow); secSt.comp(textRow); secSt.comp(borderRow);
        nodeProps.add(secSt);
        nodeProps.add(Box.createVerticalGlue());
    }

    // ── ЛІНІЯ ──────────────────────────────────────────────
    void buildEdgePanel() {
        edgeProps = propRoot();
        edgeProps.add(sectionTitle("ЛІНІЯ"));

        Sec secSt = new Sec("Стиль лінії");
        lineStyleBox = styledCombo(new String[]{"Суцільна","Штрих","Точкова"});
        lineStyleBox.addActionListener(e -> updateEdge());
        secSt.row("Тип:", lineStyleBox);

        lineWSpin = mkSpin(2,1,10,1); lineWSpin.addChangeListener(e->updateEdge());
        secSt.row("Товщина:", lineWSpin);

        dashedCB = styledCB("Штрихпунктирна", false); dashedCB.addActionListener(e->updateEdge());
        secSt.comp(dashedCB);

        edgeColorRow = new ColorRow("Колір:", new Color(40,45,60));
        edgeColorRow.setPickListener(() -> {
            if (canvas.selEdge==null) return;
            canvas.saveState();
            Color c = JColorChooser.showDialog(this,"Колір лінії",canvas.selEdge.color);
            if (c!=null) { canvas.selEdge.color=c; edgeColorRow.setColor(c); canvas.repaint(); }
        });
        secSt.comp(edgeColorRow);
        edgeProps.add(secSt);

        Sec secArr = new Sec("Стрілки");
        arrowStyleBox = styledCombo(new String[]{"Заповнена","Відкрита","Кругла"});
        arrowStyleBox.addActionListener(e->updateEdge());
        secArr.row("Вигляд:", arrowStyleBox);

        arrowStartCB = styledCB("Стрілка на початку", false); arrowStartCB.addActionListener(e->updateEdge());
        arrowEndCB   = styledCB("Стрілка в кінці",    true);  arrowEndCB.addActionListener(e->updateEdge());
        secArr.comp(arrowStartCB); secArr.comp(arrowEndCB);
        edgeProps.add(secArr);
        edgeProps.add(Box.createVerticalGlue());
    }

    // ================================================================
    //  КОМПОНУВАННЯ
    // ================================================================
    void buildLayout() {
        JSplitPane rSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, rightPanel);
        rSplit.setResizeWeight(1.0); rSplit.setBorder(null); rSplit.setDividerSize(1);

        JSplitPane mSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rSplit);
        mSplit.setBorder(null); mSplit.setDividerSize(1);


        add(toolbar,   BorderLayout.NORTH);
        add(mSplit,    BorderLayout.CENTER);
        syncProps();
    }

    // ================================================================
    //  ТЕМА
    // ================================================================
    void toggleTheme() { T.dark = !T.dark; applyTheme(); }

    void applyTheme() {
        canvas.isDark = T.dark;
        canvas.setBackground(T.canvas());
        applyRec(getContentPane());
        toolbar.refreshColors();
        repaint(); canvas.repaint();
    }

    private void applyRec(Component c) {
        if      (c instanceof JPanel || c instanceof JSplitPane) { c.setBackground(T.bg()); }
        else if (c instanceof JLabel l)      { l.setForeground(T.text()); }
        else if (c instanceof JCheckBox cb)  { cb.setBackground(T.side()); cb.setForeground(T.text()); }
        else if (c instanceof JTextField tf) { tf.setBackground(T.surf()); tf.setForeground(T.text()); tf.setCaretColor(T.text()); }
        else if (c instanceof JSpinner sp)   { sp.setBackground(T.surf()); sp.getEditor().setBackground(T.surf()); }
        else if (c instanceof JComboBox<?> cb){ cb.setBackground(T.surf()); cb.setForeground(T.text()); }
        if (c instanceof Container ct)
            for (Component ch : ct.getComponents()) applyRec(ch);
    }

    // ================================================================
    //  СИНХРОНІЗАЦІЯ ПРАВОЇ ПАНЕЛІ
    // ================================================================
    void syncProps() {
        updatingUI = true;
        if (!canvas.selNodes.isEmpty()) {
            FlowNode p = canvas.selNodes.get(canvas.selNodes.size()-1);
            nodeTF.setText(p.text);
            nodeW.setText(String.valueOf(p.width));
            nodeH.setText(String.valueOf(p.height));
            fontBox.setSelectedItem(p.fontName);
            fontSzSpin.setValue(p.fontSize);
            borderWSpin.setValue(p.borderW);
            boldCB.setSelected(p.bold);
            italicCB.setSelected(p.italic);
            alignBox.setSelectedIndex(p.align);
            bgRow.setColor(p.bgColor);
            textRow.setColor(p.textColor);
            borderRow.setColor(p.borderColor);
            cardLayout.show(rightPanel, "NODE");
        } else if (canvas.selEdge != null) {
            FlowEdge e = canvas.selEdge;
            dashedCB.setSelected(e.dashed);
            arrowStartCB.setSelected(e.arrowStart);
            arrowEndCB.setSelected(e.arrowEnd);
            lineWSpin.setValue(e.strokeW);
            lineStyleBox.setSelectedIndex(e.lineStyle);
            arrowStyleBox.setSelectedIndex(e.arrowStyle);
            edgeColorRow.setColor(e.color);
            cardLayout.show(rightPanel, "EDGE");
        } else {
            cardLayout.show(rightPanel, "CANVAS");
        }
        updatingUI = false;
    }

    void updateNode() {
        if (updatingUI || canvas.selNodes.isEmpty()) return;
        canvas.saveState();
        for (FlowNode n : canvas.selNodes) {
            n.text    = nodeTF.getText();
            n.fontName= (String)fontBox.getSelectedItem();
            n.fontSize= (int)fontSzSpin.getValue();
            n.borderW = (int)borderWSpin.getValue();
            n.bold    = boldCB.isSelected();
            n.italic  = italicCB.isSelected();
            n.align   = alignBox.getSelectedIndex();
            try {
                int w=Math.max(30,Integer.parseInt(nodeW.getText()));
                int h=Math.max(30,Integer.parseInt(nodeH.getText()));
                n.width  = canvas.snapToGrid ? canvas.snap(w) : w;
                n.height = canvas.snapToGrid ? canvas.snap(h) : h;
            } catch (NumberFormatException ignored) {}
        }
        canvas.repaint();
    }

    void updateEdge() {
        if (updatingUI || canvas.selEdge==null) return;
        canvas.saveState();
        FlowEdge e = canvas.selEdge;
        e.dashed    = dashedCB.isSelected();
        e.arrowStart= arrowStartCB.isSelected();
        e.arrowEnd  = arrowEndCB.isSelected();
        e.strokeW   = (int)lineWSpin.getValue();
        e.lineStyle = lineStyleBox.getSelectedIndex();
        e.arrowStyle= arrowStyleBox.getSelectedIndex();
        canvas.repaint();
    }

    void pickNodeColor(String tgt) {
        if (canvas.selNodes.isEmpty()) return;
        FlowNode p = canvas.selNodes.get(canvas.selNodes.size()-1);
        Color init = tgt.equals("bg") ? p.bgColor : tgt.equals("text") ? p.textColor : p.borderColor;
        Color c = JColorChooser.showDialog(this, "Вибір кольору", init);
        if (c==null) return;
        canvas.saveState();
        for (FlowNode n : canvas.selNodes) {
            if (tgt.equals("bg"))     { n.bgColor=c;     bgRow.setColor(c); }
            else if (tgt.equals("text"))   { n.textColor=c;   textRow.setColor(c); }
            else if (tgt.equals("border")) { n.borderColor=c; borderRow.setColor(c); }
        }
        canvas.repaint();
    }

    // ================================================================
    //  ФАЙЛОВІ ОПЕРАЦІЇ — нативний діалог ОС
    // ================================================================
    private File nativeDlg(boolean open, String title, String ext) {
        FileDialog fd = new FileDialog(this, title, open ? FileDialog.LOAD : FileDialog.SAVE);
        if (ext!=null) fd.setFilenameFilter((d,n) -> n.toLowerCase().endsWith("."+ext));
        fd.setVisible(true);
        String dir=fd.getDirectory(), name=fd.getFile();
        if (dir==null||name==null) return null;
        File f = new File(dir, name);
        if (!open && ext!=null && !f.getName().toLowerCase().endsWith("."+ext))
            f = new File(f.getAbsolutePath()+"."+ext);
        return f;
    }

    void saveProject() {
        File f = nativeDlg(false,"Зберегти проєкт","fcp"); if(f==null) return;
        try (ObjectOutputStream o=new ObjectOutputStream(new FileOutputStream(f))) {
            o.writeObject(new ProjectState(canvas.nodes,canvas.edges,canvas.getBackground(),canvas.snapToGrid,canvas.gridSize,canvas.orthogonal));
            Toast.show(this,"Проєкт збережено",true);
        } catch (Exception ex) { Toast.show(this,"Помилка: "+ex.getMessage(),false); }
    }

    void loadProject() {
        File f = nativeDlg(true,"Відкрити проєкт","fcp"); if(f==null) return;
        try (ObjectInputStream in=new ObjectInputStream(new FileInputStream(f))) {
            ProjectState s=(ProjectState)in.readObject();
            canvas.nodes.clear(); canvas.edges.clear();
            canvas.nodes.addAll(s.nodes); canvas.edges.addAll(s.edges);
            canvas.setBackground(s.bgColor); canvas.snapToGrid=s.snapToGrid;
            canvas.gridSize=s.gridSize; canvas.orthogonal=s.orthogonal;
            canvas.selNodes.clear(); canvas.selEdge=null;
            canvas.scale=1; canvas.tx=0; canvas.ty=0;
            canvas.repaint(); syncProps();
            Toast.show(this,"Проєкт відкрито",true);
        } catch (Exception ex) { Toast.show(this,"Помилка: "+ex.getMessage(),false); }
    }

    void importTxt() {
        File f = nativeDlg(true,"Відкрити псевдокод","txt"); if(f==null) return;
        try { buildFromPseudo(Files.readAllLines(f.toPath())); Toast.show(this,"Блок-схему згенеровано",true); }
        catch (Exception ex) { Toast.show(this,"Помилка читання",false); }
    }

    void exportPNG() {
        if (canvas.nodes.isEmpty()) { Toast.show(this,"Полотно порожнє",false); return; }
        JCheckBox trCB = new JCheckBox("Прозорий фон");
        if (JOptionPane.showConfirmDialog(this,new Object[]{"",trCB},"Експорт PNG",JOptionPane.OK_CANCEL_OPTION)!=0) return;
        File f = nativeDlg(false,"Зберегти PNG","png"); if(f==null) return;

        int x0=Integer.MAX_VALUE,y0=Integer.MAX_VALUE,x1=Integer.MIN_VALUE,y1=Integer.MIN_VALUE;
        for (FlowNode n:canvas.nodes) { x0=Math.min(x0,n.x);y0=Math.min(y0,n.y);x1=Math.max(x1,n.x+n.width);y1=Math.max(y1,n.y+n.height); }
        for (FlowEdge e:canvas.edges) for(Point p:e.drawPts(canvas.orthogonal)){x0=Math.min(x0,p.x);y0=Math.min(y0,p.y);x1=Math.max(x1,p.x);y1=Math.max(y1,p.y);}
        int mg=28;
        BufferedImage img=new BufferedImage((x1-x0)+2*mg,(y1-y0)+2*mg,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2=img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (!trCB.isSelected()) { g2.setColor(canvas.getBackground()); g2.fillRect(0,0,img.getWidth(),img.getHeight()); }
        g2.translate(-x0+mg,-y0+mg);
        for (FlowEdge e:canvas.edges) e.draw(g2,canvas.orthogonal,false,T.dark);
        for (FlowNode n:canvas.nodes) n.draw(g2,false);
        g2.dispose();
        try { ImageIO.write(img,"PNG",f); Toast.show(this,"PNG збережено",true); }
        catch (Exception ex) { Toast.show(this,"Помилка",false); }
    }

    // ================================================================
    //  ПСЕВДОКОД → БЛОК-СХЕМА (з прив'язкою до сітки)
    // ================================================================
    void buildFromPseudo(List<String> lines) {
        canvas.saveState(); canvas.clear();
        int cx = canvas.snap(Math.max(500, canvas.getWidth()/2));
        int cy = canvas.snap(60);
        List<FlowNode> pending = new ArrayList<>();
        Stack<DecCtx>  ifStk  = new Stack<>();
        Stack<LoopCtx> loStk  = new Stack<>();

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()||line.startsWith("//")) continue;
            String cmd, txt="";
            int sp=line.indexOf(' ');
            if(sp!=-1){cmd=line.substring(0,sp).toUpperCase();txt=line.substring(sp+1).trim();}
            else        cmd=line.toUpperCase();

            // ELSE
            if (cmd.equals("ELSE")) {
                if (!ifStk.isEmpty()) {
                    DecCtx c=ifStk.peek(); c.endBranch.addAll(pending);
                    pending.clear(); pending.add(c.node); c.inElse=true; cy=canvas.snap(c.startY+100);
                } continue;
            }
            // ENDIF
            if (cmd.equals("ENDIF")) {
                if (!ifStk.isEmpty()) {
                    DecCtx c=ifStk.pop(); pending.addAll(c.endBranch);
                    int my=0; for(FlowNode p:pending) my=Math.max(my,p.y+p.height);
                    cy=canvas.snap(my+60);
                } continue;
            }
            // ENDLOOP
            if (cmd.equals("ENDLOOP")) {
                if (!loStk.isEmpty()) {
                    LoopCtx c=loStk.pop(); int lmy=0;
                    for(FlowNode p:pending) {
                        lmy=Math.max(lmy,p.y+p.height);
                        FlowEdge e=new FlowEdge(p,2,c.node,3,T.dark);
                        int bx=canvas.snap(cx-200), by=canvas.snap(lmy+40);
                        e.wps.add(new Point(bx,by)); e.wps.add(new Point(bx,c.node.y+c.node.height/2));
                        canvas.edges.add(e);
                    }
                    pending.clear(); pending.add(c.node); cy=canvas.snap(lmy+120);
                } continue;
            }

            ShapeType type = switch(cmd) {
                case "START","END" -> ShapeType.TERMINATOR;
                case "INPUT","OUTPUT" -> ShapeType.DATA;
                case "IF"   -> ShapeType.DECISION;
                case "LOOP" -> ShapeType.CYCLE;
                default     -> ShapeType.PROCESS;
            };

            int nx = !ifStk.isEmpty()
                    ? canvas.snap(ifStk.peek().inElse ? cx-200 : cx+200)
                    : cx;

            // Прив'язка до сітки при імпорті
            FlowNode nn = new FlowNode(canvas.snap(nx-60), canvas.snap(cy), type, T.dark);
            nn.text = txt.isEmpty() ? cmd : txt;
            canvas.nodes.add(nn);

            for (FlowNode p : pending) {
                FlowEdge e = new FlowEdge(p,2,nn,0,T.dark);
                if (p.type==ShapeType.DECISION) e.srcAnchor=(nn.x>p.x+10)?1:3;
                else if (p.type==ShapeType.CYCLE) {
                    boolean inBody=false; for(LoopCtx lc:loStk) if(lc.node==p) inBody=true;
                    if (!inBody) { e.srcAnchor=1; int ex2=canvas.snap(cx+200),ey2=canvas.snap(nn.y-60); e.wps.add(new Point(ex2,p.y+p.height/2)); e.wps.add(new Point(ex2,ey2)); }
                }
                canvas.edges.add(e);
            }
            pending.clear(); pending.add(nn);
            if(cmd.equals("IF"))   ifStk.push(new DecCtx(nn));
            if(cmd.equals("LOOP")) loStk.push(new LoopCtx(nn));
            cy = canvas.snap(cy+100);
        }
        canvas.repaint();
    }

    void showKeys() {
        JOptionPane.showMessageDialog(this,
                "ЛКМ                    — виділити фігуру або лінію\n"+
                        "Ctrl + ЛКМ             — додати / прибрати з виділення\n"+
                        "drag / Ctrl + drag     — виділити прямокутником\n"+
                        "Shift + ЛКМ            — виділити ланцюжок (BFS)\n"+
                        "ПКМ (тримати)          — переміщення полотна\n"+
                        "Колісце                — переміщення полотна ↕\n"+
                        "Shift + Колісце        — переміщення полотна ↔\n"+
                        "Ctrl + Колісце         — масштаб\n"+
                        "2× клік на лінії      — додати точку перегину\n"+
                        "2× клік на точці      — видалити точку перегину\n"+
                        "Delete / Backspace     — видалити\n"+
                        "Ctrl + C / V           — копіювати / вставити\n"+
                        "Ctrl + Z / Y           — Undo / Redo\n"+
                        "Ctrl + A               — виділити все\n"+
                        "Escape                 — зняти виділення",
                "Гарячі клавіші", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── UI helpers ─────────────────────────────────────────
    private JPanel propRoot() { JPanel p=new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setOpaque(false); return p; }
    private JLabel sectionTitle(String t) { JLabel l=new JLabel(t); l.setFont(T.F_BOLD); l.setForeground(T.text3()); l.setBorder(new EmptyBorder(10,10,6,10)); l.setAlignmentX(LEFT_ALIGNMENT); return l; }
    private JCheckBox styledCB(String l, boolean s) { JCheckBox c=new JCheckBox(l,s); c.setFont(T.F_UI); c.setForeground(T.text()); c.setOpaque(false); return c; }
    private JTextField styledTF() { JTextField f=new JTextField(); f.setFont(T.F_UI); f.setForeground(T.text()); f.setBackground(T.surf()); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(T.border(),1,true),new EmptyBorder(3,6,3,6))); return f; }
    private JComboBox<String> styledCombo(String[] items) { JComboBox<String> c=new JComboBox<>(items); c.setFont(T.F_UI); c.setBackground(T.surf()); c.setForeground(T.text()); return c; }
    private JSpinner mkSpin(int val, int min, int max, int step) { JSpinner s=new JSpinner(new SpinnerNumberModel(val,min,max,step)); s.setFont(T.F_UI); return s; }
    private DocumentListener dl(Runnable r) { return new DocumentListener(){ public void insertUpdate(DocumentEvent e){r.run();} public void removeUpdate(DocumentEvent e){r.run();} public void changedUpdate(DocumentEvent e){r.run();} }; }
}

// ================================================================
//  ДОПОМІЖНІ КЛАСИ
// ================================================================
class DecCtx { FlowNode node; List<FlowNode> endBranch=new ArrayList<>(); int startY; boolean inElse; DecCtx(FlowNode n){node=n;startY=n.y;} }
class LoopCtx { FlowNode node; LoopCtx(FlowNode n){node=n;} }
enum ShapeType implements Serializable { PROCESS,TERMINATOR,DECISION,CYCLE,DATA,CONNECTOR,COMMENT }

class ProjectState implements Serializable {
    private static final long serialVersionUID=3L;
    List<FlowNode> nodes; List<FlowEdge> edges; Color bgColor; boolean snapToGrid; int gridSize; boolean orthogonal;
    ProjectState(List<FlowNode> n,List<FlowEdge> e,Color bg,boolean s,int g,boolean o){ nodes=new ArrayList<>(n);edges=new ArrayList<>(e);bgColor=bg;snapToGrid=s;gridSize=g;orthogonal=o; }
}

// ================================================================
//  ПОЛОТНО
// ================================================================
class CanvasPanel extends JPanel {
    final List<FlowNode> nodes = new ArrayList<>();
    final List<FlowEdge> edges = new ArrayList<>();

    boolean snapToGrid=true, orthogonal=true, dstuMode=false;
    double  scale=1, tx=0, ty=0;
    boolean isDark;
    Consumer<Double> onZoom;

    List<FlowNode> selNodes  = new ArrayList<>();
    FlowEdge       selEdge;
    Point          selWp;

    // прямокутник виділення
    Point     rectStart; Rectangle rectCur; boolean isRect;
    // панорамування
    Point panStart;
    // перетягування
    Map<FlowNode,Point>       dragPos=new HashMap<>();
    Map<FlowEdge,List<Point>> dragWps=new HashMap<>();
    Point dragMouse; boolean isResizing, hasMoved;
    // з'єднання (малювання нової лінії)
    FlowNode connNode; int connAnchor=-1; Point connPt;
    // буфер
    List<FlowNode> clipN=new ArrayList<>(); List<FlowEdge> clipE=new ArrayList<>();

    Runnable onSel;
    LinkedList<Snap> undoStack=new LinkedList<>(), redoStack=new LinkedList<>();
    static final int HIST=40;

    CanvasPanel(Runnable onSel, boolean dark) {
        this.onSel=onSel; this.isDark=dark;
        setFocusable(true); setBackground(T.canvas());
        bindKeys(); setupMouse();

        // --- ДОДАНО: Механізм прийому даних при киданні (Drop) ---
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                // Дозволяємо кидати тільки рядки (наш ShapeType)
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    // Отримуємо тип фігури, яку кинули
                    String shapeName = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    ShapeType t = ShapeType.valueOf(shapeName);

                    // Отримуємо координати миші в момент відпускання кнопки
                    Point dropPoint = support.getDropLocation().getDropPoint();

                    // Переводимо екранні координати в координати полотна (з урахуванням масштабу і зсуву)
                    // Віднімаємо 60 і 30, щоб фігура з'явилася рівно по центру курсора миші
                    int cx = snap((int)((dropPoint.x - tx) / scale) - 60);
                    int cy = snap((int)((dropPoint.y - ty) / scale) - 30);

                    saveState();
                    FlowNode node = new FlowNode(cx, cy, t, T.dark);
                    nodes.add(node);
                    repaint();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
        // --------------------------------------------------------
    }

    // ── undo/redo ───────────────────────────────────────────
    void saveState() { if(undoStack.size()>=HIST) undoStack.removeFirst(); undoStack.add(new Snap(nodes,edges)); redoStack.clear(); }
    void undo() { if(!undoStack.isEmpty()){redoStack.add(new Snap(nodes,edges));restore(undoStack.removeLast());} }
    void redo() { if(!redoStack.isEmpty()){undoStack.add(new Snap(nodes,edges));restore(redoStack.removeLast());} }
    private void restore(Snap s) { Snap c=new Snap(s.nodes,s.edges); nodes.clear();edges.clear();nodes.addAll(c.nodes);edges.addAll(c.edges);selNodes.clear();selEdge=null;onSel.run();repaint(); }

    // ── ДСТУ ───────────────────────────────────────────────
    // Стрілка ставиться рівно в кінці кожного "зворотнього" відрізка
    // (тобто там де відрізок іде вліво або вгору), рівно одна на поворот.
    void rebuildDstu() {
        for (FlowEdge e : edges) {
            e.midArrows.clear();
            List<Point> pts = e.drawPts(orthogonal);
            for (int i = 0; i < pts.size() - 1; i++) {
                int dx = pts.get(i+1).x - pts.get(i).x;
                int dy = pts.get(i+1).y - pts.get(i).y;
                // "зворотній" напрямок: вліво (dx < 0) АБО вгору (dy < 0) при вертикальному відрізку
                boolean goLeft = dx < -4;
                boolean goUp   = dy < -4 && Math.abs(dx) < 4;
                if (goLeft || goUp) {
                    // Стрілка точно в кінці цього відрізка — точка pts[i+1]
                    Point tip = new Point(pts.get(i+1).x, pts.get(i+1).y);
                    double angle = Math.atan2(dy, dx);
                    e.midArrows.add(new MidArrow(tip, angle));
                }
            }
        }
    }
    void clearDstu() { for (FlowEdge e : edges) e.midArrows.clear(); }

    // ── гарячі клавіші ──────────────────────────────────────
    void bindKeys() {
        kb("DELETE",   "del",   e->deleteSelected());
        kb("BACK_SPACE","del2", e->deleteSelected());
        kb(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK,"copy", e->copySelected());
        kb(KeyEvent.VK_V,InputEvent.CTRL_DOWN_MASK,"paste",e->pasteItems());
        kb(KeyEvent.VK_Z,InputEvent.CTRL_DOWN_MASK,"undo", e->undo());
        kb(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK,"redo", e->redo());
        kb(KeyEvent.VK_A,InputEvent.CTRL_DOWN_MASK,"selAll",e->{ selNodes.clear();selNodes.addAll(nodes);selEdge=null;onSel.run();repaint(); });
        kb("ESCAPE","esc",e->{ selNodes.clear();selEdge=null;connNode=null;connAnchor=-1;connPt=null;onSel.run();repaint(); });
    }
    private void kb(String k,String n,ActionListener a){ getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(k),n); getActionMap().put(n,new AbstractAction(){public void actionPerformed(ActionEvent e){a.actionPerformed(e);}}); }
    private void kb(int k,int m,String n,ActionListener a){ getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(k,m),n); getActionMap().put(n,new AbstractAction(){public void actionPerformed(ActionEvent e){a.actionPerformed(e);}}); }

    // ── дії ────────────────────────────────────────────────
    void deleteSelected() {
        saveState();
        if (!selNodes.isEmpty()) { for(FlowNode n:selNodes) edges.removeIf(e->e.src==n||e.tgtNode==n); nodes.removeAll(selNodes); selNodes.clear(); }
        else if (selEdge!=null) { edges.removeIf(e->e.tgtEdge==selEdge); edges.remove(selEdge); selEdge=null; }
        onSel.run(); repaint();
    }
    void copySelected() {
        clipN.clear(); clipE.clear();
        Map<FlowNode,FlowNode> m=new HashMap<>();
        for(FlowNode n:selNodes){FlowNode c=n.clone2();clipN.add(c);m.put(n,c);}
        for(FlowEdge e:edges){ if(selNodes.contains(e.src)&&e.tgtNode!=null&&selNodes.contains(e.tgtNode)){ FlowEdge c=new FlowEdge(m.get(e.src),e.srcAnchor,m.get(e.tgtNode),e.tgtAnchor,e.color); for(Point p:e.wps)c.wps.add(new Point(p)); c.dashed=e.dashed;c.arrowStart=e.arrowStart;c.arrowEnd=e.arrowEnd;c.strokeW=e.strokeW;c.lineStyle=e.lineStyle;c.arrowStyle=e.arrowStyle; clipE.add(c); }}
    }
    void pasteItems() {
        if(clipN.isEmpty()) return; saveState(); selNodes.clear(); selEdge=null;
        Point mp; try{mp=MouseInfo.getPointerInfo().getLocation();SwingUtilities.convertPointFromScreen(mp,this);}catch(Exception ex){mp=new Point(getWidth()/2,getHeight()/2);}
        Point lp=lp(mp); int minX=clipN.stream().mapToInt(n->n.x).min().orElse(0), minY=clipN.stream().mapToInt(n->n.y).min().orElse(0);
        int dx=(snapToGrid?snap(lp.x):lp.x)-minX, dy=(snapToGrid?snap(lp.y):lp.y)-minY;
        Map<FlowNode,FlowNode> m=new HashMap<>();
        for(FlowNode cb:clipN){FlowNode p=cb.clone2();p.x+=dx;p.y+=dy;nodes.add(p);m.put(cb,p);selNodes.add(p);}
        for(FlowEdge ce:clipE){FlowEdge p=new FlowEdge(m.get(ce.src),ce.srcAnchor,m.get(ce.tgtNode),ce.tgtAnchor,ce.color);for(Point wp:ce.wps)p.wps.add(new Point(wp.x+dx,wp.y+dy));p.dashed=ce.dashed;p.arrowStart=ce.arrowStart;p.arrowEnd=ce.arrowEnd;p.strokeW=ce.strokeW;p.lineStyle=ce.lineStyle;p.arrowStyle=ce.arrowStyle;edges.add(p);}
        onSel.run(); repaint();
    }
    void clear() { nodes.clear();edges.clear();selNodes.clear();selEdge=null;scale=1;tx=0;ty=0;onSel.run();repaint(); }

    // ── миша ───────────────────────────────────────────────
    void setupMouse() {
        // Колісце: звичайне = вертикальний pan, Shift = горизонтальний pan, Ctrl = zoom
        addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                double f=(e.getWheelRotation()<0)?1.12:1/1.12, ns=scale*f;
                if(ns<0.08||ns>10) return;
                tx=e.getX()-(e.getX()-tx)*f; ty=e.getY()-(e.getY()-ty)*f;
                scale=ns; if(onZoom!=null)onZoom.accept(scale); repaint();
            } else if (e.isShiftDown()) { tx-=e.getWheelRotation()*40; repaint(); }
            else                         { ty-=e.getWheelRotation()*40; repaint(); }
        });

        MouseAdapter h = new MouseAdapter() {

            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                Point lp = lp(e.getPoint());

                // ── ПКМ або ЦКМ = панорамування ─────────────
                if (SwingUtilities.isRightMouseButton(e)||SwingUtilities.isMiddleMouseButton(e)) {
                    panStart=e.getPoint(); setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); return;
                }

                // ── 2× клік ──────────────────────────────────
                if (e.getClickCount()==2) {
                    if (selWp!=null&&selEdge!=null) { saveState();selEdge.wps.remove(selWp);selWp=null;repaint();return; }
                    if (selEdge!=null) { saveState();selEdge.insertWp(snapPt(lp));repaint();return; }
                }

                // ── ЛКМ + фігура ────────────────────────────
                FlowNode clicked = nodeAt(lp);
                if (clicked!=null) {
                    // Ctrl + ЛКМ: мультивибір
                    if (e.isControlDown()) {
                        if(selNodes.contains(clicked)) selNodes.remove(clicked); else selNodes.add(clicked);
                        selEdge=null; prepDrag(lp); onSel.run(); repaint(); return;
                    }
                    // Shift + ЛКМ: ланцюжок BFS
                    if (e.isShiftDown()&&!selNodes.isEmpty()) {
                        for(FlowNode n:bfs(selNodes.get(selNodes.size()-1),clicked)) if(!selNodes.contains(n)) selNodes.add(n);
                        selEdge=null; prepDrag(lp); onSel.run(); repaint(); return;
                    }
                    // Анкер — початок з'єднання
                    int ai=clicked.anchorAt(lp);
                    if (ai!=-1&&selNodes.size()<=1) { connNode=clicked;connAnchor=ai;connPt=lp;selEdge=null;onSel.run();repaint();return; }
                    // Звичайне виділення + можливий resize
                    if (!selNodes.contains(clicked)) { selNodes.clear();selNodes.add(clicked); }
                    isResizing=clicked.isResize(lp); selEdge=null; prepDrag(lp); onSel.run(); repaint(); return;
                }

                // ── ЛКМ у порожній зоні ────────────────────
                // Ctrl або просто drag — прямокутник виділення
                selNodes.clear();
                FlowEdge fe=edgeAt(lp);
                if (fe!=null) { selEdge=fe; selWp=fe.wpAt(lp); }
                else { selEdge=null;selWp=null; rectStart=lp;rectCur=null;isRect=true; }
                onSel.run(); repaint();
            }

            @Override public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
                if (panStart!=null) { panStart=null; return; }
                Point lp=lp(e.getPoint());

                // Завершення прямокутника
                if (isRect&&rectStart!=null) {
                    if (rectCur!=null) {
                        if (!e.isControlDown()) selNodes.clear();
                        for(FlowNode n:nodes) if(rectCur.intersects(n.x,n.y,n.width,n.height)&&!selNodes.contains(n)) selNodes.add(n);
                    }
                    isRect=false;rectStart=null;rectCur=null;onSel.run();repaint();return;
                }

                // Завершення з'єднання
                if (connNode!=null) {
                    saveState(); boolean ok=false;
                    for(FlowNode n:nodes){ if(n==connNode)continue; int ai=n.anchorAt(lp); if(ai!=-1){edges.add(new FlowEdge(connNode,connAnchor,n,ai,isDark));ok=true;break;} }
                    if(!ok){ FlowEdge te=edgeAt(lp); if(te!=null) edges.add(new FlowEdge(connNode,connAnchor,te,isDark)); }
                    connNode=null;connAnchor=-1;connPt=null; repaint();
                }
                hasMoved=false;isResizing=false;
            }

            @Override public void mouseDragged(MouseEvent e) {
                Point lp=lp(e.getPoint());

                // Панорамування
                if (panStart!=null) { tx+=e.getX()-panStart.x;ty+=e.getY()-panStart.y;panStart=e.getPoint();repaint();return; }

                // Прямокутник виділення
                if (isRect&&rectStart!=null) {
                    int x=Math.min(rectStart.x,lp.x),y=Math.min(rectStart.y,lp.y),w=Math.abs(lp.x-rectStart.x),h=Math.abs(lp.y-rectStart.y);
                    rectCur=new Rectangle(x,y,w,h); repaint(); return;
                }

                // З'єднання — preview
                if (connNode!=null) {
                    boolean sn=false;
                    for(FlowNode n:nodes){if(n==connNode)continue;int ai=n.anchorAt(lp);if(ai!=-1){connPt=n.anchors()[ai];sn=true;break;}}
                    if(!sn)connPt=snapPt(lp); repaint(); return;
                }

                // Переміщення / resize фігур
                if (!selNodes.isEmpty()&&dragMouse!=null) {
                    if(!hasMoved){saveState();hasMoved=true;}
                    if(isResizing&&selNodes.size()==1) {
                        FlowNode n=selNodes.get(0);
                        int nw=Math.max(40,lp.x-dragPos.get(n).x), nh=Math.max(40,lp.y-dragPos.get(n).y);
                        n.width=snapToGrid?snap(nw):nw; n.height=snapToGrid?snap(nh):nh;
                    } else {
                        int dx=lp.x-dragMouse.x, dy=lp.y-dragMouse.y;
                        for(FlowNode n:selNodes){int nx=dragPos.get(n).x+dx,ny=dragPos.get(n).y+dy;n.x=snapToGrid?snap(nx):nx;n.y=snapToGrid?snap(ny):ny;}
                        for(Map.Entry<FlowEdge,List<Point>> en:dragWps.entrySet()){List<Point> sw=en.getValue();for(int i=0;i<en.getKey().wps.size();i++){int nx=sw.get(i).x+dx,ny=sw.get(i).y+dy;en.getKey().wps.get(i).x=snapToGrid?snap(nx):nx;en.getKey().wps.get(i).y=snapToGrid?snap(ny):ny;}}
                    }
                    onSel.run(); repaint();
                }

                // Переміщення точки перегину
                if (selWp!=null) { if(!hasMoved){saveState();hasMoved=true;} selWp.x=snapToGrid?snap(lp.x):lp.x; selWp.y=snapToGrid?snap(lp.y):lp.y; repaint(); }
            }

            @Override public void mouseMoved(MouseEvent e) {
                Point lp=lp(e.getPoint());
                for(FlowNode n:nodes){
                    if(n.anchorAt(lp)!=-1){setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));return;}
                    if(n.isResize(lp))    {setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));return;}
                }
                setCursor(Cursor.getDefaultCursor());
            }
        };
        addMouseListener(h); addMouseMotionListener(h);
    }

    private void prepDrag(Point lp) {
        dragMouse=lp;dragPos.clear();dragWps.clear();hasMoved=false;
        for(FlowNode n:selNodes)dragPos.put(n,new Point(n.x,n.y));
        for(FlowEdge e:edges){if(selNodes.contains(e.src)&&e.tgtNode!=null&&selNodes.contains(e.tgtNode)){List<Point> w=new ArrayList<>();for(Point p:e.wps)w.add(new Point(p));dragWps.put(e,w);}}
    }

    // BFS для ланцюжку
    private List<FlowNode> bfs(FlowNode s, FlowNode t) {
        Map<FlowNode,FlowNode> par=new HashMap<>(); Queue<FlowNode> q=new LinkedList<>();
        q.add(s);par.put(s,null);boolean f=false;
        while(!q.isEmpty()){FlowNode c=q.poll();if(c==t){f=true;break;}for(FlowEdge e:edges){if(e.tgtNode==null)continue;if(e.src==c&&!par.containsKey(e.tgtNode)){par.put(e.tgtNode,c);q.add(e.tgtNode);}if(e.tgtNode==c&&!par.containsKey(e.src)){par.put(e.src,c);q.add(e.src);}}}
        List<FlowNode> r=new ArrayList<>();if(f){FlowNode c=t;while(c!=null){r.add(c);c=par.get(c);}}else r.add(t);return r;
    }

    // ── утиліти ────────────────────────────────────────────
    Point lp(Point p) { return new Point((int)((p.x-tx)/scale),(int)((p.y-ty)/scale)); }
    int   snap(int v) { return Math.round(v/(float)gridSize)*gridSize; }
    int gridSize = 20;
    Point snapPt(Point p) { return snapToGrid?new Point(snap(p.x),snap(p.y)):new Point(p.x,p.y); }
    private FlowNode nodeAt(Point p) { for(int i=nodes.size()-1;i>=0;i--) if(nodes.get(i).contains(p)) return nodes.get(i); return null; }
    private FlowEdge edgeAt(Point p) { for(FlowEdge e:edges) if(e.hits(p,orthogonal)) return e; return null; }

    // ── МАЛЮВАННЯ ──────────────────────────────────────────
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        // Сітка-точки
        if (snapToGrid) {
            g2.setColor(isDark ? new Color(48,52,62) : new Color(198,202,215));
            int gx=(int)(-tx/scale/gridSize)*gridSize, gy=(int)(-ty/scale/gridSize)*gridSize;
            int ex=(int)((getWidth()-tx)/scale), ey=(int)((getHeight()-ty)/scale);
            for(int i=gx;i<=ex;i+=gridSize) for(int j=gy;j<=ey;j+=gridSize) g2.fillRect((int)(i*scale+tx),(int)(j*scale+ty),1,1);
        }

        AffineTransform saved=g2.getTransform();
        g2.translate(tx,ty); g2.scale(scale,scale);

        // Лінії
        for (FlowEdge e:edges) e.draw(g2,orthogonal,e==selEdge,isDark);

        // Preview нового з'єднання
        if (connNode!=null&&connPt!=null) {
            g2.setColor(new Color(T.ACCENT.getRed(),T.ACCENT.getGreen(),T.ACCENT.getBlue(),170));
            g2.setStroke(new BasicStroke(2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{6},0));
            Point s=connNode.anchors()[connAnchor];
            if (orthogonal) { int off=20; Point st=new Point(s.x,s.y); switch(connAnchor){case 0->st.y-=off;case 1->st.x+=off;case 2->st.y+=off;case 3->st.x-=off;} g2.drawLine(s.x,s.y,st.x,st.y);g2.drawLine(st.x,st.y,connPt.x,st.y);g2.drawLine(connPt.x,st.y,connPt.x,connPt.y); }
            else g2.drawLine(s.x,s.y,connPt.x,connPt.y);
            // підсвічування анкерів
            g2.setColor(new Color(80,210,80,140));
            for(FlowNode n:nodes){if(n==connNode)continue;for(Point a:n.anchors())g2.fillOval(a.x-9,a.y-9,18,18);}
        }

        // Фігури
        for (FlowNode n:nodes) n.draw(g2,selNodes.contains(n));

        g2.setTransform(saved);

        // Прямокутник виділення (в екранних координатах)
        if (isRect&&rectCur!=null) {
            Rectangle sr=toScreen(rectCur);
            g2.setColor(new Color(79,127,255,30)); g2.fillRect(sr.x,sr.y,sr.width,sr.height);
            g2.setColor(new Color(79,127,255,200)); g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10,new float[]{5},0));
            g2.drawRect(sr.x,sr.y,sr.width,sr.height);
        }
    }

    private Rectangle toScreen(Rectangle r) {
        int x1=(int)(r.x*scale+tx),y1=(int)(r.y*scale+ty),x2=(int)((r.x+r.width)*scale+tx),y2=(int)((r.y+r.height)*scale+ty);
        return new Rectangle(Math.min(x1,x2),Math.min(y1,y2),Math.abs(x2-x1),Math.abs(y2-y1));
    }
}

// ================================================================
//  SNAPSHOT
// ================================================================
class Snap {
    List<FlowNode> nodes; List<FlowEdge> edges;
    Snap(List<FlowNode> ns, List<FlowEdge> es) {
        nodes=new ArrayList<>(); Map<FlowNode,FlowNode> m=new HashMap<>();
        for(FlowNode n:ns){FlowNode c=n.clone2();nodes.add(c);m.put(n,c);}
        edges=new ArrayList<>(); Map<FlowEdge,FlowEdge> em=new HashMap<>();
        for(FlowEdge e:es){FlowEdge c=new FlowEdge();c.src=m.get(e.src);c.srcAnchor=e.srcAnchor;if(e.tgtNode!=null){c.tgtNode=m.get(e.tgtNode);c.tgtAnchor=e.tgtAnchor;}c.wps=new ArrayList<>();for(Point p:e.wps)c.wps.add(new Point(p));c.dashed=e.dashed;c.arrowStart=e.arrowStart;c.arrowEnd=e.arrowEnd;c.color=e.color;c.strokeW=e.strokeW;c.lineStyle=e.lineStyle;c.arrowStyle=e.arrowStyle;edges.add(c);em.put(e,c);}
        for(int i=0;i<es.size();i++) if(es.get(i).tgtEdge!=null) edges.get(i).tgtEdge=em.get(es.get(i).tgtEdge);
    }
}

// ================================================================
//  ФІГУРА
// ================================================================
class FlowNode implements Serializable {
    private static final long serialVersionUID=4L;
    int x,y,width=120,height=60; String text; ShapeType type;
    Color bgColor,textColor,borderColor;
    String fontName="Segoe UI"; int fontSize=14,borderW=2,align=0;
    boolean bold,italic;

    FlowNode(int x,int y,ShapeType t,boolean dark) {
        this.x=x;this.y=y;this.type=t;
        text=switch(t){case COMMENT->"Коментар";case CYCLE->"Цикл";case TERMINATOR->"Початок";default->"Текст";};
        if(t==ShapeType.CONNECTOR){width=40;height=40;}
        bgColor     = DefaultStyles.nodeBg;
        textColor   = DefaultStyles.nodeText;
        borderColor = DefaultStyles.nodeBorder;
        borderW     = DefaultStyles.nodeBorderW;
        fontName    = DefaultStyles.nodeFontName;
        fontSize    = DefaultStyles.nodeFontSize;
    }

    FlowNode clone2() {
        FlowNode c=new FlowNode(x,y,type,false);
        c.width=width;c.height=height;c.text=text;c.bgColor=bgColor;c.textColor=textColor;c.borderColor=borderColor;
        c.fontName=fontName;c.fontSize=fontSize;c.borderW=borderW;c.bold=bold;c.italic=italic;c.align=align;return c;
    }

    boolean contains(Point p) { return p.x>=x&&p.x<=x+width&&p.y>=y&&p.y<=y+height; }
    boolean isResize(Point p) { return p.x>=x+width-12&&p.x<=x+width&&p.y>=y+height-12&&p.y<=y+height; }
    int anchorAt(Point p) { Point[] a=anchors(); for(int i=0;i<a.length;i++) if(p.distance(a[i])<15) return i; return -1; } //РОЗМІР АНКЕРА ANKER SIZE
    Point[] anchors() { return new Point[]{new Point(x+width/2,y),new Point(x+width,y+height/2),new Point(x+width/2,y+height),new Point(x,y+height/2)}; }

    void draw(Graphics2D g, boolean sel) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        if (type==ShapeType.COMMENT) {
            g.setColor(sel?T.ACCENT:borderColor);
            g.setStroke(new BasicStroke(borderW,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{6},0));
            g.drawLine(x,y,x,y+height); g.setStroke(new BasicStroke(borderW));
            g.drawLine(x,y,x+14,y); g.drawLine(x,y+height,x+14,y+height);
        } else {
            Shape sh=shape();
            // Тінь
            AffineTransform at=g.getTransform(); g.setColor(new Color(0,0,0,20)); g.translate(2,3); g.fill(sh); g.setTransform(at);
            // Фон
            g.setColor(bgColor); g.fill(sh);
            // Межа
            g.setStroke(new BasicStroke(sel?borderW+1.5f:borderW,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g.setColor(sel?T.ACCENT:borderColor); g.draw(sh);
            // Анкери при виділенні
            if (sel) {
                g.setColor(T.ACCENT);
                for(Point p:anchors()) g.fillOval(p.x-5,p.y-5,10,10);
                g.fillRect(x+width-8,y+height-8,8,8);
            }
        }
        int st=Font.PLAIN; if(bold)st|=Font.BOLD; if(italic)st|=Font.ITALIC;
        g.setFont(new Font(fontName,st,fontSize)); g.setColor(textColor);
        drawText(g,text,x+8,y+4,width-16,height-8);
    }

    private void drawText(Graphics2D g,String txt,int rx,int ry,int rw,int rh) {
        if(txt==null||txt.isEmpty()||rw<=0) return;
        FontMetrics fm=g.getFontMetrics(); List<String> lines=new ArrayList<>();
        for(String par:txt.split("\n")){
            String[] words=par.split(" "); StringBuilder cur=new StringBuilder();
            for(String wd:words){
                while(fm.stringWidth(wd)>rw&&wd.length()>1){int fi=1;while(fi<wd.length()&&fm.stringWidth(wd.substring(0,fi+1))<=rw)fi++;if(cur.length()>0){lines.add(cur.toString().trim());cur=new StringBuilder();}lines.add(wd.substring(0,fi));wd=wd.substring(fi);}
                if(wd.isEmpty())continue; String test=cur.length()==0?wd:cur+" "+wd;
                if(fm.stringWidth(test)<=rw)cur=new StringBuilder(test);else{if(cur.length()>0)lines.add(cur.toString().trim());cur=new StringBuilder(wd);}
            }
            if(cur.length()>0)lines.add(cur.toString().trim());
        }
        int lh=fm.getHeight(),totalH=lines.size()*lh,sy=ry+(rh-totalH)/2+fm.getAscent();
        for(String line:lines){int lw=fm.stringWidth(line),sx=switch(align){case 1->rx;case 2->rx+rw-lw;default->rx+(rw-lw)/2;};g.drawString(line,sx,sy);sy+=lh;}
    }

    private Shape shape() {
        return switch(type){
            case TERMINATOR->{yield new RoundRectangle2D.Double(x,y,width,height,40,40);}
            case DECISION->{Polygon p=new Polygon();p.addPoint(x+width/2,y);p.addPoint(x+width,y+height/2);p.addPoint(x+width/2,y+height);p.addPoint(x,y+height/2);yield p;}
            case DATA->{Polygon p=new Polygon();p.addPoint(x+15,y);p.addPoint(x+width,y);p.addPoint(x+width-15,y+height);p.addPoint(x,y+height);yield p;}
            case CYCLE->{Polygon p=new Polygon();int o=20;p.addPoint(x+o,y);p.addPoint(x+width-o,y);p.addPoint(x+width,y+height/2);p.addPoint(x+width-o,y+height);p.addPoint(x+o,y+height);p.addPoint(x,y+height/2);yield p;}
            case CONNECTOR->{yield new Ellipse2D.Double(x,y,width,height);}
            default->{yield new Rectangle2D.Double(x,y,width,height);}
        };
    }
}

// ================================================================
//  ДСТУ ПРОМІЖНА СТРІЛКА
// ================================================================
class MidArrow implements Serializable {
    Point pos; double angle;
    MidArrow(Point p, double a) { pos=p; angle=a; }
}

// ================================================================
//  ЛІНІЯ
// ================================================================
class FlowEdge implements Serializable {
    private static final long serialVersionUID=4L;
    FlowNode src; int srcAnchor;
    FlowNode tgtNode; int tgtAnchor=-1;
    FlowEdge tgtEdge;
    List<Point> wps=new ArrayList<>();

    boolean dashed, arrowStart, arrowEnd=true;
    Color   color;
    int     strokeW=2, lineStyle=0, arrowStyle=0;

    List<MidArrow> midArrows=new ArrayList<>();

    FlowEdge(){}
    FlowEdge(FlowNode s,int sa,FlowNode t,int ta,Color c){src=s;srcAnchor=sa;tgtNode=t;tgtAnchor=ta;color=c;}
    FlowEdge(FlowNode s,int sa,FlowNode t,int ta,boolean dark){
        src=s;srcAnchor=sa;tgtNode=t;tgtAnchor=ta;
        color=DefaultStyles.edgeColorForTheme(dark);
        strokeW=DefaultStyles.edgeStrokeW; lineStyle=DefaultStyles.edgeLineStyle;
        arrowStyle=DefaultStyles.edgeArrowStyle; dashed=DefaultStyles.edgeDashed;
        arrowStart=DefaultStyles.edgeArrowStart; arrowEnd=DefaultStyles.edgeArrowEnd;
    }
    FlowEdge(FlowNode s,int sa,FlowEdge te,boolean dark){
        src=s;srcAnchor=sa;tgtEdge=te;
        color=DefaultStyles.edgeColorForTheme(dark);
        strokeW=DefaultStyles.edgeStrokeW; lineStyle=DefaultStyles.edgeLineStyle;
        arrowStyle=DefaultStyles.edgeArrowStyle; dashed=DefaultStyles.edgeDashed;
        arrowStart=DefaultStyles.edgeArrowStart; arrowEnd=DefaultStyles.edgeArrowEnd;
    }

    void insertWp(Point p) {
        List<Point> pts=new ArrayList<>(); pts.add(src.anchors()[srcAnchor]); pts.addAll(wps);
        pts.add(tgtNode!=null?tgtNode.anchors()[tgtAnchor]:proj(p,tgtEdge,false));
        int bi=0; double md=Double.MAX_VALUE;
        for(int i=0;i<pts.size()-1;i++){double d=Line2D.ptSegDist(pts.get(i).x,pts.get(i).y,pts.get(i+1).x,pts.get(i+1).y,p.x,p.y);if(d<md){md=d;bi=i;}}
        wps.add(bi,p);
    }

    boolean hits(Point p, boolean o) {
        for(Point wp:wps) if(wp.distance(p)<10) return true;
        List<Point> pts=drawPts(o);
        for(int i=0;i<pts.size()-1;i++) if(Line2D.ptSegDist(pts.get(i).x,pts.get(i).y,pts.get(i+1).x,pts.get(i+1).y,p.x,p.y)<8) return true;
        return false;
    }

    Point wpAt(Point p) { for(Point wp:wps) if(wp.distance(p)<10) return wp; return null; }

    private Point proj(Point from, FlowEdge e, boolean o) {
        List<Point> pts=e.drawPts(o); Point cl=pts.get(0); double md=Double.MAX_VALUE;
        for(int i=0;i<pts.size()-1;i++){Point a=pts.get(i),b=pts.get(i+1);double l2=a.distanceSq(b);if(l2==0)continue;double t2=Math.max(0,Math.min(1,((from.x-a.x)*(b.x-a.x)+(from.y-a.y)*(b.y-a.y))/l2));Point pr=new Point((int)(a.x+t2*(b.x-a.x)),(int)(a.y+t2*(b.y-a.y)));if(from.distance(pr)<md){md=from.distance(pr);cl=pr;}}
        return cl;
    }

    private Point stub(Point p,int ai,int off) { return switch(ai){case 0->new Point(p.x,p.y-off);case 1->new Point(p.x+off,p.y);case 2->new Point(p.x,p.y+off);case 3->new Point(p.x-off,p.y);default->new Point(p.x,p.y);}; }

    List<Point> drawPts(boolean o) {
        List<Point> pts=new ArrayList<>(); Point s=src.anchors()[srcAnchor]; pts.add(s); Point cur=s;
        if(o){Point st=stub(s,srcAnchor,20);pts.add(st);cur=st;}
        for(Point wp:wps){if(o){pts.add(new Point(wp.x,cur.y));pts.add(wp);}else pts.add(wp);cur=wp;}
        if(tgtNode!=null){Point tp=tgtNode.anchors()[tgtAnchor];if(o){Point ts=stub(tp,tgtAnchor,20);pts.add(new Point(ts.x,cur.y));pts.add(ts);pts.add(tp);}else pts.add(tp);}
        else if(tgtEdge!=null){Point tp=proj(cur,tgtEdge,o);if(o){pts.add(new Point(tp.x,cur.y));pts.add(tp);}else pts.add(tp);}
        return pts;
    }

    private Stroke mkStroke(boolean sel) {
        float w=sel?strokeW+1:strokeW;
        return switch(lineStyle){
            case 1->new BasicStroke(w,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10,new float[]{12,4},0);
            case 2->new BasicStroke(w,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,10,new float[]{2,6},0);
            default->dashed?new BasicStroke(w,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10,new float[]{10,5},0):new BasicStroke(w,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
        };
    }

    void draw(Graphics2D g, boolean o, boolean sel, boolean dark) {
        Color ac = color;
        g.setColor(sel ? T.ACCENT : ac); g.setStroke(mkStroke(sel));
        List<Point> pts=drawPts(o); Path2D path=new Path2D.Double(); path.moveTo(pts.get(0).x,pts.get(0).y);
        for(int i=1;i<pts.size();i++) path.lineTo(pts.get(i).x,pts.get(i).y);
        g.draw(path);
        if(sel) for(Point wp:wps) g.fillOval(wp.x-5,wp.y-5,10,10);

        Color arc=sel?T.ACCENT:ac;
        if(arrowEnd  &&pts.size()>=2) arrowAt(g,tail(pts,pts.size()-1,-1),pts.get(pts.size()-1),tgtAnchor,true,arc);
        if(arrowStart&&pts.size()>=2) arrowAt(g,tail(pts,0,1),pts.get(0),srcAnchor,false,arc);

        // ДСТУ проміжні стрілки — одна на кінці кожного зворотнього відрізка
        for (MidArrow ma : midArrows) {
            g.setColor(arc);
            g.setStroke(new BasicStroke(sel ? strokeW+1 : strokeW,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            head(g, ma.pos, ma.angle);
        }
    }

    private Point tail(List<Point> pts,int tip,int dir) { Point t=pts.get(tip);int i=tip+dir;while(i>=0&&i<pts.size()){if(!pts.get(i).equals(t))return pts.get(i);i+=dir;}return t; }

    private void arrowAt(Graphics2D g, Point tail, Point tip, int ai, boolean isTgt, Color c) {
        double angle;
        if(ai!=-1) angle=isTgt?switch(ai){case 0->Math.PI/2;case 1->Math.PI;case 2->-Math.PI/2;default->0.0;}:switch(ai){case 0->-Math.PI/2;case 1->0.0;case 2->Math.PI/2;default->Math.PI;};
        else angle=Math.atan2(tip.y-tail.y,tip.x-tail.x);
        g.setColor(c); g.setStroke(new BasicStroke(1)); head(g,tip,angle);
    }

    private void head(Graphics2D g, Point tip, double angle) {
        int sz=12+strokeW;
        switch(arrowStyle){
            case 1->{g.setStroke(new BasicStroke(strokeW,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));Path2D p=new Path2D.Double();p.moveTo(tip.x-sz*Math.cos(angle-Math.PI/7),tip.y-sz*Math.sin(angle-Math.PI/7));p.lineTo(tip.x,tip.y);p.lineTo(tip.x-sz*Math.cos(angle+Math.PI/7),tip.y-sz*Math.sin(angle+Math.PI/7));g.draw(p);}
            case 2->g.fillOval(tip.x-sz/3,tip.y-sz/3,sz*2/3,sz*2/3);
            default->{Path2D p=new Path2D.Double();p.moveTo(tip.x,tip.y);p.lineTo(tip.x-sz*Math.cos(angle-Math.PI/8),tip.y-sz*Math.sin(angle-Math.PI/8));p.lineTo(tip.x-sz*Math.cos(angle+Math.PI/8),tip.y-sz*Math.sin(angle+Math.PI/8));p.closePath();g.fill(p);}
        }
    }
}