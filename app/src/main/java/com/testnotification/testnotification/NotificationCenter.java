package com.testnotification.testnotification;

import android.app.Application;
import android.util.SparseArray;

import androidx.annotation.UiThread;

import java.util.ArrayList;

public class NotificationCenter {

    private static int totalEvents = 1;

    public static final int firstNotify = totalEvents++;


    private SparseArray<ArrayList<Object>> observers = new SparseArray<>();
    private SparseArray<ArrayList<Object>> removeAfterBroadcast = new SparseArray<>();
    private SparseArray<ArrayList<Object>> addAfterBroadcast = new SparseArray<>();
    private SparseArray<ArrayList<Object>> delayedPost = new SparseArray<>();

    private int broadcasting = 0;
    private int currentHeavyOperationFlags;
    private boolean animationInProgress;
    private int[] allowedNotification;


    public interface NotificationDelegate {
        void didReceivedNotificationDelegate(int id, Object... args);
    }

    private class DelayedPost {
        private int id;
        private Object[] args;

        private DelayedPost(int id, Object[] args) {
            this.id = id;
            this.args = args;

        }
    }

    private static volatile NotificationCenter Instance = new NotificationCenter();

    @UiThread
    public static NotificationCenter getInstance() {
        NotificationCenter localInstance = Instance;
        if (localInstance == null) {
            synchronized (NotificationCenter.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new NotificationCenter();
                }
            }
        }
        return localInstance;
    }

    public void setAllowedNotificationsDuringAnimation(int[] notifications) {
        allowedNotification = notifications;
    }

//    public void setAnimationInProgress(boolean value){
//        if (value){
//            NotificationCenter
//        }
//    }

    public void postNotificationName(int id, Object... args) {
        boolean allowDuringAnimation = true;
        //Đoạn này chưa hiểu
//        boolean allowDuringAnimation = id == startAllHeavyOperations || id == stopAllHeavyOperations;
//        if (!allowDuringAnimation && allowedNotifications != null) {
//            for (int a = 0; a < allowedNotifications.length; a++) {
//                if (allowedNotifications[a] == id) {
//                    allowDuringAnimation = true;
//                    break;
//                }
//            }
//        }
//        if (id == startAllHeavyOperations) {
//            Integer flags = (Integer) args[0];
//            currentHeavyOperationFlags &= ~flags;
//        } else if (id == stopAllHeavyOperations) {
//            Integer flags = (Integer) args[0];
//            currentHeavyOperationFlags |= flags;
//        }

        postNotificationNameInternal(id, allowDuringAnimation, args);
    }

    @UiThread
    private void postNotificationNameInternal(int id, boolean allowDuringAnimation, Object[] args) {
        //đoạn này check sau
//        if (Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
//            throw new RuntimeException("postNotificationName allowed only from MAIN thread");
//        }

        if (!allowDuringAnimation && animationInProgress) {
//            DelayedPost delayedPost = new DelayedPost(id, args);
//            delayedPosts.add(delayedPost);
//                FileLog.e("delay post notification " + id + " with args count = " + args.length);
            return;
        }
        broadcasting++;
        ArrayList<Object> objects = observers.get(id);
        if (objects != null && !objects.isEmpty()) {
            for (int i = 0; i < objects.size(); i++) {
                Object obj = objects.get(i);
                ((NotificationDelegate) obj).didReceivedNotificationDelegate(id, args);
            }
        }
        broadcasting--;
        if (broadcasting == 0) {
            if (removeAfterBroadcast.size() != 0) {
                for (int i = 0; i < removeAfterBroadcast.size(); i++) {
                    int key = removeAfterBroadcast.keyAt(i);
                    ArrayList<Object> arrayList = removeAfterBroadcast.get(key);
                    for (int j = 0; j < arrayList.size(); j++) {
                        removeObserver(arrayList.get(j), key);
                    }
                }
                removeAfterBroadcast.clear();
            }
            if (addAfterBroadcast.size() != 0) {
                for (int i = 0; i < addAfterBroadcast.size(); i++) {
                    int key = addAfterBroadcast.keyAt(i);
                    ArrayList<Object> arrayList = addAfterBroadcast.get(key);
                    for (int j = 0; j < arrayList.size(); j++) {
                        addObserver(arrayList.get(j), key);
                    }
                }
                addAfterBroadcast.clear();
            }
        }


    }

    private void addObserver(Object observer, int id) {
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = addAfterBroadcast.get(id);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                addAfterBroadcast.put(id, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        ArrayList<Object> objects = observers.get(id);
        if (objects == null) {
            observers.put(id, (objects = new ArrayList<>()));
        }
        if (objects.contains(observer)) {
            return;
        }
        objects.add(observer);
    }

    private void removeObserver(Object o, int id) {
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = removeAfterBroadcast.get(id);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                removeAfterBroadcast.put(id, arrayList);
            }
            arrayList.add(observers);
            return;
        }
        ArrayList<Object> objects = observers.get(id);
        if (objects != null) {
            objects.remove(o);
        }
    }

    public boolean hasObservers(int id) {
        return observers.indexOfKey(id) >= 0;
    }
}
