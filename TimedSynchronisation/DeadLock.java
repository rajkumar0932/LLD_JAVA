// Deadlock demo + the fix (lock ordering), side by side.
//   - runTransfers(false) -> two threads lock in OPPOSITE order -> DEADLOCK (hangs forever)
//   - runTransfers(true)  -> both threads lock LOWER id first     -> completes fine

interface Account{
    void addMoney(int amount);
    void deductMoney(int amount);
    int id();                       // rank used to order locks (low -> high)
    String show();
}

class Account1 implements Account{
    private final int id;
    private int money;
    private String name;
    Account1(int id, int money, String name){
        this.id=id;
        this.money=money;
        this.name=name;
    }
    public void addMoney(int amount){ this.money+=amount; }
    public void deductMoney(int amount){ this.money-=amount; }
    public int id(){ return id; }
    public String show(){ return name + "=" + money; }
}

class Account2 implements Account{
    private final int id;
    private int money;
    private String name;
    Account2(int id, int money, String name){
        this.id=id;
        this.money=money;
        this.name=name;
    }
    public void addMoney(int amount){ this.money+=amount; }
    public void deductMoney(int amount){ this.money-=amount; }
    public int id(){ return id; }
    public String show(){ return name + "=" + money; }
}


class MoneyTransfer implements Runnable{
    private String name;
    private Account from;        // money leaves here
    private Account to;          // money arrives here
    private int money;
    private boolean ordered;     // true = lock by id order (safe), false = lock from->to (can deadlock)

    MoneyTransfer(String name, Account from, Account to, int money, boolean ordered){
        this.name=name;
        this.from=from;
        this.to=to;
        this.money=money;
        this.ordered=ordered;
    }

    public void run(){
        // Decide WHICH lock to take first.
        //   ordered == false -> always 'from' then 'to'  (opposite for the two threads -> deadlock)
        //   ordered == true  -> always the LOWER id first (same for both threads -> no cycle)
        Account first  = from;
        Account second = to;
        if (ordered && from.id() > to.id()) {   // swap so lower id is locked first
            first  = to;
            second = from;
        }

        System.out.println(name + ": wants FIRST lock  (acct" + first.id() + ")");
        synchronized(first){
            System.out.println(name + ": got   FIRST lock  (acct" + first.id() + ")");
            sleep(1000);                        // hold long enough for the other thread to grab its first lock
            System.out.println(name + ": wants SECOND lock (acct" + second.id() + ") ...");
            synchronized(second){
                System.out.println(name + ": got   SECOND lock (acct" + second.id() + ")");
                from.deductMoney(money);        // money semantics use from/to, NOT first/second
                to.addMoney(money);
                System.out.println(name + ": DONE transfer of " + money);
            }
        }
    }

    static void sleep(long ms){
        try{ Thread.sleep(ms); } catch(InterruptedException e){ Thread.currentThread().interrupt(); }
    }
}


public class DeadLock {
    public static void main(String [] str){
        // ---- DEADLOCK version (opposite lock order) ----
        // Uncomment the next line to watch it HANG forever (Ctrl+C to stop):
        // runTransfers(false);

        // ---- FIXED version (lock ordering by account id) ----
        runTransfers(true);
    }

    static void runTransfers(boolean ordered){
        System.out.println(ordered ? "=== ORDERED (safe) ===" : "=== UNORDERED (deadlocks) ===");
        Account a1 = new Account1(1, 1000, "ram");
        Account a2 = new Account2(2, 2000, "shayam");

        // opposite DIRECTIONS on purpose: T1 moves a1->a2, T2 moves a2->a1
        Thread t1 = new Thread(new MoneyTransfer("T1", a1, a2, 200, ordered));
        Thread t2 = new Thread(new MoneyTransfer("T2", a2, a1, 300, ordered));
        t1.start();
        t2.start();
        try{
            t1.join();
            t2.join();
        }
        catch(InterruptedException e){ e.printStackTrace(); }
        System.out.println("Both finished: " + a1.show() + " | " + a2.show());
    }
}
