package com.okcoin.dapp.bundler.infra.storage;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author fanweiqiang
 * @create 2023/10/20 11:13
 */
@Slf4j
public abstract class AbstractTemplate<T> {

    private static final String DB_ROOT_PATH = "leveldb";
    private static final Charset CHARSET = Charset.forName("utf-8");


    private File file;

    private Map<String, T> memoryMap;

    private Set<String> updateKeys;

    private Set<String> deleteKeys;

    private Object updateLock = new Object();
    private Object deleteLock = new Object();

    @PostConstruct
    public void initMemory() {
        memoryMap = new ConcurrentHashMap<>();
        updateKeys = new HashSet<>();
        deleteKeys = new HashSet<>();
        List<T> records = null;
        try {
            records = getAllRecordsFromBb();
        } catch (Exception e) {
            log.error("failed load table data from db, table: {}, exception: {}", getTableName(), e.getLocalizedMessage());
        }
        if (CollectionUtils.isEmpty(records)) {
            log.info("load empty data from db for table: {}", getTableName());
            return;
        }
        log.info("success load data from db for table: {}, size: {}", getTableName(), records.size());
        records.forEach(item -> memoryMap.put(getSaveKey(item), item));
    }

    public abstract Class<T> getTClass();

    /**
     * ensure the server system, linux for /xxx, windows \xxx
     *
     * @return
     */
    public abstract String getTableName();

    public abstract String getSaveKey(T t);

    private File getDbFile() {
        if (file != null) {
            return file;
        }
        file = new File(DB_ROOT_PATH + getTableName());
        return file;
    }

    public boolean save(T... objs) {
        List<String> keys = new ArrayList<>(objs.length);
        for (T obj : objs) {
            String key = getSaveKey(obj);
            memoryMap.put(key, obj);
            keys.add(key);
        }
        synchronized (updateLock) {
            synchronized (deleteLock) {
                updateKeys.addAll(Lists.newArrayList(keys));
                deleteKeys.removeAll(keys);
            }
        }
        return true;
    }

    public boolean saveIntoDb(T... objs) {
        DBFactory factory = new Iq80DBFactory();
        // 默认如果没有则创建
        Options options = new Options();
        //sync写数据后强制写入磁盘
        WriteOptions writeOptions = new WriteOptions().sync(true);
        DB db = null;
        List<String> keys = new ArrayList<>(objs.length);
        try {
            db = factory.open(getDbFile(), options);
            WriteBatch writeBatch = db.createWriteBatch();
            for (T t : objs) {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(baos);
//            oos.writeObject(t);
//            db.put(keyByte, baos.toByteArray(), writeOptions);
                String key = getSaveKey(t);
                keys.add(key);
                writeBatch.put(key.getBytes(CHARSET), JSON.toJSONString(t).getBytes(CHARSET));
            }
            db.write(writeBatch, writeOptions);
            log.info("success save objs in table: {}, keys: {}", getTableName(), JSON.toJSONString(keys));
            return true;
        } catch (IOException e) {
            log.error("failed save objs in table: {}, keys: {}, exception: {}", getTableName(), JSON.toJSONString(keys),
                    e.getLocalizedMessage());
            throw new RuntimeException(e);
        } finally {
            closeDb(db);
        }
    }

    private void closeDb(DB db) {
        if (db == null) {
            return;
        }
        try {
            db.close();
        } catch (IOException e) {
            log.error("failed close db io for table: {}, exception: {}", getTableName(), e);
        }
    }

    public T getObj(String key) {
        return memoryMap.get(key);
    }

    public T getObjFromDb(String key) {
        DBFactory factory = new Iq80DBFactory();
        Options options = new Options();
        DB db = null;
        try {
            db = factory.open(getDbFile(), options);
            byte[] data = db.get(key.getBytes(CHARSET));
            if (data == null || data.length == 0) {
                return null;
            }
            return JSON.parseObject(new String(data, CHARSET), getTClass());
        } catch (IOException e) {
            log.error("failed get obj from table: {}, by key: {}, exception: {}", getTableName(), key, e);
            throw new RuntimeException(e);
        } finally {
            closeDb(db);
        }
    }

    public boolean deleteKeys(String... keys) {
        for (String key : keys) {
            memoryMap.remove(key);
        }
        synchronized (deleteLock) {
            deleteKeys.addAll(Lists.newArrayList(keys));
        }
        return true;
    }

    public boolean deleteKeysFromDb(String... keys) {
        DBFactory factory = new Iq80DBFactory();
        Options options = new Options();
        WriteOptions writeOptions = new WriteOptions().sync(true);
        DB db = null;
        try {
            db = factory.open(getDbFile(), options);
            WriteBatch writeBatch = db.createWriteBatch();
            for (String key : keys) {
                writeBatch.delete(key.getBytes(CHARSET));
            }
            db.write(writeBatch, writeOptions);
            log.info("success delete objs in table: {}, keys: {}", getTableName(), JSON.toJSONString(keys));
            return true;
        } catch (IOException e) {
            log.error("failed delete objs in table: {}, keys: {}, exception: {}", getTableName(), JSON.toJSONString(keys),
                    e.getLocalizedMessage());
            throw new RuntimeException(e);
        } finally {
            closeDb(db);
        }
    }

    public List<T> getAllRecords() {
        return new ArrayList<>(memoryMap.values());
    }

    public List<T> getAllRecordsFromBb() {
        DBFactory factory = new Iq80DBFactory();
        Options options = new Options();
        ReadOptions readOptions = new ReadOptions();
        DB db = null;
        try {
            db = factory.open(getDbFile(), options);
            Snapshot snapshot = db.getSnapshot();
            readOptions.snapshot(snapshot);
            DBIterator it = db.iterator(readOptions);
            List<T> rets = new ArrayList<>();
            while (it.hasNext()) {
                Map.Entry<byte[], byte[]> entry = it.next();
                rets.add(JSON.parseObject(new String(entry.getValue(), CHARSET), getTClass()));
            }
            return rets;
        } catch (IOException e) {
            log.error("failed get all records in table: {}, exception: {}", getTableName(), e.getLocalizedMessage());
            throw new RuntimeException(e);
        } finally {
            closeDb(db);
        }
    }

    private Set<String> copySet(Set<String> original) {
        Set<String> newSet = new HashSet<>(original.size());
        newSet.addAll(original);
        return newSet;
    }

    public void synToDb() {
        synchronized (updateLock) {
            Set<String> updateKeysCopy = copySet(updateKeys);
            if (!updateKeysCopy.isEmpty()) {
                List<T> modifyRecords = updateKeysCopy.stream().map(item -> memoryMap.get(item)).collect(Collectors.toList());
                saveIntoDb((T[]) modifyRecords.toArray());
            }
            updateKeys = new HashSet<>();
        }
        log.info("finish syn memory modify data to db for table: {}", getTableName());
        synchronized (deleteLock) {
            Set<String> deleteKeysCopy = copySet(deleteKeys);
            if (!deleteKeysCopy.isEmpty()) {
                deleteKeysFromDb((String[]) deleteKeysCopy.toArray());
            }
            deleteKeys = new HashSet<>();
        }
        log.info("finish syn memory delete data to db for table: {}", getTableName());
    }

}
