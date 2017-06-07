package amrita.cse.amuda.amqp;

/**
 * Created by SucharithaReddy on 6/6/2017.
 */

public interface QueueIntf {
    int size();
    boolean isEmpty();
    void enqueue(int e);
    int dequeue();
    int first();
}
