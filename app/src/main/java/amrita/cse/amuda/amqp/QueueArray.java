package amrita.cse.amuda.amqp;

/**
 * Created by SucharithaReddy on 6/6/2017.
 */

public class QueueArray implements QueueIntf {
    public static final int CAPACITY=100;
    private int[] data;
    private int f=0;
    private int sz=0;

    public QueueArray() { this(CAPACITY); }
    public QueueArray(int capacity) {
        data=  new int[capacity];
    }
    public int size() {
        return sz;
    }

    public boolean isEmpty() {
        return (sz==0);
    }

    public void enqueue(int e) {
        if(sz==data.length)
            System.out.println("Queue is Full");
        else
        {
            int avail=(f+sz)% data.length;
            data[avail]=e;
            sz++;
        }

    }
    public int dequeue() {
        if (isEmpty()) return 0;
        else
        {	int answer = data[f];
            data[f] = 0;
            // TODO Auto-generated method stub
            f = (f + 1) % data.length;
            sz--;
            return answer;
        }
    }

    public int first() {
        if (isEmpty()) return 0;
        return data[f];
    }

}