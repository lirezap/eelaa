/*
 * Copyright 2026 Alireza Pourtaghi <lirezap@protonmail.com>
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
 * List of LMDB related flags for passing into its functions.
 *
 * @author Alireza Pourtaghi
 */
public final class LMDBFlags {
    public static final int MDB_FIXEDMAP = 0x01;
    public static final int MDB_NOSUBDIR = 0x4000;
    public static final int MDB_NOSYNC = 0x10000;
    public static final int MDB_RDONLY = 0x20000;
    public static final int MDB_NOMETASYNC = 0x40000;
    public static final int MDB_WRITEMAP = 0x80000;
    public static final int MDB_MAPASYNC = 0x100000;
    public static final int MDB_NOTLS = 0x200000;
    public static final int MDB_NOLOCK = 0x400000;
    public static final int MDB_NORDAHEAD = 0x800000;
    public static final int MDB_NOMEMINIT = 0x1000000;
    public static final int MDB_PREVSNAPSHOT = 0x2000000;
    public static final int MDB_REMAP_CHUNKS = 0x4000000;
    public static final int MDB_REVERSEKEY = 0x02;
    public static final int MDB_DUPSORT = 0x04;
    public static final int MDB_INTEGERKEY = 0x08;
    public static final int MDB_DUPFIXED = 0x10;
    public static final int MDB_INTEGERDUP = 0x20;
    public static final int MDB_REVERSEDUP = 0x40;
    public static final int MDB_CREATE = 0x40000;
    public static final int MDB_NOOVERWRITE = 0x10;
    public static final int MDB_NODUPDATA = 0x20;
    public static final int MDB_CURRENT = 0x40;
    public static final int MDB_RESERVE = 0x10000;
    public static final int MDB_APPEND = 0x20000;
    public static final int MDB_APPENDDUP = 0x40000;
    public static final int MDB_MULTIPLE = 0x80000;
    public static final int MDB_CP_COMPACT = 0x01;
}
