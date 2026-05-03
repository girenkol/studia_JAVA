import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadingProject_P1 {
    // # config
    public static final int ELEMENTS = 100;
    public static final int POOL_SIZE = 4;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnimationWindow().setVisible(true));
    }
}

class AnimationWindow extends JFrame {
    private AnimationPanel panel;
    private Executor basicExecutor;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutor;
    private List<Thread> classicThreads;
    private volatile boolean running = false;

    public AnimationWindow() {
        setTitle("Animacja obciazajaca CPU");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new AnimationPanel();
        add(panel, BorderLayout.CENTER);
        
        JPanel controls = new JPanel();
        JButton btnThread = new JButton("Thread + Runnable");
        JButton btnExecutor = new JButton("Executor");
        JButton btnExecService = new JButton("ExecutorService");
        JButton btnScheduled = new JButton("ScheduledExecutor");
        JButton btnStop = new JButton("Stop");
        
        btnThread.addActionListener(e -> startClassic());
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
    
    private void startClassic() {
        stopAll();
        running = true;
        panel.initElements(ThreadingProject.ELEMENTS);
        classicThreads = new ArrayList<>();
        
        for (MovingElement el : panel.getElements()) {
            // # wymog 2: klasyczna technika
            Thread t = new Thread(new WorkerTask(el, panel, this));
            classicThreads.add(t);
            t.start();
        }
    }
    
    private void startExecutor() {
        stopAll();
        running = true;
        panel.initElements(ThreadingProject.ELEMENTS);
        
        // # wymog glowny: podstawowy Executor
        basicExecutor = command -> new Thread(command).start();
        
        for (MovingElement el : panel.getElements()) {
            basicExecutor.execute(new WorkerTask(el, panel, this));
        }
    }
    
    private void startExecutorService() {
        stopAll();
        running = true;
        panel.initElements(ThreadingProject.ELEMENTS);
        
        // # wymog 3: pula watkow
        executorService = Executors.newFixedThreadPool(ThreadingProject.POOL_SIZE);
        
        for (MovingElement el : panel.getElements()) {
            executorService.submit(new WorkerTask(el, panel, this));
        }
    }
    
    private void startScheduled() {
        stopAll();
        running = true;
        panel.initElements(ThreadingProject.ELEMENTS);
        
        // # wymog glowny: ScheduledExecutorService
        scheduledExecutor = Executors.newScheduledThreadPool(ThreadingProject.POOL_SIZE);
        
        for (MovingElement el : panel.getElements()) {
            scheduledExecutor.scheduleAtFixedRate(() -> {
                if (running && el.x < panel.getWidth() - 20) {
                    heavyCalculations();
                    el.x += 2;
                    panel.repaint();
                }
            }, 0, 10, TimeUnit.MILLISECONDS);
        }
    }
    
    private void stopAll() {
        running = false;
        if (classicThreads != null) {
            classicThreads.forEach(Thread::interrupt);
            classicThreads.clear();
        }
        if (executorService != null) executorService.shutdownNow();
        if (scheduledExecutor != null) scheduledExecutor.shutdownNow();
    }
    
    public boolean isRunning() { return running; }
    
    // # symulacja duzego obciazenia CPU
    public static void heavyCalculations() {
        double result = 0;
        for (int i = 0; i < 50000; i++) {
            result += Math.sin(i) * Math.cos(i);
        }
    }
}

class WorkerTask implements Runnable {
    private MovingElement el;
    private AnimationPanel panel;
    private AnimationWindow window;
    
    public WorkerTask(MovingElement el, AnimationPanel panel, AnimationWindow window) {
        this.el = el;
        this.panel = panel;
        this.window = window;
    }
    
    @Override
    public void run() {
        while (window.isRunning() && el.x < panel.getWidth() - 20 && !Thread.currentThread().isInterrupted()) {
            // # obciazenie zamiast spania
            AnimationWindow.heavyCalculations();
            el.x += 2;
            SwingUtilities.invokeLater(panel::repaint);
        }
    }
}

class AnimationPanel extends JPanel {
    private List<MovingElement> elements = new CopyOnWriteArrayList<>();
    
    public void initElements(int count) {
        elements.clear();
        for (int i = 0; i < count; i++) {
            elements.add(new MovingElement(10, 10 + (i * 5)));
        }
        repaint();
    }
    
    public List<MovingElement> getElements() { return elements; }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (MovingElement el : elements) {
            g.fillRect(el.x, el.y, 10, 4);
        }
    }
}

class MovingElement {
    int x, y;
    public MovingElement(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
