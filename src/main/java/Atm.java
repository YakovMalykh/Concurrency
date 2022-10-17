public class Atm {
    private final Object monitor = new Object();
    private long amountOfMoney = 100_000;

    public void withdrawsMoney(String name, long amount) {
        System.out.println(name + " подошел к банкомату");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (monitor){
            if (amountOfMoney > amount) {

                long cashBalanceATM = amountOfMoney - amount;
                amountOfMoney = cashBalanceATM;
                System.out.println(name + " вывел " + amount + " рублей. В банкомате осталось "
                        + cashBalanceATM + " рублей");

            } else {
                System.out.println("В банкомате не достаточно денег для " + name);
            }
        }


    }

}
