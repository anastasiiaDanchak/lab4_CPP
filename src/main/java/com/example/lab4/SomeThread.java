package com.example.lab4;

public class SomeThread {
    private static int sharedVariable = 0; // Спільна змінна

    public static void main(String[] args) {
        Object lock = new Object();

        // Перший потік
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    synchronized (lock) {
                        sharedVariable++;
                    }
                }
                System.out.println("Thread 1 completed.");
            }
        });

        // Другий потік
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    synchronized (lock) {
                        sharedVariable++;
                    }
                }
                System.out.println("Thread 2 completed.");
            }
        });

        // Третій потік
        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    thread1.join();
                    thread2.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Final value of sharedVariable: " + sharedVariable);
            }
        });

        // Запускаємо потоки
        thread1.start();
        thread2.start();
        thread3.start();
    }
}


