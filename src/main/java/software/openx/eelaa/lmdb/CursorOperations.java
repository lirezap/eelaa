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
