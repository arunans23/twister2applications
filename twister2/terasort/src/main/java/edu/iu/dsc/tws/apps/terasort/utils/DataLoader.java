//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
package edu.iu.dsc.tws.apps.terasort.utils;

import org.apache.hadoop.io.Text;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Assume datacols is partitioned into file
 */
public final class DataLoader {
    private static final Logger LOG = Logger.getLogger(DataLoader.class.getName());

    private DataLoader() {

    }

    public static List<Record> load(int rank, String inFileName) {
        List<Record> records = new ArrayList<>();
        byte[] buffer = new byte[Record.RECORD_LENGTH];

        try {
            DataInputStream in = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(new File(inFileName))));
            while (true) {
                int read = 0;
                Text key = new Text();
                Text value = new Text();
                while (read < Record.RECORD_LENGTH) {
                    long newRead = in.read(buffer, read, Record.RECORD_LENGTH - read);
                    if (newRead == -1) {
                        if (read == 0) {
                            return records;
                        } else {
                            throw new EOFException("read past eof");
                        }
                    }
                    read += newRead;
                }
                key.set(buffer, 0, Record.KEY_SIZE);
                value.set(buffer, Record.KEY_SIZE, Record.DATA_SIZE);
                records.add(new Record(key, value));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to read the file: " + rank, e);
            throw new RuntimeException(e);
        }
    }

    public static List<Record> load(DataInputStream in, int limit, int rank) {
        List<Record> records = new ArrayList<>();
        byte[] buffer = new byte[Record.RECORD_LENGTH];
        int count = 0;
        try {
            while (count < limit) {
                int read = 0;
                Text key = new Text();
                Text value = new Text();
                while (read < Record.RECORD_LENGTH) {
                    long newRead = in.read(buffer, read, Record.RECORD_LENGTH - read);
                    if (newRead == -1) {
                        if (read == 0) {
                            return records;
                        } else {
                            throw new EOFException("read past eof");
                        }
                    }
                    read += newRead;
                }
                key.set(buffer, 0, Record.KEY_SIZE);
                value.set(buffer, Record.KEY_SIZE, Record.DATA_SIZE);
                records.add(new Record(key, value));
                count++;
            }
            return records;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to read the file: " + rank, e);
            throw new RuntimeException(e);
        }
    }

    public static int load(DataInputStream in, int limit, int rank, List<byte[]> recordsKeys, List<byte[]> recordsVals) {
        int count = 0;
        long newRead;
        try {
            while (count < limit) {
                int read = 0;
                int readval = 0;
                while (read < Record.RECORD_LENGTH) {
                    if(read < 10){
                        newRead = in.read(recordsKeys.get(count), read, Record.KEY_SIZE - read);
                    }else{
                        newRead = in.read(recordsVals.get(count), readval, Record.RECORD_LENGTH - read);
                        readval += newRead;
                    }
                    if (newRead == -1) {
                        if (read == 0) {
                            return count;
                        } else {
                            throw new EOFException("read past eof");
                        }
                    }
                    read += newRead;
                }
                count++;
            }
            return count;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to read the file: " + rank, e);
            throw new RuntimeException(e);
        }
    }

    public static List<Record> loadFrom(int rank, String inFileName) {
        List<Record> records = new ArrayList<>();
        byte[] buffer = new byte[Record.RECORD_LENGTH];

        try {
            DataInputStream in = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(new File(inFileName))));
            while (true) {
                int read = 0;
                Text key = new Text();
                Text value = new Text();
                while (read < Record.RECORD_LENGTH) {
                    long newRead = in.read(buffer, read, Record.RECORD_LENGTH - read);
                    if (newRead == -1) {
                        if (read == 0) {
                            return records;
                        } else {
                            throw new EOFException("read past eof");
                        }
                    }
                    read += newRead;
                }
                key.set(buffer, 0, Record.KEY_SIZE);
                value.set(buffer, Record.KEY_SIZE, Record.DATA_SIZE);
                records.add(new Record(key, value));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to read the file: " + rank, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * load only a subset of the records
     *
     * @param rank
     * @param inFileName
     * @param numRecords
     * @return
     */
    public static List<Record> load(int rank, String inFileName, int numRecords) {
        List<Record> records = new ArrayList<>();
        byte[] buffer = new byte[Record.RECORD_LENGTH];

        try {
            DataInputStream in = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(new File(inFileName))));
            for (int k = 0; k < numRecords; k++) {
                int read = 0;
                Text key = new Text();
                Text value = new Text();
                while (read < Record.RECORD_LENGTH) {
                    long newRead = in.read(buffer, read, Record.RECORD_LENGTH - read);
                    if (newRead == -1) {
                        if (read == 0) {
                            return records;
                        } else {
                            throw new EOFException("read past eof");
                        }
                    }
                    read += newRead;
                }
                key.set(buffer, 0, Record.KEY_SIZE);
                value.set(buffer, Record.KEY_SIZE, Record.DATA_SIZE);
                records.add(new Record(key, value));
            }
            return records;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to read the file: " + rank, e);
            throw new RuntimeException(e);
        }
    }

    public static byte[] loadArray(int rank, String inFileName) {
        try {
            long fileSize = new File(inFileName).length();
            DataInputStream in = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(new File(inFileName))));
            if (fileSize > Integer.MAX_VALUE) {
                throw new RuntimeException("Failed to load file because of size > " + Integer.MAX_VALUE);
            }
            int size = (int) fileSize;

            byte[] content = new byte[size];
            int read = 0;
            while (read < size) {
                long newRead = in.read(content, read, size - read);
                if (newRead == -1) {
                    throw new EOFException("read past eof");
                }
                read += newRead;
            }
            LOG.info("Rank: " + rank + " Read amount: " + read);
            return content;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to read the file: " + rank, e);
            throw new RuntimeException(e);
        }
    }

    public static void save(Record[] records, String outFileName) {
        DataOutputStream os;
        try {
            os = new DataOutputStream(new FileOutputStream(outFileName));
            for (int i = 0; i < records.length; i++) {
                Record r = records[i];
                os.write(r.getKey().getBytes(), 0, Record.KEY_SIZE);
                os.write(r.getText().getBytes(), 0, Record.DATA_SIZE);
            }
            os.close();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed write to disc", e);
            throw new RuntimeException(e);
        }
    }

    public static void saveFast(Record[] records, String outFileName) {
        try {
            FileChannel rwChannel = new RandomAccessFile(outFileName, "rw").getChannel();
            ByteBuffer os = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0,
                    records.length * Record.RECORD_LENGTH);
            for (int i = 0; i < records.length; i++) {
                Record r = records[i];
                os.put(r.getKey().getBytes(), 0, Record.KEY_SIZE);
                os.put(r.getText().getBytes(), 0, Record.DATA_SIZE);
            }
            rwChannel.close();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed write to disc", e);
            throw new RuntimeException(e);
        }
    }

    public static void saveFast(Record[] records, int size, String outFileName) {
        int i = 0;
        try {
            FileChannel rwChannel = new RandomAccessFile(outFileName, "rw").getChannel();
            ByteBuffer os = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, size * Record.RECORD_LENGTH);
            for (i = 0; i < size; i++) {
                Record r = records[i];
                os.put(r.getKey().getBytes(), 0, Record.KEY_SIZE);
                os.put(r.getText().getBytes(), 0, Record.DATA_SIZE);
            }
            rwChannel.close();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed write to disc", e);
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            LOG.info(String.format("Null pointer size %d, i %d", size, i));
        }
    }
}
