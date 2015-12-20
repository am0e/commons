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
package com.github.am0e;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;

import com.github.am0e.json.JsonObjectReader;
import com.github.am0e.json.JsonObjectWriter;
import com.github.am0e.lib.AntLib;
import com.github.am0e.msgs.Msgs;

public class JsonTests {

    public final static class Coll implements Iterable<String> {
        private final List<String> numbers;

        public Coll() {
            numbers = new ArrayList<>();
        }

        public Coll(Collection<String> c) {
            numbers = new ArrayList<>(c);
        }

        public void add(String s) {
            numbers.add(s);
        }

        public Iterator<String> iterator() {
            return numbers.iterator();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof Coll) {
                Coll other = (Coll) obj;
                return numbers.equals(other.numbers);
            } else {
                return false;
            }
        }
    }

    public final static class PhoneType {
        public String typeId;

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj, false);
        }
    };

    public final static class Phone {
        public String number;
        public PhoneType type;

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj, false);
        }
    };

    public final static class PhoneColl {
        public String name;
        public Coll numbers;
    };

    public final static class Person {
        public String name;
        public int id;
        public Double number;
        public Date birthDate;
        public Object empty;
        public List<Prop> propertiesList;
        public Map<String, Prop> propertiesMap;
        public Set<Prop> propertiesSet;
        public Prop[] propertiesArray;
        public List<Prop> nullList;
        public LinkedList<Prop> emptyList;
        public Collection<Prop> emptyColl;
        public List<Long> numbersList;
        public Map<String, String> simpleMap;
        public Phone phoneNumber;
        public String comment;
        public Coll coll;
        public Coll nullColl;
        public boolean enabled;
        public short height;
        public transient Object transientAdr;

        public boolean testEquals(Person other) {
            assertEquals(name, other.name);
            assertEquals(id, other.id);
            assertEquals(number, other.number);
            assertEquals(birthDate, other.birthDate);
            assertEquals(empty, empty);
            assertEquals(propertiesList, other.propertiesList);
            assertEquals(propertiesMap, other.propertiesMap);
            // assertEquals(propertiesSet, other.propertiesSet);
            assertTrue(Arrays.equals(propertiesArray, other.propertiesArray));
            assertEquals(nullList, other.nullList);
            assertEquals(emptyList, other.emptyList);
            assertEquals(emptyColl.size(), other.emptyColl.size());
            assertEquals(numbersList, other.numbersList);
            assertEquals(simpleMap, other.simpleMap);
            assertEquals(phoneNumber, other.phoneNumber);
            assertEquals(comment, other.comment);
            assertEquals(coll, other.coll);
            assertEquals(enabled, other.enabled);
            assertEquals(height, other.height);
            assertEquals(other.transientAdr, null);
            return true;
        }
    }

    // Use 2 levels to test fields in super class are serialized.
    //
    public static class PropBase {
        public String name;
        public String value;

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj, false);
        }

        @Override
        public String toString() {
            return Msgs.format("{}:{}", name, value);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public final static class Prop extends PropBase {
        public Prop() {
        }

        public Prop(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private Person testPerson;

    @SuppressWarnings("deprecation")
    public JsonTests() {
        // Setup person.
        //
        Person p = new Person();
        p.name = "Joe Foo";
        p.id = 101;
        p.number = 10.01;
        p.birthDate = new Date(Date.UTC(56, 2, 25, 12, 00, 01));
        p.comment = " <>\"\',\n\r\t ";
        p.enabled = true;
        p.height = 170;
        p.transientAdr = new String("not serialized");
        p.coll = new Coll();
        p.coll.add("number 1");
        p.coll.add("number 2");
        p.coll.add("number 3");
        p.phoneNumber = new Phone();
        p.phoneNumber.number = "0999-1999";
        p.phoneNumber.type = new PhoneType();
        p.phoneNumber.type.typeId = "Mobile";

        p.propertiesArray = new Prop[] { new Prop("n1", "v1"), new Prop("n2", "v2"), };
        p.propertiesList = Arrays.asList(new Prop("n1", "v1"), new Prop("n2", "v2"));
        p.numbersList = Arrays.asList(new Long(100), new Long(200));
        p.simpleMap = new HashMap<>();
        p.simpleMap.put("map_key1", "map_val1");
        p.simpleMap.put("map_key2", "map_val2");

        p.emptyList = new LinkedList<>();
        p.emptyColl = AntLib.newHashSet();
        p.nullList = null;

        p.propertiesMap = new HashMap<>();
        p.propertiesMap.put("p1", new Prop("n1", "v1"));
        p.propertiesMap.put("p2", new Prop("n2", "v2"));
        p.propertiesSet = AntLib.newHashSet();
        p.propertiesSet.add(new Prop("n1", "v1"));
        p.propertiesSet.add(new Prop("n2", "v2"));

        this.testPerson = p;
    }

    @Test
    public void testJson() {

        // Write person json.
        //
        StringWriter sw1 = new StringWriter();
        JsonObjectWriter out = new JsonObjectWriter();
        out.write(sw1, testPerson);

        // Unserialize from json back to Person
        //
        StringReader sr = new StringReader(sw1.toString());
        JsonObjectReader in = new JsonObjectReader();
        Person copy = in.readObject(sr, Person.class);

        assertNull(copy.transientAdr);
        assertEquals(copy.birthDate, testPerson.birthDate);
        testPerson.testEquals(copy);

        // Write person copy back to json.
        //
        StringWriter sw2 = new StringWriter();
        out.write(sw2, copy);

        // Now compare the two Strings
        //
        assertTrue(sw1.toString().equals(sw2.toString()));
    }

    @Test
    public void testJsonAdaptors() {
        StringWriter sw1 = new StringWriter();
        JsonObjectWriter out = new JsonObjectWriter();

        out.registerAdaptor(Coll.class, (jw, coll) -> {
            jw.startArray();
            for (String it : coll.numbers) {
                jw.genValue("_" + it + "_");
            }
            jw.endArray();
        });

        PhoneColl col = new PhoneColl();
        col.name = "Beaker";
        col.numbers = new Coll();
        col.numbers.add("number 1");
        col.numbers.add("number 2");
        col.numbers.add("number 3");

        out.write(sw1, col);
    }

}
