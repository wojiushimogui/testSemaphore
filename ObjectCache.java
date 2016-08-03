package com.wrh.semaphore;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectCache<T> {
	interface ObjectFactory<T>{
		T makeObject();
	}
	private Semaphore semaphore;
	private int capacity;
	private ObjectFactory<T> factory;
	private Lock lock;
	
	private Node head,tail;
	private class Node{
		T obj;
		Node next;
	}
	public ObjectCache(int capacity,
			ObjectFactory<T> factory) {
		this.capacity = capacity;
		this.factory = factory;
		this.lock = new ReentrantLock();
		this.semaphore =new Semaphore(capacity);
		head = tail = null;
	}	
	
	public T getObject(){
		try {
			semaphore.acquire();//如果还有资源，则允许通过
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return getNextObject();
	
	}

	private T getNextObject() {
		lock.lock();
		
		try{
			if(head==null){//目前还没有任何产品，应该生产
				return factory.makeObject();
			}
			T value = head.obj;
			Node next = head.next;
			if(next==null){
				tail = null;
			}
			else{
				head.next = null;//help GC
				head = next;
			}
			return value;
		}finally{
			lock.unlock();
		}
	}
	
	public void returnObject(T t){
		returnObjectToPool(t);
		semaphore.release();//表示释放资源
	}
	
	public void returnObjectToPool(T t){
		lock.lock();
		
		try{
			Node node = new Node();
			node.obj = t;
			if(head==null){
				head = tail = node;
			}
			else{
				tail.next = node;
				tail = node;
			}
			
		}finally{
			lock.unlock();
		}
	}
}
