/*
 * Copyright 2026 lirezap@protonmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.openx.eelaa.lmdb;

/**
 * List of LMDB's cursor operations.
 *
 * @author Alireza Pourtaghi
 */
public final class CursorOperations {
    public static final int MDB_FIRST = 0;
    public static final int MDB_FIRST_DUP = 1;
    public static final int MDB_GET_BOTH = 2;
    public static final int MDB_GET_BOTH_RANGE = 3;
    public static final int MDB_GET_CURRENT = 4;
    public static final int MDB_GET_MULTIPLE = 5;
    public static final int MDB_LAST = 6;
    public static final int MDB_LAST_DUP = 7;
    public static final int MDB_NEXT = 8;
    public static final int MDB_NEXT_DUP = 9;
    public static final int MDB_NEXT_MULTIPLE = 10;
    public static final int MDB_NEXT_NODUP = 11;
    public static final int MDB_PREV = 12;
    public static final int MDB_PREV_DUP = 13;
    public static final int MDB_PREV_NODUP = 14;
    public static final int MDB_SET = 15;
    public static final int MDB_SET_KEY = 16;
    public static final int MDB_SET_RANGE = 17;
    public static final int MDB_PREV_MULTIPLE = 18;
}
