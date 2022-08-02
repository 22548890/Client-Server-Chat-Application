public class Timeout implements Runnable {

    public Timeout() {

    }

    @Override
    public void run() {

        try {
            Thread.sleep(1000);
            System.out.println("Waiting to connect...");
            Thread.sleep(4000);
            System.out.println("Failed to connect");
            System.exit(0);
        } catch (InterruptedException e) {
        }
        
    }
    
}
