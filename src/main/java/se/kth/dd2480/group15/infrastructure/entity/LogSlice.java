package se.kth.dd2480.group15.infrastructure.entity;

/**
 * Represents a slice or portion of a log file, containing a segment of the log
 * and the offset that indicates the position for where the next segment can be read.
 * The log segment is limited to a maximum number of characters.
 *
 * @param content the segment of the log that is retrieved
 * @param nextOffset the offset indicating where to start reading the next segment
 * @param endReached boolean indicating whether the end of the log has been read
 */
public record LogSlice(String content, int nextOffset, boolean endReached) { }
