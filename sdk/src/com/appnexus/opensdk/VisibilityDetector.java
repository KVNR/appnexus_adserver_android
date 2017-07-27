/*
 *    Copyright 2014 APPNEXUS INC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.appnexus.opensdk;

import android.graphics.Rect;
import android.view.View;

import com.appnexus.opensdk.utils.Clog;
import com.appnexus.opensdk.utils.Settings;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class VisibilityDetector {
    static final long VISIBILITY_THROTTLE_MILLIS = 250;
    private boolean scheduled = false;
    private View mView; // not null
    private ArrayList<VisibilityListener> listeners;
    private Runnable visibilityCheck;
    private ScheduledExecutorService tasker;

    static VisibilityDetector create(View view) {
        if (view == null) {
            Clog.d(Clog.nativeLogTag, "Unable to check visibility");
            return null;
        }

        return new VisibilityDetector(view);
    }

    private VisibilityDetector(View view) {
        this.mView = view;
        this.listeners = new ArrayList<VisibilityListener>();
        scheduleVisibilityCheck();
    }

    void addVisibilityListener(VisibilityListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    boolean removeVisibilityListener(VisibilityListener listener) {
        return listeners.remove(listener);
    }

    void scheduleVisibilityCheck(){
        if(scheduled) return;
        scheduled = true;
        this.visibilityCheck = new Runnable() {
            @Override
            public void run() {
                if (listeners != null) {
                    // copy listeners to a new array to avoid concurrentmodificationexception
                    ArrayList<VisibilityListener> tempList = new ArrayList<VisibilityListener>();
                    for (VisibilityListener listener : listeners) {
                        tempList.add(listener);
                    }
                    if (isVisible()) {
                        for (VisibilityListener listener : tempList) {
                            listener.onVisibilityChanged(true);
                        }
                    } else {
                        for (VisibilityListener listener : tempList) {
                            listener.onVisibilityChanged(false);
                        }
                    }
                }
            }
        };
        tasker = Executors.newSingleThreadScheduledExecutor();
        tasker.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mView.post(visibilityCheck);
            }
        }, 0, VISIBILITY_THROTTLE_MILLIS, TimeUnit.MILLISECONDS);
    }

    boolean isVisible() {
        if (mView == null || mView.getVisibility() != View.VISIBLE || mView.getParent() == null) {
            return false;
        }

        // holds the visible part of a view
        Rect clippedArea = new Rect();

        if (!mView.getGlobalVisibleRect(clippedArea)) {
            return false;
        }

        final int visibleViewArea = clippedArea.height() * clippedArea.width();
        final int totalArea = mView.getHeight() * mView.getWidth();

        if (totalArea <= 0) {
            return false;
        }

        return 100 * visibleViewArea >= Settings.MIN_PERCENTAGE_VIEWED * totalArea;
    }

    void destroy() {
        if (tasker != null) {
            tasker.shutdownNow();
        }
        mView.removeCallbacks(visibilityCheck);
        mView = null;
        listeners = null;
    }

    interface VisibilityListener {
        void onVisibilityChanged(boolean visible);
    }

}
