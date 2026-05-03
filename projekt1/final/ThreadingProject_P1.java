import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadingProject_P1 {
    public static final int elementy = 100;
    public static final int pula = 4; //rozmiar puli wątków dla ExecutorService
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Animacja().setVisible(true));
    }
}

class Animacja extends JFrame { 
    private PanelAnimacji panel;
    private Executor basicExecutor;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutor;
    private List<Thread> klasyczneThreads;
    private volatile boolean running = false;

    public Animacja() { //konstruktor GUI z przypisaniem metod do przycisków
        setTitle("Animacja obciazajaca CPU");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new PanelAnimacji();
        add(panel, BorderLayout.CENTER);
        
        JPanel controls = new JPanel();
        JButton btnThread = new JButton("Thread + Runnable");
        JButton btnExecutor = new JButton("Executor");
        JButton btnExecService = new JButton("ExecutorService");
        JButton btnScheduled = new JButton("ScheduledExecutor");
        JButton btnStop = new JButton("Stop");
        
        btnThread.addActionListener(e -> startThreads());
        btnExecutor.addActionListener(e -> startExecutor());
        btnExecService.addActionListener(e -> startExecutorService());
        btnScheduled.addActionListener(e -> startScheduled());
        btnStop.addActionListener(e -> stopAll());
        
        controls.add(btnThread);
        controls.add(btnExecutor);
        controls.add(btnExecService);
        controls.add(btnScheduled);
        controls.add(btnStop);
        add(controls, BorderLayout.SOUTH);
    }
    
    private void startThreads() { // thread + runnable(w KrokAnimacji): tworzy nowy obiekt Thread dla każdego elementu
        stopAll();
        running = true;
        panel.initElements(ThreadingProject_P1.elementy);
        klasyczneThreads = new ArrayList<>();
        
        for (RuchomyElement element : panel.getElementy()) {
            Thread t = new Thread(new KrokAnimacji(element, panel, this));
            klasyczneThreads.add(t);
            t.start();
        }
    }
    
    private void startExecutor() { // metoda z interfejsem Executor - manualnie startuje nowe wątki
        stopAll();
        running = true;
        panel.initElements(ThreadingProject_P1.elementy);
        
        basicExecutor = command -> new Thread(command).start();
        
        for (RuchomyElement el : panel.getElementy()) {
            basicExecutor.execute(new KrokAnimacji(el, panel, this));
        }
    }
    
    private void startExecutorService() { // metoda wysokopoziomowa z ExecutorService delegująca ruch elementów do stałej puli wątków za pomocą Exectuor Service
        stopAll();
        running = true;
        panel.initElements(ThreadingProject_P1.elementy);
        
        executorService = Executors.newFixedThreadPool(ThreadingProject_P1.pula);
        
        for (RuchomyElement el : panel.getElementy()) {
            executorService.submit(new KrokAnimacji(el, panel, this));
        }
    }
    
    private void startScheduled() { // metoda używająca ScheduledExectuorService do planowania cyklicznych ruchów z opóźnieniem
        stopAll();
        running = true;
        panel.initElements(ThreadingProject_P1.elementy);
        
        scheduledExecutor = Executors.newScheduledThreadPool(ThreadingProject_P1.pula);
        
        for (RuchomyElement el : panel.getElementy()) {
            scheduledExecutor.scheduleAtFixedRate(() -> {
                if (running && el.x < panel.getWidth() - 20) {
                    obciazenie();
                    el.x += 2;
                    panel.repaint();
                }
            }, 0, 10, TimeUnit.MILLISECONDS);
        }
    }

    private void stopAll() { // metoda przerywająca działanie, czyszcząca listy i zatrzumująca pule wątków
        running = false;
        if (klasyczneThreads != null) {
            klasyczneThreads.forEach(Thread::interrupt);
            klasyczneThreads.clear();
        }
        if (executorService != null) executorService.shutdownNow();
        if (scheduledExecutor != null) scheduledExecutor.shutdownNow();
    }
    
    public boolean isRunning() { return running; }
    
    public static void obciazenie() { // sztuczne obciążenie procesora funkcjami sinus*tangens
        double trygonometrysta = 0;
        for (int i = 0; i < 50000; i++) {
            trygonometrysta += Math.sin(i) * Math.tan(i);
        }
    }
}

class KrokAnimacji implements Runnable {
    private RuchomyElement klocek;
    private PanelAnimacji panel;
    private Animacja window;
    
    public KrokAnimacji(RuchomyElement el, PanelAnimacji panel, Animacja window) {
        this.klocek = el;
        this.panel = panel;
        this.window = window;
    }
    
    @Override
    public void run() {
        while (window.isRunning() && klocek.x < panel.getWidth() - 20 && !Thread.currentThread().isInterrupted()) {
            Animacja.obciazenie();
            klocek.x += 2;
            SwingUtilities.invokeLater(panel::repaint);
        }
    }
}

class PanelAnimacji extends JPanel {
    private List<RuchomyElement> elementy = new CopyOnWriteArrayList<>();
    
    public void initElements(int count) {
        elementy.clear();
        for (int i = 0; i < count; i++) {
            elementy.add(new RuchomyElement(10, 10 + (i * 5)));
        }
        repaint();
    }
    
    public List<RuchomyElement> getElementy() { return elementy; }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (RuchomyElement el : elementy) {
            g.fillRect(el.x, el.y, 10, 4);
        }
    }
}

class RuchomyElement {
    int x, y;
    public RuchomyElement(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
