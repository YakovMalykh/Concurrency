public class MFU {
    private final Object monitor1 = new Object();
    private final Object monitor2 = new Object();

    public void print(int numberOfPages) {
        synchronized (monitor1) {
            for (int i = 0; i < numberOfPages; i++) {
                System.out.println("Отпечатано " + (i + 1) + " стр");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void scan(int numberOfPages) {
        synchronized (monitor2) {
            for (int i = 0; i < numberOfPages; i++) {
                System.out.println("Отсканировано " + (i + 1) + " стр");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
