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
