package com.cit.test.runintest;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtility {

	private static final String TAG = "FileUtility";

	public static String getSysfsFile(String fname ) {
		String res = "";
		try {
			FileReader fr = new FileReader( fname );
			BufferedReader br = new BufferedReader( fr );
			res = br.readLine();
			br.close();
		}
		catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		return res;
	}
	
	public static int setSysfsFile(String fname, String val ) {
		
		try {
			File f = new File( fname );
			if ( f.exists() ) {
				FileWriter wr = new FileWriter( f, false );
				wr.write( val );
				wr.close();
			}
			else {
				Log.e( TAG, "ERR: " + fname + " not exists!!!" );
				return -1;
			}
		}
		catch ( IOException e ) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	public static int delFile( String fname ) {
		File f = new File( fname );
		if ( f.exists() ) {
			f.delete();
		}
		
		return 0;
	}
	
	public static int writeFileLine(String fname, String line, boolean append ) {
		if ( fname == null || line == null ) {
			return -1;
		}
		try {
			File f = new File( fname );
			FileWriter wr = new FileWriter( f, append );
			wr.write(line);
			wr.close();
		}
		catch ( IOException e ) {
			e.printStackTrace();
			return -1;
		}

		return 0;
	}
	
	public static String readTextFile(String filename ) {
		String res = "";
		try {
			String line;
			FileReader fr = new FileReader( filename );
			BufferedReader br = new BufferedReader( fr );
			while ( true ) {
				line = br.readLine();
				if ( line == null ) {
					break;
				}
				Log.i(TAG, "[read] " + line );
				res += line.trim() + "\n";
			}
			
			br.close();
		}
		catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		
		return res;
	}
}

