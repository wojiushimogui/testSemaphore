package com.wrh.semaphore;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class SemaphoreDemo {
	private Semaphore produceSem;
	private Semaphore customerSem;
	
	private Semaphore mutex;
	private Object[] warehouse;
	private int head,tail;
	public SemaphoreDemo(int capacity){
		produceSem = new Semaphore(capacity);
		customerSem = new Semaphore(0);
		warehouse = new Object[capacity];
		head = 0;
		tail = 0;
		mutex = new Semaphore(1);
	}
	
	public void put(Object o){
		try {
			produceSem.acquire();//��ȡ�洢�ʸ�
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		putObject(o);
		
		customerSem.release();//�����ѵ���Դ��
	}
	
	private void putObject(Object obj){
		try {
			//����
			mutex.acquire();
			warehouse[tail++] = obj;
			if(tail==warehouse.length){
				tail = 0;
			}	
			System.out.println(Thread.currentThread().getName()+"������Ʒ��   "+(Integer)obj);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			//�ͷ���
			mutex.release();
		}
		
	}
	
	public Object get(){
		try {
			customerSem.acquire();//��֤����Դ��������
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Object obj = getObject();
		//System.out.println(Thread.currentThread().getName()+"�õ���Ʒ:  "+obj);
		produceSem.release();// ���ӿ����������ź���
		return obj;
	}

	private Object getObject() {
		try {
			mutex.acquire();//�����ڻ�ȡ��
			Object obj = warehouse[head];
			
			head++;
			if(head==warehouse.length){
				head = 0;
			}
			System.out.println(Thread.currentThread().getName()+"�õ���Ʒ:  "+obj);
			return obj;
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally{
			mutex.release();
		}
		
		return null;
	}
	private static AtomicInteger at = new AtomicInteger(0);
	public static void main(String[] args){
		SemaphoreDemo sd = new SemaphoreDemo(10);
		//����3�������ߡ��������߳�
		for(int i=0;i<3;i++){
			new Thread(new Runnable(){
				
				@Override
				public void run() {
					while(true){
						int val = at.incrementAndGet();
						sd.put(val);
						
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
				
			},"produceThread"+i).start();
			new Thread(new Runnable(){
				@Override
				public void run() {
					while(true){
						sd.get();
						//System.out.println(Thread.currentThread().getName()+"�õ��Ĳ�ƷΪ:"+str);
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
				
			},"customerThread"+i).start();
		}
	}
	
}
