package br.gov.go.saude.pentaho.fastsync.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ByteArrayInOutStream extends ByteArrayOutputStream {
	public ByteArrayInOutStream() {
	}

	public ByteArrayInOutStream(int size) {
		super(size);
	}

	public ByteArrayInputStream getInputStream() {
		ByteArrayInputStream in = new ByteArrayInputStream(this.buf);

		this.buf = null;

		return in;
	}
}
