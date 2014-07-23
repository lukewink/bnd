package aQute.bnd.osgi;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

import aQute.lib.zip.*;

public class ZipResource implements Resource {
	ZipFile		zip;
	ZipEntry	entry;
	long		lastModified;
	String		extra;

	ZipResource(ZipFile zip, ZipEntry entry) throws UnsupportedEncodingException {
		this.zip = zip;
		this.entry = entry;
		this.lastModified = -11L;
		byte[] data = entry.getExtra();
		if (data != null)
			this.extra = new String(data, "UTF-8");
	}

	public InputStream openInputStream() throws IOException {
		return zip.getInputStream(entry);
	}

	@Override
	public String toString() {
		return ":" + zip.getName() + "(" + entry.getName() + "):";
	}

	public static ZipFile build(Jar jar, File file) throws ZipException, IOException {
		return build(jar, file, null);
	}

	public static ZipFile build(Jar jar, File file, Pattern pattern) throws ZipException, IOException {

		try {
			ZipFile zip = new ZipFile(file);
			nextEntry: for (Enumeration< ? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
				ZipEntry entry = e.nextElement();
				if (pattern != null) {
					Matcher m = pattern.matcher(entry.getName());
					if (!m.matches())
						continue nextEntry;
				}
				if (!entry.isDirectory()) {

					jar.putResource(entry.getName(), new ZipResource(zip, entry), true);
				}
			}
			return zip;
		}
		catch (ZipException ze) {
			throw new ZipException("The JAR/ZIP file (" + file.getAbsolutePath() + ") seems corrupted, error: "
					+ ze.getMessage());
		}
		catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Problem opening JAR: " + file.getAbsolutePath());
		}
	}

	public void write(OutputStream out) throws Exception {
		FileResource.copy(this, out);
	}

	public long lastModified() {
		try {
			if (lastModified == -11L) {
				lastModified = ZipUtil.getModifiedTime(entry);
			}
			return lastModified;
		}
		catch (IOException e) {
			return lastModified = -1;
		}
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public long size() {
		return entry.getSize();
	}
}
