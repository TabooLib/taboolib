package me.skymc.taboolib.fileutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import me.skymc.taboolib.methods.MethodsUtils;

@Deprecated
public class CopyUtils {
	
	public static long Copy(File file1, File file2) throws IOException {
		// CHECK THE FILE
		if (!file1.exists()) {
			file1.createNewFile();
		}
		if (!file2.exists()) {
			file2.createNewFile();
		}
		
		// RESET TIME
		long time = System.currentTimeMillis();
		
		// I/O SETTING
		FileInputStream in = new FileInputStream(file1);
		FileOutputStream out = new FileOutputStream(file2);
		FileChannel inC = in.getChannel();
		FileChannel outC = out.getChannel();
		ByteBuffer b = null;
		
		// CAPACITY [2GB]
		Integer length = 2097152;
		
		// WORKSPACE
		while (true) {
			if (inC.position() == inC.size()) {
				inC.close();
				outC.close();
				return System.currentTimeMillis() - time;
			}
			if ((inC.size() - inC.position()) < length) {
				length = (int) (inC.size()-inC.position());
			}
			else {
				length = 2097152;
			}
			
			b = ByteBuffer.allocateDirect(length);
			inC.read(b);
			b.flip();
			outC.write(b);
			outC.force(false);
		}
	}
	
	public static long Copy(FileInputStream in, File file2) throws IOException {
		// CHECK THE FILE
		if (!file2.exists()) {
			file2.createNewFile();
		}
		
		// RESET TIME
		long time = System.currentTimeMillis();
		
		// I/O SETTING
		FileOutputStream out = new FileOutputStream(file2);
		FileChannel inC = in.getChannel();
		FileChannel outC = out.getChannel();
		ByteBuffer b = null;
		
		// CAPACITY [2GB]
		Integer length = 2097152;
		
		// WORKSPACE
		while (true) {
			if (inC.position() == inC.size()) {
				inC.close();
				outC.close();
				return System.currentTimeMillis() - time;
			}
			if ((inC.size() - inC.position()) < length) {
				length = (int) (inC.size()-inC.position());
			}
			else {
				length = 2097152;
			}
			
			b = ByteBuffer.allocateDirect(length);
			inC.read(b);
			b.flip();
			outC.write(b);
			outC.force(false);
		}
	}

}
