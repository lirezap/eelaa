package software.openx.eelaa.lmdb;

/**
 * @author Alireza Pourtaghi
 */
public final class MDBEnvOpenFlags {
    public static final int MDB_FIXEDMAP = 0x01;
    public static final int MDB_NOSUBDIR = 0x4000;
    public static final int MDB_RDONLY = 0x20000;
    public static final int MDB_WRITEMAP = 0x80000;
    public static final int MDB_NOMETASYNC = 0x40000;
    public static final int MDB_NOSYNC = 0x10000;
    public static final int MDB_MAPASYNC = 0x100000;
    public static final int MDB_NOTLS = 0x200000;
    public static final int MDB_NOLOCK = 0x400000;
    public static final int MDB_NORDAHEAD = 0x800000;
    public static final int MDB_NOMEMINIT = 0x1000000;
    public static final int MDB_PREVSNAPSHOT = 0x2000000;
}
