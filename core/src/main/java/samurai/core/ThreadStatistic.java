/*
 * Copyright 2003-2012 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package samurai.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ThreadStatistic implements ThreadDumpRenderer, Serializable {
    private List<FullThreadDump> fullThreadDumps = new ArrayList<FullThreadDump>();

    private List<ThreadDumpSequence> threadDumpsList = new ArrayList<ThreadDumpSequence>();
    private static final long serialVersionUID = 871320558326468787L;

    public ThreadStatistic() {
    }

    public String getFirstThreadId() {
        return this.threadDumpsList.get(0).getId();
    }

    public synchronized void reset() {
        this.fullThreadDumps.clear();
        this.threadDumpsList.clear();
    }

    public void onFullThreadDump(FullThreadDump fullThreadDump) {
        this.fullThreadDumps.add(fullThreadDump);
        List<ThreadDumpSequence> newThreadDumpsList = new ArrayList<ThreadDumpSequence>(threadDumpsList.size());
        for (int i = 0; i < fullThreadDump.getThreadCount(); i++) {
            for (ThreadDumpSequence sequence :  threadDumpsList) {
                if (fullThreadDump.getThreadDump(i).getId().equals(sequence.getId())) {
                    newThreadDumpsList.add(sequence);
                    break;
                }
            }
        }
        threadDumpsList = newThreadDumpsList;
    }

    public void onThreadDump(ThreadDump threadDump) {
        int size = threadDumpsList.size();
        boolean found = false;
        for (int i = 0; i < size; i++) {
            ThreadDumpSequence dumps = threadDumpsList.get(i);
            if (dumps.getId().equals(threadDump.getId())) {
                found = true;
                dumps.addThreadDump(threadDump);
                break;
            }
        }
        if (!found) {
            threadDumpsList.add(new ThreadDumpSequence(threadDump, getFullThreadDumpCount() + 1));
        }
    }

    public ThreadDumpSequence getPreviousThreadDumps(String threadId) {
        ThreadDumpSequence lastThreadDumps = null;
        for (ThreadDumpSequence td : threadDumpsList) {
            if (td.getId().equals(threadId)) {
                break;
            }
            lastThreadDumps = td;
        }
        return lastThreadDumps;
    }

    public ThreadDumpSequence getNextThreadDumps(String threadId) {
        boolean found = false;
        ThreadDumpSequence nextThreadDumps = null;
        for (ThreadDumpSequence td : threadDumpsList) {
            if (found) {
                nextThreadDumps = td;
                break;
            }
            if (td.getId().equals(threadId)) {
                found = true;
            }
        }
        return nextThreadDumps;
    }

    public ThreadDumpSequence[] getStackTracesAsArray() {
        return threadDumpsList.toArray(new ThreadDumpSequence[threadDumpsList.size()]);
    }

    public int getFullThreadDumpCount() {
        return this.fullThreadDumps.size();
    }

    public FullThreadDump getFullThreadDump(int index) {
        return this.fullThreadDumps.get(index);
    }

    public List<FullThreadDump> getFullThreadDumps() {
        return fullThreadDumps;
    }

    public ThreadDumpSequence getStackTracesById(String id) {
        for (ThreadDumpSequence sequence :  threadDumpsList) {
            if (sequence.getId().equals(id)) {
                return sequence;
            }
        }
        throw new AssertionError("no thread dump with id:" + id + " found");
    }


}
