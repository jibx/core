/*
 * Copyright (c) 2000-2001 Sosnoski Software Solutions, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

import java.io.*;
import java.util.*;

/**
 * Source configuration processor program. This program processes a set of Java
 * source files (or standard input to standard output, if no source files are
 * specified), scanning for configuration control lines. A control line is a
 * line that starts with "//#token*" (valid only as the first line of a
 * file), "//#token{" (beginning an option block), "//#}token{" (inverting an
 * option block), or "//#token} (closing an option block). The first two formats
 * also allow a '!' immediately before the token in order to invert the token
 * state.<p>
 *
 * The token strings to be processed are specified on the command line as
 * either enabled or disabled. See the program documentation for more details
 * of the command line options and usage.<p>
 *
 * Nested option blocks are allowed, but overlapping option blocks are an
 * error. In order to ensure proper processing of nested option blocks, the
 * user should generally specify <b>every</b> token used for the nested blocks
 * as either enabled or disabled if <b>any</b> of them are either enabled or
 * disabled. It is an error if an indeterminant beginning of block token (one
 * with a token which is not on either list) is immediately contained within
 * a block with an enabled token. In other words, the case:
 * <pre>
 *   //#a{
 *   //#b{
 *   //#c{
 *	 //#c}
 *	 //#b}
 *	 //#a}
 * </pre>
 * gives an error if "a" is on the enabled list and "b" is not on either list,
 * or if "a" is not on the disabled list, "b" is on the enabled list,  and "c"
 * is not on either list.
 *
 * @author Dennis M. Sosnoski
 * @version 0.8
 */

public class JEnable
{
	/** Lead text for option token. */
	protected static final String OPTION_LEAD = "//#";
	/** Length of lead text for option token. */
	protected static final int LEAD_LENGTH = OPTION_LEAD.length();

	/** Size of buffer used to copy file. */
	protected static final int COPY_BUFFER_SIZE = 4096;

	/** Return code for not an option line. */
	protected static final int NOT_OPTION = 0;
	/** Return code for file option line. */
	protected static final int FILE_OPTION = 1;
	/** Return code for block start option line. */
	protected static final int BLOCK_START_OPTION = 2;
	/** Return code for block else option line. */
	protected static final int BLOCK_ELSE_OPTION = 3;
	/** Return code for block end option line. */
	protected static final int BLOCK_END_OPTION = 4;
	/** Return code for block comment option line. */
	protected static final int BLOCK_COMMENT_OPTION = 5;

	/** Error text for file option not on first line. */
	protected static final String FILE_OPTION_ERR =
		"file option token must be first line of file";
	/** Error text for token nested within block for same token. */
	protected static final String NESTED_SAME_ERR =
		"block option start cannot be nested within block with same token";
	/** Error text for indeterminant token nested in enabled block. */
	protected static final String INDETERM_ERR =
		"block option token within enabled block must be enabled or disabled";
	/** Error text for block end token not matching block start token. */
	protected static final String UNBALANCED_ERR =
		"block option end without matching start";
	/** Error text for block else token not matching block start token. */
	protected static final String BADELSE_ERR =
		"block option else without matching start";
	/** Error text for end of file with open block. */
	protected static final String UNCLOSED_ERR =
		"end of file with open block";
	/** Error text for unknown option token type. */
	protected static final String UNKNOWN_OPTION_ERR =
		"unknown option line type";
	/** Error text for file option line with unknown file extension. */
	protected static final String EXTENSION_ERR =
		"unknown file option line extension";
	/** Error text for unable to rename file to backup directory. */
	protected static final String BACKUP_DIR_ERR =
		"unable to create backup directory";
	/** Error text for unable to delete old backup file. */
	protected static final String OLD_BACKUP_ERR =
		"unable to delete old backup file";
	/** Error text for unable to rename file within directory. */
	protected static final String BACKUP_FILE_ERR =
		"unable to rename file for backup";
	/** Error text for unable to delete original file. */
	protected static final String DELETE_ERR =
		"unable to delete input file";
	/** Error text for unable to rename original file. */
	protected static final String RENAME_ERR =
		"unable to rename source file";
	/** Error text for unable to rename temp file. */
	protected static final String TEMP_RENAME_ERR =
		"unable to rename temp file";
	/** Error text for unable to delete temporary file. */
	protected static final String TEMP_DELETE_ERR =
		"unable to delete temporary output file";
	/** Error text for unable to change file modify timestamp. */
	protected static final String STAMP_ERR =
		"unable to change file modify timestamp";
	/** Error text for token in both sets. */
	protected static final String DUAL_USE_ERR =
		"Same token cannot be both enabled and disabled";

	/** Preserve timestamp on modified files flag. */
	protected boolean m_keepStamp;

	/** Mark backup file with tilde flag. */
	protected boolean m_markBackup;

	/** List modified files flag. */
	protected boolean m_listModified;

	/** List all files processed flag. */
	protected boolean m_listProcessed;

	/** List file summary by path flag. */
	protected boolean m_listSummary;

	/** Backup root path (null if not backing up). */
	protected File m_backupDir;

	/** Map for enabled tokens (values same as keys). */
	protected Hashtable m_enabledTokens;

	/** Map for disabled tokens (values same as keys). */
	protected Hashtable m_disabledTokens;

	/** Number of files matched. */
	protected int m_matchedCount;

	/** Number of files modified. */
	protected int m_modifiedCount;

	/** Current option token, set by check method. */
	private String m_token;

	/** Inverted token flag, set by check method. */
	private boolean m_invert;

	/** Offset past end of token in line, set by check method. */
	private int m_endOffset;

	/**
	 * Constructor.
	 *
	 * @param keep preserve timestamp on modified files flag
	 * @param mark mark backup files with tilde flag
	 * @param mods list modified files flag
	 * @param quiet do not list file summaries by path flag
	 * @param verbose list all files processed flag
	 * @param backup root back for backup directory tree (<code>null</code> if
	 * no backups)
	 * @param enabled map of enabled tokens
	 * @param disabled map of disabled tokens
	 */

	protected JEnable(boolean keep, boolean mark, boolean mods, boolean quiet,
		boolean verbose, File backup, Hashtable enabled, Hashtable disabled) {
		m_keepStamp = keep;
		m_markBackup = mark;
		m_listModified = mods;
		m_listProcessed = verbose;
		m_listSummary = !quiet;
		m_backupDir = backup;
		m_enabledTokens = enabled;
		m_disabledTokens = disabled;
	}

	/**
	 * Checks for valid first character of token. The first character must be
	 * an alpha or underscore.
	 *
	 * @param chr character to be validated
	 * @return <code>true</code> if valid first character, <code>false</code>
	 * if not
	 */

	protected static boolean isTokenLeadChar(char chr) {
		return (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z') ||
			chr == '_';
	}

	/**
	 * Checks for valid body character of token. All body characters must be
	 * an alpha, digits, or underscore.
	 *
	 * @param chr character to be validated
	 * @return <code>true</code> if valid body character, <code>false</code>
	 * if not
	 */

	protected static boolean isTokenBodyChar(char chr) {
		return (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z') ||
			chr == '_' || (chr >= '0' && chr <= '9');
	}

	/**
	 * Convenience method for generating an error report exception.
	 *
	 * @param lnum line number within file
	 * @param line source line to check for option
	 * @param msg error message text
	 * @throws IOException wrapping the error information
	 */

	protected void throwError(int lnum, String line, String msg)
		throws IOException {
		throw new IOException("Error on input line " + lnum + ", " +
			msg + ":\n" + line);
	}

	/**
	 * Check if line is an option. Returns a code for the option line type
	 * (or "not an option line"), with the actual token from the line stored
	 * in the instance variable. Ugly technique, but the only easy way to
	 * return multiple results without using another class.
	 *
	 * @param lnum line number within file (used for error reporting)
	 * @param line source line to check for option
	 * @return return code for option line type
	 * @throws IOException on option line error
	 */

	protected int checkOptionLine(int lnum, String line) throws IOException {

		// check if line is a candidate for option token
		int type = NOT_OPTION;
		m_token = null;
		m_invert = false;
		m_endOffset = 0;
		if (line.length() > LEAD_LENGTH && line.startsWith(OPTION_LEAD)) {

			// check for special leading character before token
			int offset = LEAD_LENGTH;
			char lead = line.charAt(offset);
			if (lead == '!' || lead == '}') {
				offset++;
			} else {
				lead = 0;
			}

			// make sure a valid token start character follows
			int start = offset;
			if (isTokenLeadChar(line.charAt(start))) {

				// parse the token characters
				int scan = LEAD_LENGTH+1;
				while (scan < line.length()) {
					char chr = line.charAt(scan++);
					if (!isTokenBodyChar(chr)) {

						// token found, classify and set type
						m_token = line.substring(start, scan-1);
						m_endOffset = scan;
						switch (chr) {

							case '*':

								// file option, inverted token is only variation
								type = FILE_OPTION;
								if (lead == '!') {
									m_invert = true;
								} else if (lead != 0) {
									throwError(lnum, line, UNKNOWN_OPTION_ERR);
								}
								break;

							case '{':

								// block start option, check variations
								if (lead == '}') {
									type = BLOCK_ELSE_OPTION;
								} else {
									if (lead == '!') {
										m_invert = true;
									}
									type = BLOCK_START_OPTION;
								}
								break;

							case '}':

								// block end option, no variations allowed
								type = BLOCK_END_OPTION;
								if (lead != 0) {
									throwError(lnum, line, UNKNOWN_OPTION_ERR);
								}
								break;

							case ':':

								// block comment option, no variations allowed
								type = BLOCK_COMMENT_OPTION;
								if (lead != 0) {
									throwError(lnum, line, UNKNOWN_OPTION_ERR);
								}
								break;

							default:

								// no idea what this is supposed to be
								throwError(lnum, line, UNKNOWN_OPTION_ERR);
								break;

						}
						break;
					}
				}
			}
		}
		return type;
	}

	/**
	 * Processes source options for a text stream. If an error occurs in
	 * processing, this generates an <code>IOException</code> with the error
	 * information (including input line number).
	 *
	 * @param in input reader for source data
	 * @param lead first line of file (previously read for checking file enable
	 * or disable)
	 * @param out output writer for modified source data
	 * @return <code>true</code> if source modified, <code>false</code> if not
	 */

	protected boolean processStream(BufferedReader in, String lead,
		BufferedWriter out) throws IOException {

		// initialize state information
		Stack enables = new Stack();
		Stack nests = new Stack();
		String disable = null;
		boolean changed = false;

		// basic file line copy loop
		String line = lead;
		int lnum = 1;
		while (line != null) {

			// process based on option type
			boolean option = true;
			int type = checkOptionLine(lnum, line);
			switch (type) {

				case NOT_OPTION:
					option = false;
					break;

				case FILE_OPTION:

					// file option processed outside, but must be first line
					if (lnum > 1) {
						throwError(lnum, line, FILE_OPTION_ERR);
					}
					break;

				case BLOCK_START_OPTION:

					// option block start token, must not be duplicated
					if (nests.indexOf(m_token) >= 0) {
						throwError(lnum, line, NESTED_SAME_ERR);
					} else {

						// push to nesting stack and check if we're handling
						nests.push(m_token);
						if (disable == null) {

							// see if we know about this token
							boolean on = m_enabledTokens.containsKey(m_token);
							boolean off = m_disabledTokens.containsKey(m_token);

							// swap flags if inverted token
							if (m_invert) {
								boolean hold = on;
								on = off;
								off = hold;
							}

							// handle start of block
							if (off) {

								// set disabling option
								disable = m_token;

							} else if (on) {

								// stack enabled option
								enables.push(m_token);

							} else if (!enables.empty()) {

								// error if unknown inside enable
								throwError(lnum, line, INDETERM_ERR);
							}
						}
					}
					break;

				case BLOCK_ELSE_OPTION:

					// option block else, must match top of nesting stack
					if (nests.empty() || !nests.peek().equals(m_token)) {
						throwError(lnum, line, BADELSE_ERR);
					} else {

						// reverse current state, if known
						if (disable == null) {

							// enabled state, check if top of stack
							if (!enables.empty()) {
								if (enables.peek().equals(m_token)) {

									// flip to disable state
									enables.pop();
									disable = m_token;

								}
							}

						} else if (disable.equals(m_token)) {

							// flip to enable state
							disable = null;
							enables.push(m_token);

						}
					}
					break;

				case BLOCK_END_OPTION:

					// option block end, must match top of nesting stack
					if (nests.empty() || !nests.peek().equals(m_token)) {
						throwError(lnum, line, UNBALANCED_ERR);
					} else {

						// remove from nesting stack and check state
						nests.pop();
						if (disable == null) {

							// enabled state, check if top of stack
							if (!enables.empty()) {
								if (enables.peek().equals(m_token)) {
									enables.pop();
								}
							}

						} else if (disable.equals(m_token)) {
							disable = null;
						}
					}
					break;

				case BLOCK_COMMENT_OPTION:

					// disabled line option, check if clearing
					if ((disable != null && !disable.equals(m_token)) ||
						(disable == null && enables.contains(m_token))) {

						// clear disabled line option
						line = line.substring(m_endOffset);
						option = false;
						changed = true;
					}
					break;

				default:
					throwError(lnum, line, UNKNOWN_OPTION_ERR);
			}

			// check for disabling lines
			if (!option && disable != null) {

				// change line to disabled state
				line = OPTION_LEAD + disable + ':' + line;
				changed = true;
			}

			// write (possibly modified) line to output
			out.write(line);
			out.newLine();

			// read next line of input
			line = in.readLine();
			lnum++;

		}

		// check for valid end state
		if (nests.size() > 0) {
			throwError(lnum, (String)nests.pop(), UNCLOSED_ERR);
		}
		out.flush();
		return changed;
	}

	/**
	 * Processes source options for a file. Starts by checking the first line
	 * of the file for a file option and processing that. If, after processing
	 * the file option, the file has a ".java" extension, it is processed for
	 * other option lines.<p>
	 *
	 * This saves the output to a temporary file, then if processing is
	 * completed successfully first renames or moves the original file (if
	 * backup has been requested), or deletes it (if backup not requested),
	 * and then renames the temporary file to the original file name.<p>
	 *
	 * Processing errors are printed to <code>System.err</code>, and any
	 * results are discarded without being saved.
	 *
	 * @param file source file to be processed
	 * @return <code>true</code> if source modified, <code>false</code> if not
	 */

	protected boolean processFile(File file) {
		File temp = null;
		try {

			// set up basic information
			String name = file.getName();
			String dir = file.getParent();
			int split = name.lastIndexOf('.');
			String ext = (split >= 0) ? name.substring(split+1) : "";
			String toext = ext;
			long stamp = m_keepStamp ? file.lastModified() : 0;
			File target = file;

			// check first line for file option
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = in.readLine();
			int type = checkOptionLine(1, line);
			if (type == FILE_OPTION) {

				// make sure we have one of the extensions we know about
				if (ext.equals("java") || ext.equals("javx")) {

					// set "to" extension based on option setting
					if (m_enabledTokens.contains(m_token)) {
						toext = m_invert ? "javx" : "java";
					} else if (m_disabledTokens.contains(m_token)) {
						toext = m_invert ? "java" : "javx";
					}

					// generate new target file name if different extension
					if (!toext.equals(ext)) {
						split = name.indexOf('.');
						name = name.substring(0, split) + '.' + toext;
						target = new File(dir, name);
					}

				} else {
					throw new IOException(EXTENSION_ERR);
				}

			}

			// check if extension valid for processing
			boolean changed = false;
			if (!toext.equals("javx")) {

				// set up output to temporary file in same directory
				temp = File.createTempFile("sop", null, file.getParentFile());
				BufferedWriter out = new BufferedWriter(new FileWriter(temp));

				// process the file for changes
				changed = processStream(in, line, out);
				in.close();
				out.close();
				if (changed) {

					// handle backup of original file
					if (m_backupDir != null) {

						// construct path within backup directory
						String extra = file.getCanonicalPath();
						int mark = extra.indexOf(File.separatorChar);
						if (mark >= 0) {
							extra = extra.substring(mark+1);
						}
						File backup = new File(m_backupDir, extra);

						// copy file to backup directory
						File backdir = backup.getParentFile();
						if (!backdir.exists() && !backdir.mkdirs()) {
							throw new IOException(BACKUP_DIR_ERR + '\n' +
								backdir.getPath());
						}
						if (backup.exists()) {
							if (!backup.delete()) {
								throw new IOException(OLD_BACKUP_ERR);
							}
						}
						byte[] buff = new byte[COPY_BUFFER_SIZE];
						InputStream is = new FileInputStream(file);
						OutputStream os = new FileOutputStream(backup);
						int bytes;
						while ((bytes = is.read(buff)) >= 0) {
							os.write(buff, 0, bytes);
						}
						is.close();
						os.close();
						backup.setLastModified(file.lastModified());

					}
					if (m_markBackup) {

						// suffix file name with tilde
						File backup = new File(dir, name+'~');
						if (backup.exists()) {
							if (!backup.delete()) {
								throw new IOException(OLD_BACKUP_ERR);
							}
						}
						if (!file.renameTo(backup)) {
							throw new IOException(BACKUP_FILE_ERR);
						}

					} else {

						// just delete the original file
						if (!file.delete()) {
							throw new IOException(DELETE_ERR);
						}
					}

					// rename temp to target name
					if (temp.renameTo(target)) {
						if (m_keepStamp && !target.setLastModified(stamp)) {
							throw new IOException(STAMP_ERR);
						}
					} else {
						throw new IOException(TEMP_RENAME_ERR);
					}

				} else {

					// just discard the temporary output file
					if (!temp.delete()) {
						throw new IOException(TEMP_DELETE_ERR);
					}

					// check if file needs to be renamed
					if (!toext.equals(ext)) {

						// just rename file for file option result
						if (file.renameTo(target)) {
							changed = true;
							if (m_keepStamp && !target.setLastModified(stamp)) {
								throw new IOException(STAMP_ERR);
							}
						} else {
							throw new IOException(RENAME_ERR);
						}
					}
				}

			} else if (!toext.equals(ext)) {

				// just rename file for file option result
				in.close();
				if (file.renameTo(target)) {
					changed = true;
					if (m_keepStamp && !target.setLastModified(stamp)) {
						throw new IOException(STAMP_ERR);
					}
				} else {
					throw new IOException(RENAME_ERR);
				}

			}

			// check file listing
			if (changed && (m_listProcessed || m_listModified)) {
				System.out.println("  modified file " + file.getPath());
			} else if (m_listProcessed) {
				System.out.println("  checked file " + file.getPath());
			}
			return changed;

		} catch (Exception ex) {

			// report error
			System.err.println("Error processing " + file.getPath());
			System.err.println(ex.getMessage());

			// discard temporary output file
			if (temp != null) {
				try {
					temp.delete();
				} catch (Exception ex2) {}
			}
			return false;

		}
	}

	/**
	 * Checks if file or directory name directly matches a pattern. This
	 * method accepts one or more '*' wildcard characters in the pattern,
	 * calling itself recursively in order to handle multiple wildcards.
	 *
	 * @param name file or directory name
	 * @param pattern match pattern
	 * @return <code>true</code> if any pattern matched, <code>false</code>
	 * if not
	 */

	protected boolean isPathMatch(String name, String pattern) {

		// check special match cases first
		if (pattern.length() == 0) {
			return name.length() == 0;
		} else if (pattern.charAt(0) == '*') {

			// check if the wildcard is all that's left of pattern
			if (pattern.length() == 1) {
				return true;
			} else {

				// check if another wildcard follows next segment of text
				pattern = pattern.substring(1);
				int split = pattern.indexOf('*');
				if (split > 0) {

					// recurse on each match to text segment
					String piece = pattern.substring(0, split);
					pattern = pattern.substring(split);
					int offset = -1;
					while ((offset = name.indexOf(piece, ++offset)) > 0) {
						int end = offset + piece.length();
						if (isPathMatch(name.substring(end), pattern)) {
							return true;
						}
					}

				} else {

					// no more wildcards, need exact match to end of name
					return name.endsWith(pattern);

				}
			}
		} else {

			// check for leading text before first wildcard
			int split = pattern.indexOf('*');
			if (split > 0) {

				// match leading text to start of name
				String piece = pattern.substring(0, split);
				if (name.startsWith(piece)) {
					return isPathMatch(name.substring(split),
						pattern.substring(split));
				} else {
					return false;
				}

			} else {

				// no wildcards, need exact match
				return name.equals(pattern);

			}
		}
		return false;
	}

	/**
	 * Checks if file name matches a pattern. This works a little differently
	 * from the general path matching in that if the pattern does not include
	 * an extension both ".java" and ".javx" file extensions are matched. If
	 * the pattern includes an extension ending in '*' it is blocked from
	 * matching with a tilde final character in the file name as a special case.
	 *
	 * @param name file or directory name
	 * @param pattern match pattern
	 * @return <code>true</code> if any file modified, <code>false</code> if not
	 */

	protected boolean isNameMatch(String name, String pattern) {

		// check for extension included in pattern
		if (pattern.indexOf('.') >= 0) {

			// pattern includes extension, use as is except for tilde endings
			if (name.charAt(name.length()-1) != '~' ||
				pattern.charAt(pattern.length()-1) == '~') {
				return isPathMatch(name, pattern);
			}

		} else {

			// split extension from file name
			int split = name.lastIndexOf('.');
			if (split >= 0) {

				// check for valid extension with match on name
				String ext = name.substring(split+1);
				if (ext.equals("java") || ext.equals("javx")) {
					return isPathMatch(name.substring(0, split), pattern);
				}
			}

		}
		return false;
	}

	/**
	 * Process files matching path segment. This method matches a single step
	 * (directory specification) in a path for each call, calling itself
	 * recursively to match the complete path.
	 *
	 * @param base base directory for path match
	 * @param path file path remaining to be processed
	 */

	protected void matchPathSegment(File base, String path) {

		// find break for leading directory if any in path
		File[] files = base.listFiles();
		int split = path.indexOf('/');
		if (split >= 0) {

			// split off the directory and check it
			String dir = path.substring(0, split);
			String next = path.substring(split+1);
			if (dir.equals("**")) {

				// match directly against files in this directory
				matchPathSegment(base, next);

				// walk all directories in tree under this one
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						matchPathSegment(files[i], path);
					}
				}

			} else {

				// try for concrete match to directory
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						if (isPathMatch(files[i].getName(), dir)) {
							matchPathSegment(files[i], next);
						}
					}
				}

			}
		} else {

			// match directly against files in this directory
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {
					if (isNameMatch(files[i].getName(), path)) {
						m_matchedCount++;
						if (processFile(files[i])) {
							m_modifiedCount++;
						}
					}
				}
			}

		}
	}

	/**
	 * Process all files matching path and print summary. The file path
	 * format is similar to Ant, supporting arbitrary directory recursion using
	 * '**' separators between '/' separators. Single '*'s may be used within
	 * names for wildcarding, but aside from the special case of the directory
	 * recursion matcher only one '*' may be used per name.<p>
	 *
	 * If an extension is
	 * not specified for the final name in the path both ".java" and ".javx"
	 * extensions are checked, but after checking for file option lines (which
	 * may change the file extension) only ".java" extensions are processed for
	 * other source options.
	 *
	 * @param path file path to be processed
	 */

	protected void processPath(String path) {

		// make sure we have something to process
		if (path.length() > 0) {

			// begin matching from root or current directory
			if (path.charAt(0) == '/') {
				matchPathSegment(new File(File.separator), path.substring(1));
			} else {
				matchPathSegment(new File("."), path);
			}

			// print summary information for path
			if (m_listSummary) {
				System.out.println(" matched " + m_matchedCount +
					" files and modified " + m_modifiedCount +
					" for path: " + path);
				m_matchedCount = 0;
				m_modifiedCount = 0;
			}
		}
	}

	/**
	 * Parse comma-separated token list. Parses and validates the tokens,
	 * adding them to the supplied list. errors are signalled by throwing
	 * <code>IllegalArgumentException</code>.
	 *
	 * @param list comma-separated token list to be parsed
	 * @param tokens list of tokens to add to
	 * @throws IllegalArgumentException on error in supplied list
	 */

	protected static void parseTokenList(String list, Vector tokens) {

		// accumulate comma-delimited tokens from list
		while (list.length() > 0) {

			// find end for next token
			int mark = list.indexOf(',');
			String token;
			if (mark == 0) {
				throw new IllegalArgumentException("Empty token not " +
					"allowed: \"" + list + '"');
			} else if (mark > 0) {

				// split token off list
				token = list.substring(0, mark);
				list = list.substring(mark+1);

			} else {

				// use rest of list as final token
				token = list;
				list = "";
			}

			// validate the token
			for (int i = 0; i < token.length(); i++) {
				char chr = token.charAt(i);
				if ((i == 0 && !isTokenLeadChar(chr)) ||
					(i > 0 && !isTokenBodyChar(chr))) {
					throw new IllegalArgumentException("Illegal " +
						"character in token: \"" + token + '"');
				}
			}

			// add validated token to list
			tokens.add(token);
		}
	}

	/**
	 * Test driver, just reads the input parameters and executes the source
	 * checks.
	 *
	 * @param argv command line arguments
	 */

	public static void main(String[] argv) {
		if (argv.length > 0) {

			// parse the leading command line parameters
			boolean valid = true;
			boolean keep = false;
			boolean listmod = false;
			boolean quiet = false;
			boolean tilde = false;
			boolean verbose = false;
			File backup = null;
			Vector enables = new Vector();
			Vector disables = new Vector();
			int anum = 0;
			while (anum < argv.length && argv[anum].charAt(0) == '-') {
				String arg = argv[anum++];
				int cnum = 1;
				while (cnum < arg.length()) {
					char option = Character.toLowerCase(arg.charAt(cnum++));
					switch (option) {

						case 'b':
							if (anum < argv.length) {
								try {
									backup = new File(argv[anum++]);
									if (!backup.isDirectory()) {
										System.err.println("Backup directory " +
											"path must be a directory");
									}
								} catch (SecurityException ex) {
									System.err.println("Unable to access " +
										"backup directory");
								}
							} else {
								System.err.println("Missing directory path " +
									"for -b option");
							}
							break;

						case 'd':
						case 'e':
							if (anum < argv.length) {

								// accumulate comma-delimited tokens from list
								Vector tokens = (option == 'd') ?
									disables : enables;
								try {
									parseTokenList(argv[anum++], tokens);
								} catch (IllegalArgumentException ex) {
									System.err.println(ex.getMessage());
									return;
								}
								if (option == 'd') {
									disables = tokens;
								} else {
									enables = tokens;
								}

							} else {
								System.err.println("Missing token list for -" +
									option + " option");
								return;
							}
							break;

						case 'p':
							keep = true;
							break;

						case 'q':
							quiet = true;
							break;

						case 'm':
							listmod = true;
							break;

						case 't':
							tilde = true;
							break;

						case 'v':
							verbose = true;
							break;

						default:
							System.err.println("Unknown option -" + option);
							return;
					}
				}
			}

			// build hashsets of the tokens, checking for overlap
			Hashtable enabled = new Hashtable();
			for (int i = 0; i < enables.size(); i++) {
				Object token = enables.elementAt(i);
				enabled.put(token, token);
			}
			Hashtable disabled = new Hashtable();
			for (int i = 0; i < disables.size(); i++) {
				Object token = disables.elementAt(i);
				disabled.put(token, token);
				if (enabled.containsKey(token)) {
					System.err.println(DUAL_USE_ERR + ": " + token);
					return;
				}
			}

			// construct an instance of class
			JEnable opt = new JEnable(keep, tilde, listmod, quiet, verbose,
				backup, enabled, disabled);

			// check if we have file paths
			if (anum < argv.length) {

				// process each file path supplied
				while (anum < argv.length) {
					String arg = argv[anum++];
					int split;
					while ((split = arg.indexOf(',')) > 0) {
						String path = arg.substring(0, split);
						opt.processPath(path);
						arg = arg.substring(split+1);
					}
					opt.processPath(arg);
				}

			} else {

				// just process standard input to standard output
				BufferedReader in = new BufferedReader
					(new InputStreamReader(System.in));
				BufferedWriter out = new BufferedWriter
					(new OutputStreamWriter(System.out));

				// check first line for disabled token
				try {
					String line = in.readLine();
					int type = opt.checkOptionLine(1, line);
					if (type == FILE_OPTION) {
						boolean discard = opt.m_invert ?
							enabled.contains(opt.m_token) :
							disabled.contains(opt.m_token);
						if (discard) {
							return;
						}
					}
					opt.processStream(in, line, out);
				} catch (IOException ex) {
					System.err.println(ex.getMessage());
				}
			}
		} else {
			System.err.println
				("\nJEnable Java source configuration processor version " +
				"0.8\nUsage: JEnable [-options] path-list\n" +
				"Options are:\n" +
				" -b   backup directory tree (base directory is next " +
				"argument)\n" +
				" -d   disabled token list (comma-separated token name list" +
				" is next argument)\n" +
				" -e   enabled token list (comma-separated token name list" +
				" is next argument)\n" +
				" -m   list modified files as they're processed\n" +
				" -p   preserve timestamp on modified files\n" +
				" -q   quiet mode, do not print file summary by path\n" +
				" -t   backup modified files in same directory with '~' " +
				"suffix\n" +
				" -v   verbose listing of all files processed (modified or " +
				"not)\n" +
				"These options may be concatenated together with a single" +
				" leading dash.\n\n" +
				"Path lists may include '*' wildcards, and may consist of " +
				"multiple paths\n" +
				"separated by ',' characters. The special directory " +
				"pattern '**' matches\n" +
				"any number of intervening directories. Any number of path " +
				"list parameters may\n" +
				"be supplied.\n");
		}
	}
}
