package com.wuyunxing.vae.aidlserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.util.LinkedList;
import java.util.List;

public class PersonManagerService extends Service{

    private LinkedList<Person> personList = new LinkedList<Person>();

    private RemoteCallbackList<IOnNewPersonArrivedListener> mListenerList=new RemoteCallbackList<IOnNewPersonArrivedListener>();

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new ServiceWorker()).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IPersonManager.Stub mBinder = new IPersonManager.Stub(){

        @Override
        public void savePersonInfo(Person person) throws RemoteException {
            if (person != null){
                personList.add(person);
            }
        }

        @Override
        public List<Person> getAllPerson() throws RemoteException {
            return personList;
        }

        @Override
        public void registerListener(IOnNewPersonArrivedListener listener) throws RemoteException {
            mListenerList.register(listener);
        }

        @Override
        public void unregisterListener(IOnNewPersonArrivedListener listener) throws RemoteException {
            mListenerList.unregister(listener);
        }
    };

    private void onNewPersonArrived(Person person) throws RemoteException{
        personList.add(person);
        int n=mListenerList.beginBroadcast();
        for(int i=0;i<n;i++){
            IOnNewPersonArrivedListener l=mListenerList.getBroadcastItem(i);
            if(l!=null){
                try {
                    l.onNewPersonArrivedListener(person);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }

    private class ServiceWorker implements Runnable{

        @Override
        public void run() {
            for(;;){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int age=personList.size()+1;
                Person person=new Person();
                person.setAge(age);
                person.setName("person"+age);
                try {
                    onNewPersonArrived(person);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
