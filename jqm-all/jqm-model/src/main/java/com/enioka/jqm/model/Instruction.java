package com.enioka.jqm.model;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * The different orders a {@link JobInstance} may receive while running
 */
public enum Instruction {
    /** The normal order - just run **/
    RUN,
    /** Die, thread, die! (or at least try to) **/
    KILL,
    /** Try to pause **/
    PAUSE
}
