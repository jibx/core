/*
Copyright (c) 2003, Dennis M. Sosnoski.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of Sosnoski Software Solutions, Inc. nor the names of its
   personnel may be used to endorse or promote products derived from this
   software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.sosnoski.site;

import java.io.*;

/**
 * Parse buffer wrapping reader. This class implements basic matching and
 * searching methods for parsing text, along with marking and position methods
 * for working with text in a buffer.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class ParseBuffer
{
	/** Default length used for buffer. */
	public static final int DEFAULT_BUFFER_LENGTH = 4096;

	/** Default initial depth of mark stack (expands as needed). */
	private static final int DEFAULT_STACK_DEPTH = 6;

	/** Message text for end of file exception. */
	protected static final String END_OF_FILE_MESSAGE =
		"Unexpected end of file";

	/** Message text for invalid position exception. */
	private static final String BAD_BUFFER_POSITION = 
		"Invalid buffer position";

	/** Message text for mark not set exception. */
	private static final String NO_MARK_MESSAGE = "No mark is set";

	/** Message text for invalid range exception. */
	private static final String INVALID_RANGE_LENGTH =
		"Invalid length for range";
	
	/** Offset in file of first character in buffer. */
	private int m_baseOffset;
	
	/** Starting offset of data in buffer. */
	private int m_startOffset;

	/** Ending offset of data in buffer. */
	private int m_endOffset;

	/** Offset of current input character in buffer. */
	private int m_dataOffset;

	/** Offset of lowest mark character position in buffer (negative if no
	 mark). */
	private int m_markOffset;

	/** Array containing buffered data. */
	private char[] m_buffer;
	
	/** Number of mark positions on stack. */
	private int m_markCount;

	/** Stack of mark offsets from lowest mark character position. */
	private int[] m_markStack;
	
	/** Flag for no more data available. */
	private boolean m_isEnd;
	
	/** Reader instance wrapped by buffer. */
	private Reader m_reader;
	
	/**
	 * Constructor.
	 *
	 * @param reader reader supplying text
	 * @param length initial buffer size
	 */
	
	public ParseBuffer(Reader reader, int length) {
		m_buffer = new char[length];
		m_reader = reader;
		m_markOffset = -1;
		m_markStack = new int[DEFAULT_STACK_DEPTH];
	}
	
	/**
	 * Constructor. This version just uses a default size for the buffer.
	 * 
	 * @param reader reader supplying text
	 * @param subst text substitution table
	 */
	
	public ParseBuffer(Reader reader) {
		this(reader, DEFAULT_BUFFER_LENGTH);
	}
	
	/**
	 * Reads data into the buffer. If a mark has been set and the array is
	 * full with retained data, a replacement array of twice the size is 
	 * allocated and the retained data is copied across. Otherwise, any 
	 * retained data is copied down to the start of the buffer array. Next,
	 * data is read from the wrapped reader into the available space in the
	 * buffer array.<p>
	 *
	 * The actual number of characters read by a call to this method is 
	 * normally between one and the space available in the buffer array (after
	 * doubling the size of the array, if it was previously full). If no data
	 * is available the end of file flag is instead set before the return.
	 *
	 * @return <code>true</code> if one or more bytes read, <code>false</code>
	 * if at end of file
	 * @exception IOException if a read error occurs
	 */
	
	private boolean fillBuffer() throws IOException {
		
		// make sure we're not already at end
		while (!m_isEnd) {
			
			// compute the starting point and space available
			int base = Math.min(m_dataOffset, m_endOffset);
			if (m_markOffset >= 0 && base > m_markOffset) {
				base = m_markOffset;
			}
			
			// check handling needed for existing data
			if (base > 0) {
				
				// move existing data down to start of buffer
				System.arraycopy(m_buffer, base, m_buffer, 0, m_endOffset-base);
				m_baseOffset += base;
				m_startOffset = 0;
				m_endOffset -= base;
				m_dataOffset -= base;
				if (m_markOffset >= 0) {
					m_markOffset -= base;
				}
				
			} else if (m_endOffset == m_buffer.length) {
					
				// double the buffer size to retain more data
				int space = m_buffer.length - base;
				space *= 2;
				char[] grow = new char[space];
				System.arraycopy(m_buffer, 0, grow, 0, m_buffer.length);
				m_buffer = grow;
					
			}
			
			// read data from input stream
			int space = m_buffer.length - m_endOffset;
			int actual = m_reader.read(m_buffer, m_endOffset, space);
			if (actual > 0) {
				m_endOffset += actual;
				if (m_endOffset > m_dataOffset) {
					return true;
				}
			} else {
				m_isEnd = true;
			}
			
		}
		return false;
	}
	
	/**
	 * Reads data into the buffer. This method guarantees that at least one
	 * character of data will be read by the call, throwing an exception
	 * otherwise. As with {@link fillBuffer}, this method may need to replace
	 * the actual character array used for the buffer, so the caller must not
	 * keep a local copy of the buffer referenced across calls.
	 *
	 * @exception IOException if a read error occurs
	 * @see #fillBuffer
	 */
	
	private void forceFill() throws IOException {
		if (!fillBuffer()) {
			throw new EOFException(END_OF_FILE_MESSAGE);
		}
	}

	/**
	 * Read next character. This method guarantees that the character
	 * read will remain in the buffer until after the next buffer method call,
	 * so that a <code>skip(-1)</code> call can always be used after a
	 * successful character read operation to position for rereading the same
	 * character.
	 * 
	 * @return character read, or <code>-1</code> if end of input
	 * @exception IOException if a read error occurs
	 * @see #skip
	 */
	
	public final int read() throws IOException {
		
		// read data to buffer if none available
		while (m_dataOffset >= m_endOffset) {
			if (!fillBuffer()) {
				return -1;
			}
		}
		
		// return next character from buffer
		return m_buffer[m_dataOffset++];
	}

	/**
	 * Read next character, which must be present. This method guarantees that
	 * the character read will remain in the buffer until after the next buffer
	 * method call, so that a <code>skip(-1)</code> call can always be used 
	 * after a successful character read operation to position for rereading the
	 * same character.
	 * 
	 * @return character read
	 * @exception IOException if a read error or end of input
	 * @see #skip
	 */
	
	public final int forceRead() throws IOException {
		int chr = read();
		if (chr < 0) {
			throw new EOFException(END_OF_FILE_MESSAGE);
		} else {
			return chr;
		}
	}
	
	/**
	 * Read characters into a portion of an array.
	 * 
	 * @param buff destination array for characters read
	 * @param offset starting offset in array
	 * @param length number of characters to read
	 * @exception IOException if a read error occurs (including end of data)
	 */
	
	public void read(char[] buff, int offset, int length) throws IOException {
		
		// loop until the full amount required has been read
		while (length > 0) {
			if (m_dataOffset >= m_endOffset) {
				forceFill();
			}
			int use = Math.min(length, m_endOffset - m_dataOffset);
			System.arraycopy(m_buffer, m_dataOffset, buff, offset, use);
			m_dataOffset += use;
			offset += use;
			length -= use;
		}
	}
	
	/**
	 * Read characters as a <code>String</code>.
	 * 
	 * @param length number of characters to read
	 * @return <code>String</code> consisting of the characters read
	 * @exception IOException if a read error occurs (including end of data)
	 */
	
	public String read(int length) throws IOException {
		
		// read all the characters into buffer
		pushMark(0);
		try {
			while (m_dataOffset + length > m_endOffset) {
				forceFill();
			}
		} finally {
			popMark();
		}
		
		// convert the characters to a string
		String result = new String(m_buffer, m_dataOffset, length);
		m_dataOffset += length;
		return result;
	}
	
	/**
	 * Get current character position in input.
	 * 
	 * @return index of current character from start of input
	 */
	
	public int position() {
		return m_baseOffset + m_dataOffset;
	}
	
	/**
	 * Set current character position in input. The character position set
	 * must be within the buffered data.
	 * 
	 * @param index of character from start of input
	 */
	
	public void setPosition(int position) {
		skip(position - m_baseOffset - m_dataOffset);
	}
	
	/**
	 * Check end of input reached. If currently positioned at the end of the
	 * data in buffer this forces a read of more data.
	 * 
	 * @return <code>true</code> if end of input, <code>false</code> otherwise
	 */
	
	public final boolean isEnd() throws IOException {
		if (m_isEnd) {
			return true;
		} else if (m_dataOffset >= m_endOffset) {
			fillBuffer();
			return m_dataOffset >= m_endOffset;
		} else {
			return false;
		}
	}
	
	/**
	 * Marks a position in the buffer. When a mark has been set, the buffer
	 * guarantees that all data from the marked position on will be retained
	 * until the mark is cleared. Multiple marks may be set, but they must be
	 * nested - each mark must refer to a later position than the prior mark
	 * in the sequence. This form marks a position relative to the current
	 * input position.
	 * 
	 * @param offset mark position offset relative to current position
	 * @exception IllegalArgumentException if a the requested mark position is
	 * invalid (preceding a prior mark or the start of buffered data)
	 */

	public void pushMark(int offset) {
		
		// compute mark position in buffer
		int position = m_dataOffset + offset;
		if (position < m_startOffset) {
			throw new IllegalArgumentException(BAD_BUFFER_POSITION);
		}
		
		// check if this is the first mark
		int value = offset;
		if (m_markOffset >= 0) {
			
			// find offset from existing mark
			value = position - m_markOffset;
			if (value < 0) {
				throw new IllegalArgumentException(BAD_BUFFER_POSITION);
			}
			
		} else {
			m_markOffset = position;
			value = 0;
		}
			
		// store mark position on stack
		if (m_markCount >= m_markStack.length) {
			int[] stack = new int[m_markStack.length*2];
			System.arraycopy(m_markStack, 0, stack, 0, m_markStack.length);
			m_markStack = stack;
		}
		m_markStack[m_markCount++] = value;
	}
	
	/**
	 * Marks the current position in the buffer. When a mark has been set, the
	 * buffer guarantees that all data from the marked position on will be
	 * retained until the mark is cleared. Multiple marks may be set, but they
	 * must be nested - each mark must refer to a later position than the prior
	 * mark in the sequence.
	 * 
	 * @exception IllegalArgumentException if a the requested mark position is
	 * invalid (preceding a prior mark or the start of buffered data)
	 */
	
	public void pushMark() {
		pushMark(0);
	}
	
	/**
	 * Clear the most recent mark still active. This method removes the
	 * innermost nested mark without effecting the current position, returning
	 * the offset of the mark cleared from the current position.
	 * 
	 * @return current position offset from marked position
	 * @exception IllegalStateException if no mark is set
	 */
	
	public int clearMark() {
		
		// check for a mark set
		if (m_markCount > 0) {
			
			// find this mark offset from current position
			int value = m_markStack[--m_markCount];
			int offset = m_markOffset + value - m_dataOffset;
			
			// clear base mark offset if this was the last one
			if (m_markCount == 0) {
				m_markOffset = -1;
			}
			
			// return offset from current position
			return offset;
			
		} else {
			throw new IllegalStateException(NO_MARK_MESSAGE);
		}
	}
	
	/**
	 * Restore the most recent mark still active. This method removes the
	 * innermost nested mark, resetting the current input position to that 
	 * marked.
	 * 
	 * @return adjustment applied to current input position
	 * @exception IllegalStateException if no mark is set
	 */
	
	public int popMark() {
		int adj = clearMark();
		m_dataOffset += adj;
		return adj;
	}

	/**
	 * Skip input characters. Moves the current input position forward or
	 * backward by the requested number of characters. The skipped characters
	 * must be present in the buffer if moving backward, so this is often used
	 * in combination with a mark position - set a mark to guarantee that all
	 * input from some point will be buffered, assuring that we can freely move
	 * forward and backward within the buffered data.
	 * 
	 * @param length number of characters to be skipped (current position 
	 * moved forward if positive, backward if negative)
	 * @exception IllegalArgumentException if the resulting position is before
	 * the start of data
	 */
	
	public void skip(int length) {
		int position = m_dataOffset + length;
		if (position < m_startOffset) {
			throw new IllegalArgumentException("Negative buffer position");
		}
		m_dataOffset = position;
	}

	/**
	 * Skip past character. Skips all characters in the data stream until the
	 * target character is found, and returns with input positioned to the
	 * character following the target character.
	 * 
	 * @param match character to be found
	 * @exception IOException if character not found in data stream, or if
	 * read error
	 */
	
	public void skipPastChar(int match) throws IOException {
		while (read() != match) {
			if (isEnd()) {
				throw new IOException(END_OF_FILE_MESSAGE);
			}
		}
	}

	/**
	 * Skip past string. Skips all characters in the data stream until the
	 * target string is found, and returns with input positioned to the 
	 * character following the last character of the string.
	 * 
	 * @param text string to be found (non-<code>null</code>, non-empty)
	 * @exception IOException if text not found in data stream, or if
	 * read error
	 */
	
	public void skipPastString(String text) throws IOException {
		
		// loop until match found
		loop: while (!isEnd()) {
			
			// find the first character of the target
			skipPastChar(text.charAt(0));
			
			// try to match remaining characters from this point
			pushMark();
			for (int i = 1; i < text.length(); i++) {
				if (read() != text.charAt(i)) {
					
					// mismatch found, continue from after this match
					popMark();
					continue loop;
					
				}
			}
			
			// match found, return positioned past target
			clearMark();
			return;
			
		}
		throw new IOException(END_OF_FILE_MESSAGE);
	}

	/**
	 * Match string to current position. Attempts matching the characters at
	 * the current position to a target string, skipping past the string if
	 * found.
	 * 
	 * @param text string to be matched (non-<code>null</code>, non-empty)
	 * @return <code>true</code> if text matched, <code>false</code> if not
	 * @exception IOException if read error
	 */
	
	public boolean matchString(String text) throws IOException {
		
		// match from current position
		pushMark();
		for (int i = 0; i < text.length(); i++) {
			if (read() != text.charAt(i)) {
				
				// mismatch found, restore position and fail
				popMark();
				return false;
				
			}
		}
		
		// match found, return positioned past target
		clearMark();
		return true;
	}
}
