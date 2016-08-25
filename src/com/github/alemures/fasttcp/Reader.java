package com.github.alemures.fasttcp;

import java.util.ArrayList;

final class Reader {
	private byte[] buffer;
	private int offset;
	private int bytesRead;
	private int messageLength;

	private int offsetChunk;

	public ArrayList<byte[]> read(byte[] chunk) {
		offsetChunk = 0;
		ArrayList<byte[]> buffers = new ArrayList<byte[]>();

		while (offsetChunk < chunk.length) {
			if (bytesRead < 4) {
				if (readMessageLength(chunk)) {
					createBuffer();
				} else {
					break;
				}
			}

			if (bytesRead < buffer.length && !readMessageContent(chunk)) {
				break;
			}

			// Buffer ready, store it and keep reading the chunk
			buffers.add(buffer);
			offset = 0;
			bytesRead = 0;
			messageLength = 0;
		}

		return buffers;
	}

	private boolean readMessageLength(byte[] chunk) {
		for (; offsetChunk < chunk.length && bytesRead < 4; offsetChunk++, bytesRead++) {
			messageLength |= chunk[offsetChunk] << (bytesRead * 8);
		}

		return bytesRead == 4;
	}

	private boolean readMessageContent(byte[] chunk) {
		int bytesToRead = buffer.length - bytesRead;
		int bytesInChunk = chunk.length - offsetChunk;
		int end = bytesToRead > bytesInChunk ? chunk.length : offsetChunk + bytesToRead;

		Utils.copyBuffer(chunk, buffer, offset, offsetChunk, end);

		int bytesActuallyRead = end - offsetChunk;

		bytesRead += bytesActuallyRead;
		offset += bytesActuallyRead;
		offsetChunk = end;

		return bytesRead == buffer.length;
	}

	private void createBuffer() {
		buffer = new byte[4 + messageLength];
		Utils.writeInt48ToBuffer(messageLength, buffer, offset);
		offset += 4;
	}
}
