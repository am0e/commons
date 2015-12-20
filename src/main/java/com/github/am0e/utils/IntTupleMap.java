/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.github.am0e.utils;

import java.util.LinkedList;
import java.util.List;

import com.github.am0e.lib.AntLib;
import com.github.am0e.msgs.Msgs;

import gnu.trove.list.array.TIntArrayList;

/**
 * Integer tuple map. Simple map to hold intger tuples [c1,c2]. In addition an
 * integer value can be associated with the tuple and queried upon -
 * [c1,c2,value].
 * 
 * @author Anthony (ARPT)
 */
public final class IntTupleMap {

    protected static final int DEFAULT_CAPACITY = 128;

    protected Tuple[] table;
    protected int bucketmask;
    protected int capacity;
    private boolean allowDuplicateValues;

    public IntTupleMap() {
        this(DEFAULT_CAPACITY, false);
    }

    public IntTupleMap(int capacity) {
        this(capacity, false);
    }

    public IntTupleMap(int capacity, boolean allowDuplicateValues) {
        this.capacity = capacity;
        this.table = new Tuple[capacity];
        this.bucketmask = capacity - 1;
        this.allowDuplicateValues = allowDuplicateValues;
    }

    public Tuple getTuple(int c1, int c2) {
        int index = ((int) c1) & bucketmask;
        for (Tuple e = table[index]; e != null; e = e.next) {
            if (e.c1 == c1 && e.c2 == c2) {
                return e;
            }
        }
        return null;
    }

    public Tuple putifAbsent(int c1, int c2) {

        // Look for a duplicate & update value if there.
        //
        Tuple tuple = null;
        if (allowDuplicateValues || (tuple = getTuple(c1, c2)) == null) {
            // Empty list, start a new entry.
            //
            tuple = insert(c1, c2, 0);
        }
        return tuple;
    }

    private Tuple insert(int c1, int c2, int value) {
        // start a new entry.
        //
        int index = ((int) c1) & bucketmask;
        Tuple tuple = new Tuple(c1, c2, value, table[index]);
        table[index] = tuple;
        return tuple;
    }

    /**
     * Adds a tuple to the set with an optional integer value
     * 
     * @param c1
     *            Tuple integer 1
     * @param c2
     *            Tuple integer 2
     */
    public Tuple putTuple(int c1, int c2, int value) {

        // Look for a duplicate & update value if there.
        //
        Tuple tuple = null;
        if (allowDuplicateValues || (tuple = getTuple(c1, c2)) == null) {
            tuple = insert(c1, c2, value);

        } else {
            // Update existing.
            //
            tuple.value = value;
        }
        return tuple;
    }

    public void removeTuple(int c1, int c2) {
        int index = ((int) c1) & bucketmask;
        Tuple prev = table[index];
        Tuple e = prev;

        while (e != null) {
            Tuple next = e.next;
            if (e.c1 == c1 && e.c2 == c2) {
                if (prev == e) {
                    table[index] = next;
                } else {
                    prev.next = next;
                }
                return;
            }
            prev = e;
            e = next;
        }
    }

    /**
     * Retrieves the set of all integers (c2 values) associated with c1.
     */
    public int[] c2values(int c1) {
        TIntArrayList list = new TIntArrayList();

        for (Tuple e = table[((int) c1) & bucketmask]; e != null; e = e.next) {
            if (e.c1 == c1) {
                list.add(e.c2);
            }
        }

        return list.toArray();
    }

    public Tuple[] tuples(int c1) {
        List<Tuple> array = AntLib.newList();

        for (Tuple e = table[((int) c1) & bucketmask]; e != null; e = e.next) {
            if (e.c1 == c1) {
                array.add(e);
            }
        }

        return array.toArray(new Tuple[0]);
    }

    public int[] values(int c1, int c2) {
        TIntArrayList list = new TIntArrayList();

        for (Tuple e = table[((int) c1) & bucketmask]; e != null; e = e.next) {
            if (e.c1 == c1 && e.c2 == c2) {
                list.add(e.value);
            }
        }

        return list.toArray();
    }

    public boolean containsTuple(int c1, int c2) {
        Tuple tuple = getTuple(c1, c2);
        return tuple == null ? false : true;
    }

    public boolean containsTuples(int c1) {
        for (Tuple e = table[((int) c1) & bucketmask]; e != null; e = e.next) {
            if (e.c1 == c1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieves the set of all values (c1 integer)
     */
    public int[] getAllIntValues1() {
        TIntArrayList array = new TIntArrayList();
        for (int pos = 0; pos != table.length; pos++) {
            for (Tuple e = table[pos]; e != null; e = e.next) {
                if (array.contains(e.c1) == false)
                    array.add(e.c1);
            }
        }

        return array.toArray();
    }

    /**
     * Retrieves the set of all Tuples
     */
    public Tuple[] allTuples() {
        List<Tuple> array = new LinkedList<>();
        for (int pos = 0; pos != table.length; pos++) {
            for (Tuple e = table[pos]; e != null; e = e.next) {
                array.add(e);
            }
        }

        return array.toArray(new Tuple[0]);
    }

    static public final class Tuple {
        private final int c1;
        private final int c2;
        public int value;
        private Tuple next;

        /**
         * Create new entry.
         */
        Tuple(int c1, int c2, int value, Tuple n) {
            this.c1 = c1;
            this.c2 = c2;
            this.value = value;
            this.next = n;
        }

        public int hashCode() {
            return ((int) c1);
        }

        public String toString() {
            return Msgs.format("{}:{}:{}", c1, c2, value);
        }

        public final int getC1() {
            return c1;
        }

        public final int getC2() {
            return c2;
        }

        public final int getValue() {
            return value;
        }

        public final void setValue(int v) {
            value = v;
        }
    }
}